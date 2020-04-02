INSERT INTO CASELOADS (CASELOAD_ID, DESCRIPTION, CASELOAD_TYPE, LIST_SEQ, TRUST_ACCOUNTS_FLAG, ACCESS_PROPERTY_FLAG, TRUST_CASELOAD_ID, PAYROLL_FLAG, ACTIVE_FLAG, DEACTIVATION_DATE, COMMISSARY_FLAG, PAYROLL_TRUST_CASELOAD, COMMISSARY_TRUST_CASELOAD, TRUST_COMMISSARY_CASELOAD, COMMUNITY_TRUST_CASELOAD, MDT_FLAG, CASELOAD_FUNCTION)
VALUES ('MTC', 'MOVE_TO_CELL (HMP)', 'INST', 20, 'Y', 'Y', null, 'N', 'Y', null, 'N', null, null, null, null, 'Y', 'GENERAL');

INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('MTC', 'MOVE_TO_CELL', 'INST', 'Y');

INSERT INTO CASELOAD_AGENCY_LOCATIONS (CASELOAD_ID, AGY_LOC_ID)
VALUES ('MTC', 'MTC');
INSERT INTO CASELOAD_AGENCY_LOCATIONS (CASELOAD_ID, AGY_LOC_ID)
VALUES  ('MTC', 'OUT');
INSERT INTO CASELOAD_AGENCY_LOCATIONS (CASELOAD_ID, AGY_LOC_ID)
VALUES  ('MTC', 'TRN');

INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME, STATUS, SEX_CODE, BIRTHDATE)
VALUES (-101,  'MTC',  'MTC',  'Move to cell User',     'API',     'MTC_USER', 'ACTIVE', 'M', '1970-02-01');

INSERT INTO STAFF_USER_ACCOUNTS (USERNAME, STAFF_ID, STAFF_USER_TYPE, ID_SOURCE, WORKING_CASELOAD_ID)
VALUES ('MTC_USER', -101, 'GENERAL', 'USER', 'MTC');

INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE)
VALUES ('MTC', 'MTC_USER', trunc(sysdate) - 24);

INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID)
VALUES (-101, 'MTC_USER', 'MTC');


INSERT INTO AGENCY_INTERNAL_LOCATIONS (INTERNAL_LOCATION_ID, INTERNAL_LOCATION_CODE, INTERNAL_LOCATION_TYPE, AGY_LOC_ID, UNIT_TYPE, CERTIFIED_FLAG, OPERATION_CAPACITY, NO_OF_OCCUPANT, DESCRIPTION, PARENT_INTERNAL_LOCATION_ID, USER_DESC)
VALUES
(-300, 'A',    'CELL',  'MTC',   'NA', 'Y', 2, 1, 'BMI-A', null, 'Cell A'),
(-301, 'B',    'CELL',  'MTC',   'NA', 'Y', 2, 1, 'BMI-B', null, 'Cell B'),
(-302, 'C',    'CELL',  'MTC',   'NA', 'Y', 2, 1, 'BMI-C', null, 'Cell C');

INSERT INTO OFFENDERS (OFFENDER_ID, ID_SOURCE_CODE, LAST_NAME, FIRST_NAME, SEX_CODE, CREATE_DATE, LAST_NAME_KEY, OFFENDER_ID_DISPLAY, ROOT_OFFENDER_ID, RACE_CODE, ALIAS_NAME_TYPE, BIRTH_DATE, BIRTH_COUNTRY_CODE)
VALUES (-1056, 'SEQ', 'SMITH', 'MICK', 'm', TO_DATE('2020-03-31', 'YYYY-MM-DD'), 'SMITH', 'A1180IA', -1056, 'M2', 'A', TO_DATE('1980-05-21', 'YYYY-MM-DD'), 'UK'),
       (-1057, 'SEQ', 'JONES', 'BOB', 'm', TO_DATE('2020-03-31', 'YYYY-MM-DD'), 'JONES', 'A1180IB', -1057, 'M2', 'A', TO_DATE('1980-05-21', 'YYYY-MM-DD'), 'UK');

INSERT INTO OFFENDER_BOOKINGS (OFFENDER_BOOK_ID, BOOKING_BEGIN_DATE, BOOKING_NO, OFFENDER_ID, BOOKING_SEQ, DISCLOSURE_FLAG, IN_OUT_STATUS, ACTIVE_FLAG, YOUTH_ADULT_CODE, AGY_LOC_ID, ROOT_OFFENDER_ID, LIVING_UNIT_ID)
VALUES (-56, sysdate, 'Z00056', -1056, 1, 'N', 'IN',  'Y', 'N', 'MTC', -1056, -302),
       (-57, sysdate, 'Z00057', -1057, 1, 'N', 'IN',  'Y', 'N', 'MTC', -1057, null);
