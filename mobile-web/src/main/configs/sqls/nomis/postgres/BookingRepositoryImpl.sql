GET_BOOKING_ACTIVITIES {
  SELECT OPP.OFFENDER_BOOK_ID AS BOOKING_ID,
	       'INT_MOV' AS EVENT_CLASS,
	       COALESCE(OCA.EVENT_STATUS, 'SCH') AS EVENT_STATUS,
         'PRISON_ACT' AS EVENT_TYPE,
         RD1.DESCRIPTION AS EVENT_TYPE_DESC,
	       CA.COURSE_ACTIVITY_TYPE AS EVENT_SUB_TYPE,
	       RD2.DESCRIPTION AS EVENT_SUB_TYPE_DESC,
	       CS.SCHEDULE_DATE AS EVENT_DATE,
	       CS.START_TIME,
	       CS.END_TIME,
         COALESCE(AIL.USER_DESC, AIL.DESCRIPTION, AGY.DESCRIPTION, ADDR.PREMISE) AS EVENT_LOCATION,
         'PA' AS EVENT_SOURCE,
         CA.CODE AS EVENT_SOURCE_CODE,
         CA.DESCRIPTION AS EVENT_SOURCE_DESC
  FROM OFFENDER_PROGRAM_PROFILES OPP
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y' AND OB.BOOKING_SEQ = 1
    INNER JOIN COURSE_ACTIVITIES CA ON CA.CRS_ACTY_ID = OPP.CRS_ACTY_ID
    INNER JOIN COURSE_SCHEDULES CS ON OPP.CRS_ACTY_ID = CS.CRS_ACTY_ID
      AND CS.SCHEDULE_DATE >= date_trunc('day', OPP.OFFENDER_START_DATE)
      AND date_trunc('day', CS.SCHEDULE_DATE) <= COALESCE(OPP.OFFENDER_END_DATE, CA.SCHEDULE_END_DATE, CS.SCHEDULE_DATE)
      AND CS.SCHEDULE_DATE >= date_trunc('day', COALESCE(:fromDate, CS.SCHEDULE_DATE))
      AND date_trunc('day', CS.SCHEDULE_DATE) <= COALESCE(:toDate, CS.SCHEDULE_DATE)
    LEFT JOIN OFFENDER_COURSE_ATTENDANCES OCA ON OCA.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID
      AND date_trunc('day', OCA.EVENT_DATE) = date_trunc('day', CS.SCHEDULE_DATE)
      AND OCA.CRS_SCH_ID = CS.CRS_SCH_ID
    LEFT JOIN REFERENCE_CODES RD1 ON RD1.CODE = 'PRISON_ACT' AND RD1.DOMAIN = 'INT_SCH_TYPE'
    LEFT JOIN REFERENCE_CODES RD2 ON RD2.CODE = CA.COURSE_ACTIVITY_TYPE AND RD2.DOMAIN = 'INT_SCH_RSN'
    LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON CA.INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID
    LEFT JOIN AGENCY_LOCATIONS AGY ON CA.AGY_LOC_ID = AGY.AGY_LOC_ID
    LEFT JOIN ADDRESSES ADDR ON CA.SERVICES_ADDRESS_ID = ADDR.ADDRESS_ID
  WHERE OPP.OFFENDER_BOOK_ID = :bookingId
    AND OPP.OFFENDER_PROGRAM_STATUS = 'ALLOC'
    AND COALESCE(OPP.SUSPENDED_FLAG, 'N') = 'N'
    AND CA.ACTIVE_FLAG = 'Y'
    AND CA.COURSE_ACTIVITY_TYPE IS NOT NULL
    AND CS.CATCH_UP_CRS_SCH_ID IS NULL
    AND (UPPER(TO_CHAR(CS.SCHEDULE_DATE, 'DY')), CS.SLOT_CATEGORY_CODE) NOT IN
	    (SELECT OE.EXCLUDE_DAY, COALESCE(OE.SLOT_CATEGORY_CODE, CS.SLOT_CATEGORY_CODE)
       FROM OFFENDER_EXCLUDE_ACTS_SCHDS OE
       WHERE OE.OFF_PRGREF_ID = OPP.OFF_PRGREF_ID)
}

