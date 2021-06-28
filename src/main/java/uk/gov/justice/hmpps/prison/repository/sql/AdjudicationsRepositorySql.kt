package uk.gov.justice.hmpps.prison.repository.sql

enum class AdjudicationsRepositorySql(val sql: String) {
  FIND_AWARDS(
    """
        SELECT S.OIC_SANCTION_CODE   SANCTION_CODE,
        RC1.DESCRIPTION       SANCTION_CODE_DESCRIPTION,
        S.SANCTION_MONTHS     MONTHS,
        S.SANCTION_DAYS       DAYS,
        S.COMPENSATION_AMOUNT LIMIT,
        S.COMMENT_TEXT        "COMMENT",
        S.EFFECTIVE_DATE      EFFECTIVE_DATE,
        S.STATUS,
        RC2.DESCRIPTION       STATUS_DESCRIPTION,
        S.OIC_HEARING_ID      HEARING_ID,
        S.RESULT_SEQ          HEARING_SEQUENCE
        FROM OFFENDER_OIC_SANCTIONS S
        INNER JOIN OIC_HEARING_RESULTS H ON S.OIC_HEARING_ID = H.OIC_HEARING_ID AND S.RESULT_SEQ = H.RESULT_SEQ
        LEFT JOIN REFERENCE_CODES RC1 ON S.OIC_SANCTION_CODE = RC1.CODE AND RC1.DOMAIN = 'OIC_SANCT'
        LEFT JOIN REFERENCE_CODES RC2 ON S.STATUS = RC2.CODE AND RC2.DOMAIN = 'OIC_SANCT_ST'
        WHERE S.OFFENDER_BOOK_ID = :bookingId
        AND H.FINDING_CODE = 'PROVED'
        ORDER BY S.OIC_HEARING_ID, S.RESULT_SEQ
    """
  ),

  FIND_LATEST_ADJUDICATION_OFFENCE_TYPES_FOR_OFFENDER(
    """
        SELECT  OO.OIC_OFFENCE_ID   ID
        ,       OO.OIC_OFFENCE_CODE CODE
        ,       OO.DESCRIPTION      DESCRIPTION
                FROM (
                        SELECT DISTINCT AIC.CHARGED_OIC_OFFENCE_ID AS OFFENCE_IDS
                        FROM AGENCY_INCIDENTS AI
                                INNER JOIN AGENCY_INCIDENT_PARTIES AIP ON AIP.AGENCY_INCIDENT_ID = AI.AGENCY_INCIDENT_ID
                                INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = AI.AGY_LOC_ID
                                INNER JOIN AGENCY_INCIDENT_CHARGES AIC ON AIC.AGENCY_INCIDENT_ID = AIP.AGENCY_INCIDENT_ID AND AIC.PARTY_SEQ = AIP.PARTY_SEQ
                                INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = AIP.OFFENDER_BOOK_ID
                                INNER JOIN OFFENDERS OFF ON OFF.OFFENDER_ID = OB.OFFENDER_ID
                        WHERE OFF.OFFENDER_ID_DISPLAY = :offenderNo
                              AND OB.BOOKING_SEQ = 1
                )
        INNER JOIN OIC_OFFENCES OO ON OO.OIC_OFFENCE_ID = OFFENCE_IDS
        ORDER BY DESCRIPTION
    """
  ),

  FIND_LATEST_ADJUDICATION_AGENCIES_FOR_OFFENDER(
    """
        SELECT  AL.AGY_LOC_ID AGENCY_ID
        ,       AL.AGENCY_LOCATION_TYPE AGENCY_TYPE
        ,       AL.DESCRIPTION
        ,       CASE WHEN AL.ACTIVE_FLAG = 'Y' THEN 1 ELSE 0 END AS ACTIVE
        FROM (
                SELECT DISTINCT AI.AGY_LOC_ID AS AGENCY_LOCATION_ID
                FROM AGENCY_INCIDENTS AI
                        INNER JOIN AGENCY_INCIDENT_PARTIES AIP ON AIP.AGENCY_INCIDENT_ID = AI.AGENCY_INCIDENT_ID
                        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = AIP.OFFENDER_BOOK_ID
                        INNER JOIN OFFENDERS OFF ON OFF.OFFENDER_ID = OB.OFFENDER_ID
                        WHERE OFF.OFFENDER_ID_DISPLAY = :offenderNo
                              AND OB.BOOKING_SEQ = 1
        )
        INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = AGENCY_LOCATION_ID
        ORDER BY AL.LONG_DESCRIPTION
    """
  ),

