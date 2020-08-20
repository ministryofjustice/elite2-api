package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.Account;

public interface FinanceRepository {

    Account getBalances(long bookingId, final String agencyId);

    void insertIntoOffenderTrans(final String txType);
    String processGlTransNew();
}
