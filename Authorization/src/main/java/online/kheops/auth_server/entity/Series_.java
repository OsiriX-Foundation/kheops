package online.kheops.auth_server.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Series.class)
public abstract class Series_ {

	public static volatile SingularAttribute<Series, LocalDateTime> updatedTime;
	public static volatile SingularAttribute<Series, String> seriesInstanceUID;
	public static volatile SingularAttribute<Series, String> timezoneOffsetFromUTC;
	public static volatile SingularAttribute<Series, Study> study;
	public static volatile SingularAttribute<Series, String> modality;
	public static volatile SingularAttribute<Series, String> seriesDescription;
	public static volatile SingularAttribute<Series, Integer> numberOfSeriesRelatedInstances;
	public static volatile SingularAttribute<Series, LocalDateTime> createdTime;
	public static volatile SingularAttribute<Series, String> bodyPartExamined;
	public static volatile SingularAttribute<Series, Boolean> populated;
	public static volatile SingularAttribute<Series, Long> pk;
	public static volatile SingularAttribute<Series, Integer> seriesNumber;

	public static final String UPDATED_TIME = "updatedTime";
	public static final String SERIES_INSTANCE_UI_D = "seriesInstanceUID";
	public static final String TIMEZONE_OFFSET_FROM_UT_C = "timezoneOffsetFromUTC";
	public static final String STUDY = "study";
	public static final String MODALITY = "modality";
	public static final String SERIES_DESCRIPTION = "seriesDescription";
	public static final String NUMBER_OF_SERIES_RELATED_INSTANCES = "numberOfSeriesRelatedInstances";
	public static final String CREATED_TIME = "createdTime";
	public static final String BODY_PART_EXAMINED = "bodyPartExamined";
	public static final String POPULATED = "populated";
	public static final String PK = "pk";
	public static final String SERIES_NUMBER = "seriesNumber";

}

