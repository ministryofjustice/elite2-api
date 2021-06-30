package uk.gov.justice.hmpps.prison.repository.sql

enum class AgencyRepositorySql(val sql: String) {
  GET_AGENCIES(
    """
        SELECT DISTINCT AGY_LOC_ID AGENCY_ID,
        DESCRIPTION,
        AGENCY_LOCATION_TYPE AGENCY_TYPE,
        ACTIVE_FLAG ACTIVE
        FROM AGENCY_LOCATIONS
        WHERE ACTIVE_FLAG = 'Y'
        AND AGY_LOC_ID NOT IN ('OUT','TRN')
    """
  ),

  GET_AGENCIES_BY_TYPE(
    """
        SELECT DISTINCT AGY_LOC_ID AGENCY_ID,
        DESCRIPTION,
        ACTIVE_FLAG ACTIVE,
        AGENCY_LOCATION_TYPE AGENCY_TYPE
                FROM AGENCY_LOCATIONS
                WHERE ACTIVE_FLAG = :activeFlag
        AND AGENCY_LOCATION_TYPE = :agencyType
        AND AGY_LOC_ID NOT IN (:excludeIds)
    """
  ),

  FIND_AGENCIES_BY_USERNAME(
    """
        SELECT DISTINCT A.AGY_LOC_ID AGENCY_ID,
        A.DESCRIPTION,
        A.AGENCY_LOCATION_TYPE AGENCY_TYPE
                FROM AGENCY_LOCATIONS A
        INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
                WHERE A.ACTIVE_FLAG = 'Y'
        AND A.AGY_LOC_ID NOT IN ('OUT', 'TRN')
        AND C.CASELOAD_ID IN (
                SELECT UCR.CASELOAD_ID
                        FROM USER_ACCESSIBLE_CASELOADS UCR JOIN CASELOADS CL ON CL.CASELOAD_ID = UCR.CASELOAD_ID AND CL.CASELOAD_TYPE = :caseloadType AND CL.CASELOAD_FUNCTION = :caseloadFunction
                WHERE UCR.USERNAME = :username)

    """
  ),

  FIND_AGENCIES_BY_CURRENT_CASELOAD(
    """
        SELECT DISTINCT A.AGY_LOC_ID AGENCY_ID,
        A.DESCRIPTION,
        A.AGENCY_LOCATION_TYPE AGENCY_TYPE
                FROM AGENCY_LOCATIONS A
        INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
                WHERE A.ACTIVE_FLAG = 'Y'
        AND A.AGY_LOC_ID NOT IN ('OUT', 'TRN')
        AND C.CASELOAD_ID IN (
                SELECT SUA.WORKING_CASELOAD_ID
                        FROM STAFF_USER_ACCOUNTS SUA
                        WHERE SUA.USERNAME = :username)
    """
  ),

  FIND_PRISON_ADDRESSES_PHONE_NUMBERS(
    """
        SELECT
        al.AGY_LOC_ID agency_id,
        al.DESCRIPTION,
        ad.address_type,
        ad.PREMISE,
        ad.STREET,
        ad.LOCALITY,
        city.DESCRIPTION CITY,
        country.DESCRIPTION COUNTRY,
        ad.POSTAL_CODE,
        p.PHONE_TYPE,
        p.PHONE_NO,
        p.EXT_NO
        FROM AGENCY_LOCATIONS al LEFT JOIN ADDRESSES ad ON ad.owner_class = 'AGY' AND ad.PRIMARY_FLAG = 'Y'
        AND ad.owner_code = al.agy_loc_id
                LEFT JOIN PHONES p ON p.owner_class = 'ADDR'
        AND p.owner_id = ad.address_id
                LEFT JOIN REFERENCE_CODES city ON city.CODE = ad.CITY_CODE and city.DOMAIN = 'CITY'
        LEFT JOIN REFERENCE_CODES country ON country.CODE = ad.COUNTRY_CODE and country.DOMAIN = 'COUNTRY'
        WHERE al.ACTIVE_FLAG = 'Y'
        AND al.AGY_LOC_ID NOT IN ('OUT', 'TRN')
        AND al.AGENCY_LOCATION_TYPE = 'INST'
        AND (:agencyId is NULL OR al.AGY_LOC_ID = :agencyId)
    """
  ),