  FIND_LATEST_ADJUDICATIONS_FOR_OFFENDER(
    """
        SELECT  AIP.OIC_INCIDENT_ID   ADJUDICATION_NUMBER
        ,		AI.REPORT_TIME
        ,       AI.AGENCY_INCIDENT_ID
        ,       AL.AGY_LOC_ID         AGENCY_ID
        ,       AIC.OIC_CHARGE_ID
        ,		OO.DESCRIPTION        OFFENCE_DESCRIPTION
        ,       OO.OIC_OFFENCE_CODE   OFFENCE_CODE
        ,       AIP.PARTY_SEQ
        ,       OHR.FINDING_CODE
        FROM  AGENCY_INCIDENTS AI
              INNER JOIN AGENCY_INCIDENT_PARTIES AIP ON AIP.AGENCY_INCIDENT_ID = AI.AGENCY_INCIDENT_ID  AND AIP.INCIDENT_ROLE = 'S'
              INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = AI.AGY_LOC_ID
              INNER JOIN AGENCY_INCIDENT_CHARGES AIC  ON AIC.AGENCY_INCIDENT_ID = AIP.AGENCY_INCIDENT_ID AND AIC.PARTY_SEQ = AIP.PARTY_SEQ
              INNER JOIN OIC_OFFENCES OO ON OO.OIC_OFFENCE_ID = AIC.CHARGED_OIC_OFFENCE_ID
              INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = AIP.OFFENDER_BOOK_ID
              INNER JOIN OFFENDERS OFF ON OFF.OFFENDER_ID = OB.OFFENDER_ID
              LEFT  JOIN OIC_HEARING_RESULTS OHR ON OHR.AGENCY_INCIDENT_ID = AIC.AGENCY_INCIDENT_ID AND OHR.CHARGE_SEQ = AIC.CHARGE_SEQ
        WHERE OFF.OFFENDER_ID_DISPLAY = :offenderNo
        AND OB.BOOKING_SEQ = 1
        AND (:findingCode IS NULL OR OHR.FINDING_CODE = :findingCode)
        AND (:agencyLocationId is NULL OR AL.AGY_LOC_ID = :agencyLocationId)
        AND (:offenceId IS NULL OR CHARGED_OIC_OFFENCE_ID = :offenceId)
        AND (:startDate IS NULL OR trunc(REPORT_TIME) >= :startDate)
        AND (:endDate IS NULL OR trunc(REPORT_TIME) <= :endDate)
        ORDER BY AI.REPORT_TIME DESC
    """
  ),

  FIND_ADJUDICATION(
    """
        SELECT  AIP.OFFENDER_BOOK_ID
        ,       AIP.OIC_INCIDENT_ID       ADJUDICATION_NUMBER
        ,       AI.INCIDENT_TIME
        ,       RC.DESCRIPTION            REPORT_TYPE
        ,       AI.AGY_LOC_ID             AGENCY_ID
        ,       AI.INTERNAL_LOCATION_ID
        ,       AI.AGENCY_INCIDENT_ID     REPORT_NUMBER
        ,       AI.INCIDENT_DETAILS
        ,       SM.FIRST_NAME             REPORTER_FIRST_NAME
        ,       SM.LAST_NAME              REPORTER_LAST_NAME
        ,       AI.REPORT_TIME
        FROM  AGENCY_INCIDENTS AI
        INNER JOIN AGENCY_INCIDENT_PARTIES AIP ON AIP.AGENCY_INCIDENT_ID = AI.AGENCY_INCIDENT_ID
                INNER JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = AI.REPORTED_STAFF_ID
                INNER JOIN REFERENCE_CODES RC ON "DOMAIN" = 'INC_TYPE' AND CODE = AI.INCIDENT_TYPE
        WHERE AIP.OFFENDER_BOOK_ID IN (
                SELECT OFFENDER_BOOK_ID FROM OFFENDER_BOOKINGS OB
                        JOIN OFFENDERS OFF ON OFF.OFFENDER_ID = OB.OFFENDER_ID
                        WHERE OFF.OFFENDER_ID_DISPLAY = :offenderNo)
        AND AIP.OIC_INCIDENT_ID = :adjudicationNo
        AND AIP.INCIDENT_ROLE = 'S'
    """
  ),

