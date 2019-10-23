GET_RECENT_MOVEMENTS_BY_DATE_FOR_BATCH {
 SELECT OFFENDERS.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
        OEM.CREATE_DATETIME            AS CREATE_DATE_TIME,
        OEM.FROM_AGY_LOC_ID            AS FROM_AGENCY,
        OEM.TO_AGY_LOC_ID              AS TO_AGENCY,
        OEM.MOVEMENT_TYPE,
        OEM.DIRECTION_CODE
  FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
    INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
    INNER JOIN OFFENDERS               ON OFFENDERS.OFFENDER_ID = OB.OFFENDER_ID
  WHERE OEM.MOVEMENT_DATE = :movementDate
    AND OEM.CREATE_DATETIME >= :fromDateTime
    AND OEM.MOVEMENT_TYPE IN (:movementTypes)
    AND OEM.MOVEMENT_SEQ = (SELECT MAX(OEM2.MOVEMENT_SEQ) FROM OFFENDER_EXTERNAL_MOVEMENTS OEM2
                            WHERE OEM2.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID)
    AND OB.AGY_LOC_ID <> 'ZZGHI'
}

GET_MOVEMENT_BY_BOOKING_AND_SEQUENCE {
    SELECT OFFENDERS.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
           OEM.CREATE_DATETIME            AS CREATE_DATE_TIME,
           OEM.FROM_AGY_LOC_ID            AS FROM_AGENCY,
           OEM.TO_AGY_LOC_ID              AS TO_AGENCY,
           OEM.MOVEMENT_TIME,
           OEM.MOVEMENT_TYPE,
           OEM.DIRECTION_CODE,
           AL1.DESCRIPTION               AS FROM_AGENCY_DESCRIPTION,
           AL2.DESCRIPTION               AS TO_AGENCY_DESCRIPTION,
           RC1.DESCRIPTION               AS MOVEMENT_TYPE_DESCRIPTION,
           RC2.DESCRIPTION               AS MOVEMENT_REASON,
           RC3.DESCRIPTION               AS FROM_CITY,
           RC4.DESCRIPTION               AS TO_CITY
    FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
             INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
             INNER JOIN OFFENDERS               ON OFFENDERS.OFFENDER_ID = OB.OFFENDER_ID
             LEFT JOIN AGENCY_LOCATIONS AL1 ON OEM.FROM_AGY_LOC_ID = AL1.AGY_LOC_ID
             LEFT JOIN AGENCY_LOCATIONS AL2 ON OEM.TO_AGY_LOC_ID = AL2.AGY_LOC_ID
             LEFT JOIN REFERENCE_CODES RC1 ON RC1.CODE = OEM.MOVEMENT_TYPE AND RC1.DOMAIN = 'MOVE_TYPE'
             LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OEM.MOVEMENT_REASON_CODE AND RC2.DOMAIN = 'MOVE_RSN'
             LEFT JOIN REFERENCE_CODES RC3 ON RC3.CODE = OEM.FROM_CITY AND RC3.DOMAIN = 'CITY'
             LEFT JOIN REFERENCE_CODES RC4 ON RC4.CODE = OEM.TO_CITY AND RC4.DOMAIN = 'CITY'
    WHERE OEM.MOVEMENT_SEQ = :sequenceNumber
      AND OEM.OFFENDER_BOOK_ID = :bookingId
}

