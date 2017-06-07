CREATE USER ITAG_USER PASSWORD 'password';
CREATE USER ELITE2_API_USER PASSWORD 'password';

INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, USER_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME, PERSONNEL_TYPE)
VALUES (-1, 'LEI', 'LEI', 'ELITE2_API_USER', 'USER', 'ELITE2', 'API', 'STAFF');
INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, USER_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME, PERSONNEL_TYPE)
VALUES (-2, 'LEI', 'LEI', 'ITAG_USER', 'USER', 'API', 'ITAG', 'STAFF');

INSERT INTO STAFF_MEMBER_ROLES (STAFF_ID, ROLE_ID, ROLE_CODE) VALUES (-1, -1, 'ADMIN');

INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS)
VALUES (-1, -1, 'STF', 'EMAIL', 'elite2-api-user@syscon.net');
INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS)
VALUES (-2, -2, 'STF', 'EMAIL', 'itaguser@syscon.net');

INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID)
VALUES (-1, 'ELITE2_API_USER', 'LEI');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID)
VALUES (-2, 'ITAG_USER', 'LEI');

INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE, PARENT_ROLE_CODE)
VALUES ( -1, 'Administrator', 1, 'ADMIN', null);
INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE, PARENT_ROLE_CODE)
VALUES ( -2, 'Wing Officer', 1, 'WING_OFF', null);
