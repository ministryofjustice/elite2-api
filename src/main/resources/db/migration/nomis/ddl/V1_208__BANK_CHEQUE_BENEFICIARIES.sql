
  CREATE TABLE "BANK_CHEQUE_BENEFICIARIES"
   (    "CHEQUE_TXN_ID" NUMBER(10,0) NOT NULL,
    "CHEQUE_AMOUNT" NUMBER(11,2) NOT NULL,
    "PERSON_ID" NUMBER(10,0),
    "CORPORATE_ID" NUMBER(10,0),
    "TXN_ID" NUMBER(10,0),
    "OFFENDER_ID" NUMBER(10,0),
    "AMOUNT" NUMBER(11,2) NOT NULL,
    "OFFENDER_DEDUCTION_ID" NUMBER(10,0),
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "MODIFY_DATETIME" TIMESTAMP (9),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
     CONSTRAINT "BANK_CHEQUE_BENEFICIARIES_FK10" FOREIGN KEY ("OFFENDER_ID")
      REFERENCES "OFFENDERS" ("OFFENDER_ID")
  );

  CREATE INDEX "BANK_CHEQUE_BENEFICIARIES_FK1" ON "BANK_CHEQUE_BENEFICIARIES" ("OFFENDER_ID");


  CREATE INDEX "BANK_CHEQUE_BENEFICIARIES_NI1" ON "BANK_CHEQUE_BENEFICIARIES" ("CHEQUE_TXN_ID");


  CREATE INDEX "BC_BEN_NI1" ON "BANK_CHEQUE_BENEFICIARIES" ("PERSON_ID");


  CREATE INDEX "BC_BEN_NI2" ON "BANK_CHEQUE_BENEFICIARIES" ("CORPORATE_ID");