GET_MOVEMENTS_BY_OFFENDERS_AND_MOVEMENT_TYPES {
  SELECT OFFENDERS.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
        OEM.CREATE_DATETIME            AS CREATE_DATE_TIME,
        OEM.FROM_AGY_LOC_ID            AS FROM_AGENCY,
        OEM.TO_AGY_LOC_ID              AS TO_AGENCY,
        OEM.MOVEMENT_TIME,
        OEM.MOVEMENT_TYPE,
        OEM.DIRECTION_CODE,
        AL1.DESCRIPTION               AS FROM_AGENCY_DESCRIPTION,
        AL2.DESCRIPTION               AS TO_AGENCY_DESCRIPTION,
        RC1.DESCRIPTION               AS MOVEMENT_TYPE_DESCRIPTION,
        RC2.DESCRIPTION               AS MOVEMENT_REASON,
        RC3.DESCRIPTION               AS FROM_CITY,
        RC4.DESCRIPTION               AS TO_CITY
  FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
    INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
    INNER JOIN OFFENDERS               ON OFFENDERS.OFFENDER_ID = OB.OFFENDER_ID
    LEFT JOIN AGENCY_LOCATIONS AL1 ON OEM.FROM_AGY_LOC_ID = AL1.AGY_LOC_ID
    LEFT JOIN AGENCY_LOCATIONS AL2 ON OEM.TO_AGY_LOC_ID = AL2.AGY_LOC_ID
    LEFT JOIN REFERENCE_CODES RC1 ON RC1.CODE = OEM.MOVEMENT_TYPE AND RC1.DOMAIN = 'MOVE_TYPE'
    LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OEM.MOVEMENT_REASON_CODE AND RC2.DOMAIN = 'MOVE_RSN'
    LEFT JOIN REFERENCE_CODES RC3 ON RC3.CODE = OEM.FROM_CITY AND RC3.DOMAIN = 'CITY'
    LEFT JOIN REFERENCE_CODES RC4 ON RC4.CODE = OEM.TO_CITY AND RC4.DOMAIN = 'CITY'
  WHERE (:latestOnly = 0 OR
         OEM.MOVEMENT_SEQ = (SELECT MAX(OEM2.MOVEMENT_SEQ) FROM OFFENDER_EXTERNAL_MOVEMENTS OEM2
                             WHERE OEM2.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID
                               AND OEM2.MOVEMENT_TYPE IN (:movementTypes)))
    AND OB.AGY_LOC_ID <> 'ZZGHI'
    AND OFFENDERS.OFFENDER_ID_DISPLAY in (:offenderNumbers)
    AND OEM.MOVEMENT_TYPE IN (:movementTypes)
}

GET_MOVEMENTS_BY_OFFENDERS {
  SELECT OFFENDERS.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
        OEM.CREATE_DATETIME            AS CREATE_DATE_TIME,
        OEM.MOVEMENT_TYPE,
        OEM.MOVEMENT_TIME,
        OEM.FROM_AGY_LOC_ID            AS FROM_AGENCY,
        OEM.TO_AGY_LOC_ID              AS TO_AGENCY,
        OEM.DIRECTION_CODE,
        OEM.COMMENT_TEXT,
        AL1.DESCRIPTION               AS FROM_AGENCY_DESCRIPTION,
        AL2.DESCRIPTION               AS TO_AGENCY_DESCRIPTION,
        RC1.DESCRIPTION               AS MOVEMENT_TYPE_DESCRIPTION,
        RC2.DESCRIPTION               AS MOVEMENT_REASON,
        RC3.DESCRIPTION               AS FROM_CITY,
        RC4.DESCRIPTION               AS TO_CITY
  FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
    INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
    INNER JOIN OFFENDERS               ON OFFENDERS.OFFENDER_ID = OB.OFFENDER_ID
    LEFT JOIN AGENCY_LOCATIONS AL1 ON OEM.FROM_AGY_LOC_ID = AL1.AGY_LOC_ID
    LEFT JOIN AGENCY_LOCATIONS AL2 ON OEM.TO_AGY_LOC_ID = AL2.AGY_LOC_ID
    LEFT JOIN REFERENCE_CODES RC1 ON RC1.CODE = OEM.MOVEMENT_TYPE AND RC1.DOMAIN = 'MOVE_TYPE'
    LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OEM.MOVEMENT_REASON_CODE AND RC2.DOMAIN = 'MOVE_RSN'
    LEFT JOIN REFERENCE_CODES RC3 ON RC3.CODE = OEM.FROM_CITY AND RC3.DOMAIN = 'CITY'
    LEFT JOIN REFERENCE_CODES RC4 ON RC4.CODE = OEM.TO_CITY AND RC4.DOMAIN = 'CITY'
  WHERE (:latestOnly = 0 OR
       OEM.MOVEMENT_SEQ = (SELECT MAX(OEM2.MOVEMENT_SEQ) FROM OFFENDER_EXTERNAL_MOVEMENTS OEM2
                           WHERE OEM2.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID))
    AND OB.AGY_LOC_ID <> 'ZZGHI'
    AND OFFENDERS.OFFENDER_ID_DISPLAY in (:offenderNumbers)
}


