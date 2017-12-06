-------------------------------------------------------------------
-- Seed data for Prison Activities (PRISON_ACT) Scheduled Events --
-------------------------------------------------------------------

-- COURSE_SCHEDULES (Course activity classes/occurrences/sessions)
-- NB: Dates deliberately out of sequence for first 5 records (to allow default sorting to be verified)
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-1, -1, TO_DATE('2017-09-12', 'YYYY-MM-DD'), TO_DATE('2017-09-12 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-12 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-2, -1, TO_DATE('2017-09-15', 'YYYY-MM-DD'), TO_DATE('2017-09-15 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-15 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-3, -1, TO_DATE('2017-09-13', 'YYYY-MM-DD'), TO_DATE('2017-09-13 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-13 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-4, -1, TO_DATE('2017-09-11', 'YYYY-MM-DD'), TO_DATE('2017-09-11 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-11 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-5, -1, TO_DATE('2017-09-14', 'YYYY-MM-DD'), TO_DATE('2017-09-14 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-14 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-6, -2, TO_DATE('2017-09-11', 'YYYY-MM-DD'), TO_DATE('2017-09-11 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-11 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-7, -2, TO_DATE('2017-09-12', 'YYYY-MM-DD'), TO_DATE('2017-09-12 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-12 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-8, -2, TO_DATE('2017-09-13', 'YYYY-MM-DD'), TO_DATE('2017-09-13 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-13 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-9, -2, TO_DATE('2017-09-14', 'YYYY-MM-DD'), TO_DATE('2017-09-14 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-14 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-10, -2, TO_DATE('2017-09-15', 'YYYY-MM-DD'), TO_DATE('2017-09-15 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-15 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-11, -3, TO_DATE('2017-09-12', 'YYYY-MM-DD'), TO_DATE('2017-09-12 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-12 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-12, -3, TO_DATE('2017-09-15', 'YYYY-MM-DD'), TO_DATE('2017-09-15 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-15 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-13, -3, TO_DATE('2017-09-13', 'YYYY-MM-DD'), TO_DATE('2017-09-13 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-13 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-14, -3, TO_DATE('2017-09-11', 'YYYY-MM-DD'), TO_DATE('2017-09-11 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-11 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-15, -3, TO_DATE('2017-09-14', 'YYYY-MM-DD'), TO_DATE('2017-09-14 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-14 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-16, -1, TO_DATE('2017-09-19', 'YYYY-MM-DD'), TO_DATE('2017-09-19 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-19 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-17, -1, TO_DATE('2017-09-22', 'YYYY-MM-DD'), TO_DATE('2017-09-22 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-22 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-18, -1, TO_DATE('2017-09-20', 'YYYY-MM-DD'), TO_DATE('2017-09-20 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-20 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-19, -1, TO_DATE('2017-09-18', 'YYYY-MM-DD'), TO_DATE('2017-09-18 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-18 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-20, -1, TO_DATE('2017-09-21', 'YYYY-MM-DD'), TO_DATE('2017-09-21 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-21 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-21, -4, TO_DATE('2017-09-25', 'YYYY-MM-DD'), TO_DATE('2017-09-25 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-25 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-22, -4, TO_DATE('2017-09-26', 'YYYY-MM-DD'), TO_DATE('2017-09-26 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-26 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-23, -4, TO_DATE('2017-09-27', 'YYYY-MM-DD'), TO_DATE('2017-09-27 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-27 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-24, -4, TO_DATE('2017-09-28', 'YYYY-MM-DD'), TO_DATE('2017-09-28 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-28 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS) VALUES (-25, -4, TO_DATE('2017-09-29', 'YYYY-MM-DD'), TO_DATE('2017-09-29 09:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-09-29 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCH');

-- These course schedules defined for current day, this week and next week (to test 'today', 'thisWeek' and 'nextWeek' endpoint actions).
INSERT INTO COURSE_SCHEDULES (CRS_SCH_ID, CRS_ACTY_ID, SCHEDULE_DATE, START_TIME, END_TIME, SCHEDULE_STATUS)
VALUES (-26, -2, current_date, sysdate + INTERVAL '5' SECOND, sysdate + INTERVAL '5' SECOND, 'SCH'),
(-27, -2, current_date, sysdate + INTERVAL '6' SECOND, sysdate + INTERVAL '6' SECOND, 'SCH'),
(-28, -5, current_date + INTERVAL  '6' DAY, sysdate + INTERVAL  '6' DAY, sysdate + INTERVAL  '6' DAY, 'SCH'),
(-29, -6, current_date + INTERVAL  '3' DAY, sysdate + INTERVAL  '3' DAY, sysdate + INTERVAL  '3' DAY, 'SCH'),
(-30, -5, current_date + INTERVAL '17' DAY, sysdate + INTERVAL '17' DAY, sysdate + INTERVAL '17' DAY, 'SCH'),
(-31, -6, current_date + INTERVAL  '9' DAY, sysdate + INTERVAL  '9' DAY, sysdate + INTERVAL  '9' DAY, 'SCH');