  FIND_AGENCIES_BY_CASELOAD(
    """
        SELECT A.AGY_LOC_ID AGENCY_ID,
        A.DESCRIPTION,
        A.AGENCY_LOCATION_TYPE AGENCY_TYPE
                FROM AGENCY_LOCATIONS A
        INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
                WHERE A.ACTIVE_FLAG = 'Y'
        AND A.AGY_LOC_ID NOT IN ('OUT','TRN')
        AND C.CASELOAD_ID = :caseloadId
    """
  ),

  GET_AGENCY(
    """
        SELECT A.AGY_LOC_ID AGENCY_ID,
        A.DESCRIPTION,
        A.AGENCY_LOCATION_TYPE AGENCY_TYPE
                FROM AGENCY_LOCATIONS A
        WHERE A.ACTIVE_FLAG = COALESCE(:activeFlag, A.ACTIVE_FLAG)
        AND AGENCY_LOCATION_TYPE = COALESCE(:agencyType, A.AGENCY_LOCATION_TYPE)
        AND A.AGY_LOC_ID = :agencyId
    """
  ),

  GET_AGENCY_LOCATIONS(
    """
        SELECT A.INTERNAL_LOCATION_ID LOCATION_ID,
        A.AGY_LOC_ID AGENCY_ID,
        A.INTERNAL_LOCATION_TYPE LOCATION_TYPE,
        A.DESCRIPTION,
        A.PARENT_INTERNAL_LOCATION_ID PARENT_LOCATION_ID,
        A.NO_OF_OCCUPANT CURRENT_OCCUPANCY,
        A.OPERATION_CAPACITY OPERATIONAL_CAPACITY,
        A.USER_DESC USER_DESCRIPTION
                FROM AGENCY_INTERNAL_LOCATIONS A
        WHERE A.ACTIVE_FLAG = 'Y'
        AND A.AGY_LOC_ID = :agencyId
    """
  ),

  GET_AGENCY_LOCATIONS_FOR_EVENT_TYPE(
    """
        SELECT AIL.INTERNAL_LOCATION_ID LOCATION_ID,
        AIL.AGY_LOC_ID AGENCY_ID,
        AIL.INTERNAL_LOCATION_TYPE LOCATION_TYPE,
        ILU.INTERNAL_LOCATION_USAGE LOCATION_USAGE,
        AIL.DESCRIPTION,
        AIL.PARENT_INTERNAL_LOCATION_ID PARENT_LOCATION_ID,
        AIL.NO_OF_OCCUPANT CURRENT_OCCUPANCY,
        AIL.OPERATION_CAPACITY OPERATIONAL_CAPACITY,
        AIL.USER_DESC USER_DESCRIPTION
                FROM AGENCY_INTERNAL_LOCATIONS AIL
        INNER JOIN INT_LOC_USAGE_LOCATIONS ILUL ON AIL.INTERNAL_LOCATION_ID = ILUL.INTERNAL_LOCATION_ID
                INNER JOIN INTERNAL_LOCATION_USAGES ILU ON ILU.AGY_LOC_ID = AIL.AGY_LOC_ID
                AND ILU.INTERNAL_LOCATION_USAGE_ID = ILUL.INTERNAL_LOCATION_USAGE_ID
                WHERE ILU.INTERNAL_LOCATION_USAGE in ( :eventTypes )
        AND AIL.AGY_LOC_ID = :agencyId
        AND AIL.ACTIVE_FLAG = 'Y'
        AND AIL.DEACTIVATE_DATE IS NULL
                AND AIL.INTERNAL_LOCATION_CODE <> 'RTU'
        AND NOT EXISTS (SELECT 1
                FROM INT_LOC_USAGE_LOCATIONS
                WHERE PARENT_USAGE_LOCATION_ID = ILUL.USAGE_LOCATION_ID)
    """
  ),

