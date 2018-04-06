DROP DATABASE IF EXISTS kheops;

CREATE DATABASE kheops
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kheops;

-- create table series (pk bigint not null auto_increment, body_part varchar(255) not null, completeness integer not null, created_time datetime not null, expiration_date varchar(255), ext_retrieve_aet varchar(255), failed_retrieves integer not null, inst_purge_state integer not null, inst_purge_time datetime, institution varchar(255) not null, department varchar(255) not null, laterality varchar(255) not null, metadata_update_time datetime, modality varchar(255) not null, pps_cuid varchar(255) not null, pps_iuid varchar(255) not null, pps_start_date varchar(255) not null, pps_start_time varchar(255) not null, rejection_state integer not null, series_custom1 varchar(255) not null, series_custom2 varchar(255) not null, series_custom3 varchar(255) not null, series_desc varchar(255) not null, series_iuid varchar(255) not null, series_no integer, series_size bigint not null, sop_cuid varchar(255) not null, src_aet varchar(255), station_name varchar(255) not null, tsuid varchar(255) not null, updated_time datetime not null, version bigint, dicomattrs_fk bigint not null, inst_code_fk bigint, metadata_fk bigint, perf_phys_name_fk bigint, study_fk bigint not null, primary key (pk));
CREATE TABLE users (
  pk BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL,

  PRIMARY KEY (pk),
  INDEX username_index (username)
);

CREATE TABLE studies (
  pk BIGINT NOT NULL AUTO_INCREMENT,
  study_uid VARCHAR(255) NOT NULL,

  PRIMARY KEY (pk),
  INDEX study_uid_index (study_uid)
);

CREATE TABLE user_studies (
  pk BIGINT NOT NULL AUTO_INCREMENT,
  user_fk BIGINT NOT NULL,
  study_fk BIGINT NOT NULL,

  PRIMARY KEY (pk),
  INDEX user_fk_index (user_fk),
  INDEX study_fk_index (study_fk),

  FOREIGN KEY (user_fk)
    REFERENCES users(pk)
    ON DELETE RESTRICT,
  FOREIGN KEY (study_fk)
    REFERENCES studies(pk)
    ON DELETE RESTRICT
);

CREATE TABLE series (
  pk BIGINT NOT NULL AUTO_INCREMENT,
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL,
  modality VARCHAR(255),
  series_uid VARCHAR(255) NOT NULL,
  series_size BIGINT NOT NULL,
  study_fk BIGINT NOT NULL,

  PRIMARY KEY (pk),
  INDEX series_uid_index (series_uid),
  INDEX study_fk_index (study_fk),

  FOREIGN KEY (study_fk)
    REFERENCES studies(pk)
    ON DELETE RESTRICT
);

CREATE TABLE user_studies_series (
  user_study_fk BIGINT NOT NULL,
  series_fk BIGINT NOT NULL,

  PRIMARY KEY (user_study_fk, series_fk),

  FOREIGN KEY (user_study_fk)
    REFERENCES user_studies(pk)
    ON DELETE RESTRICT,
  FOREIGN KEY (series_fk)
    REFERENCES series(pk)
    ON DELETE RESTRICT
);
