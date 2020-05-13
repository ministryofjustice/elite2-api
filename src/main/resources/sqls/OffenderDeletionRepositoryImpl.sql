OD_OFFENDER_IDS {
SELECT OFFENDER_ID FROM OFFENDERS WHERE OFFENDER_ID_DISPLAY = :offenderNo
}

OD_ENABLE_PARALLEL_HINTS {
ALTER SESSION ENABLE PARALLEL DML
}

OD_OFFENDER_BOOKING_IDS {
SELECT OFFENDER_BOOK_ID FROM OFFENDER_BOOKINGS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_INTERNET_ADDRESSES_BY_BOOK_IDS {
DELETE FROM INTERNET_ADDRESSES
WHERE OWNER_CLASS IN ('OFF_EMP', 'OFF_EDU')
AND OWNER_ID IN (:bookIds)
}

OD_DELETE_PHONES_BY_BOOK_IDS {
DELETE FROM PHONES P
WHERE (P.OWNER_CLASS IN ('OFF_EMP', 'OFF_EDU') AND P.OWNER_ID IN (:bookIds))
OR (P.OWNER_CLASS = 'ADDR' AND EXISTS (SELECT 1 FROM ADDRESSES A
                                       WHERE P.OWNER_ID = A.ADDRESS_ID
                                         AND A.OWNER_CLASS IN ('OFF_EMP', 'OFF_EDU')
                                         AND A.OWNER_ID IN (:bookIds)))
}

OD_DELETE_ADDRESS_USAGES_BY_BOOK_IDS {
DELETE FROM ADDRESS_USAGES AU
WHERE EXISTS (SELECT 1
              FROM ADDRESSES A
              WHERE AU.ADDRESS_ID = A.ADDRESS_ID
                AND A.OWNER_CLASS IN ('OFF_EMP', 'OFF_EDU')
                AND A.OWNER_ID IN (:bookIds))
}

OD_DELETE_ADDRESSES_BY_BOOK_IDS {
DELETE FROM ADDRESSES
WHERE OWNER_CLASS IN ('OFF_EMP', 'OFF_EDU')
AND OWNER_ID IN (:bookIds)
}

OD_DELETE_WORK_FLOWS {
DELETE FROM WORK_FLOWS WHERE OBJECT_ID IN (:bookIds)
}

OD_DELETE_WORK_FLOW_LOGS {
DELETE /*+ PARALLEL(WFL) */ FROM WORK_FLOW_LOGS WFL
WHERE EXISTS (SELECT 1
              FROM WORK_FLOWS WF
              WHERE WF.WORK_FLOW_ID = WFL.WORK_FLOW_ID
              AND WF.OBJECT_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_OIC_SANCTIONS {
DELETE FROM OFFENDER_OIC_SANCTIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OIC_HEARING_RESULTS {
DELETE FROM OIC_HEARING_RESULTS OHR
WHERE EXISTS (
  SELECT 1
  FROM OIC_HEARINGS OH
  INNER JOIN AGENCY_INCIDENT_PARTIES AIP
    ON AIP.OIC_INCIDENT_ID = OH.OIC_INCIDENT_ID
  WHERE OH.OIC_HEARING_ID = OHR.OIC_HEARING_ID
  AND AIP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OIC_HEARING_COMMENTS {
DELETE FROM OIC_HEARING_COMMENTS OHC
WHERE EXISTS (
  SELECT 1
  FROM OIC_HEARINGS OH
  INNER JOIN AGENCY_INCIDENT_PARTIES AIP
    ON AIP.OIC_INCIDENT_ID = OH.OIC_INCIDENT_ID
  WHERE OH.OIC_HEARING_ID = OHC.OIC_HEARING_ID
  AND AIP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OIC_HEARING_NOTICES {
DELETE FROM OIC_HEARING_NOTICES OHN
WHERE EXISTS (
  SELECT 1
  FROM OIC_HEARINGS OH
  INNER JOIN AGENCY_INCIDENT_PARTIES AIP
    ON AIP.OIC_INCIDENT_ID = OH.OIC_INCIDENT_ID
  WHERE OH.OIC_HEARING_ID = OHN.OIC_HEARING_ID
  AND AIP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OIC_HEARINGS {
DELETE FROM OIC_HEARINGS OH
WHERE EXISTS (SELECT 1
              FROM AGENCY_INCIDENT_PARTIES AIP
              WHERE OH.OIC_INCIDENT_ID = AIP.OIC_INCIDENT_ID
                AND AIP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_AGY_INC_INV_STATEMENTS {
DELETE FROM AGY_INC_INV_STATEMENTS AIIS
WHERE EXISTS (
  SELECT 1
  FROM AGY_INC_INVESTIGATIONS AII
  INNER JOIN AGENCY_INCIDENT_PARTIES AIP
    ON AII.AGENCY_INCIDENT_ID = AIP.AGENCY_INCIDENT_ID
    AND AII.PARTY_SEQ = AIP.PARTY_SEQ
  WHERE AIIS.AGY_INC_INVESTIGATION_ID = AII.AGY_INC_INVESTIGATION_ID
  AND AIP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_AGY_INC_INVESTIGATIONS {
DELETE FROM AGY_INC_INVESTIGATIONS AII
WHERE EXISTS (SELECT 1
              FROM AGENCY_INCIDENT_PARTIES AIP
              WHERE AII.AGENCY_INCIDENT_ID = AIP.AGENCY_INCIDENT_ID
                AND AII.PARTY_SEQ = AIP.PARTY_SEQ
                AND AIP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_AGENCY_INCIDENT_CHARGES {
DELETE FROM AGENCY_INCIDENT_CHARGES AIC
WHERE EXISTS (SELECT 1
              FROM AGENCY_INCIDENT_PARTIES AIP
              WHERE AIC.AGENCY_INCIDENT_ID = AIP.AGENCY_INCIDENT_ID
                AND AIC.PARTY_SEQ = AIP.PARTY_SEQ
                AND AIP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_AGENCY_INCIDENT_PARTIES {
DELETE FROM AGENCY_INCIDENT_PARTIES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_CASE_NOTE_SENTS {
DELETE /*+ PARALLEL(OFFENDER_CASE_NOTE_SENTS) */ FROM OFFENDER_CASE_NOTE_SENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFF_CASE_NOTE_RECIPIENTS {
DELETE FROM OFF_CASE_NOTE_RECIPIENTS OCNR
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CASE_NOTES OCN
              WHERE OCN.CASE_NOTE_ID = OCNR.CASE_NOTE_ID
                AND OCN.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CASE_NOTES {
DELETE FROM OFFENDER_CASE_NOTES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_IND_SCH_SENTS {
DELETE FROM OFFENDER_IND_SCH_SENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_IND_SCHEDULES {
DELETE /*+ PARALLEL(OFFENDER_IND_SCHEDULES) */ FROM OFFENDER_IND_SCHEDULES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_MOVEMENT_APPS {
DELETE FROM OFFENDER_MOVEMENT_APPS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_COURSE_ATTENDANCES {
DELETE /*+ PARALLEL(OCA) */ FROM OFFENDER_COURSE_ATTENDANCES OCA
WHERE EXISTS (SELECT 1
              FROM OFFENDER_PROGRAM_PROFILES OPP
              WHERE OCA.OFF_PRGREF_ID = OPP.OFF_PRGREF_ID
              AND OPP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_EXCLUDE_ACTS_SCHDS {
DELETE FROM OFFENDER_EXCLUDE_ACTS_SCHDS OEAS
WHERE EXISTS (SELECT 1
              FROM OFFENDER_PROGRAM_PROFILES OPP
              WHERE OEAS.OFF_PRGREF_ID = OPP.OFF_PRGREF_ID
                AND OPP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_PRG_PRF_PAY_BANDS {
DELETE /*+ PARALLEL(OPPPB) */ FROM OFFENDER_PRG_PRF_PAY_BANDS OPPPB
WHERE EXISTS (SELECT 1
              FROM OFFENDER_PROGRAM_PROFILES OPP
              WHERE OPPPB.OFF_PRGREF_ID = OPP.OFF_PRGREF_ID
              AND OPP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_PROGRAM_PROFILES {
DELETE /*+ PARALLEL(OFFENDER_PROGRAM_PROFILES) */ FROM OFFENDER_PROGRAM_PROFILES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_PRG_OBLIGATION_HTY {
DELETE FROM OFFENDER_PRG_OBLIGATION_HTY OPOH
WHERE EXISTS (SELECT 1
              FROM OFFENDER_PRG_OBLIGATIONS OPO
              WHERE OPO.OFFENDER_PRG_OBLIGATION_ID = OPOH.OFFENDER_PRG_OBLIGATION_ID
                AND OPO.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_PRG_OBLIGATIONS {
DELETE FROM OFFENDER_PRG_OBLIGATIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SENT_COND_STATUSES {
DELETE FROM OFFENDER_SENT_COND_STATUSES OSCS
WHERE EXISTS (SELECT 1
              FROM OFFENDER_SENT_CONDITIONS OSC
              WHERE OSC.OFFENDER_SENT_CONDITION_ID = OSCS.OFFENDER_SENT_CONDITION_ID
                AND OSC.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_SENT_CONDITIONS {
DELETE FROM OFFENDER_SENT_CONDITIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SENTENCE_ADJUSTS {
DELETE FROM OFFENDER_SENTENCE_ADJUSTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_KEY_DATE_ADJUSTS {
DELETE FROM OFFENDER_KEY_DATE_ADJUSTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_LICENCE_CONDITIONS {
DELETE FROM OFFENDER_LICENCE_CONDITIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_LICENCE_RECALLS {
DELETE FROM OFFENDER_LICENCE_RECALLS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_LICENCE_SENTENCES {
DELETE FROM OFFENDER_LICENCE_SENTENCES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SENTENCE_CHARGES {
DELETE FROM OFFENDER_SENTENCE_CHARGES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SENTENCE_STATUSES {
DELETE FROM OFFENDER_SENTENCE_STATUSES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SENTENCE_TERMS {
DELETE FROM OFFENDER_SENTENCE_TERMS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SENTENCE_UA_EVENTS {
DELETE FROM OFFENDER_SENTENCE_UA_EVENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SENTENCES {
DELETE FROM OFFENDER_SENTENCES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_REORDER_SENTENCES {
DELETE FROM OFFENDER_REORDER_SENTENCES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_ORDER_PURPOSES {
DELETE FROM ORDER_PURPOSES OP
WHERE EXISTS (SELECT 1
              FROM ORDERS O
              WHERE O.ORDER_ID = OP.ORDER_ID
                AND O.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_ORDERS {
DELETE FROM ORDERS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_LINK_CASE_TXNS {
DELETE FROM LINK_CASE_TXNS LCT
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CASES OC
              WHERE OC.CASE_ID = LCT.CASE_ID
                AND OC.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_COURT_EVENT_CHARGES {
DELETE FROM COURT_EVENT_CHARGES WHERE (EVENT_ID, OFFENDER_CHARGE_ID) IN (
  SELECT DISTINCT CEC.EVENT_ID, CEC.OFFENDER_CHARGE_ID
  FROM COURT_EVENTS CE
  FULL OUTER JOIN OFFENDER_CHARGES OC
    ON CE.OFFENDER_BOOK_ID = OC.OFFENDER_BOOK_ID
  INNER JOIN COURT_EVENT_CHARGES CEC
    ON (CE.EVENT_ID = CEC.EVENT_ID OR OC.OFFENDER_CHARGE_ID = CEC.OFFENDER_CHARGE_ID)
  WHERE CE.OFFENDER_BOOK_ID IN (:bookIds)
  AND OC.OFFENDER_BOOK_ID IN (:bookIds)
)
}

OD_DELETE_COURT_EVENTS {
DELETE FROM COURT_EVENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_CHARGES {
DELETE FROM OFFENDER_CHARGES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_CASE_IDENTIFIERS {
DELETE FROM OFFENDER_CASE_IDENTIFIERS OCI
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CASES OC
              WHERE OC.CASE_ID = OCI.CASE_ID
                AND OC.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CASE_STATUSES {
DELETE FROM OFFENDER_CASE_STATUSES OCS
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CASES OC
              WHERE OC.CASE_ID = OCS.CASE_ID
                AND OC.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CASES {
DELETE FROM OFFENDER_CASES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_PERSON_RESTRICTS {
DELETE FROM OFFENDER_PERSON_RESTRICTS OPR
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CONTACT_PERSONS OCP
              WHERE OPR.OFFENDER_CONTACT_PERSON_ID = OCP.OFFENDER_CONTACT_PERSON_ID
                AND OCP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CONTACT_PERSONS {
DELETE FROM OFFENDER_CONTACT_PERSONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_CSIP_FACTORS {
DELETE FROM OFFENDER_CSIP_FACTORS OCF
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CSIP_REPORTS OCR
              WHERE OCF.CSIP_ID = OCR.CSIP_ID
                AND OCR.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CSIP_INTVW {
DELETE FROM OFFENDER_CSIP_INTVW OCI
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CSIP_REPORTS OCR
              WHERE OCI.CSIP_ID = OCR.CSIP_ID
                AND OCR.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CSIP_PLANS {
DELETE FROM OFFENDER_CSIP_PLANS OCP
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CSIP_REPORTS OCR
              WHERE OCP.CSIP_ID = OCR.CSIP_ID
                AND OCR.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CSIP_ATTENDEES {
DELETE FROM OFFENDER_CSIP_ATTENDEES OCA
WHERE EXISTS (
  SELECT 1
  FROM OFFENDER_CSIP_REVIEWS OCREV
  INNER JOIN OFFENDER_CSIP_REPORTS OCREP
    ON OCREV.CSIP_ID = OCREP.CSIP_ID
  WHERE OCA.REVIEW_ID = OCREV.REVIEW_ID
    AND OCREP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CSIP_REVIEWS {
DELETE FROM OFFENDER_CSIP_REVIEWS OCREV
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CSIP_REPORTS OCREP
              WHERE OCREV.CSIP_ID = OCREP.CSIP_ID
                AND OCREP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_CSIP_REPORTS {
DELETE FROM OFFENDER_CSIP_REPORTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_CURFEW_ADDRESS_OCCUPANTS {
DELETE FROM CURFEW_ADDRESS_OCCUPANTS CAO
WHERE EXISTS (SELECT 1
              FROM CURFEW_ADDRESSES CA
              WHERE CAO.CURFEW_ADDRESS_ID = CA.CURFEW_ADDRESS_ID
                AND CA.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_CURFEW_ADDRESSES {
DELETE FROM CURFEW_ADDRESSES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_HDC_PROB_STAFF_RESPONSES {
DELETE FROM HDC_PROB_STAFF_RESPONSES HPSR
WHERE EXISTS (SELECT 1
              FROM HDC_REQUEST_REFERRALS HRR
              WHERE HPSR.HDC_REQUEST_REFERRAL_ID = HRR.HDC_REQUEST_REFERRAL_ID
                AND HRR.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_HDC_PROB_STAFF_COMMENTS {
DELETE FROM HDC_PROB_STAFF_COMMENTS HPSC
WHERE EXISTS (SELECT 1
              FROM HDC_REQUEST_REFERRALS HRR
              WHERE HPSC.HDC_REQUEST_REFERRAL_ID = HRR.HDC_REQUEST_REFERRAL_ID
                AND HRR.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_HDC_BOARD_DECISIONS {
DELETE FROM HDC_BOARD_DECISIONS HBD
WHERE EXISTS (SELECT 1
              FROM HDC_REQUEST_REFERRALS HRR
              WHERE HBD.HDC_REQUEST_REFERRAL_ID = HRR.HDC_REQUEST_REFERRAL_ID
                AND HRR.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_HDC_GOVERNOR_DECISIONS {
DELETE FROM HDC_GOVERNOR_DECISIONS HGD
WHERE EXISTS (SELECT 1
              FROM HDC_REQUEST_REFERRALS HRR
              WHERE HGD.HDC_REQUEST_REFERRAL_ID = HRR.HDC_REQUEST_REFERRAL_ID
                AND HRR.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_HDC_REQUEST_REFERRALS {
DELETE FROM HDC_REQUEST_REFERRALS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_HDC_STATUS_REASONS {
DELETE FROM HDC_STATUS_REASONS HSR
WHERE EXISTS (
  SELECT 1
  FROM HDC_STATUS_TRACKINGS HST
  INNER JOIN OFFENDER_CURFEWS OC
    ON OC.OFFENDER_CURFEW_ID = HST.OFFENDER_CURFEW_ID
  WHERE HST.HDC_STATUS_TRACKING_ID = HSR.HDC_STATUS_TRACKING_ID
    AND OC.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_HDC_STATUS_TRACKINGS {
DELETE FROM HDC_STATUS_TRACKINGS HST
WHERE EXISTS (SELECT 1
              FROM OFFENDER_CURFEWS OC
              WHERE HST.OFFENDER_CURFEW_ID = OC.OFFENDER_CURFEW_ID
                AND OC.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_HDC_PRISON_STAFF_COMMENTS {
DELETE FROM HDC_PRISON_STAFF_COMMENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_CURFEWS {
DELETE FROM OFFENDER_CURFEWS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_GANG_INVESTS {
DELETE FROM OFFENDER_GANG_INVESTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_GANG_EVIDENCES {
DELETE FROM OFFENDER_GANG_EVIDENCES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_GANG_AFFILIATIONS {
DELETE FROM OFFENDER_GANG_AFFILIATIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_MEDICAL_TREATMENTS {
DELETE /*+ PARALLEL(OMT) */ FROM OFFENDER_MEDICAL_TREATMENTS OMT
WHERE EXISTS (SELECT 1
              FROM OFFENDER_HEALTH_PROBLEMS OHP
WHERE OMT.OFFENDER_HEALTH_PROBLEM_ID = OHP.OFFENDER_HEALTH_PROBLEM_ID
AND OHP.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_HEALTH_PROBLEMS {
DELETE FROM OFFENDER_HEALTH_PROBLEMS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_LIDS_REMAND_DAYS {
DELETE FROM OFFENDER_LIDS_REMAND_DAYS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_LIDS_KEY_DATES {
DELETE FROM OFFENDER_LIDS_KEY_DATES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_NA_DETAILS {
DELETE FROM OFFENDER_NA_DETAILS WHERE OFFENDER_BOOK_ID IN (:bookIds) OR NS_OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_NON_ASSOCIATIONS {
DELETE FROM OFFENDER_NON_ASSOCIATIONS WHERE OFFENDER_BOOK_ID IN (:bookIds) OR NS_OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_REHAB_PROVIDERS {
DELETE FROM OFFENDER_REHAB_PROVIDERS ORP
WHERE EXISTS (SELECT 1
              FROM OFFENDER_REHAB_DECISIONS ORD
              WHERE ORD.OFFENDER_REHAB_DECISION_ID = ORP.OFFENDER_REHAB_DECISION_ID
                AND ORD.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_REHAB_DECISIONS {
DELETE FROM OFFENDER_REHAB_DECISIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_HDC_CALC_EXCLUSION_REASONS {
DELETE FROM HDC_CALC_EXCLUSION_REASONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SENT_CALCULATIONS {
DELETE FROM OFFENDER_SENT_CALCULATIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SUBSTANCE_DETAILS {
DELETE FROM OFFENDER_SUBSTANCE_DETAILS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SUBSTANCE_TREATMENTS {
DELETE FROM OFFENDER_SUBSTANCE_TREATMENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SUBSTANCE_USES {
DELETE FROM OFFENDER_SUBSTANCE_USES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_VISIT_VISITORS {
DELETE /*+ PARALLEL(OVV) */ FROM OFFENDER_VISIT_VISITORS OVV
WHERE EXISTS (SELECT 1
              FROM OFFENDER_VISITS OV
WHERE OVV.OFFENDER_VISIT_ID = OV.OFFENDER_VISIT_ID
AND OV.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_VISITS {
DELETE /*+ PARALLEL(OFFENDER_VISITS) */ FROM OFFENDER_VISITS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_VISIT_BALANCE_ADJS {
DELETE /*+ PARALLEL(OFFENDER_VISIT_BALANCE_ADJS) */ FROM OFFENDER_VISIT_BALANCE_ADJS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_VISIT_BALANCES {
DELETE FROM OFFENDER_VISIT_BALANCES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_VO_VISITORS {
DELETE /*+ PARALLEL(OVV) */ FROM OFFENDER_VO_VISITORS OVV
WHERE EXISTS (SELECT 1
              FROM OFFENDER_VISIT_ORDERS OVO
WHERE OVV.OFFENDER_VISIT_ORDER_ID = OVO.OFFENDER_VISIT_ORDER_ID
AND OVO.OFFENDER_BOOK_ID IN (:bookIds))
}

OD_DELETE_OFFENDER_VISIT_ORDERS {
DELETE FROM OFFENDER_VISIT_ORDERS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_VSC_SENTENCE_TERMS {
DELETE FROM OFFENDER_VSC_SENTENCE_TERMS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_VSC_SENTENCES {
DELETE FROM OFFENDER_VSC_SENTENCES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_INCIDENT_CASES {
SELECT INCIDENT_CASE_ID FROM INCIDENT_CASE_PARTIES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_INCIDENT_CASE_PARTIES {
DELETE FROM INCIDENT_CASE_PARTIES WHERE INCIDENT_CASE_ID IN (:incidentCaseIds)
}

OD_DELETE_INCIDENT_CASE_RESPONSES {
DELETE /*+ PARALLEL(INCIDENT_CASE_RESPONSES) */ FROM INCIDENT_CASE_RESPONSES WHERE INCIDENT_CASE_ID IN (:incidentCaseIds)
}

OD_DELETE_INCIDENT_CASE_QUESTIONS {
DELETE /*+ PARALLEL(INCIDENT_CASE_QUESTIONS) */ FROM INCIDENT_CASE_QUESTIONS WHERE INCIDENT_CASE_ID IN (:incidentCaseIds)
}

OD_DELETE_INCIDENT_QUESTIONNAIRE_HTY {
DELETE FROM INCIDENT_QUESTIONNAIRE_HTY WHERE INCIDENT_CASE_ID IN (:incidentCaseIds)
}

OD_DELETE_INCIDENT_CASE_REQUIREMENTS {
DELETE FROM INCIDENT_CASE_REQUIREMENTS WHERE INCIDENT_CASE_ID IN (:incidentCaseIds)
}

OD_DELETE_INCIDENT_CASES {
DELETE FROM INCIDENT_CASES WHERE INCIDENT_CASE_ID IN (:incidentCaseIds)
}

OD_DELETE_BED_ASSIGNMENT_HISTORIES {
DELETE /*+ PARALLEL(BED_ASSIGNMENT_HISTORIES) */ FROM BED_ASSIGNMENT_HISTORIES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_CASE_ASSOCIATED_PERSONS {
DELETE FROM CASE_ASSOCIATED_PERSONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_IWP_DOCUMENTS {
DELETE FROM IWP_DOCUMENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_ALERTS {
DELETE FROM OFFENDER_ALERTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_ASSESSMENT_ITEMS {
DELETE /*+ PARALLEL(OFFENDER_ASSESSMENT_ITEMS) */ FROM OFFENDER_ASSESSMENT_ITEMS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_ASSESSMENTS {
DELETE FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_BOOKING_AGY_LOCS {
DELETE FROM OFFENDER_BOOKING_AGY_LOCS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_BOOKING_DETAILS {
DELETE FROM OFFENDER_BOOKING_DETAILS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_BOOKING_EVENTS {
DELETE FROM OFFENDER_BOOKING_EVENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_CASE_ASSOCIATIONS {
DELETE FROM OFFENDER_CASE_ASSOCIATIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_CASE_OFFICERS {
DELETE FROM OFFENDER_CASE_OFFICERS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_DATA_CORRECTIONS_HTY {
DELETE FROM OFFENDER_DATA_CORRECTIONS_HTY WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_DISCHARGE_BALANCES {
DELETE FROM OFFENDER_DISCHARGE_BALANCES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_EDUCATIONS {
DELETE FROM OFFENDER_EDUCATIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_EMPLOYMENTS {
DELETE FROM OFFENDER_EMPLOYMENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_EXTERNAL_MOVEMENTS {
DELETE /*+ PARALLEL(OFFENDER_EXTERNAL_MOVEMENTS) */ FROM OFFENDER_EXTERNAL_MOVEMENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_FINE_PAYMENTS {
DELETE FROM OFFENDER_FINE_PAYMENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_FIXED_TERM_RECALLS {
DELETE FROM OFFENDER_FIXED_TERM_RECALLS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_IDENTIFYING_MARKS {
DELETE FROM OFFENDER_IDENTIFYING_MARKS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_IEP_LEVELS {
DELETE FROM OFFENDER_IEP_LEVELS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_IMAGES {
DELETE FROM OFFENDER_IMAGES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_IMPRISON_STATUSES {
DELETE FROM OFFENDER_IMPRISON_STATUSES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_INTER_MVMT_LOCATIONS {
DELETE FROM OFFENDER_INTER_MVMT_LOCATIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_KEY_WORKERS {
DELETE FROM OFFENDER_KEY_WORKERS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_LANGUAGES {
DELETE FROM OFFENDER_LANGUAGES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_MILITARY_RECORDS {
DELETE FROM OFFENDER_MILITARY_RECORDS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_NO_PAY_PERIODS {
DELETE FROM OFFENDER_NO_PAY_PERIODS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_OGRS3_RISK_PREDICTORS {
DELETE FROM OFFENDER_OGRS3_RISK_PREDICTORS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_PAY_STATUSES {
DELETE FROM OFFENDER_PAY_STATUSES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_PHYSICAL_ATTRIBUTES {
DELETE FROM OFFENDER_PHYSICAL_ATTRIBUTES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_PPTY_CONTAINERS {
DELETE FROM OFFENDER_PPTY_CONTAINERS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_PROFILES {
DELETE FROM OFFENDER_PROFILES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_PROFILE_DETAILS {
DELETE /*+ PARALLEL(OFFENDER_PROFILE_DETAILS) */ FROM OFFENDER_PROFILE_DETAILS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_RELEASE_DETAILS {
DELETE FROM OFFENDER_RELEASE_DETAILS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_RELEASE_DETAILS_HTY {
DELETE FROM OFFENDER_RELEASE_DETAILS_HTY WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_RESTRICTIONS {
DELETE FROM OFFENDER_RESTRICTIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_RISK_PREDICTORS {
DELETE FROM OFFENDER_RISK_PREDICTORS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_SUPERVISING_COURTS {
DELETE FROM OFFENDER_SUPERVISING_COURTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_TEAM_ASSIGNMENTS {
DELETE FROM OFFENDER_TEAM_ASSIGNMENTS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_TEAM_ASSIGN_HTY {
DELETE FROM OFFENDER_TEAM_ASSIGN_HTY WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_TEST_SELECTIONS {
DELETE FROM OFFENDER_TEST_SELECTIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_TMP_REL_SCHEDULES {
DELETE FROM OFFENDER_TMP_REL_SCHEDULES WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_TRUST_ACCOUNTS_TEMP {
DELETE FROM OFFENDER_TRUST_ACCOUNTS_TEMP WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_VSC_ERROR_LOGS {
DELETE FROM OFFENDER_VSC_ERROR_LOGS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_OFFENDER_VSC_SENT_CALCULATIONS {
DELETE FROM OFFENDER_VSC_SENT_CALCULATIONS WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_TASK_ASSIGNMENT_HTY {
DELETE FROM TASK_ASSIGNMENT_HTY WHERE OFFENDER_BOOK_ID IN (:bookIds)
}

OD_DELETE_WORKFLOW_HISTORY {
DELETE FROM WORKFLOW_HISTORY WHERE ORIG_OFFENDER_BOOK_ID IN (:bookIds)
}

OD_ANONYMISE_GL_TRANSACTIONS {
UPDATE /*+ PARALLEL(GL_TRANSACTIONS) */ GL_TRANSACTIONS
SET OFFENDER_ID = NULL, OFFENDER_BOOK_ID = NULL
WHERE OFFENDER_BOOK_ID IN (:bookIds)
OR OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_INTERNET_ADDRESSES_BY_OFFENDER_IDS {
DELETE FROM INTERNET_ADDRESSES
WHERE OWNER_CLASS = 'OFF'
AND OWNER_ID IN (:offenderIds)
}

OD_DELETE_PHONES_BY_OFFENDER_IDS {
DELETE FROM PHONES P
WHERE (P.OWNER_CLASS = 'OFF' AND P.OWNER_ID IN (:offenderIds))
OR (P.OWNER_CLASS = 'ADDR' AND EXISTS (SELECT 1 FROM ADDRESSES A
WHERE P.OWNER_ID = A.ADDRESS_ID
AND A.OWNER_CLASS = 'OFF'
AND A.OWNER_ID IN (:offenderIds)))
}

OD_DELETE_ADDRESS_USAGES_BY_OFFENDER_IDS {
DELETE FROM ADDRESS_USAGES AU
WHERE EXISTS (SELECT 1
FROM ADDRESSES A
WHERE AU.ADDRESS_ID = A.ADDRESS_ID
AND OWNER_CLASS = 'OFF'
AND A.OWNER_ID IN (:offenderIds))
}

OD_DELETE_ADDRESSES_BY_OFFENDER_IDS {
DELETE FROM ADDRESSES
WHERE OWNER_CLASS = 'OFF'
AND OWNER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_TRANSACTION_DETAILS {
DELETE /*+ PARALLEL(OTD) */ FROM OFFENDER_TRANSACTION_DETAILS OTD
WHERE EXISTS (SELECT 1
FROM OFFENDER_TRANSACTIONS OT
WHERE OTD.TXN_ID = OT.TXN_ID
AND OTD.TXN_ENTRY_SEQ = OT.TXN_ENTRY_SEQ
AND OT.OFFENDER_ID IN (:offenderIds))
}

OD_DELETE_OFFENDER_TRANSACTIONS {
DELETE /*+ PARALLEL(OFFENDER_TRANSACTIONS) */ FROM OFFENDER_TRANSACTIONS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_BENEFICIARY_TRANSACTIONS {
DELETE FROM BENEFICIARY_TRANSACTIONS BT
WHERE EXISTS (SELECT 1
FROM OFFENDER_BENEFICIARIES OB
WHERE OB.BENEFICIARY_ID = BT.BENEFICIARY_ID
AND OB.OFFENDER_ID IN (:offenderIds))
}

OD_DELETE_OFFENDER_BENEFICIARIES {
DELETE FROM OFFENDER_BENEFICIARIES WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_ADJUSTMENT_TXNS {
DELETE FROM OFFENDER_ADJUSTMENT_TXNS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_DEDUCTION_RECEIPTS {
DELETE FROM OFFENDER_DEDUCTION_RECEIPTS ODR
WHERE EXISTS (SELECT 1
FROM OFFENDER_DEDUCTIONS OD
WHERE OD.OFFENDER_DEDUCTION_ID = ODR.OFFENDER_DEDUCTION_ID
AND OD.OFFENDER_ID IN (:offenderIds))
}

OD_DELETE_OFFENDER_DEDUCTIONS {
DELETE FROM OFFENDER_DEDUCTIONS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_SUB_ACCOUNTS {
DELETE FROM OFFENDER_SUB_ACCOUNTS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_TRUST_ACCOUNTS {
DELETE FROM OFFENDER_TRUST_ACCOUNTS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_PAYMENT_PROFILES {
DELETE FROM OFFENDER_PAYMENT_PROFILES WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_BANK_CHEQUE_BENEFICIARIES {
DELETE FROM BANK_CHEQUE_BENEFICIARIES WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_DAMAGE_OBLIGATIONS {
DELETE FROM OFFENDER_DAMAGE_OBLIGATIONS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_FREEZE_DISBURSEMENTS {
DELETE FROM OFFENDER_FREEZE_DISBURSEMENTS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_IDENTIFIERS {
DELETE FROM OFFENDER_IDENTIFIERS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_MINIMUM_BALANCES {
DELETE FROM OFFENDER_MINIMUM_BALANCES WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_SYSTEM_REPORT_REQUESTS {
DELETE FROM SYSTEM_REPORT_REQUESTS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_MERGE_TRANSACTION_LOGS {
DELETE FROM MERGE_TRANSACTION_LOGS MTL
WHERE EXISTS (SELECT 1
FROM MERGE_TRANSACTIONS MT
WHERE MT.MERGE_TRANSACTION_ID = MTL.MERGE_TRANSACTION_ID
AND (MT.OFFENDER_ID_1 IN (:offenderIds) OR MT.OFFENDER_ID_2 IN (:offenderIds)))
}

OD_DELETE_MERGE_TRANSACTIONS {
DELETE FROM MERGE_TRANSACTIONS WHERE OFFENDER_ID_1 IN (:offenderIds) OR OFFENDER_ID_2 IN (:offenderIds)
}

OD_DELETE_LOCKED_MODULES {
DELETE FROM LOCKED_MODULES WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER_BOOKINGS {
DELETE FROM OFFENDER_BOOKINGS WHERE OFFENDER_ID IN (:offenderIds)
}

OD_DELETE_OFFENDER {
DELETE FROM OFFENDERS WHERE OFFENDER_ID IN (:offenderIds) OR ALIAS_OFFENDER_ID IN (:offenderIds)
}
