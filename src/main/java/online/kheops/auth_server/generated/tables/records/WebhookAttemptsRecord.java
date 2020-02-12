/*
 * This file is generated by jOOQ.
 */
package online.kheops.auth_server.generated.tables.records;


import java.sql.Timestamp;

import javax.annotation.Generated;

import online.kheops.auth_server.generated.tables.WebhookAttempts;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WebhookAttemptsRecord extends UpdatableRecordImpl<WebhookAttemptsRecord> implements Record5<Long, Long, Timestamp, Long, Long> {

    private static final long serialVersionUID = -1186925405;

    /**
     * Setter for <code>public.webhook_attempts.pk</code>.
     */
    public void setPk(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.webhook_attempts.pk</code>.
     */
    public Long getPk() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.webhook_attempts.status</code>.
     */
    public void setStatus(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.webhook_attempts.status</code>.
     */
    public Long getStatus() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.webhook_attempts.time</code>.
     */
    public void setTime(Timestamp value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.webhook_attempts.time</code>.
     */
    public Timestamp getTime() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>public.webhook_attempts.webhook_trigger_fk</code>.
     */
    public void setWebhookTriggerFk(Long value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.webhook_attempts.webhook_trigger_fk</code>.
     */
    public Long getWebhookTriggerFk() {
        return (Long) get(3);
    }

    /**
     * Setter for <code>public.webhook_attempts.attempt</code>.
     */
    public void setAttempt(Long value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.webhook_attempts.attempt</code>.
     */
    public Long getAttempt() {
        return (Long) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Long, Long, Timestamp, Long, Long> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Long, Long, Timestamp, Long, Long> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return WebhookAttempts.WEBHOOK_ATTEMPTS.PK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return WebhookAttempts.WEBHOOK_ATTEMPTS.STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field3() {
        return WebhookAttempts.WEBHOOK_ATTEMPTS.TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field4() {
        return WebhookAttempts.WEBHOOK_ATTEMPTS.WEBHOOK_TRIGGER_FK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field5() {
        return WebhookAttempts.WEBHOOK_ATTEMPTS.ATTEMPT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getPk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component2() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component3() {
        return getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component4() {
        return getWebhookTriggerFk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component5() {
        return getAttempt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getPk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value3() {
        return getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value4() {
        return getWebhookTriggerFk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value5() {
        return getAttempt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebhookAttemptsRecord value1(Long value) {
        setPk(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebhookAttemptsRecord value2(Long value) {
        setStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebhookAttemptsRecord value3(Timestamp value) {
        setTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebhookAttemptsRecord value4(Long value) {
        setWebhookTriggerFk(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebhookAttemptsRecord value5(Long value) {
        setAttempt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebhookAttemptsRecord values(Long value1, Long value2, Timestamp value3, Long value4, Long value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WebhookAttemptsRecord
     */
    public WebhookAttemptsRecord() {
        super(WebhookAttempts.WEBHOOK_ATTEMPTS);
    }

    /**
     * Create a detached, initialised WebhookAttemptsRecord
     */
    public WebhookAttemptsRecord(Long pk, Long status, Timestamp time, Long webhookTriggerFk, Long attempt) {
        super(WebhookAttempts.WEBHOOK_ATTEMPTS);

        set(0, pk);
        set(1, status);
        set(2, time);
        set(3, webhookTriggerFk);
        set(4, attempt);
    }
}
