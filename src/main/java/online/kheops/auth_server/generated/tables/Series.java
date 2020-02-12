/*
 * This file is generated by jOOQ.
 */
package online.kheops.auth_server.generated.tables;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import online.kheops.auth_server.generated.Indexes;
import online.kheops.auth_server.generated.Keys;
import online.kheops.auth_server.generated.Public;
import online.kheops.auth_server.generated.tables.records.SeriesRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


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
public class Series extends TableImpl<SeriesRecord> {

    private static final long serialVersionUID = -937850834;

    /**
     * The reference instance of <code>public.series</code>
     */
    public static final Series SERIES = new Series();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SeriesRecord> getRecordType() {
        return SeriesRecord.class;
    }

    /**
     * The column <code>public.series.pk</code>.
     */
    public final TableField<SeriesRecord, Long> PK = createField("pk", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.series.created_time</code>.
     */
    public final TableField<SeriesRecord, Timestamp> CREATED_TIME = createField("created_time", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>public.series.updated_time</code>.
     */
    public final TableField<SeriesRecord, Timestamp> UPDATED_TIME = createField("updated_time", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>public.series.modality</code>.
     */
    public final TableField<SeriesRecord, String> MODALITY = createField("modality", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.series.timezone_offset_from_utc</code>.
     */
    public final TableField<SeriesRecord, String> TIMEZONE_OFFSET_FROM_UTC = createField("timezone_offset_from_utc", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.series.series_description</code>.
     */
    public final TableField<SeriesRecord, String> SERIES_DESCRIPTION = createField("series_description", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.series.series_uid</code>.
     */
    public final TableField<SeriesRecord, String> SERIES_UID = createField("series_uid", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.series.series_number</code>.
     */
    public final TableField<SeriesRecord, Integer> SERIES_NUMBER = createField("series_number", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.series.body_part_examined</code>.
     */
    public final TableField<SeriesRecord, String> BODY_PART_EXAMINED = createField("body_part_examined", org.jooq.impl.SQLDataType.VARCHAR(32), this, "");

    /**
     * The column <code>public.series.number_of_series_related_instances</code>.
     */
    public final TableField<SeriesRecord, Integer> NUMBER_OF_SERIES_RELATED_INSTANCES = createField("number_of_series_related_instances", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.series.study_fk</code>.
     */
    public final TableField<SeriesRecord, Long> STUDY_FK = createField("study_fk", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.series.populated</code>.
     */
    public final TableField<SeriesRecord, Boolean> POPULATED = createField("populated", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

    /**
     * Create a <code>public.series</code> table reference
     */
    public Series() {
        this(DSL.name("series"), null);
    }

    /**
     * Create an aliased <code>public.series</code> table reference
     */
    public Series(String alias) {
        this(DSL.name(alias), SERIES);
    }

    /**
     * Create an aliased <code>public.series</code> table reference
     */
    public Series(Name alias) {
        this(alias, SERIES);
    }

    private Series(Name alias, Table<SeriesRecord> aliased) {
        this(alias, aliased, null);
    }

    private Series(Name alias, Table<SeriesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Series(Table<O> child, ForeignKey<O, SeriesRecord> key) {
        super(child, key, SERIES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.SERIES_PK, Indexes.SERIES_POPULATED_INDEX, Indexes.SERIES_UID_UNIQUE, Indexes.STUDY_FK_INDEX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<SeriesRecord> getPrimaryKey() {
        return Keys.SERIES_PK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<SeriesRecord>> getKeys() {
        return Arrays.<UniqueKey<SeriesRecord>>asList(Keys.SERIES_PK, Keys.SERIES_UID_UNIQUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<SeriesRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<SeriesRecord, ?>>asList(Keys.SERIES__SERIES_STUDY_FK_FKEY);
    }

    public Studies studies() {
        return new Studies(this, Keys.SERIES__SERIES_STUDY_FK_FKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Series as(String alias) {
        return new Series(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Series as(Name alias) {
        return new Series(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Series rename(String name) {
        return new Series(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Series rename(Name name) {
        return new Series(name, null);
    }
}