  FIND_HEARINGS(
    """
        SELECT OH.OIC_HEARING_ID
        ,    REFCODE.DESCRIPTION     HEARING_TYPE
        ,    OH.HEARING_TIME
        ,    OH.INTERNAL_LOCATION_ID
        ,    SM.FIRST_NAME           HEARD_BY_FIRST_NAME
        ,    SM.LAST_NAME            HEARD_BY_LAST_NAME
        ,    OH.REPRESENTATIVE_TEXT  "OTHER_REPRESENTATIVES"
        ,    OH.COMMENT_TEXT         "COMMENT"
        FROM  OIC_HEARINGS OH
        INNER JOIN REFERENCE_CODES REFCODE ON "DOMAIN" = 'OIC_HEAR' AND CODE = OH.OIC_HEARING_TYPE
        LEFT  JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = OH.HEARING_STAFF_ID
                WHERE OH.OIC_INCIDENT_ID = :adjudicationNo
    """
  ),

  FIND_RESULTS(
    """
        SELECT OH.OIC_HEARING_ID
        ,      OO.OIC_OFFENCE_CODE
        ,      OFFENCE_TYPES.DESCRIPTION   OFFENCE_TYPE
        ,      OO.DESCRIPTION              OFFENCE_DESCRIPTION
        ,      PLEA_TYPES.DESCRIPTION      PLEA
        ,      FINDING_TYPES.DESCRIPTION   FINDING
        ,      OHR.RESULT_SEQ
        FROM  OIC_HEARINGS OH
        INNER JOIN OIC_HEARING_RESULTS OHR ON OHR.OIC_HEARING_ID = OH.OIC_HEARING_ID
                INNER JOIN OIC_OFFENCES         OO ON OO.OIC_OFFENCE_ID = OHR.OIC_OFFENCE_ID
                INNER JOIN REFERENCE_CODES PLEA_TYPES    ON PLEA_TYPES."DOMAIN" = 'OIC_PLEA'  AND PLEA_TYPES.code = OHR.PLEA_FINDING_CODE
        INNER JOIN REFERENCE_CODES FINDING_TYPES ON FINDING_TYPES."DOMAIN" = 'OIC_FINDING'  AND FINDING_TYPES.code = OHR.FINDING_CODE
        INNER JOIN REFERENCE_CODES OFFENCE_TYPES ON OFFENCE_TYPES."DOMAIN" = 'OIC_OFN_TYPE' AND OFFENCE_TYPES.code = OO.OIC_OFFENCE_TYPE
        WHERE OH.OIC_HEARING_ID IN (:hearingIds)
        ORDER BY OHR.RESULT_SEQ
    """
  ),

  FIND_SANCTIONS(
    """
        SELECT OIC_HEARING_ID
        ,      SANC_TYPES.DESCRIPTION  SANCTION_TYPE
        ,      SANCTION_DAYS
        ,      SANCTION_MONTHS
        ,      COMPENSATION_AMOUNT
        ,      EFFECTIVE_DATE
        ,      SANC_STATUS.DESCRIPTION STATUS
        ,      STATUS_DATE
        ,      COMMENT_TEXT            "COMMENT"
        ,      RESULT_SEQ
        ,      SANCTION_SEQ
        ,      CONSECUTIVE_SANCTION_SEQ
        FROM OFFENDER_OIC_SANCTIONS SAN
        INNER JOIN REFERENCE_CODES SANC_TYPES   ON SANC_TYPES."DOMAIN" = 'OIC_SANCT'     AND SANC_TYPES.CODE = SAN.OIC_SANCTION_CODE
        INNER JOIN REFERENCE_CODES SANC_STATUS  ON SANC_STATUS."DOMAIN" = 'OIC_SANCT_ST' AND SANC_STATUS.CODE = SAN.STATUS
        WHERE SAN.OIC_HEARING_ID IN (:hearingIds)
        ORDER BY SAN.RESULT_SEQ, SAN.SANCTION_SEQ
    """
  )
}