GET_ROLLCOUNT_MOVEMENTS {
SELECT
       DIRECTION_CODE, MOVEMENT_TYPE,
       FROM_AGY_LOC_ID AS FROM_AGENCY,
       TO_AGY_LOC_ID AS TO_AGENCY,
       OFFENDERS.OFFENDER_ID_DISPLAY  AS OFFENDER_NO
    FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
      INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
      INNER JOIN OFFENDERS               ON OFFENDERS.OFFENDER_ID = OB.OFFENDER_ID
    WHERE MOVEMENT_DATE = :movementDate
}

GET_ENROUTE_OFFENDER_COUNT {
  SELECT count(*)
  FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
         INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
  WHERE
      OEM.MOVEMENT_SEQ = (SELECT MAX(OEM2.MOVEMENT_SEQ) FROM OFFENDER_EXTERNAL_MOVEMENTS OEM2
                          WHERE OEM2.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID
                            AND OEM2.MOVEMENT_TYPE ='TRN')

    AND OB.AGY_LOC_ID = 'TRN'
    AND OB.ACTIVE_FLAG = 'N'
    AND OEM.TO_AGY_LOC_ID = :agencyId
    AND OEM.MOVEMENT_TYPE = 'TRN'
    AND OEM.DIRECTION_CODE ='OUT'
    AND OEM.ACTIVE_FLAG ='Y'
}

GET_ENROUTE_OFFENDER_MOVEMENTS {
  SELECT
    O.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
    OB.OFFENDER_BOOK_ID AS BOOKING_ID,
    O.FIRST_NAME FIRST_NAME,
    CONCAT (O.MIDDLE_NAME, CASE WHEN MIDDLE_NAME_2 IS NOT NULL THEN CONCAT (' ', O.MIDDLE_NAME_2) ELSE '' END) MIDDLE_NAMES,
    O.LAST_NAME LAST_NAME,
    O.BIRTH_DATE DATE_OF_BIRTH,
    OEM.FROM_AGY_LOC_ID            AS FROM_AGENCY,
    OEM.TO_AGY_LOC_ID              AS TO_AGENCY,
    OEM.MOVEMENT_TYPE,
    OEM.DIRECTION_CODE,
    OEM.MOVEMENT_TIME,
    OEM.MOVEMENT_DATE,
    AL1.DESCRIPTION               AS FROM_AGENCY_DESCRIPTION,
    AL2.DESCRIPTION               AS TO_AGENCY_DESCRIPTION,
    RC1.DESCRIPTION               AS MOVEMENT_TYPE_DESCRIPTION,
    OEM.MOVEMENT_REASON_CODE      AS MOVEMENT_REASON,
    RC2.DESCRIPTION               AS MOVEMENT_REASON_DESCRIPTION
  FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
    INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
    INNER JOIN OFFENDERS O              ON O.OFFENDER_ID = OB.OFFENDER_ID
    LEFT JOIN AGENCY_LOCATIONS AL1 ON OEM.FROM_AGY_LOC_ID = AL1.AGY_LOC_ID
    LEFT JOIN AGENCY_LOCATIONS AL2 ON OEM.TO_AGY_LOC_ID = AL2.AGY_LOC_ID
    LEFT JOIN REFERENCE_CODES RC1 ON RC1.CODE = OEM.MOVEMENT_TYPE AND RC1.DOMAIN = 'MOVE_TYPE'
    LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OEM.MOVEMENT_REASON_CODE AND RC2.DOMAIN = 'MOVE_RSN'
  WHERE
    OEM.MOVEMENT_SEQ = (SELECT MAX(OEM2.MOVEMENT_SEQ) FROM OFFENDER_EXTERNAL_MOVEMENTS OEM2
                      WHERE OEM2.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID
                        AND OEM2.MOVEMENT_TYPE ='TRN')

    AND OB.AGY_LOC_ID = 'TRN'
    AND OB.ACTIVE_FLAG = 'N'
    AND OEM.TO_AGY_LOC_ID = :agencyId
    AND OEM.MOVEMENT_TYPE = 'TRN'
    AND OEM.DIRECTION_CODE ='OUT'
    AND OEM.ACTIVE_FLAG ='Y'
}

