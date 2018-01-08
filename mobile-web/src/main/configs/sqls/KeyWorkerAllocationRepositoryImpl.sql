INSERT_KEY_WORKER_ALLOCATION {
    INSERT INTO OFFENDER_KEY_WORKERS (
         OFFENDER_BOOK_ID,
         OFFICER_ID,
         ASSIGNED_DATE,
         ASSIGNED_TIME,
         AGY_LOC_ID,
         ACTIVE_FLAG,
         ALLOC_REASON,
         ALLOC_TYPE,
         USER_ID
	  ) VALUES (
	      :bookingId,
	      :staffId,
	      :assignedDate,
	      :assignedTime,
	      :agencyId,
	      :active,
	      :reason,
	      :type,
	      :userId
	  )
}

DEACTIVATE_KEY_WORKER_ALLOCATION_FOR_OFFENDER_BOOKING {
    UPDATE OFFENDER_KEY_WORKERS SET
    ACTIVE_FLAG = 'N',
    EXPIRY_DATE = :expiryDate,
    MODIFY_DATETIME = :modifyTimestamp,
    MODIFY_USER_ID = USER
    WHERE OFFENDER_BOOK_ID = :bookingId
}

GET_ACTIVE_ALLOCATION_FOR_OFFENDER_BOOKING {
  SELECT OFFENDER_BOOK_ID BOOKING_ID,
         OFFICER_ID STAFF_ID,
         ASSIGNED_TIME ASSIGNED,
         AGY_LOC_ID AGENCY_ID,
         ACTIVE_FLAG ACTIVE,
         ALLOC_REASON REASON,
         ALLOC_TYPE TYPE
  FROM OFFENDER_KEY_WORKERS
  WHERE ACTIVE_FLAG = 'Y'
    AND OFFENDER_BOOK_ID = :bookingId
}

GET_ALLOCATION_HISTORY_FOR_OFFENDER {
  SELECT OKW.OFFENDER_BOOK_ID BOOKING_ID,
         OKW.OFFICER_ID STAFF_ID,
         OKW.ASSIGNED_TIME ASSIGNED,
         OKW.AGY_LOC_ID AGENCY_ID,
         OKW.ACTIVE_FLAG ACTIVE,
         OKW.ALLOC_REASON REASON,
         OKW.ALLOC_TYPE TYPE,
         OKW.EXPIRY_DATE EXPIRY
  FROM OFFENDER_KEY_WORKERS OKW
  JOIN OFFENDER_BOOKINGS OB on OKW.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
  WHERE OB.OFFENDER_ID = :offenderId
  ORDER BY OKW.ASSIGNED_DATE, OKW.ASSIGNED_TIME
}