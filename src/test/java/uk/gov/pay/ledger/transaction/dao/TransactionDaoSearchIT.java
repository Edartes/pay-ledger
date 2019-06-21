package uk.gov.pay.ledger.transaction.dao;


import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.pay.ledger.rule.AppWithPostgresAndSqsRule;
import uk.gov.pay.ledger.transaction.model.CardDetails;
import uk.gov.pay.ledger.transaction.model.Transaction;
import uk.gov.pay.ledger.transaction.search.common.CommaDelimitedSetParameter;
import uk.gov.pay.ledger.transaction.search.common.TransactionSearchParams;
import uk.gov.pay.ledger.util.DatabaseTestHelper;
import uk.gov.pay.ledger.util.fixture.TransactionFixture;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.ledger.util.fixture.TransactionFixture.aTransactionFixture;

public class TransactionDaoSearchIT {

    @ClassRule
    public static AppWithPostgresAndSqsRule rule = new AppWithPostgresAndSqsRule();

    private TransactionFixture transactionFixture;
    private TransactionDao transactionDao;
    private TransactionSearchParams searchParams;

    private DatabaseTestHelper databaseTestHelper = DatabaseTestHelper.aDatabaseTestHelper(rule.getJdbi());

    @Before
    public void setUp() {
        databaseTestHelper.truncateAllData();
        transactionDao = new TransactionDao(rule.getJdbi());
        searchParams = new TransactionSearchParams();
    }

    @Test
    public void shouldGetAndMapTransactionCorrectly() {

        transactionFixture = aTransactionFixture()
                .insert(rule.getJdbi());

        searchParams.setAccountId(transactionFixture.getGatewayAccountId());

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(1));
        Transaction transaction = transactionList.get(0);

        assertThat(transaction.getId(), is(transactionFixture.getId()));
        assertThat(transaction.getGatewayAccountId(), is(transactionFixture.getGatewayAccountId()));
        assertThat(transaction.getAmount(), is(transactionFixture.getAmount()));
        assertThat(transaction.getState(), is(transactionFixture.getState()));
        assertThat(transaction.getReference(), is(transactionFixture.getReference()));
        assertThat(transaction.getDescription(), is(transactionFixture.getDescription()));
        assertThat(transaction.getLanguage(), is(transactionFixture.getLanguage()));
        assertThat(transaction.getReturnUrl(), is(transactionFixture.getReturnUrl()));
        assertThat(transaction.getExternalId(), is(transactionFixture.getExternalId()));
        assertThat(transaction.getEmail(), is(transactionFixture.getEmail()));
        assertThat(transaction.getPaymentProvider(), is(transactionFixture.getPaymentProvider()));
        assertThat(transaction.getCreatedAt(), is(transactionFixture.getCreatedAt()));
        assertThat(transaction.getDelayedCapture(), is(transactionFixture.getDelayedCapture()));

        assertThat(transaction.getCardDetails().getCardHolderName(), is(transactionFixture.getCardDetails().getCardHolderName()));
        assertThat(transaction.getCardDetails().getCardBrand(), is(transactionFixture.getCardDetails().getCardBrand()));