GET_OFFENDER_MOVEMENTS_IN {
SELECT  /*+ index(OEM, OFFENDER_EXT_MOVEMENTS_X01) */
       O.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
       O.FIRST_NAME AS FIRST_NAME,
       CONCAT (O.MIDDLE_NAME, CASE WHEN MIDDLE_NAME_2 IS NOT NULL THEN CONCAT (' ', O.MIDDLE_NAME_2) ELSE '' END) AS MIDDLE_NAMES,
       O.LAST_NAME AS LAST_NAME,
       O.BIRTH_DATE AS DATE_OF_BIRTH,
       OB.OFFENDER_BOOK_ID AS BOOKING_ID,
       OEM.MOVEMENT_TIME,
       AL.AGY_LOC_ID AS FROM_AGENCY_ID,
       AL.DESCRIPTION AS FROM_AGENCY_DESCRIPTION,
       AL1.AGY_LOC_ID AS TO_AGENCY_ID,
       AL1.DESCRIPTION AS TO_AGENCY_DESCRIPTION,
       COALESCE(AIL.USER_DESC, AIL.DESCRIPTION) AS LOCATION,
       RC3.DESCRIPTION               AS FROM_CITY,
       RC4.DESCRIPTION               AS TO_CITY
FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
       INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
       INNER JOIN OFFENDERS O              ON O.OFFENDER_ID = OB.OFFENDER_ID
       LEFT OUTER JOIN AGENCY_LOCATIONS AL ON OEM.FROM_AGY_LOC_ID = AL.AGY_LOC_ID
       LEFT OUTER JOIN AGENCY_LOCATIONS AL1 ON OEM.TO_AGY_LOC_ID = AL1.AGY_LOC_ID
       LEFT OUTER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
       LEFT JOIN REFERENCE_CODES RC3 ON RC3.CODE = OEM.FROM_CITY AND RC3.DOMAIN = 'CITY'
       LEFT JOIN REFERENCE_CODES RC4 ON RC4.CODE = OEM.TO_CITY AND RC4.DOMAIN = 'CITY'
WHERE
      OEM.TO_AGY_LOC_ID = :agencyId
  AND OEM.DIRECTION_CODE ='IN'
  AND OEM.MOVEMENT_DATE = :movementDate
}

GET_ROLL_COUNT {
SELECT
  AIL.INTERNAL_LOCATION_ID                             AS LIVING_UNIT_ID,
  COALESCE(AIL.USER_DESC, AIL.INTERNAL_LOCATION_CODE) AS LIVING_UNIT_DESC,
  VR.BEDS_IN_USE,
  VR.CURRENTLY_IN_CELL,
  VR.OUT_OF_LIVING_UNITS,
  VR.CURRENTLY_OUT,
  AIL.OPERATION_CAPACITY                               AS OPERATIONAL_CAPACITY,
  AIL.OPERATION_CAPACITY - VR.BEDS_IN_USE              AS NET_VACANCIES,
  AIL.CAPACITY                                         AS MAXIMUM_CAPACITY,
  AIL.CAPACITY - VR.BEDS_IN_USE                        AS AVAILABLE_PHYSICAL,
 (SELECT COUNT(*)
   FROM AGENCY_INTERNAL_LOCATIONS AIL2
     INNER JOIN LIVING_UNITS_MV LU2 ON AIL2.INTERNAL_LOCATION_ID = LU2.LIVING_UNIT_ID
   WHERE AIL2.AGY_LOC_ID = VR.AGY_LOC_ID
     AND LU2.ROOT_LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
     AND (
       AIL2.DEACTIVATE_REASON_CODE IS NULL OR
       AIL2.DEACTIVATE_REASON_CODE NOT IN (:deactivateReasonCodes)
     )
     AND :currentDateTime BETWEEN DEACTIVATE_DATE AND COALESCE(REACTIVATE_DATE,:currentDateTime)) AS OUT_OF_ORDER
FROM
  (SELECT
     LU.AGY_LOC_ID,
     LU.ROOT_LIVING_UNIT_ID,
     SUM(DECODE(OB.LIVING_UNIT_ID, NULL, 0, 1)) AS BEDS_IN_USE,
     SUM(DECODE(OB.AGENCY_IML_ID, NULL, DECODE (OB.IN_OUT_STATUS, 'IN', 1, 0), 0)) AS CURRENTLY_IN_CELL,
     SUM(DECODE(OB.AGENCY_IML_ID, NULL, 0, DECODE (OB.IN_OUT_STATUS, 'IN', 1, 0))) AS OUT_OF_LIVING_UNITS,
     SUM(DECODE(OB.IN_OUT_STATUS, 'OUT', 1, 0)) AS CURRENTLY_OUT
   FROM LIVING_UNITS_MV LU
     LEFT JOIN OFFENDER_BOOKINGS OB ON LU.LIVING_UNIT_ID = OB.LIVING_UNIT_ID AND LU.AGY_LOC_ID = OB.AGY_LOC_ID
   GROUP BY LU.AGY_LOC_ID, LU.ROOT_LIVING_UNIT_ID
  ) VR
  INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON AIL.INTERNAL_LOCATION_ID = VR.ROOT_LIVING_UNIT_ID
WHERE AIL.CERTIFIED_FLAG = :certifiedFlag
  AND AIL.UNIT_TYPE IS NOT NULL
  AND AIL.AGY_LOC_ID = :agencyId
  AND AIL.ACTIVE_FLAG = 'Y'
  AND ((AIL.PARENT_INTERNAL_LOCATION_ID IS NULL AND :livingUnitId IS NULL) OR AIL.PARENT_INTERNAL_LOCATION_ID = :livingUnitId)
ORDER BY LIVING_UNIT_DESC
}

