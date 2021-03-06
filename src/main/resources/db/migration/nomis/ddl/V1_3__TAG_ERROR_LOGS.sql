CREATE TABLE "TAG_ERROR_LOGS"
(
  "TAG_ERROR_ID"                  NUMBER(10, 0)                     NOT NULL ,
  "SID"                           NUMBER,
  "MODULE_NAME"                   VARCHAR2(20),
  "PROCEDURE_NAME"                VARCHAR2(60),
  "ERROR_MESSAGE"                 VARCHAR2(2000),
  "ERROR_LOCATION"                VARCHAR2(512),
  "MODIFY_USER_ID"                VARCHAR2(32),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32) DEFAULT USER         NOT NULL ,
  "USER_MODULE"                   VARCHAR2(70),
  "USER_LOCATION"                 VARCHAR2(12),
  "USER_MESSAGE"                  VARCHAR2(200),
  "USER_ERROR_CODE"               NUMBER(8, 0),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32),
  "AUDIT_MODULE_NAME"             VARCHAR2(65),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256),
  CONSTRAINT "TAG_ERROR_LOGS_PK" PRIMARY KEY ("TAG_ERROR_ID")
);