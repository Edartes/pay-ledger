package uk.gov.pay.ledger.app;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.commons.utils.logging.LoggingFilter;
import uk.gov.pay.ledger.event.resource.EventResource;
import uk.gov.pay.ledger.exception.BadRequestExceptionMapper;
import uk.gov.pay.ledger.healthcheck.HealthCheckResource;
import uk.gov.pay.ledger.healthcheck.SQSHealthCheck;
import uk.gov.pay.ledger.queue.managed.QueueMessageReceiver;
import uk.gov.pay.ledger.transaction.resource.TransactionResource;

import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;

public class LedgerApp extends Application<LedgerConfig> {

    public static void main(String[] args) throws Exception {
        new LedgerApp().run(args);
    }

    @Override
    public void initialize(Bootstrap<LedgerConfig> bootstrap){
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false))
        );

        bootstrap.addBundle(new MigrationsBundle<LedgerConfig>() {
            @Override
            public DataSourceFactory getDataSourceFactory(LedgerConfig configuration) {
                return configuration.getDataSourceFactory();
            }
        });

        bootstrap.addBundle(new JdbiExceptionsBundle());
    }

    @Override
    public void run(LedgerConfig config, Environment environment) {
        JdbiFactory jdbiFactory = new JdbiFactory();
        final Jdbi jdbi = jdbiFactory.build(environment, config.getDataSourceFactory(), "postgresql");

        final Injector injector = Guice.createInjector(new LedgerModule(config, environment, jdbi));

        environment.jersey().register(injector.getInstance(EventResource.class));
        environment.jersey().register(injector.getInstance(TransactionResource.class));
        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, "/v1/*");
        environment.jersey().register(new BadRequestExceptionMapper());
        environment.healthChecks().register("sqsQueue", injector.getInstance(SQSHealthCheck.class));

        if(config.getQueueMessageReceiverConfig().isBackgroundProcessingEnabled()) {
            environment.lifecycle().manage(injector.getInstance(QueueMessageReceiver.class));
        }
    }

}