GET_OFFENDERS_OUT_TODAY {
SELECT  /*+ index(OEM, OFFENDER_EXT_MOVEMENTS_X01) */
  DIRECTION_CODE,
  MOVEMENT_DATE,
  FROM_AGY_LOC_ID AS FROM_AGENCY,
  OFFENDERS.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
  OFFENDERS.FIRST_NAME,
  OFFENDERS.LAST_NAME,
  OFFENDERS.BIRTH_DATE AS DATE_OF_BIRTH,
  RC2.DESCRIPTION AS MOVEMENT_REASON_DESCRIPTION,
  OEM.MOVEMENT_TIME

FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
       INNER JOIN OFFENDER_BOOKINGS OB    ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
       INNER JOIN OFFENDERS               ON OFFENDERS.OFFENDER_ID = OB.OFFENDER_ID
       LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OEM.MOVEMENT_REASON_CODE AND RC2.DOMAIN = 'MOVE_RSN'
WHERE
      OEM.MOVEMENT_DATE = :movementDate AND
      OEM.DIRECTION_CODE = 'OUT' AND
      OEM.FROM_AGY_LOC_ID = :agencyId
}

GET_OFFENDERS_IN_RECEPTION {
SELECT
  VR.OFFENDER_NO,
  VR.FIRST_NAME,
  VR.LAST_NAME,
  VR.DATE_OF_BIRTH,
  VR.BOOKING_ID
FROM
  (SELECT
     LU.AGY_LOC_ID,
     LU.ROOT_LIVING_UNIT_ID,
     O.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
     O.FIRST_NAME AS FIRST_NAME,
     O.LAST_NAME AS LAST_NAME,
     O.BIRTH_DATE AS DATE_OF_BIRTH,
     DECODE(OB.AGENCY_IML_ID, NULL, DECODE (OB.IN_OUT_STATUS, 'IN', 1, 0), 0) AS STATUS_IN,
     OB.OFFENDER_BOOK_ID AS BOOKING_ID
   FROM LIVING_UNITS_MV LU
          INNER JOIN OFFENDER_BOOKINGS OB ON LU.LIVING_UNIT_ID = OB.LIVING_UNIT_ID AND LU.AGY_LOC_ID = OB.AGY_LOC_ID
          INNER JOIN OFFENDERS O ON O.OFFENDER_ID = OB.OFFENDER_ID

  ) VR
    INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL4 ON AIL4.INTERNAL_LOCATION_ID = VR.ROOT_LIVING_UNIT_ID
WHERE AIL4.CERTIFIED_FLAG = 'N'
  AND AIL4.UNIT_TYPE IS NOT NULL
  AND AIL4.AGY_LOC_ID = :agencyId
  AND STATUS_IN = 1
  AND AIL4.PARENT_INTERNAL_LOCATION_ID IS NULL
  AND AIL4.ACTIVE_FLAG = 'Y'
}

