@global
Feature: Location and Location Group Events

  Acceptance Criteria
  A logged on staff user can retrieve schedules for groups of prisoners.

  Background:
    Given a user has authenticated with the API

  Scenario: location group caseload not accessible
    Given an existing agency and location group
    And agency does not belong to a caseload accessible to current user
    When schedules are requested for agency and location group
    Then schedules response is HTTP 404 resource not found
    And schedules response error message is "Resource with id [ZZGHI] not found."

  Scenario: location group does not exist
    Given an agency which belongs to a caseload accessible to current user
    And location group does not exist for the agency
    When schedules are requested for agency and location group
    Then schedules response is HTTP 404 resource not found
    And schedules response error message is "Group 'doesnotexist' does not exist for agencyId 'LEI'."

  Scenario: no location group scheduled events
    Given no offender has any scheduled events for current day
    When schedules are requested for agency and location group
    Then schedules response is an empty list

  Scenario: no locations in group
    Given an existing agency and location group
    And location group does not define any locations
    When schedules are requested for agency and location group
    Then schedules response is HTTP 500 server error
    And schedules response error message is "There are no cells set up for location 'BlockE'"

  Scenario: location group scheduled events in order
    Given one or more offenders have scheduled events for current day
    And offenders are located in a location that belongs to requested agency and location group
    When schedules are requested for a valid agency and location group
    Then response is a list of offender's schedules with size 14
    And returned schedules are ordered as defined by requested location group
    And returned schedules are only for offenders located in locations "LEI-A-1-1,LEI-A-1-10"

  Scenario: location group AM timeslot
    Given one or more offenders have scheduled events for current day
    And offenders are located in a location that belongs to requested agency and location group
    When schedules are requested for a valid agency and location group with 'timeSlot' = 'AM'
    Then response is a list of offender's schedules with size 9
    And start time of all returned schedules is before 12h00
    And returned schedules are ordered as defined by requested location group
    And returned schedules are only for offenders located in locations "LEI-A-1-1,LEI-A-1-10"

  Scenario: location group PM timeslot
    Given one or more offenders have scheduled events for current day
    And offenders are located in a location that belongs to requested agency and location group
    When schedules are requested for a valid agency and location group with 'timeSlot' = 'PM'
    Then response is a list of offender's schedules with size 5
    And start time of all returned schedules is between 12h00 and 17h00
    And returned schedules are ordered as defined by requested location group
    And returned schedules are only for offenders located in locations "LEI-A-1-1,LEI-A-1-10"

  Scenario: location group ED timeslot
    Given offenders are located in a location that belongs to requested agency and location group
    When schedules are requested for a valid agency and location group with date = '2017-10-15' and 'timeSlot' = 'ED'
    Then response is a list of offender's schedules with size 1
    And start time of all returned schedules is on or after 17h00
    And returned schedules are only for offenders located in locations "LEI-A-1-1"

