CREATE TABLE OFFENDER_CSIP_REVIEWS (
	CSIP_ID NUMBER(10,0) NOT NULL,
	REVIEW_ID NUMBER(10,0) NOT NULL,
	REVIEW_SEQ NUMBER(10,0) NOT NULL,
	CREATE_DATE DATE,
	NEXT_REVIEW_DATE DATE,
	CLOSE_DATE DATE,
	REMAIN_ON_CSIP VARCHAR2(1) DEFAULT 'N',
	CSIP_UPDATED VARCHAR2(1) DEFAULT 'N',
	CASE_NOTE VARCHAR2(1) DEFAULT 'N',
	CLOSE_CSIP VARCHAR2(1) DEFAULT 'N',
	SUMMARY VARCHAR2(4000),
	CREATE_USER VARCHAR2(32),
	PEOPLE_INFORMED VARCHAR2(1) DEFAULT 'N',
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
	CONSTRAINT OFF_CSIP_REVIEWS_PK PRIMARY KEY (REVIEW_ID),
	CONSTRAINT OFF_CSIP_RVW_CSIP_REP FOREIGN KEY (CSIP_ID) REFERENCES OFFENDER_CSIP_REPORTS(CSIP_ID)
);
CREATE INDEX OFFENDER_CSIP_REVIEWS_X01 ON OFFENDER_CSIP_REVIEWS (CSIP_ID);
CREATE UNIQUE INDEX OFF_CSIP_REVIEWS_PK ON OFFENDER_CSIP_REVIEWS (REVIEW_ID);