GET_OFFENDERS_CURRENTLY_OUT_OF_LIVING_UNIT {
SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
       OB.OFFENDER_BOOK_ID AS BOOKING_ID,
       O.FIRST_NAME,
       O.LAST_NAME,
       O.BIRTH_DATE AS DATE_OF_BIRTH,
       COALESCE(AIL.USER_DESC, AIL.DESCRIPTION) AS LOCATION

  FROM LIVING_UNITS_MV LU
       JOIN OFFENDER_BOOKINGS OB ON LU.LIVING_UNIT_ID = OB.LIVING_UNIT_ID AND
                                    LU.AGY_LOC_ID = OB.AGY_LOC_ID
       JOIN OFFENDERS O ON O.OFFENDER_ID = OB.OFFENDER_ID
       LEFT OUTER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON LU.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
 WHERE OB.BOOKING_SEQ = :bookingSeq
   AND OB.IN_OUT_STATUS = :inOutStatus
   AND LU.ROOT_LIVING_UNIT_ID = :livingUnitId
}

GET_OFFENDERS_CURRENTLY_OUT_OF_AGENCY {
SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
       OB.OFFENDER_BOOK_ID AS BOOKING_ID,
       O.FIRST_NAME,
       O.LAST_NAME,
       O.BIRTH_DATE AS DATE_OF_BIRTH,
       COALESCE(AIL2.USER_DESC, AIL2.DESCRIPTION) AS LOCATION
  FROM LIVING_UNITS_MV LU
       JOIN OFFENDER_BOOKINGS OB ON LU.LIVING_UNIT_ID = OB.LIVING_UNIT_ID AND
                                    lu.AGY_LOC_ID = ob.AGY_LOC_ID
       JOIN OFFENDERS O ON O.OFFENDER_ID = OB.OFFENDER_ID
       JOIN AGENCY_INTERNAL_LOCATIONS AIL ON LU.ROOT_LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
       LEFT OUTER JOIN AGENCY_INTERNAL_LOCATIONS AIL2 ON LU.LIVING_UNIT_ID = AIL2.INTERNAL_LOCATION_ID
 WHERE OB.BOOKING_SEQ = :bookingSeq
   AND OB.IN_OUT_STATUS = :inOutStatus
   and lu.AGY_LOC_ID = :agencyId
   and ail.CERTIFIED_FLAG = :certifiedFlag
   and ail.ACTIVE_FLAG = :activeFlag
   and ail.PARENT_INTERNAL_LOCATION_ID IS NULL
}

GET_MOVEMENTS_BY_AGENCY_AND_TIME_PERIOD {
  SELECT O.OFFENDER_ID_DISPLAY         AS OFFENDER_NO,
         OEM.CREATE_DATETIME            AS CREATE_DATE_TIME,
         OEM.EVENT_ID                   AS EVENT_ID,
         OEM.FROM_AGY_LOC_ID            AS FROM_AGENCY,
         AL1.DESCRIPTION                AS FROM_AGENCY_DESCRIPTION,
         OEM.TO_AGY_LOC_ID              AS TO_AGENCY,
         AL2.DESCRIPTION                AS TO_AGENCY_DESCRIPTION,
         OEM.MOVEMENT_TIME              AS MOVEMENT_TIME,
         OEM.MOVEMENT_TYPE              AS MOVEMENT_TYPE,
         RC1.DESCRIPTION                AS MOVEMENT_TYPE_DESCRIPTION,
         OEM.MOVEMENT_REASON_CODE       AS MOVEMENT_REASON_CODE,
         RC2.DESCRIPTION                AS MOVEMENT_REASON,
         OEM.DIRECTION_CODE             AS DIRECTION_CODE,
         RC3.DESCRIPTION                AS FROM_CITY,
         RC4.DESCRIPTION                AS TO_CITY
  FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
    INNER JOIN OFFENDERS O          ON O.OFFENDER_ID = OB.OFFENDER_ID
    LEFT JOIN AGENCY_LOCATIONS AL1  ON OEM.FROM_AGY_LOC_ID = AL1.AGY_LOC_ID
    LEFT JOIN AGENCY_LOCATIONS AL2  ON OEM.TO_AGY_LOC_ID = AL2.AGY_LOC_ID
    LEFT JOIN REFERENCE_CODES RC1   ON RC1.CODE = OEM.MOVEMENT_TYPE AND RC1.DOMAIN = 'MOVE_TYPE'
    LEFT JOIN REFERENCE_CODES RC2   ON RC2.CODE = OEM.MOVEMENT_REASON_CODE AND RC2.DOMAIN = 'MOVE_RSN'
    LEFT JOIN REFERENCE_CODES RC3   ON RC3.CODE = OEM.FROM_CITY AND RC3.DOMAIN = 'CITY'
    LEFT JOIN REFERENCE_CODES RC4   ON RC4.CODE = OEM.TO_CITY AND RC4.DOMAIN = 'CITY'
  WHERE
    OEM.MOVEMENT_TIME BETWEEN :fromDateTime AND :toDateTime
    AND (OEM.FROM_AGY_LOC_ID IN (:agencyListFrom) OR OEM.TO_AGY_LOC_ID IN (:agencyListTo))
  ORDER BY OEM.MOVEMENT_TIME
}

