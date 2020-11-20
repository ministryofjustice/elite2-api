package uk.gov.justice.hmpps.prison.repository.sql

enum class KeyWorkerAllocationRepositorySql(val sql: String) {
  GET_ALLOCATION_DETAIL_FOR_KEY_WORKERS(
    """
        SELECT
        OKW.OFFENDER_BOOK_ID   BOOKING_ID,
        O.OFFENDER_ID_DISPLAY  OFFENDER_NO,
        OKW.OFFICER_ID         STAFF_ID,
        O.FIRST_NAME,
        O.LAST_NAME,
        OKW.ASSIGNED_TIME      ASSIGNED,
        OKW.AGY_LOC_ID         AGENCY_ID,
        AIL.DESCRIPTION        INTERNAL_LOCATION_DESC
        FROM OFFENDER_KEY_WORKERS OKW
        INNER JOIN OFFENDER_BOOKINGS OB         ON OB.OFFENDER_BOOK_ID = OKW.OFFENDER_BOOK_ID
        INNER JOIN OFFENDERS O                  ON OB.OFFENDER_ID = O.OFFENDER_ID
        LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
        WHERE OKW.OFFICER_ID IN (:staffIds)
        AND OB.AGY_LOC_ID IN (:agencyIds)
        AND OB.ACTIVE_FLAG = 'Y'
        AND OKW.ACTIVE_FLAG = 'Y'
    """
  ),

  GET_ALLOCATION_DETAIL_FOR_OFFENDERS(
    """
        SELECT
        OKW.OFFENDER_BOOK_ID   BOOKING_ID,
        O.OFFENDER_ID_DISPLAY  OFFENDER_NO,
        OKW.OFFICER_ID         STAFF_ID,
        O.FIRST_NAME,
        O.LAST_NAME,
        OKW.ASSIGNED_TIME      ASSIGNED,
        OKW.AGY_LOC_ID         AGENCY_ID,
        AIL.DESCRIPTION        INTERNAL_LOCATION_DESC
                FROM OFFENDER_KEY_WORKERS OKW
        INNER JOIN OFFENDER_BOOKINGS OB         ON OB.OFFENDER_BOOK_ID = OKW.OFFENDER_BOOK_ID
                INNER JOIN OFFENDERS O                  ON OB.OFFENDER_ID = O.OFFENDER_ID
                LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                WHERE O.OFFENDER_ID_DISPLAY IN (:offenderNos)
        AND OB.AGY_LOC_ID IN (:agencyIds)
        AND OB.ACTIVE_FLAG = 'Y'
        AND OKW.ACTIVE_FLAG = 'Y'
    """
  ),

  GET_AVAILABLE_KEY_WORKERS(
    """
        SELECT DISTINCT SM.LAST_NAME,
        SM.FIRST_NAME,
        SM.STAFF_ID,
        0 NUMBER_ALLOCATED
                FROM STAFF_LOCATION_ROLES SLR
        INNER JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = SLR.SAC_STAFF_ID
                WHERE SLR.CAL_AGY_LOC_ID = :agencyId
        AND SLR.ROLE = :role
        AND SM.STATUS = 'ACTIVE'
        AND TRUNC(SYSDATE) BETWEEN TRUNC(SLR.FROM_DATE) AND TRUNC(COALESCE(SLR.TO_DATE,SYSDATE))
        AND SLR.FROM_DATE = (SELECT MAX(SLR2.FROM_DATE)
                FROM STAFF_LOCATION_ROLES SLR2
                WHERE SLR2.SAC_STAFF_ID = SLR.SAC_STAFF_ID
                AND SLR2.CAL_AGY_LOC_ID = SLR.CAL_AGY_LOC_ID
                AND SLR2.POSITION = SLR.POSITION
                AND SLR2.ROLE = SLR.ROLE)
    """
  ),

  GET_KEY_WORKER_DETAILS(
    """
        SELECT SM.LAST_NAME,
        SM.FIRST_NAME,
        SM.STAFF_ID,
        0 NUMBER_ALLOCATED
                FROM STAFF_MEMBERS SM
        WHERE SM.STAFF_ID = :staffId
        AND SM.STATUS = 'ACTIVE'
    """
  ),

