FIND_ROLE_PASSWORD {
	SELECT PROFILE_VALUE ROLE_PWD
	  FROM SYSTEM_PROFILES
	 WHERE PROFILE_TYPE = 'SYS' AND PROFILE_CODE = 'ROLE_PSWD'
}

FIND_USER_BY_STAFF_ID {
    SELECT SM.STAFF_ID,
           SM.USER_ID,
           SM.FIRST_NAME,
           SM.LAST_NAME,
           (   SELECT INTERNET_ADDRESS
                 FROM  INTERNET_ADDRESSES
                WHERE OWNER_CLASS = 'STF' AND INTERNET_ADDRESS_CLASS = 'EMAIL' AND OWNER_ID = :staffId
           ) EMAIL,
           (   SELECT MAX(IMAGE_ID)
                 FROM IMAGES
                WHERE IMAGE_OBJECT_ID = :staffId AND IMAGE_OBJECT_TYPE = 'STAFF' AND ACTIVE_FLAG = 'Y'
           ) IMAGE_ID
      FROM STAFF_MEMBERS SM
     WHERE SM.PERSONNEL_TYPE = 'STAFF'
           AND SM.STAFF_ID = :staffId
}

FIND_USER_BY_USERNAME {
    SELECT SM.STAFF_ID,
           SM.USER_ID,
           SM.FIRST_NAME,
           SM.LAST_NAME,
           (   SELECT INTERNET_ADDRESS
                 FROM  INTERNET_ADDRESSES
                WHERE OWNER_CLASS = 'STF' AND INTERNET_ADDRESS_CLASS = 'EMAIL' AND OWNER_ID = SM.STAFF_ID
           ) EMAIL,
           (   SELECT MAX(IMAGE_ID)
                 FROM IMAGES
                WHERE IMAGE_OBJECT_ID = SM.STAFF_ID AND IMAGE_OBJECT_TYPE = 'STAFF' AND ACTIVE_FLAG = 'Y'
           ) IMAGE_ID
      FROM STAFF_MEMBERS SM
     WHERE SM.PERSONNEL_TYPE = 'STAFF'
           AND SM.USER_ID = :username
}


FIND_ROLES_BY_USERNAME {
	SELECT DISTINCT REPLACE(RL.ROLE_CODE, '-', '_') ROLE_CODE
	  FROM STAFF_MEMBERS SM
	       INNER JOIN STAFF_MEMBER_ROLES RL ON SM.STAFF_ID = RL.STAFF_ID
	 WHERE SM.PERSONNEL_TYPE = 'STAFF' 
	       AND SM.USER_ID = ?
	 ORDER BY 1
}



