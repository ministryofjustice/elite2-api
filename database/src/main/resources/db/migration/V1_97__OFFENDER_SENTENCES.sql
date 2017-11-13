CREATE TABLE "OFFENDER_SENTENCES"
(
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ENABLE,
  "SENTENCE_SEQ"                  NUMBER(6, 0)                      NOT NULL ENABLE,
  "ORDER_ID"                      NUMBER(10, 0),
  "SENTENCE_CALC_TYPE"            VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "SENTENCE_STATUS"               VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "CONSEC_TO_SENTENCE_SEQ"        NUMBER(6, 0),
  "START_DATE"                    DATE                              NOT NULL ENABLE,
  "END_DATE"                      DATE,
  "COMMENT_TEXT"                  VARCHAR2(240 CHAR),
  "TERMINATION_REASON"            VARCHAR2(12 CHAR),
  "NO_OF_UNEXCUSED_ABSENCE"       NUMBER(6, 0),
  "CASE_ID"                       NUMBER(10, 0),
  "ETD_CALCULATED_DATE"           DATE,
  "MTD_CALCULATED_DATE"           DATE,
  "LTD_CALCULATED_DATE"           DATE,
  "ARD_CALCULATED_DATE"           DATE,
  "CRD_CALCULATED_DATE"           DATE,
  "PED_CALCULATED_DATE"           DATE,
  "APD_CALCULATED_DATE"           DATE,
  "NPD_CALCULATED_DATE"           DATE,
  "LED_CALCULATED_DATE"           DATE,
  "SED_CALCULATED_DATE"           DATE,
  "PRRD_CALCULATED_DATE"          DATE,
  "TARIFF_CALCULATED_DATE"        DATE,
  "AGG_SENTENCE_SEQ"              NUMBER(6, 0),
  "SENTENCE_CATEGORY"             VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "FINE_AMOUNT"                   NUMBER(11, 2),
  "HDCED_CALCULATED_DATE"         DATE,
  "SENTENCE_TEXT"                 VARCHAR2(40 CHAR),
  "REVOKED_DATE"                  DATE,
  "REVOKED_STAFF_ID"              NUMBER(10, 0),
  "BREACH_LEVEL"                  NUMBER(6, 0),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ENABLE,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ENABLE,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "TERMINATION_DATE"              DATE,
  "AGGREGATE_TERM"                NUMBER(10, 0),
  "AGGREGATE_ADJUST_DAYS"         NUMBER(10, 0),
  "SENTENCE_LEVEL"                VARCHAR2(12 CHAR) DEFAULT 'IND'   NOT NULL ENABLE,
  "EXTENDED_DAYS"                 NUMBER(6, 0),
  "COUNTS"                        NUMBER(6, 0),
  "DISCHARGE_DATE"                DATE,
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "STATUS_UPDATE_REASON"          VARCHAR2(12 CHAR),
  "STATUS_UPDATE_COMMENT"         VARCHAR2(400 CHAR),
  "STATUS_UPDATE_DATE"            DATE,
  "STATUS_UPDATE_STAFF_ID"        NUMBER(10, 0),
  "NOMSENTDETAILREF"              NUMBER(10, 0),
  "NOMCONSTOSENTDETAILREF"        NUMBER(10, 0),
  "NOMCONSFROMSENTDETAILREF"      NUMBER(10, 0),
  "NOMCONCWITHSENTDETAILREF"      NUMBER(10, 0),
  "WORKFLOW_ID"                   NUMBER(32, 0),
  "LINE_SEQ"                      NUMBER(6, 0),
  "HDC_EXCLUSION_FLAG"            VARCHAR2(1 CHAR) DEFAULT 'N',
  "HDC_EXCLUSION_REASON"          VARCHAR2(12 CHAR),
  "CJA_ACT"                       VARCHAR2(12 CHAR),
  "DPRRD_CALCULATED_DATE"         DATE,
  "START_DATE_2CALC"              DATE,
  "SLED_2CALC"                    DATE,
  "TUSED_CALCULATED_DATE"         DATE,
  CONSTRAINT "OFFENDER_SENTENCES_PK" PRIMARY KEY ("OFFENDER_BOOK_ID", "SENTENCE_SEQ"),
  CONSTRAINT "OFF_SENT_OFF_CASE_FK" FOREIGN KEY ("CASE_ID")
  REFERENCES "OFFENDER_CASES" ("CASE_ID") ENABLE,
  CONSTRAINT "OFF_SENT_SENT_CALC_TYPE_FK" FOREIGN KEY ("SENTENCE_CATEGORY", "SENTENCE_CALC_TYPE")
  REFERENCES "SENTENCE_CALC_TYPES" ("SENTENCE_CATEGORY", "SENTENCE_CALC_TYPE") ENABLE,
  CONSTRAINT "OFF_SENT_ORDER_FK" FOREIGN KEY ("ORDER_ID")
  REFERENCES "ORDERS" ("ORDER_ID") ENABLE,
  CONSTRAINT "OFFENDER_SENTENCES_FK9" FOREIGN KEY ("OFFENDER_BOOK_ID")
  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID") ENABLE
);


COMMENT ON COLUMN "OFFENDER_SENTENCES"."DPRRD_CALCULATED_DATE" IS 'DTO Post Recall calculated Release Date';
COMMENT ON COLUMN "OFFENDER_SENTENCES"."START_DATE_2CALC" IS 'To hold start date for 2calc calculation';
COMMENT ON COLUMN "OFFENDER_SENTENCES"."SLED_2CALC" IS 'To hold SLED for 2calc calculation';
COMMENT ON COLUMN "OFFENDER_SENTENCES"."TUSED_CALCULATED_DATE" IS 'Top up Supervision Expiry Date - Calculated Date.';


CREATE INDEX "OFFENDER_SENTENCES_NI1"
  ON "OFFENDER_SENTENCES" ("ORDER_ID");
CREATE INDEX "OFFENDER_SENTENCES_NI2"
  ON "OFFENDER_SENTENCES" ("SENTENCE_CATEGORY", "SENTENCE_CALC_TYPE");
CREATE INDEX "OFFENDER_SENTENCES_NI3"
  ON "OFFENDER_SENTENCES" ("CASE_ID");
CREATE INDEX "OFFENDER_SENTENCES_NI4"
  ON "OFFENDER_SENTENCES" ("REVOKED_STAFF_ID");
CREATE INDEX "OFFENDER_SENTENCES_NI5"
  ON "OFFENDER_SENTENCES" ("START_DATE");
CREATE INDEX "OFFENDER_SENTENCES_NI6"
  ON "OFFENDER_SENTENCES" ("END_DATE");