        assertThat(transaction.getCardDetails().getBillingAddress().getAddressLine1(), is(transactionFixture.getCardDetails().getBillingAddress().getAddressLine1()));
        assertThat(transaction.getCardDetails().getBillingAddress().getAddressLine2(), is(transactionFixture.getCardDetails().getBillingAddress().getAddressLine2()));
        assertThat(transaction.getCardDetails().getBillingAddress().getAddressCounty(), is(transactionFixture.getCardDetails().getBillingAddress().getAddressCounty()));
        assertThat(transaction.getCardDetails().getBillingAddress().getAddressCity(), is(transactionFixture.getCardDetails().getBillingAddress().getAddressCity()));
        assertThat(transaction.getCardDetails().getBillingAddress().getAddressPostCode(), is(transactionFixture.getCardDetails().getBillingAddress().getAddressPostCode()));
        assertThat(transaction.getCardDetails().getBillingAddress().getAddressCountry(), is(transactionFixture.getCardDetails().getBillingAddress().getAddressCountry()));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(1L));
    }

    @Test
    public void shouldReturn2Records_whenSearchingBySpecificGatewayAccountId() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .insert(rule.getJdbi());
        }
        aTransactionFixture()
                .insert(rule.getJdbi());

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(2));
        assertThat(transactionList.get(0).getGatewayAccountId(), is(gatewayAccountId));
        assertThat(transactionList.get(1).getGatewayAccountId(), is(gatewayAccountId));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(2L));
    }

    @Test
    public void shouldReturn1Record_whenSearchingByEmail() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .withEmail("testemail" + i + "@example.org")
                    .insert(rule.getJdbi());
        }

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setEmail("testemail1");

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), is(1));
        assertThat(transactionList.get(0).getEmail(), is("testemail1@example.org"));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(1L));
    }

    @Test
    public void shouldReturn1Record_whenSearchingByReference() {

        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withAmount(100l + i)
                    .withGatewayAccountId(gatewayAccountId)
                    .withReference("reference " + i)
                    .withDescription("description " + i)
                    .insert(rule.getJdbi());
        }

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setReference("reference 1");

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(1));
        assertThat(transactionList.get(0).getReference(), is("reference 1"));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(1L));
    }

    @Test
    public void shouldReturn1Record_whenSearchingByCardHolderName() {

        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            CardDetails cardDetails = new CardDetails("name" + i, null, null);

            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .withReference("reference " + i)
                    .withCardDetails(cardDetails)
                    .withDescription("description " + i)
                    .insert(rule.getJdbi());
        }

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setCardHolderName("name1");

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(1));
        assertThat(transactionList.get(0).getCardDetails().getCardHolderName(), is("name1"));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(1L));
    }

    @Test
    public void shouldReturn1Record_withFromDateSet() {

        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 1; i < 4; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .withCreatedDate(ZonedDateTime.now().minusDays(i))
                    .insert(rule.getJdbi());
        }

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setFromDate(ZonedDateTime.now().minusDays(1).minusMinutes(10).toString());

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(1));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(1L));
    }

    @Test
    public void shouldReturn2Records_withToDateSet() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 1; i < 4; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .withCreatedDate(ZonedDateTime.now().minusDays(i))
                    .insert(rule.getJdbi());
        }

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setToDate(ZonedDateTime.now().minusDays(2).plusMinutes(10).toString());

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(2));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(2L));
    }

    @Test
    public void shouldReturn10Records_withPagesize10() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 1; i < 20; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .insert(rule.getJdbi());
        }

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setDisplaySize(10l);

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(10));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(19L));
    }

    @Test
    public void shouldReturn2Records_withOffsetAndPagesizeSet() {
        String gatewayAccountId = "account-id-" + nextLong();
        long id = nextLong();

        for (int i = 1; i < 20; i++) {
            aTransactionFixture()
                    .withId(id + i)
                    .withGatewayAccountId(gatewayAccountId)
                    .withReference("reference" + i)
                    .insert(rule.getJdbi());
        }

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setDisplaySize(2l);
        searchParams.setPageNumber(3l);


        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(2));
        assertThat(transactionList.get(0).getReference(), is("reference15"));
        assertThat(transactionList.get(1).getReference(), is("reference14"));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(19L));
    }

    @Test
    public void shouldReturn2Records_WhenSearchingByCreatedState() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .insert(rule.getJdbi());
        }
        aTransactionFixture()
                .withState("random-state")
                .withGatewayAccountId(gatewayAccountId)
                .insert(rule.getJdbi());

        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setPaymentStates(new CommaDelimitedSetParameter("created"));

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(2));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(2L));
    }

    @Test
    public void shouldReturnNoRecords_WhenStateIsOtherThanCreated() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .withState("random-state")
                    .insert(rule.getJdbi());
        }
        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setPaymentStates(new CommaDelimitedSetParameter("random-state"));

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(0));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(0L));
    }

    @Test
    public void shouldReturnNoRecords_whenRefundStatesAreSpecified() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .withState("random-refund-state")
                    .insert(rule.getJdbi());
        }
        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setRefundStates(new CommaDelimitedSetParameter("random-refund-state"));

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(0));

        Long total = transactionDao.getTotalForSearch(searchParams);
        assertThat(total, is(0L));
    }

    //todo: modify test to return results when last digits card number is available in DB
    @Test
    public void shouldReturnNoRecords_whenLastDigitsCardNumberIsSpecified() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .insert(rule.getJdbi());
        }
        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setLastDigitsCardNumber("1234");

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(0));
    }

    //todo: modify test to return results when first digits card number is available in DB
    @Test
    public void shouldReturnNoRecords_whenFirstDigitsCardNumberIsSpecified() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .insert(rule.getJdbi());
        }
        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setFirstDigitsCardNumber("1234");

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(0));
    }

    //todo: modify test to return results when card_brand is available in DB
    @Test
    public void shouldReturnNoRecords_whenCardBrandIsSpecified() {
        String gatewayAccountId = "account-id-" + nextLong();

        for (int i = 0; i < 2; i++) {
            aTransactionFixture()
                    .withGatewayAccountId(gatewayAccountId)
                    .insert(rule.getJdbi());
        }
        TransactionSearchParams searchParams = new TransactionSearchParams();
        searchParams.setAccountId(gatewayAccountId);
        searchParams.setCardBrands(Arrays.asList("random-card-brand"));

        List<Transaction> transactionList = transactionDao.searchTransactions(searchParams);

        assertThat(transactionList.size(), Matchers.is(0));
    }
}