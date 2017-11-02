-- COURSE_ACTIVITIES (Course activity definition)
INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-1, 'LEI', 'LEI', 'Chapel Cleaner', 15, 'Y', '2016-08-08', 'INST', -1, -25, 'AGY', 'LEI', 'CC1', 'CHAP', 'BAS');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-2, 'LEI', 'LEI', 'Woodwork', 10, 'Y', '2012-02-28', 'INST', -2, -26, 'AGY', 'LEI', 'WOOD', 'EDUC', 'STD');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-3, 'LEI', 'LEI', 'Substance misuse course', 5, 'Y', '2011-01-04', 'INST', -3, -27, 'AGY', 'LEI', 'SUBS', 'EDUC', 'BAS');

INSERT INTO COURSE_ACTIVITIES (CRS_ACTY_ID, CASELOAD_ID, AGY_LOC_ID, DESCRIPTION, CAPACITY, ACTIVE_FLAG, SCHEDULE_START_DATE, CASELOAD_TYPE, PROGRAM_ID, INTERNAL_LOCATION_ID, PROVIDER_PARTY_CLASS, PROVIDER_PARTY_CODE, CODE, COURSE_ACTIVITY_TYPE, IEP_LEVEL)
VALUES (-4, 'LEI', 'LEI', 'Core classes', 25, 'Y', '2009-07-04', 'INST', -4, -27, 'AGY', 'LEI', 'CORE', 'EDUC', 'STD');

-- COURSE_SCHEDULES (Course activity classes/occurrences/sessions)
-- NB: Dates deliberately out of sequence for first 5 records (to allow default sorting to be verified)
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-1, -1, '2017-09-12', '2017-09-12 09:30:00', '2017-09-12 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-2, -1, '2017-09-15', '2017-09-15 09:30:00', '2017-09-15 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-3, -1, '2017-09-13', '2017-09-13 09:30:00', '2017-09-13 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-4, -1, '2017-09-11', '2017-09-11 09:30:00', '2017-09-11 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-5, -1, '2017-09-14', '2017-09-14 09:30:00', '2017-09-14 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-6, -2, '2017-09-11', '2017-09-11 13:00:00', '2017-09-11 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-7, -2, '2017-09-12', '2017-09-12 13:00:00', '2017-09-12 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-8, -2, '2017-09-13', '2017-09-13 13:00:00', '2017-09-13 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-9, -2, '2017-09-14', '2017-09-14 13:00:00', '2017-09-14 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-10, -2, '2017-09-15', '2017-09-15 13:00:00', '2017-09-15 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-11, -3, '2017-09-12', '2017-09-12 13:00:00', '2017-09-12 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-12, -3, '2017-09-15', '2017-09-15 13:00:00', '2017-09-15 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-13, -3, '2017-09-13', '2017-09-13 13:00:00', '2017-09-13 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-14, -3, '2017-09-11', '2017-09-11 13:00:00', '2017-09-11 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-15, -3, '2017-09-14', '2017-09-14 13:00:00', '2017-09-14 15:00:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-16, -1, '2017-09-19', '2017-09-19 09:30:00', '2017-09-19 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-17, -1, '2017-09-22', '2017-09-22 09:30:00', '2017-09-22 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-18, -1, '2017-09-20', '2017-09-20 09:30:00', '2017-09-20 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-19, -1, '2017-09-18', '2017-09-18 09:30:00', '2017-09-18 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-20, -1, '2017-09-21', '2017-09-21 09:30:00', '2017-09-21 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-21, -4, '2017-09-25', '2017-09-25 09:30:00', '2017-09-25 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-22, -4, '2017-09-26', '2017-09-26 09:30:00', '2017-09-26 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-23, -4, '2017-09-27', '2017-09-27 09:30:00', '2017-09-27 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-24, -4, '2017-09-28', '2017-09-28 09:30:00', '2017-09-28 11:30:00', 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-25, -4, '2017-09-29', '2017-09-29 09:30:00', '2017-09-29 11:30:00', 'SCH');

-- These course schedules defined for current day (to test 'today' endpoint action).
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-26, -2, current_date, now(), now(), 'SCH');

INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-27, -2, current_date, now(), now(), 'SCH');

-- OFFENDER_PROGRAM_PROFILES (Allocation of offenders to course activities)
INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
VALUES (-1, -1, -1, '2016-11-09', 'ALLOC', -1, null, null, null, null, null, 'N', 'LEI', null, null);

INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
VALUES (-2, -1, -2, '2012-07-05', 'END', -2, null, null, 'TRF', null, '2016-10-21', 'N', 'LEI', null, null);

INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
VALUES (-3, -1, -3, '2016-11-09', 'ALLOC', -3, null, null, null, null, null, 'N', 'LEI', null, null);

INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
VALUES (-4, -1, -4, null, 'PLAN', null, null, null, null, null, null, 'N', null, null, -1);

INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
VALUES (-5, -2, -2, '2016-11-09', 'ALLOC', -2, null, null, null, null, null, 'N', 'LEI', null, null);

INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
VALUES (-6, -3, -2, '2016-11-09', 'ALLOC', -2, null, null, null, null, null, 'N', 'LEI', null, null);

INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
VALUES (-7, -4, -4, '2016-11-09', 'ALLOC', -4, null, null, null, null, null, 'N', 'LEI', null, null);

INSERT INTO OFFENDER_PROGRAM_PROFILES (OFF_PRGREF_ID, OFFENDER_BOOK_ID, PROGRAM_ID, OFFENDER_START_DATE, OFFENDER_PROGRAM_STATUS, CRS_ACTY_ID, REFERRAL_PRIORITY, REFERRAL_DATE, OFFENDER_END_REASON, WAITLIST_DECISION_CODE, OFFENDER_END_DATE, SUSPENDED_FLAG, AGY_LOC_ID, PARENT_OFF_PRGREF_ID, OFFENDER_PRG_OBLIGATION_ID)
VALUES (-8, -5, -1, '2016-11-09', 'ALLOC', -1, null, null, null, null, null, 'N', 'LEI', null, null);

-- OFFENDER_COURSE_ATTENDANCES (record of offenders having attended scheduled activitie)
INSERT INTO OFFENDER_COURSE_ATTENDANCES (EVENT_ID, OFFENDER_BOOK_ID, CRS_SCH_ID, EVENT_CLASS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_DATE, EVENT_STATUS)
VALUES (-1, -3, -6, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-11', 'EXP');

INSERT INTO OFFENDER_COURSE_ATTENDANCES (EVENT_ID, OFFENDER_BOOK_ID, CRS_SCH_ID, EVENT_CLASS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_DATE, EVENT_STATUS)
VALUES (-2, -3, -7, 'INT_MOV', 'PRISON_ACT', 'EDUC', '2017-09-12', 'SCH');
