package net.syscon.prison.service;


import lombok.Builder;
import lombok.Getter;
import net.syscon.prison.api.support.PageRequest;

import java.time.LocalDate;

@Getter
@Builder
public class AdjudicationSearchCriteria {

    private final String offenderNumber;
    private final String offenceId;
    private final String agencyId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final PageRequest pageRequest;
}
