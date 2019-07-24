--liquibase formatted sql

--changeset uk.gov.pay:index_created_date_for_order_by

CREATE INDEX created_date_idx ON transaction USING btree(created_date);