  GET_AGENCY_LOCATIONS_FOR_EVENTS_BOOKED(
    """
        SELECT DISTINCT AIL.INTERNAL_LOCATION_ID LOCATION_ID,
        AIL.USER_DESC USER_DESCRIPTION
                FROM AGENCY_INTERNAL_LOCATIONS AIL
        WHERE AIL.AGY_LOC_ID = :agencyId
        AND AIL.ACTIVE_FLAG = 'Y'
        AND AIL.DEACTIVATE_DATE IS NULL
                AND AIL.INTERNAL_LOCATION_ID in (
                (SELECT distinct CA.INTERNAL_LOCATION_ID
                        FROM OFFENDER_PROGRAM_PROFILES OPP
                        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
                INNER JOIN COURSE_ACTIVITIES CA ON CA.CRS_ACTY_ID = OPP.CRS_ACTY_ID
                INNER JOIN COURSE_SCHEDULES CS ON CA.CRS_ACTY_ID = CS.CRS_ACTY_ID
                AND CS.SCHEDULE_DATE >= TRUNC(OPP.OFFENDER_START_DATE)
                AND TRUNC(CS.SCHEDULE_DATE) <=
                COALESCE(OPP.OFFENDER_END_DATE, CA.SCHEDULE_END_DATE, CS.SCHEDULE_DATE)
                AND CS.START_TIME BETWEEN :periodStart AND :periodEnd
                AND CS.SCHEDULE_DATE = TRUNC(:periodStart)
        WHERE OPP.OFFENDER_PROGRAM_STATUS = 'ALLOC'
        AND CA.ACTIVE_FLAG = 'Y'
        AND CA.COURSE_ACTIVITY_TYPE IS NOT NULL
        AND CS.CATCH_UP_CRS_SCH_ID IS NULL
                AND CA.AGY_LOC_ID = :agencyId
        ) UNION (
                SELECT distinct OIS.TO_INTERNAL_LOCATION_ID
                        FROM OFFENDER_IND_SCHEDULES OIS
                        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
                WHERE OIS.EVENT_TYPE = 'APP'
                AND OIS.START_TIME BETWEEN :periodStart AND :periodEnd
                AND OIS.EVENT_DATE = TRUNC(:periodStart)
        AND OIS.AGY_LOC_ID = :agencyId
        ) UNION (
                SELECT distinct VIS.VISIT_INTERNAL_LOCATION_ID
                        FROM OFFENDER_VISITS VIS
                        WHERE VIS.START_TIME BETWEEN :periodStart AND :periodEnd
                AND VIS.VISIT_DATE = TRUNC(:periodStart)
        AND VIS.AGY_LOC_ID = :agencyId
        )
        )
        ORDER BY USER_DESCRIPTION
    """
  ),

  GET_AGENCY_IEP_LEVELS(
    """
        SELECT IEP_LEVEL,
        RC.DESCRIPTION IEP_DESCRIPTION
                FROM IEP_LEVELS IL
        LEFT JOIN REFERENCE_CODES RC ON RC.CODE = IL.IEP_LEVEL AND RC.DOMAIN = :refCodeDomain
        WHERE IL.AGY_LOC_ID = :agencyId
        AND IL.ACTIVE_FLAG = :activeFlag
    """
  ),

