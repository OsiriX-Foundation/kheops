DROP DATABASE IF EXISTS kheops;

CREATE DATABASE kheops
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kheops;

CREATE TABLE users (
  pk BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL,

  PRIMARY KEY (pk),
  INDEX username_index (username),

  UNIQUE username_unique (username)
);

CREATE TABLE studies (
  pk BIGINT NOT NULL AUTO_INCREMENT,
  study_uid VARCHAR(255) NOT NULL,

  PRIMARY KEY (pk),
  INDEX study_uid_index (study_uid),

  UNIQUE study_uid_unique (study_uid)
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
    ON DELETE RESTRICT,

  UNIQUE series_uid_unique (series_uid)
);

CREATE TABLE user_series (
  user_fk BIGINT NOT NULL,
  series_fk BIGINT NOT NULL,

  PRIMARY KEY (user_fk, series_fk),

  FOREIGN KEY (user_fk)
    REFERENCES users(pk)
    ON DELETE RESTRICT,
  FOREIGN KEY (series_fk)
    REFERENCES series(pk)
    ON DELETE RESTRICT
);
