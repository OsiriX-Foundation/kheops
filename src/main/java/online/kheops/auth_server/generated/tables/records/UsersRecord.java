/*
 * This file is generated by jOOQ.
 */
package online.kheops.auth_server.generated.tables.records;


import javax.annotation.Generated;

import online.kheops.auth_server.generated.tables.Users;

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
public class UsersRecord extends UpdatableRecordImpl<UsersRecord> implements Record5<Long, String, Long, String, String> {

    private static final long serialVersionUID = 1020778410;

    /**
     * Setter for <code>public.users.pk</code>.
     */
    public void setPk(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.users.pk</code>.
     */
    public Long getPk() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.users.sub</code>.
     */
    public void setSub(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.users.sub</code>.
     */
    public String getSub() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.users.inbox_fk</code>.
     */
    public void setInboxFk(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.users.inbox_fk</code>.
     */
    public Long getInboxFk() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>public.users.email</code>.
     */
    public void setEmail(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.users.email</code>.
     */
    public String getEmail() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.users.name</code>.
     */
    public void setName(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.users.name</code>.
     */
    public String getName() {
        return (String) get(4);
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
    public Row5<Long, String, Long, String, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Long, String, Long, String, String> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Users.USERS.PK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Users.USERS.SUB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return Users.USERS.INBOX_FK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Users.USERS.EMAIL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Users.USERS.NAME;
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
    public String component2() {
        return getSub();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component3() {
        return getInboxFk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getEmail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getName();
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
    public String value2() {
        return getSub();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value3() {
        return getInboxFk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getEmail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsersRecord value1(Long value) {
        setPk(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsersRecord value2(String value) {
        setSub(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsersRecord value3(Long value) {
        setInboxFk(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsersRecord value4(String value) {
        setEmail(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsersRecord value5(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsersRecord values(Long value1, String value2, Long value3, String value4, String value5) {
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
     * Create a detached UsersRecord
     */
    public UsersRecord() {
        super(Users.USERS);
    }

    /**
     * Create a detached, initialised UsersRecord
     */
    public UsersRecord(Long pk, String sub, Long inboxFk, String email, String name) {
        super(Users.USERS);

        set(0, pk);
        set(1, sub);
        set(2, inboxFk);
        set(3, email);
        set(4, name);
    }
}
