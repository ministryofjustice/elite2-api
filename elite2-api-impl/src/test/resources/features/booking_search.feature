@global
Feature: Booking Search

  Acceptance Criteria:
  A logged in staff user can search for bookings by providing a full offender last name (in upper, lower or mixed case).
  A logged in staff user can search for bookings by providing a partial offender last name (in upper, lower or mixed case).
  A logged in staff user can search for bookings by providing a full offender first name (in upper, lower or mixed case).
  A logged in staff user can search for bookings by providing a partial offender first name (in upper, lower or mixed case).
  A logged in staff user can search for bookings based on a matching first name and last name.
  A logged in staff user can search for bookings based on a matching first name or last name.

  Background:
    Given a user has authenticated with the API

  Scenario: Search all offenders
    When a booking search is made without any criteria
    Then "10" booking records are returned
    And  "31" total booking records are available

  Scenario Outline: Search based on full offender last name
    When a booking search is made with full last "<name>" of existing offender
    Then "<number>" booking records are returned
    And offender first names match "<first name list>"
    And offender middle names match "<middle name list>"
    And living unit descriptions match "<living unit list>"
    And image id match "<image ids>"
    And their dob match "<DOB>"

    Examples:
      | name     | number | first name list | middle name list | living unit list    |  image ids   | DOB                   |
      | ANDERSON | 2      | ARTHUR,GILLIAN  | BORIS,EVE        | A-1-1,H-1-5         | -1,-2        | 1969-12-30,1998-08-28 |
      | DUCK     | 1      | DONALD          | JEFFREY          | A-1-10              | -999         | 1956-02-28            |
      | anderson | 2      | ARTHUR,GILLIAN  | BORIS,EVE        | A-1-1,H-1-5         | -1,-2        | 1969-12-30,1998-08-28 |
      | AnDersOn | 2      | ARTHUR,GILLIAN  | BORIS,EVE        | A-1-1,H-1-5         | -1,-2        | 1969-12-30,1998-08-28 |
      | UNKNOWN  | 0      |                 |                  |                     |              |                       |
      |          | 0      |                 |                  |                     |              |                       |

  Scenario Outline: Search based on partial offender last name
    When a booking search is made with partial last "<name>" of existing offender
    Then "<number>" booking records are returned
    And offender first names match "<first name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name | number | first name list        | middle name list |
      | AND% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |
      | and% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |
      | AnD% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |
      | XX%  | 0      |                        |                  |
      |      | 0      |                        |                  |

  Scenario Outline: Search based on full offender first name
    When a booking search is made with full first "<name>" of existing offender
    Then "<number>" booking records are returned
    And offender last names match "<last name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name    | number | last name list | middle name list |
      | DONALD  | 2      | DUCK,TRUMP     | JEFFREY          |
      | donald  | 2      | DUCK,TRUMP     | JEFFREY          |
      | DoNAld  | 2      | DUCK,TRUMP     | JEFFREY          |
      | UNKNOWN | 0      |                |                  |
      |         | 0      |                |                  |

  Scenario Outline: Search based on partial offender first name
    When a booking search is made with partial first "<name>" of existing offender
    Then "<number>" booking records are returned
    And offender last names match "<last name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name | number | last name list          | middle name list |
      | CH%  | 3      | CHAPLIN,THOMPSON,WOAKES | JAMES,JAMES      |
      | ch%  | 3      | CHAPLIN,THOMPSON,WOAKES | JAMES,JAMES      |
      | Ch%  | 3      | CHAPLIN,THOMPSON,WOAKES | JAMES,JAMES      |
      | XX%  | 0      |                         |                  |
      |      | 0      |                         |                  |

  Scenario Outline: Search based on offender first name and last name
    When a booking search is made with "<first name>" and "<last name>" of existing offender
    Then "<number>" booking records are returned
    And offender first names match "<first name list>"
    And offender last names match "<last name list>"

    Examples:
      | first name | last name | number | first name list            | last name list             |
      | DONALD     | TRUMP     | 1      | DONALD                     | TRUMP                      |
      | CHARLES    | CHAPLIN   | 1      | CHARLES                    | CHAPLIN                    |
      | JOHN       | DOE       | 0      |                            |                            |
      | DA%        | SMITH     | 2      | DANIEL,DARIUS              | SMITH,SMITH                |
      | DANIEL     | SM%       | 2      | DANIEL,DANIEL              | SMITH,SMELLEY              |
      | DA%        | SM%       | 3      | DANIEL,DANIEL,DARIUS       | SMITH,SMITH,SMELLEY        |
      |            | SM%       | 0      |                            |                            |
      | DA%        |           | 0      |                            |                            |
      |            |           | 0      |                            |                            |

  Scenario Outline: Search based on offender first name or last name
    When a booking search is made with "<first name>" or "<last name>" of existing offender
    Then "<number>" booking records are returned
    And offender first names match "<first name list>"
    And offender last names match "<last name list>"

    Examples:
      | first name | last name | number | first name list                    | last name list                   |
      | DONALD     | CHAPLIN   | 3      | CHARLES,DONALD,DONALD              | CHAPLIN,DUCK,TRUMP               |
      | CHARLES    | TRUMP     | 2      | CHARLES,DONALD                     | CHAPLIN,TRUMP                    |
      | JOHN       | DOE       | 0      |                                    |                                  |
      | DA%        | SMITH     | 5      | DANIEL,DANIEL,DARIUS,GILES,MATTHEW | SMITH,SMITH,SMITH,SMITH,SMELLEY  |
      | DANIEL     | SM%       | 5      | DANIEL,DANIEL,DARIUS,GILES,MATTHEW | SMITH,SMITH,SMITH,SMITH,SMELLEY  |
      | DA%        | SM%       | 5      | DANIEL,DANIEL,DARIUS,GILES,MATTHEW | SMITH,SMITH,SMITH,SMITH,SMELLEY  |

    Scenario: Search for all offenders in Leeds
      When a booking search is made in "LEI-A"
      Then only offenders situated in "A-" be present in the results