GET_BOOKING_VISITS {
  SELECT VIS.OFFENDER_BOOK_ID AS BOOKING_ID,
	       'INT_MOV' AS EVENT_CLASS,
	       'SCH' AS EVENT_STATUS,
         'VISIT' AS EVENT_TYPE,
         RC1.DESCRIPTION AS EVENT_TYPE_DESC,
	       'VISIT' AS EVENT_SUB_TYPE,
	       RC2.DESCRIPTION AS EVENT_SUB_TYPE_DESC,
	       VIS.VISIT_DATE AS EVENT_DATE,
	       VIS.START_TIME,
	       VIS.END_TIME,
         COALESCE(AIL.USER_DESC, AIL.DESCRIPTION, AGY.DESCRIPTION) AS EVENT_LOCATION,
         'VIS' AS EVENT_SOURCE,
         VIS.VISIT_TYPE AS EVENT_SOURCE_CODE,
         RC3.DESCRIPTION AS EVENT_SOURCE_DESC
  FROM OFFENDER_VISITS VIS
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = VIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y' AND OB.BOOKING_SEQ = 1
    LEFT JOIN REFERENCE_CODES RC1 ON RC1.CODE = 'VISIT' AND RC1.DOMAIN = 'INT_SCH_TYPE'
    LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = 'VISIT' AND RC2.DOMAIN = 'INT_SCH_RSN'
    LEFT JOIN REFERENCE_CODES RC3 ON RC3.CODE = VIS.VISIT_TYPE AND RC3.DOMAIN = 'VISIT_TYPE'
    LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON VIS.VISIT_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID
    LEFT JOIN AGENCY_LOCATIONS AGY ON VIS.AGY_LOC_ID = AGY.AGY_LOC_ID
  WHERE VIS.OFFENDER_BOOK_ID = :bookingId
    AND VIS.VISIT_STATUS = 'SCH'
    AND VIS.VISIT_DATE >= date_trunc('day', COALESCE(:fromDate, VIS.VISIT_DATE))
    AND date_trunc('day', VIS.VISIT_DATE) <= COALESCE(:toDate, VIS.VISIT_DATE)
}

GET_BOOKING_APPOINTMENTS {
  SELECT OIS.OFFENDER_BOOK_ID AS BOOKING_ID,
	       OIS.EVENT_CLASS,
	       OIS.EVENT_STATUS,
         OIS.EVENT_TYPE,
         RC1.DESCRIPTION AS EVENT_TYPE_DESC,
	       OIS.EVENT_SUB_TYPE,
	       RC2.DESCRIPTION AS EVENT_SUB_TYPE_DESC,
	       OIS.EVENT_DATE,
	       OIS.START_TIME,
	       OIS.END_TIME,
         COALESCE(AIL.USER_DESC, AIL.DESCRIPTION, AGY.DESCRIPTION, ADDR.PREMISE, RC3.DESCRIPTION) AS EVENT_LOCATION,
         'APP' AS EVENT_SOURCE,
         'APP' AS EVENT_SOURCE_CODE,
         OIS.COMMENT_TEXT AS EVENT_SOURCE_DESC
  FROM OFFENDER_IND_SCHEDULES OIS
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y' AND OB.BOOKING_SEQ = 1
    LEFT JOIN REFERENCE_CODES RC1 ON RC1.CODE = OIS.EVENT_TYPE AND RC1.DOMAIN = 'INT_SCH_TYPE'
    LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OIS.EVENT_SUB_TYPE AND RC2.DOMAIN = 'INT_SCH_RSN'
    LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OIS.TO_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID
    LEFT JOIN AGENCY_LOCATIONS AGY ON OIS.TO_AGY_LOC_ID = AGY.AGY_LOC_ID
    LEFT JOIN ADDRESSES ADDR ON OIS.TO_ADDRESS_ID = ADDR.ADDRESS_ID
    LEFT JOIN REFERENCE_CODES RC3 ON RC3.CODE = OIS.TO_CITY_CODE AND RC3.DOMAIN = 'CITY'
  WHERE OIS.OFFENDER_BOOK_ID = :bookingId
    AND OIS.EVENT_TYPE = 'APP'
    AND OIS.EVENT_STATUS = 'SCH'
    AND OIS.EVENT_DATE >= date_trunc('day', COALESCE(:fromDate, OIS.EVENT_DATE))
    AND date_trunc('day', OIS.EVENT_DATE) <= COALESCE(:toDate, OIS.EVENT_DATE)
}
