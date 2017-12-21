INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE, PARENT_ROLE_CODE) VALUES ( -1, 'System User', 1, 'SYSTEM_USER', null);
INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE, PARENT_ROLE_CODE) VALUES ( -2, 'General', 1, 'GEN', null);

INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME) VALUES (-1, 'AG1', 'AG1', 'Roderick', 'Royce', 'Merle');
INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME) VALUES (-2, 'AG2', 'AG2', 'Wallis', 'Gerry', 'Jayce');
INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME) VALUES (-3, 'AG3', 'AG3', 'Simms', 'Stan', 'Brion');
INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME) VALUES (-4, 'AG4', 'AG4', 'Danielson', 'Sherwood', 'Trent');
INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME) VALUES (-5, 'AG5', 'AG5', 'Asher', 'Woody', 'Rowland');
INSERT INTO STAFF_MEMBERS (STAFF_ID, ASSIGNED_CASELOAD_ID, WORKING_CASELOAD_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME) VALUES (-6, 'AG2', 'AG2', 'Eliott', 'Daniel', null);

INSERT INTO STAFF_USER_ACCOUNTS (USERNAME, STAFF_ID, STAFF_USER_TYPE, ID_SOURCE, WORKING_CASELOAD_ID) VALUES ('OFFICER1', -1, 'GENERAL', 'USER', 'AG1');
INSERT INTO STAFF_USER_ACCOUNTS (USERNAME, STAFF_ID, STAFF_USER_TYPE, ID_SOURCE, WORKING_CASELOAD_ID) VALUES ('OFFICER2', -2, 'GENERAL', 'USER', 'AG2');
INSERT INTO STAFF_USER_ACCOUNTS (USERNAME, STAFF_ID, STAFF_USER_TYPE, ID_SOURCE, WORKING_CASELOAD_ID) VALUES ('OFFICER3', -3, 'GENERAL', 'USER', 'AG3');
INSERT INTO STAFF_USER_ACCOUNTS (USERNAME, STAFF_ID, STAFF_USER_TYPE, ID_SOURCE, WORKING_CASELOAD_ID) VALUES ('OFFICER4', -4, 'GENERAL', 'USER', 'AG4');
INSERT INTO STAFF_USER_ACCOUNTS (USERNAME, STAFF_ID, STAFF_USER_TYPE, ID_SOURCE, WORKING_CASELOAD_ID) VALUES ('OFFICER5', -5, 'GENERAL', 'USER', 'AG5');
INSERT INTO STAFF_USER_ACCOUNTS (USERNAME, STAFF_ID, STAFF_USER_TYPE, ID_SOURCE, WORKING_CASELOAD_ID) VALUES ('ADMINSTAFF', -6, 'GENERAL', 'USER', 'AG2');

INSERT INTO STAFF_MEMBER_ROLES (ROLE_ID, STAFF_ID, ROLE_CODE) VALUES (-2, -1, 'GEN');
INSERT INTO STAFF_MEMBER_ROLES (ROLE_ID, STAFF_ID, ROLE_CODE) VALUES (-2, -2, 'GEN');
INSERT INTO STAFF_MEMBER_ROLES (ROLE_ID, STAFF_ID, ROLE_CODE) VALUES (-2, -3, 'GEN');
INSERT INTO STAFF_MEMBER_ROLES (ROLE_ID, STAFF_ID, ROLE_CODE) VALUES (-2, -4, 'GEN');
INSERT INTO STAFF_MEMBER_ROLES (ROLE_ID, STAFF_ID, ROLE_CODE) VALUES (-2, -5, 'GEN');
INSERT INTO STAFF_MEMBER_ROLES (ROLE_ID, STAFF_ID, ROLE_CODE) VALUES (-2, -6, 'GEN');
INSERT INTO STAFF_MEMBER_ROLES (ROLE_ID, STAFF_ID, ROLE_CODE) VALUES (-1, -6, 'SYSTEM_USER');

INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS) VALUES (-1, -1, 'STF', 'EMAIL', 'RRoderick@syscon.net');
INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS) VALUES (-2, -2, 'STF', 'EMAIL', 'GWallis@syscon.net');
INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS) VALUES (-3, -3, 'STF', 'EMAIL', 'SSimms@syscon.net');
INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS) VALUES (-4, -4, 'STF', 'EMAIL', 'SDanielson@syscon.net');
INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS) VALUES (-5, -5, 'STF', 'EMAIL', 'WAsher@syscon.net');
INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS) VALUES (-6, -6, 'STF', 'EMAIL', 'DEliott@syscon.net');

INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG1', 'OFFICER1');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG2', 'OFFICER2');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG3', 'OFFICER3');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG4', 'OFFICER4');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG5', 'OFFICER5');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG1', 'ADMINSTAFF');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG2', 'ADMINSTAFF');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG3', 'ADMINSTAFF');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG4', 'ADMINSTAFF');
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME) VALUES ('AG5', 'ADMINSTAFF');

INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'OFFICER1', 'AG1');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'OFFICER2', 'AG2');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'OFFICER3', 'AG3');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'OFFICER4', 'AG4');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'OFFICER5', 'AG5');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'ADMINSTAFF', 'AG1');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-1, 'ADMINSTAFF', 'AG1');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'ADMINSTAFF', 'AG2');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-1, 'ADMINSTAFF', 'AG2');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'ADMINSTAFF', 'AG3');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-1, 'ADMINSTAFF', 'AG3');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'ADMINSTAFF', 'AG4');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-1, 'ADMINSTAFF', 'AG4');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-2, 'ADMINSTAFF', 'AG5');
INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID) VALUES (-1, 'ADMINSTAFF', 'AG5');

