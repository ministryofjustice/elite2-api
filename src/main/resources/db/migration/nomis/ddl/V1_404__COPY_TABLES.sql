CREATE TABLE COPY_TABLES
(
    TABLE_OPERATION_CODE          VARCHAR2(12)                      NOT NULL,
    MOVEMENT_TYPE                 VARCHAR2(12)                      NOT NULL,
    MOVEMENT_REASON_CODE          VARCHAR2(12)                      NOT NULL,
    TABLE_NAME                    VARCHAR2(40)                      NOT NULL,
    ACTIVE_FLAG                   VARCHAR2(1)  DEFAULT 'Y'          NOT NULL,
    EXPIRY_DATE                   DATE,
    LIST_SEQ                      NUMBER(6, 0) DEFAULT 1            NOT NULL,
    COL_NAME                      VARCHAR2(40),
    SEQ_NAME                      VARCHAR2(40),
    PARENT_TABLE                  VARCHAR2(40),
    UPDATE_ALLOWED_FLAG           VARCHAR2(1)  DEFAULT 'Y'          NOT NULL,
    CREATE_USER_ID                VARCHAR2(32) DEFAULT USER         NOT NULL,
    CREATE_DATETIME               TIMESTAMP(9) DEFAULT systimestamp NOT NULL,
    MODIFY_DATETIME               TIMESTAMP(9),
    MODIFY_USER_ID                VARCHAR2(32),
    AUDIT_TIMESTAMP               TIMESTAMP(9),
    AUDIT_USER_ID                 VARCHAR2(32),
    AUDIT_MODULE_NAME             VARCHAR2(65),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256),
    CONSTRAINT COPY_TABLES_PK PRIMARY KEY (TABLE_OPERATION_CODE, MOVEMENT_TYPE, MOVEMENT_REASON_CODE,
                                             TABLE_NAME)
);
