/*
 * This file is generated by jOOQ.
 */
package online.kheops.auth_server.generated.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import online.kheops.auth_server.generated.Indexes;
import online.kheops.auth_server.generated.Keys;
import online.kheops.auth_server.generated.Public;
import online.kheops.auth_server.generated.tables.records.EventsRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row12;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Events extends TableImpl<EventsRecord> {

    private static final long serialVersionUID = -1983157479;

    /**
     * The reference instance of <code>public.events</code>
     */
    public static final Events EVENTS = new Events();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EventsRecord> getRecordType() {
        return EventsRecord.class;
    }

    /**
     * The column <code>public.events.pk</code>.
     */
    public final TableField<EventsRecord, Long> PK = createField(DSL.name("pk"), org.jooq.impl.SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.events.event_type</code>.
     */
    public final TableField<EventsRecord, String> EVENT_TYPE = createField(DSL.name("event_type"), org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.events.album_fk</code>.
     */
    public final TableField<EventsRecord, Long> ALBUM_FK = createField(DSL.name("album_fk"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.events.study_fk</code>.
     */
    public final TableField<EventsRecord, Long> STUDY_FK = createField(DSL.name("study_fk"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.events.event_time</code>.
     */
    public final TableField<EventsRecord, LocalDateTime> EVENT_TIME = createField(DSL.name("event_time"), org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false), this, "");

    /**
     * The column <code>public.events.user_fk</code>.
     */
    public final TableField<EventsRecord, Long> USER_FK = createField(DSL.name("user_fk"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.events.capability_fk</code>.
     */
    public final TableField<EventsRecord, Long> CAPABILITY_FK = createField(DSL.name("capability_fk"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.events.private_target_user_fk</code>.
     */
    public final TableField<EventsRecord, Long> PRIVATE_TARGET_USER_FK = createField(DSL.name("private_target_user_fk"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.events.comment</code>.
     */
    public final TableField<EventsRecord, String> COMMENT = createField(DSL.name("comment"), org.jooq.impl.SQLDataType.VARCHAR(1024), this, "");

    /**
     * The column <code>public.events.mutation_type</code>.
     */
    public final TableField<EventsRecord, String> MUTATION_TYPE = createField(DSL.name("mutation_type"), org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.events.to_user_fk</code>.
     */
    public final TableField<EventsRecord, Long> TO_USER_FK = createField(DSL.name("to_user_fk"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.events.report_provider_fk</code>.
     */
    public final TableField<EventsRecord, Long> REPORT_PROVIDER_FK = createField(DSL.name("report_provider_fk"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>public.events</code> table reference
     */
    public Events() {
        this(DSL.name("events"), null);
    }

    /**
     * Create an aliased <code>public.events</code> table reference
     */
    public Events(String alias) {
        this(DSL.name(alias), EVENTS);
    }

    /**
     * Create an aliased <code>public.events</code> table reference
     */
    public Events(Name alias) {
        this(alias, EVENTS);
    }

    private Events(Name alias, Table<EventsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Events(Name alias, Table<EventsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> Events(Table<O> child, ForeignKey<O, EventsRecord> key) {
        super(child, key, EVENTS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.EVENTS_USER_FK_INDEX);
    }

    @Override
    public Identity<EventsRecord, Long> getIdentity() {
        return Keys.IDENTITY_EVENTS;
    }

    @Override
    public UniqueKey<EventsRecord> getPrimaryKey() {
        return Keys.EVENT_PK;
    }

    @Override
    public List<UniqueKey<EventsRecord>> getKeys() {
        return Arrays.<UniqueKey<EventsRecord>>asList(Keys.EVENT_PK);
    }

    @Override
    public List<ForeignKey<EventsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<EventsRecord, ?>>asList(Keys.EVENTS__EVENT_ALBUM_FK_FKEY, Keys.EVENTS__EVENT_STUDY_FK_FKEY, Keys.EVENTS__EVENT_USER_FK_FKEY, Keys.EVENTS__EVENT_CAPABILITY_FK_FKEY, Keys.EVENTS__EVENT_PRIVATE_TARGET_USER_FK_FKEY, Keys.EVENTS__EVENT_TO_USER_FK_FKEY, Keys.EVENTS__EVENT_REPORT_PROVIDER_FK_FKEY);
    }

    public Albums albums() {
        return new Albums(this, Keys.EVENTS__EVENT_ALBUM_FK_FKEY);
    }

    public Studies studies() {
        return new Studies(this, Keys.EVENTS__EVENT_STUDY_FK_FKEY);
    }

    public Users eventUserFkFkey() {
        return new Users(this, Keys.EVENTS__EVENT_USER_FK_FKEY);
    }

    public Capabilities capabilities() {
        return new Capabilities(this, Keys.EVENTS__EVENT_CAPABILITY_FK_FKEY);
    }

    public Users eventPrivateTargetUserFkFkey() {
        return new Users(this, Keys.EVENTS__EVENT_PRIVATE_TARGET_USER_FK_FKEY);
    }

    public Users eventToUserFkFkey() {
        return new Users(this, Keys.EVENTS__EVENT_TO_USER_FK_FKEY);
    }

    public ReportProviders reportProviders() {
        return new ReportProviders(this, Keys.EVENTS__EVENT_REPORT_PROVIDER_FK_FKEY);
    }

    @Override
    public Events as(String alias) {
        return new Events(DSL.name(alias), this);
    }

    @Override
    public Events as(Name alias) {
        return new Events(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Events rename(String name) {
        return new Events(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Events rename(Name name) {
        return new Events(name, null);
    }

    // -------------------------------------------------------------------------
    // Row12 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row12<Long, String, Long, Long, LocalDateTime, Long, Long, Long, String, String, Long, Long> fieldsRow() {
        return (Row12) super.fieldsRow();
    }
}