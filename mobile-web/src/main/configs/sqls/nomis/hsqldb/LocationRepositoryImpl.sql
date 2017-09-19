FIND_LOCATIONS_BY_AGENCY_AND_TYPE {
SELECT A.INTERNAL_LOCATION_ID,
  A.AGY_LOC_ID,
  A.INTERNAL_LOCATION_TYPE,
  A.DESCRIPTION as LOCATION_PREFIX,
  A.DESCRIPTION,
  A.PARENT_INTERNAL_LOCATION_ID,
  A.NO_OF_OCCUPANT,
  LIST_SEQ
FROM AGENCY_INTERNAL_LOCATIONS A
WHERE A.ACTIVE_FLAG = 'Y'
      AND A.AGY_LOC_ID = :agencyId
      AND A.INTERNAL_LOCATION_TYPE = :locationType
order by PARENT_INTERNAL_LOCATION_ID,LIST_SEQ
}