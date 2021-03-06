CREATE TABLE OFFENDER_CSIP_PLANS (
	CSIP_ID NUMBER(10,0) NOT NULL,
	PLAN_ID NUMBER(10,0) NOT NULL,
	CREATE_DATE DATE NOT NULL,
	TARGET_DATE DATE NOT NULL,
	CLOSED_DATE DATE,
	IDENTIFIED_NEED VARCHAR2(1000) NOT NULL,
	PROGRESSION VARCHAR2(4000),
	INTERVENTION VARCHAR2(4000) NOT NULL,
	BY_WHOM VARCHAR2(100) NOT NULL,
	CREATE_DATETIME TIMESTAMP DEFAULT systimestamp,
	CREATE_USER_ID VARCHAR2(32) DEFAULT USER ,
	MODIFY_DATETIME TIMESTAMP,
	MODIFY_USER_ID VARCHAR2(32),
	AUDIT_TIMESTAMP TIMESTAMP,
	AUDIT_USER_ID VARCHAR2(32),
	AUDIT_MODULE_NAME VARCHAR2(65),
	AUDIT_CLIENT_USER_ID VARCHAR2(64),
	AUDIT_CLIENT_IP_ADDRESS VARCHAR2(39),
	AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
	AUDIT_ADDITIONAL_INFO VARCHAR2(256),
	CONSTRAINT OFF_CSIP_PLANS_PK PRIMARY KEY (PLAN_ID),
	CONSTRAINT OFF_CSIP_PLAN_CSIP_REP FOREIGN KEY (CSIP_ID) REFERENCES OFFENDER_CSIP_REPORTS(CSIP_ID)
);
CREATE INDEX OFFENDER_CSIP_PLANS_X01 ON OFFENDER_CSIP_PLANS (CSIP_ID);
CREATE UNIQUE INDEX OFF_CSIP_PLANS_PK ON OFFENDER_CSIP_PLANS (PLAN_ID);
