package online.kheops.auth_server.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Study.class)
public abstract class Study_ {

	public static volatile SingularAttribute<Study, LocalDateTime> updatedTime;
	public static volatile SingularAttribute<Study, String> patientName;
	public static volatile SingularAttribute<Study, String> studyTime;
	public static volatile SingularAttribute<Study, String> timezoneOffsetFromUTC;
	public static volatile SingularAttribute<Study, String> patientSex;
	public static volatile SingularAttribute<Study, String> patientID;
	public static volatile SingularAttribute<Study, String> studyInstanceUID;
	public static volatile SingularAttribute<Study, String> studyDescription;
	public static volatile SingularAttribute<Study, Boolean> populated;
	public static volatile SingularAttribute<Study, String> referringPhysicianName;
	public static volatile SingularAttribute<Study, String> accessionNumber;
	public static volatile SingularAttribute<Study, String> patientBirthDate;
	public static volatile SingularAttribute<Study, LocalDateTime> createdTime;
	public static volatile SingularAttribute<Study, String> studyDate;
	public static volatile SingularAttribute<Study, String> studyID;
	public static volatile SingularAttribute<Study, Long> pk;

	public static final String UPDATED_TIME = "updatedTime";
	public static final String PATIENT_NAME = "patientName";
	public static final String STUDY_TIME = "studyTime";
	public static final String TIMEZONE_OFFSET_FROM_UT_C = "timezoneOffsetFromUTC";
	public static final String PATIENT_SEX = "patientSex";
	public static final String PATIENT_ID = "patientID";
	public static final String STUDY_INSTANCE_UI_D = "studyInstanceUID";
	public static final String STUDY_DESCRIPTION = "studyDescription";
	public static final String POPULATED = "populated";
	public static final String REFERRING_PHYSICIAN_NAME = "referringPhysicianName";
	public static final String ACCESSION_NUMBER = "accessionNumber";
	public static final String PATIENT_BIRTH_DATE = "patientBirthDate";
	public static final String CREATED_TIME = "createdTime";
	public static final String STUDY_DATE = "studyDate";
	public static final String STUDY_ID = "studyID";
	public static final String PK = "pk";

}