  GET_KEY_WORKER_DETAILS_FOR_OFFENDER(
    """
        SELECT
        SM.STAFF_ID,
        SM.LAST_NAME,
        SM.FIRST_NAME
        FROM OFFENDER_KEY_WORKERS OKW JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = OKW.OFFICER_ID
                WHERE OKW.OFFENDER_BOOK_ID = :bookingId
        AND SM.STATUS = 'ACTIVE'
        AND OKW.ACTIVE_FLAG = 'Y'
        AND (OKW.EXPIRY_DATE is null OR OKW.EXPIRY_DATE >= :currentDate)
    """
  ),

  CHECK_KEY_WORKER_EXISTS(
    """
        SELECT SM.STAFF_ID
                FROM STAFF_MEMBERS SM
        WHERE SM.STAFF_ID = :staffId
        AND SM.STATUS = 'ACTIVE'
    """
  ),

  GET_ALLOCATION_HISTORY_BY_AGENCY(
    """
        SELECT
        O.OFFENDER_ID_DISPLAY OFFENDER_NO,
        OKW.OFFICER_ID        STAFF_ID,
        OKW.AGY_LOC_ID        AGENCY_ID,
        OKW.ASSIGNED_TIME     ASSIGNED,
        OKW.EXPIRY_DATE       EXPIRED,
        OKW.USER_ID           USER_ID,
        OKW.ACTIVE_FLAG       ACTIVE,
        OKW.CREATE_DATETIME   CREATED,
        OKW.CREATE_USER_ID    CREATED_BY,
        OKW.MODIFY_DATETIME   MODIFIED,
        OKW.MODIFY_USER_ID    MODIFIED_BY
                FROM OFFENDER_KEY_WORKERS OKW
        INNER JOIN OFFENDER_BOOKINGS OB         ON OB.OFFENDER_BOOK_ID = OKW.OFFENDER_BOOK_ID
                INNER JOIN OFFENDERS O                  ON OB.OFFENDER_ID = O.OFFENDER_ID
                WHERE OKW.AGY_LOC_ID = :agencyId
    """
  ),

  GET_ALLOCATION_HISTORY_BY_STAFF(
    """
        SELECT
        O.OFFENDER_ID_DISPLAY OFFENDER_NO,
        OKW.OFFICER_ID        STAFF_ID,
        OKW.AGY_LOC_ID        AGENCY_ID,
        OKW.ASSIGNED_TIME     ASSIGNED,
        OKW.EXPIRY_DATE       EXPIRED,
        OKW.USER_ID           USER_ID,
        OKW.ACTIVE_FLAG       ACTIVE,
        OKW.CREATE_DATETIME   CREATED,
        OKW.CREATE_USER_ID    CREATED_BY,
        OKW.MODIFY_DATETIME   MODIFIED,
        OKW.MODIFY_USER_ID    MODIFIED_BY
                FROM OFFENDER_KEY_WORKERS OKW
        INNER JOIN OFFENDER_BOOKINGS OB         ON OB.OFFENDER_BOOK_ID = OKW.OFFENDER_BOOK_ID
                INNER JOIN OFFENDERS O                  ON OB.OFFENDER_ID = O.OFFENDER_ID
                WHERE OKW.OFFICER_ID in (:staffIds)
    """
  ),

  GET_ALLOCATION_HISTORY_BY_OFFENDER(
    """
        SELECT
        O.OFFENDER_ID_DISPLAY OFFENDER_NO,
        OKW.OFFICER_ID        STAFF_ID,
        OKW.AGY_LOC_ID        AGENCY_ID,
        OKW.ASSIGNED_TIME     ASSIGNED,
        OKW.EXPIRY_DATE       EXPIRED,
        OKW.USER_ID           USER_ID,
        OKW.ACTIVE_FLAG       ACTIVE,
        OKW.CREATE_DATETIME   CREATED,
        OKW.CREATE_USER_ID    CREATED_BY,
        OKW.MODIFY_DATETIME   MODIFIED,
        OKW.MODIFY_USER_ID    MODIFIED_BY
                FROM OFFENDER_KEY_WORKERS OKW
        INNER JOIN OFFENDER_BOOKINGS OB         ON OB.OFFENDER_BOOK_ID = OKW.OFFENDER_BOOK_ID
                INNER JOIN OFFENDERS O                  ON OB.OFFENDER_ID = O.OFFENDER_ID
                WHERE O.OFFENDER_ID_DISPLAY in (:offenderNos)
    """
  )
}