GET_COURT_EVENTS_BY_AGENCY_AND_TIME_PERIOD {
SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
       CE.CREATE_DATETIME    AS CREATE_DATE_TIME,
       CE.EVENT_ID           AS EVENT_ID,
       OB.AGY_LOC_ID         AS FROM_AGENCY,
       AL1.DESCRIPTION       AS FROM_AGENCY_DESCRIPTION,
       CE.AGY_LOC_ID         AS TO_AGENCY,
       AL2.DESCRIPTION       AS TO_AGENCY_DESCRIPTION,
       CE.EVENT_DATE         AS EVENT_DATE,
       CE.START_TIME         AS START_TIME,
       CE.END_TIME           AS END_TIME,
       DECODE (OB.in_out_status,
           'IN',  DECODE (OB.active_flag, 'Y', 'EXT_MOV', 'COMM'),
           'OUT', DECODE (OB.active_flag, 'Y', 'EXT_MOV', 'COMM'), 'COMM')
                             AS EVENT_CLASS,
       'CRT'                 AS EVENT_TYPE,
       ce.COURT_EVENT_TYPE   AS EVENT_SUB_TYPE,
       DECODE (ce.event_status,
           NULL, 'SCH',
           ce.event_status)  AS EVENT_STATUS,
       CE.JUDGE_NAME         AS JUDGE_NAME,
       CE.DIRECTION_CODE     AS DIRECTION_CODE,
       CE.COMMENT_TEXT       AS COMMENT_TEXT,
       DECODE(OB.ACTIVE_FLAG, 'Y', 1, 0) AS BOOKING_ACTIVE_FLAG,
       OB.IN_OUT_STATUS      AS BOOKING_IN_OUT_STATUS
FROM COURT_EVENTS CE
       INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = CE.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
       INNER JOIN OFFENDERS O          ON O.OFFENDER_ID = OB.OFFENDER_ID
       LEFT JOIN AGENCY_LOCATIONS AL1  ON AL1.AGY_LOC_ID = OB.AGY_LOC_ID
       LEFT JOIN AGENCY_LOCATIONS AL2  ON AL2.AGY_LOC_ID = CE.AGY_LOC_ID
WHERE CE.HOLD_FLAG <>  'Y'
  AND CE.START_TIME BETWEEN :fromDateTime AND :toDateTime
  AND (OB.AGY_LOC_ID IN (:agencyListFrom) OR CE.AGY_LOC_ID IN (:agencyListTo))
}

