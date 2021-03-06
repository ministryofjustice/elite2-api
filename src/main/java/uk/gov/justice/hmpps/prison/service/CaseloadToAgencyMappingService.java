package uk.gov.justice.hmpps.prison.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;

import java.util.Collections;
import java.util.List;

@Service
public class CaseloadToAgencyMappingService {

    private final AgencyService agencyService;
    private final CaseLoadService caseLoadService;

    public CaseloadToAgencyMappingService(final AgencyService agencyService, final CaseLoadService caseLoadService) {
        this.agencyService = agencyService;
        this.caseLoadService = caseLoadService;
    }

    public List<Agency> agenciesForUsersWorkingCaseload(final String username) {
        return caseLoadService
                .getWorkingCaseLoadForUser(username)
                .map(CaseLoad::getCaseLoadId)
                .map(agencyService::getAgenciesByCaseload)
                .orElse(Collections.emptyList());
    }
}