  GET_AGENCY_IEP_REVIEW_INFORMATION(
    """
        SELECT OB.OFFENDER_BOOK_ID AS BOOKING_ID,
        COALESCE (POS_NEG_IEPS.POSITIVE_IEPS, 0) AS POSITIVE_IEPS,
        COALESCE (POS_NEG_IEPS.NEGATIVE_IEPS, 0) AS NEGATIVE_IEPS,
        COALESCE (PROVEN_ADJUDICATIONS.PROVEN_ADJUDICATIONS, 0) AS PROVEN_ADJUDICATIONS,
        IEP_DETAILS.IEP_TIME AS LAST_REVIEW_TIME,
        IEP_DETAILS.IEP_LEVEL AS CURRENT_LEVEL,
        OFFENDER_DETAILS.FIRST_NAME,
        OFFENDER_DETAILS.MIDDLE_NAME,
        OFFENDER_DETAILS.LAST_NAME,
        OFFENDER_DETAILS.CELL_LOCATION,
        OFFENDER_DETAILS.OFFENDER_ID_DISPLAY AS OFFENDER_NO
        FROM OFFENDER_BOOKINGS OB
        LEFT OUTER JOIN ( SELECT OIL.OFFENDER_BOOK_ID,
                OIL.IEP_TIME AS IEP_TIME,
                COALESCE(RC.DESCRIPTION, OIL.IEP_LEVEL) AS IEP_LEVEL
                        FROM OFFENDER_IEP_LEVELS OIL
                        LEFT OUTER JOIN OFFENDER_BOOKINGS OB ON OIL.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
                        LEFT JOIN REFERENCE_CODES RC ON RC.CODE = OIL.IEP_LEVEL AND RC.DOMAIN = 'IEP_LEVEL',
                (
                        SELECT OIL.OFFENDER_BOOK_ID,
                MAX(IEP_TIME) AS MAX_TIME
                        FROM OFFENDER_IEP_LEVELS OIL
                        GROUP BY OIL.OFFENDER_BOOK_ID ) GROUPED_BY_TIME
                WHERE OB.AGY_LOC_ID = :agencyId
        AND OB.BOOKING_SEQ = :bookingSeq
        AND OIL.IEP_TIME = GROUPED_BY_TIME.MAX_TIME
                AND OIL.OFFENDER_BOOK_ID = GROUPED_BY_TIME.OFFENDER_BOOK_ID
        ) IEP_DETAILS ON OB.OFFENDER_BOOK_ID = IEP_DETAILS.OFFENDER_BOOK_ID
        LEFT OUTER JOIN (SELECT OCN.OFFENDER_BOOK_ID,
                SUM(CASE WHEN OCN.CASE_NOTE_TYPE = 'POS'
                        AND OCN.CASE_NOTE_SUB_TYPE = 'IEP_ENC'
                        AND TRUNC(OCN.CREATE_DATETIME) > TO_DATE(:threeMonthsAgo, 'YYYY-MM-DD')
                        THEN 1 ELSE 0 END) AS POSITIVE_IEPS,
                SUM(CASE WHEN OCN.CASE_NOTE_TYPE = 'NEG'
                        AND OCN.CASE_NOTE_SUB_TYPE = 'IEP_WARN'
                        AND TRUNC(OCN.CREATE_DATETIME) > TO_DATE(:threeMonthsAgo, 'YYYY-MM-DD')
                        THEN 1 ELSE 0 END) AS NEGATIVE_IEPS
                        FROM OFFENDER_CASE_NOTES OCN
                        LEFT OUTER JOIN OFFENDER_BOOKINGS OB ON OCN.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
                        WHERE OB.AGY_LOC_ID = :agencyId
                        AND OB.BOOKING_SEQ = :bookingSeq
                GROUP BY  OCN.OFFENDER_BOOK_ID) POS_NEG_IEPS ON OB.OFFENDER_BOOK_ID = POS_NEG_IEPS.OFFENDER_BOOK_ID
                LEFT OUTER JOIN (
                SELECT OOS.OFFENDER_BOOK_ID,
                COUNT (DISTINCT OHR.OIC_HEARING_ID) AS PROVEN_ADJUDICATIONS
                        FROM OFFENDER_OIC_SANCTIONS OOS
                        LEFT OUTER JOIN OFFENDER_BOOKINGS OB ON OOS.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
                        JOIN OIC_HEARING_RESULTS OHR ON OOS.OIC_HEARING_ID = OHR.OIC_HEARING_ID
                        WHERE OB.AGY_LOC_ID = :agencyId
                AND OB.BOOKING_SEQ = :bookingSeq
                AND OHR.FINDING_CODE = :hearingFinding
                GROUP BY OOS.OFFENDER_BOOK_ID
                        ORDER BY PROVEN_ADJUDICATIONS DESC
        ) PROVEN_ADJUDICATIONS ON OB.OFFENDER_BOOK_ID = PROVEN_ADJUDICATIONS.OFFENDER_BOOK_ID
                LEFT OUTER JOIN (SELECT OB.OFFENDER_BOOK_ID,
                O.OFFENDER_ID_DISPLAY,
                O.FIRST_NAME,
                CONCAT(O.middle_name, CASE WHEN middle_name_2 IS NOT NULL THEN concat(' ', O.middle_name_2) ELSE '' END) MIDDLE_NAME,
        O.LAST_NAME,
        AIL.DESCRIPTION AS CELL_LOCATION
        FROM OFFENDER_BOOKINGS OB
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
                INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                WHERE OB.AGY_LOC_ID = :agencyId
        AND OB.BOOKING_SEQ = :bookingSeq
        ) OFFENDER_DETAILS ON OB.OFFENDER_BOOK_ID = OFFENDER_DETAILS.OFFENDER_BOOK_ID
        WHERE (:iepLevel is NULL OR IEP_DETAILS.IEP_LEVEL = :iepLevel)
        AND (:location IS NULL OR OFFENDER_DETAILS.CELL_LOCATION LIKE CONCAT(:location, '%'))
        AND OB.AGY_LOC_ID = :agencyId
        AND OB.BOOKING_SEQ = :bookingSeq
        ORDER BY POS_NEG_IEPS.NEGATIVE_IEPS DESC
    """
  )
}
