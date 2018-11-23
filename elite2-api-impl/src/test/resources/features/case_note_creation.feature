@global
Feature: Case Note Creation and Update

  Acceptance Criteria:
  A logged in staff user can create and update case notes for an existing offender booking.

  Background:
    Given a user has authenticated with the API
    And case note test harness initialized

  Scenario: Create a case note 1
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | OBSERVE                                     |
      | subType            | OBS_GEN                                     |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note is successfully created
    And correct case note source is used

  Scenario: Create a case note 2
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | OBSERVE                                     |
      | subType            | OBS_GEN                                     |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note is successfully created
    And correct case note source is used

  Scenario: Create a case note with invalid type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | doesnotexist                                |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "CaseNote (type,subtype)=(doesnotexist,OSE) does not exist" occurs

  Scenario: Create a case note with valid type but invalid subType
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | GEN                                         |
      | subType            | doesnotexist                                |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "CaseNote (type,subtype)=(GEN,doesnotexist) does not exist" occurs

  Scenario: Create a case note with invalid combination of type and sub-type for any caseload
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | DRR                                         |
      | subType            | HIS                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "CaseNote (type,subtype)=(DRR,HIS) does not exist" occurs

  Scenario: Create a case note with a type and sub-type combination that is valid for different caseload but not current caseload
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | REC                                         |
      | subType            | RECRP                                       |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
  Then case note validation error "CaseNote (type,subtype)=(REC,RECRP) does not exist" occurs

  Scenario: Create a case note with invalid type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | toolongtoolongtoolong                    |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value is too long: max length is 12|CaseNote (type,subtype)=(toolongtoolongtoolong,OSE) does not exist|

  Scenario: Create a case note with invalid sub-type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | GEN                                         |
      | subType            | toolongtoolongtoolong                     |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value is too long: max length is 12|CaseNote (type,subtype)=(GEN,toolongtoolongtoolong) does not exist|

  Scenario: Create a case note with blank type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               |                                             |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value cannot be blank|CaseNote (type,subtype)=(,OSE) does not exist|

  Scenario: Create a case note with blank sub-type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | GEN                                         |
      | subType            |                                             |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value cannot be blank|CaseNote (type,subtype)=(GEN,) does not exist|

  Scenario: Attempt to create case note for offender is not part of any of logged on staff user's caseloads
    When attempt is made to create case note for booking:
      | bookingId          | -16                                         |
      | type               | GEN                                         |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then resource not found response is received from casenotes API

  Scenario: Attempt to create case note for offender that does not exist
    When attempt is made to create case note for booking:
      | bookingId          | -99                                         |
      | type               | GEN                                         |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then resource not found response is received from casenotes API
