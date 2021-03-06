CREATE TABLE "CASELOAD_AGENCY_LOCATIONS"
(
  "CASELOAD_ID"                   VARCHAR2(6 CHAR)                  NOT NULL ,
  "AGY_LOC_ID"                    VARCHAR2(6 CHAR)                  NOT NULL ,
  "UPDATE_ALLOWED_FLAG"           VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "CASELOAD_AGENCY_LOCATIONS_PK" PRIMARY KEY ("CASELOAD_ID", "AGY_LOC_ID"),
  CONSTRAINT "CSLD_AL_AGY_LOC_F1" FOREIGN KEY ("AGY_LOC_ID")
  REFERENCES "AGENCY_LOCATIONS" ("AGY_LOC_ID") ,
  CONSTRAINT "CASELOAD_AGENCY_LOCATIONS_FK5" FOREIGN KEY ("CASELOAD_ID")
  REFERENCES "CASELOADS" ("CASELOAD_ID")
);


COMMENT ON COLUMN "CASELOAD_AGENCY_LOCATIONS"."CASELOAD_ID" IS ' An identifying code for a caseload.';

COMMENT ON COLUMN "CASELOAD_AGENCY_LOCATIONS"."AGY_LOC_ID" IS 'The location residing within an agency.';

COMMENT ON COLUMN "CASELOAD_AGENCY_LOCATIONS"."UPDATE_ALLOWED_FLAG" IS ' Defines whether users can update at agency location within caseload.';

COMMENT ON COLUMN "CASELOAD_AGENCY_LOCATIONS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "CASELOAD_AGENCY_LOCATIONS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "CASELOAD_AGENCY_LOCATIONS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "CASELOAD_AGENCY_LOCATIONS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "CASELOAD_AGENCY_LOCATIONS" IS 'A physical location at which a Caseload is operative.';


CREATE INDEX "CASELOAD_AGENCY_LOCATIONS_NI1"
  ON "CASELOAD_AGENCY_LOCATIONS" ("AGY_LOC_ID");