GET_OFFENDER_TRANSFERS_BY_AGENCY_AND_TIME_PERIOD {
SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
       OIS.CREATE_DATETIME   AS CREATE_DATE_TIME,
       OIS.EVENT_ID          AS EVENT_ID,
       OIS.AGY_LOC_ID        AS FROM_AGENCY,
       AL1.DESCRIPTION       AS FROM_AGENCY_DESCRIPTION,
       OIS.TO_AGY_LOC_ID     AS TO_AGENCY,
       AL2.DESCRIPTION       AS TO_AGENCY_DESCRIPTION,
       RC3.DESCRIPTION       AS TO_CITY,
       OIS.EVENT_STATUS      AS EVENT_STATUS,
       OIS.EVENT_CLASS       AS EVENT_CLASS,
       OIS.EVENT_TYPE        AS EVENT_TYPE,
       OIS.EVENT_SUB_TYPE    AS EVENT_SUB_TYPE,
       OIS.EVENT_DATE        AS EVENT_DATE,
       OIS.START_TIME        AS START_TIME,
       OIS.END_TIME          AS END_TIME,
       OIS.OUTCOME_REASON_CODE AS OUTCOME_REASON_CODE,
       OIS.JUDGE_NAME        AS JUDGE_NAME,
       OIS.ENGAGEMENT_CODE   AS ENGAGEMENT_CODE,
       OIS.ESCORT_CODE       AS ESCORT_CODE,
       OIS.PERFORMANCE_CODE  AS PERFORMANCE_CODE,
       OIS.DIRECTION_CODE    AS DIRECTION_CODE,
       DECODE(OB.ACTIVE_FLAG, 'Y', 1, 0) AS BOOKING_ACTIVE_FLAG,
       OB.IN_OUT_STATUS      AS BOOKING_IN_OUT_STATUS
FROM OFFENDER_IND_SCHEDULES OIS
       INNER JOIN OFFENDER_BOOKINGS OB  ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
       INNER JOIN OFFENDERS O           ON O.OFFENDER_ID = OB.OFFENDER_ID
       LEFT JOIN AGENCY_LOCATIONS AL1   ON AL1.AGY_LOC_ID = OIS.AGY_LOC_ID
       LEFT JOIN AGENCY_LOCATIONS AL2   ON AL2.AGY_LOC_ID = OIS.TO_AGY_LOC_ID
       LEFT JOIN REFERENCE_CODES RC3    ON RC3.CODE = OIS.TO_CITY_CODE AND RC3.DOMAIN = 'CITY'
WHERE OIS.EVENT_STATUS <> 'DEL'
  AND OIS.EVENT_CLASS = 'EXT_MOV'
  AND OIS.START_TIME BETWEEN :fromDateTime AND :toDateTime
  AND (OIS.AGY_LOC_ID IN (:agencyListFrom) OR OIS.TO_AGY_LOC_ID IN (:agencyListTo))
}

GET_OFFENDER_RELEASES_BY_AGENCY_AND_DATE {
  SELECT O.OFFENDER_ID_DISPLAY    AS OFFENDER_NO,
         ORD.CREATE_DATETIME      AS CREATE_DATE_TIME,
         ORD.EVENT_ID             AS EVENT_ID,
         OB.AGY_LOC_ID            AS FROM_AGENCY,
         AL1.DESCRIPTION          AS FROM_AGENCY_DESCRIPTION,
         ORD.RELEASE_DATE         AS RELEASE_DATE,
         ORD.APPROVED_RELEASE_DATE AS APPROVED_RELEASE_DATE,
         'EXT_MOV'                AS EVENT_CLASS,
         ORD.EVENT_STATUS         AS EVENT_STATUS,
         ORD.MOVEMENT_TYPE        AS MOVEMENT_TYPE_CODE,
         RC1.DESCRIPTION          AS MOVEMENT_TYPE_DESCRIPTION,
         ORD.MOVEMENT_REASON_CODE AS MOVEMENT_REASON_CODE,
         RC2.DESCRIPTION          AS MOVEMENT_REASON_DESCRIPTION,
         ORD.COMMENT_TEXT         AS COMMENT_TEXT,
         DECODE(OB.ACTIVE_FLAG, 'Y', 1, 0) AS BOOKING_ACTIVE_FLAG,
         OB.IN_OUT_STATUS         AS BOOKING_IN_OUT_STATUS
  FROM OFFENDER_RELEASE_DETAILS ORD
       INNER JOIN OFFENDER_BOOKINGS OB  ON OB.OFFENDER_BOOK_ID = ORD.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
       INNER JOIN OFFENDERS O           ON O.OFFENDER_ID = OB.OFFENDER_ID
       LEFT JOIN AGENCY_LOCATIONS AL1   ON AL1.AGY_LOC_ID = OB.AGY_LOC_ID
       LEFT JOIN REFERENCE_CODES RC1    ON RC1.CODE = ORD.MOVEMENT_TYPE AND RC1.DOMAIN = 'MOVE_TYPE'
       LEFT JOIN REFERENCE_CODES RC2    ON RC2.CODE = ORD.MOVEMENT_REASON_CODE AND RC2.DOMAIN = 'MOVE_RSN'
  WHERE ORD.RELEASE_DATE BETWEEN :fromDate AND :toDate
    AND OB.AGY_LOC_ID IN (:agencyListFrom)
}