###############################################################

  Scenario: location caseload not accessible
    Given an existing agency and location
    And agency does not belong to a caseload accessible to current user
    When schedules are requested for agency and location
    Then schedules response is HTTP 404 resource not found
    And schedules response error message is "Resource with id [ZZGHI] not found."

  Scenario: location does not exist
    Given an agency which belongs to a caseload accessible to current user
    And location does not exist for the agency
    When schedules are requested for agency and location
    Then schedules response is HTTP 404 resource not found
    And schedules response error message is "Resource with id [-99] not found."

  Scenario: usage not valid
    Given an existing agency and location
    And usage value is invalid
    When schedules are requested for agency and location
    Then bad request response, with "Usage not recognised." message, is received from schedules API

  Scenario: agency does not exist for none system users
    Given an agency which does not exists has been set
    And schedules are requested for agency and location
    And schedules response error message is "Resource with id [TEST_AGENCY] not found."

  Scenario: agency does not exist for system users
    Given a admin user has authenticated with the API
    And an agency which does not exists has been set
    And schedules are requested for agency and location
    And schedules response error message is "Resource with id [TEST_AGENCY] not found."

  Scenario: no location scheduled events
    Given the location within the agency has no scheduled events for current day
    When schedules are requested for agency and location
    Then schedules response is an empty list

  Scenario Outline: location scheduled events in order
    Given one or more offenders are due to attend a scheduled event on the current day at a location within an agency
    When schedules are requested for a valid agency with location "<locationId>" and usage "<usage>" and timeSlot "<timeSlot>"
    Then response is a list of offender's schedules for the current day with last name list "<last name list>"
    And the schedule event type list is "<event type list>"
    And the schedule start time list is "<start time list>"
    And returned schedules are ordered in ascending alphabetical order by offender last name
    And returned schedules are only for offenders due to attend a scheduled event on current day for requested agency and location

    Examples:
      | locationId | usage    | timeSlot | last name list                | event type list     | start time list         |
      | -28        | VISIT    |          | BATES                         | VISIT               | 01:00                   |
      | -25        | VISIT    |          | BATES, DUCK                   | VISIT,VISIT         | 00:00, 00:00            |
      | -28        | APP      |          | BATES,DUCK,DUCK               | EDUC,EDUC,EDUC      | 04:00, 01:00, 00:00     |
      | -29        | APP      |          | BATES                         | MEDE                | 03:00                   |
      | -26        | PROG     | AM       | ANDERSON,BATES, DUCK          | EDUC,EDUC, EDUC     | 00:00,00:00, 00:00      |
      | -26        | PROG     | PM       | ANDERSON,ANDERSON,BATES,BATES,DUCK,DUCK| EDUC,EDUC,EDUC,EDUC,EDUC,EDUC | 12:00,12:00,12:00,13:00,13:00,13:00 |
      | -26        | PROG     |          | ANDERSON,ANDERSON,ANDERSON,BATES,BATES,BATES,DUCK,DUCK,DUCK | EDUC,EDUC,EDUC,EDUC,EDUC,EDUC,EDUC,EDUC,EDUC  | 00:00,00:00,00:00,12:00,12:00,12:00,13:00,13:00,13:00 |

  @nomis
  Scenario Outline: Request an offenders scheduled activities for a specific date
  Note 'Chapel Cleaner' activity on Mon AM for A1234AE is excluded in Nomis only
    When activities are requested with a valid agency for date "<date>" with a time slot "<timeSlot>" and offender numbers "<offenderNo>"
    Then the following events should be returned "<events>"
    Examples:
      | date       | offenderNo | timeSlot | events         |
      | 2017-09-18 | A1234AA    | AM       | Chapel Cleaner |
      | 2017-09-11 | A1234AE    | AM       |                |
      | 2017-09-12 | A1234AE    | PM       | Woodwork       |

  @elite
  Scenario Outline: Request an offenders scheduled activities for a specific date
    As above but activities not excluded
    When activities are requested with a valid agency for date "<date>" with a time slot "<timeSlot>" and offender numbers "<offenderNo>"
    Then the following events should be returned "<events>"
    Examples:
      | date       | offenderNo | timeSlot | events                  |
      | 2017-09-11 | A1234AE    | AM       | Chapel Cleaner          |
      | 2017-09-12 | A1234AE    |          | Woodwork,Chapel Cleaner |

  Scenario Outline: Request an offenders scheduled visits for today
    Given an offender with scheduled visits
    When visits are requested with a valid agency with a time slot "<timeSlot>" and offender numbers "<offenderNo>"
    Then the following visits should be returned "<visits>"
    Examples:
      | offenderNo | timeSlot | visits                                       |
      | A1234AC    | AM       | Social Contact,Official Visit                |
      | A1234AC    | PM       | Social Contact                               |
      | A1234AC    |          | Social Contact,Social Contact,Official Visit |
      | A1234AE    | AM       | Official Visit                               |
      | A1234AE    | PM       |                                              |

  Scenario Outline: Request an offenders scheduled appointments for today
    Given an offender with scheduled appointments
    When appointments are requested with a valid agency with a time slot "<timeSlot>" and offender numbers "<offenderNo>"
    Then the following appointments should be returned "<appointments>"
    Examples:
      | offenderNo   | timeSlot | appointments |
      | A1234AC      | AM       | Education,Medical - Dentist                  |
      | A1234AC      | PM       |                                              |
      | A1234AC      |          | Education,Medical - Dentist                  |
      | A1234AE      | AM       | Education,Education                          |
      | A1234AE      | PM       |                                              |

  Scenario Outline: Request an offenders external transfers for a given date
    Given an offender that is scheduled to be transferred outside of the prison
    When a request is made for transfers with the following parameters "<offenderNumber>" and "<date>"
    Then the following offender should be returned "<firstName>", "<lastName>" along with the "<externalTransfers>"
    Examples:
    | offenderNumber | date | firstName |lastName  |externalTransfers |
    | A1234AC    | today| NORMAN    |BATES     | Compassionate Transfer |


  @nomis
  Scenario Outline: Request scheduled court events for offender list
    When Court events are requested with a valid agency with a time slot "<timeSlot>", date "<date>" and offender number list "<offenderNos>"
    Then the following events should be returned: "<events>"
    Examples:
      | offenderNos             | date       | timeSlot | events         |
      | A1234AD,A1234AE,A1234AF | 2017-02-13 | ED       | -104,-105,-106 |
      | A1234AD,A1234AE,A1234AF | 2017-02-13 | PM       |                |
      | A1234AD,A1234AE,A1234AF | 2017-02-13 |          | -104,-105,-106 |
      | A1234AD,A1234AE,A1234AF | 2017-02-14 | ED       |                |
      | A1234AC                 | 2017-10-15 | AM       | -103           |
