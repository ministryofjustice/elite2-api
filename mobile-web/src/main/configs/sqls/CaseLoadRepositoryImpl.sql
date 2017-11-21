FIND_CASE_LOADS_BY_USERNAME {
	SELECT CL.CASELOAD_ID,
	       CL.DESCRIPTION,
		  	 CL.CASELOAD_TYPE
	  FROM CASELOADS CL
	       INNER JOIN STAFF_ACCESSIBLE_CASELOADS SC ON CL.CASELOAD_ID = SC.CASELOAD_ID
	       INNER JOIN STAFF_MEMBERS SM ON SM.PERSONNEL_TYPE = 'STAFF' AND SM.STAFF_ID = SC.STAFF_ID
		WHERE SM.USER_ID = :username
}

FIND_CASE_LOAD_BY_ID {
	SELECT CL.CASELOAD_ID,
	       CL.DESCRIPTION,
				 CL.CASELOAD_TYPE
	  FROM CASELOADS CL
	 WHERE CL.CASELOAD_ID = :caseLoadId
}

FIND_ACTIVE_CASE_LOAD_BY_USERNAME {
	SELECT CL.CASELOAD_ID CASE_LOAD_ID,
		     CL.DESCRIPTION,
		     CL.CASELOAD_TYPE TYPE
	FROM CASELOADS CL
			JOIN STAFF_ACCESSIBLE_CASELOADS SC ON CL.CASELOAD_ID = SC.CASELOAD_ID
			JOIN STAFF_MEMBERS SM ON SM.PERSONNEL_TYPE = 'STAFF' AND SM.STAFF_ID = SC.STAFF_ID AND CL.CASELOAD_ID = SM.ASSIGNED_CASELOAD_ID
  WHERE SM.USER_ID = :username
}
