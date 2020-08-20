package uk.gov.justice.hmpps.prison.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

@Service
@Transactional(readOnly = true)
public class FinanceService {
    private final FinanceRepository financeRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public FinanceService(final FinanceRepository financeRepository, final BookingRepository bookingRepository) {
        this.financeRepository = financeRepository;
        this.bookingRepository = bookingRepository;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Account getBalances(final Long bookingId) {
        return bookingRepository.getBookingAgency(bookingId)
                .map(agency -> financeRepository.getBalances(bookingId, agency))
                .orElse(null);
    }

    @Transactional
    public Transaction transferToSavings(final String prisonId, final String offenderNo, final TransferTransaction transferTransaction) {
        financeRepository.insertIntoOffenderTrans("CR");
        financeRepository.insertIntoOffenderTrans("DR");
        final var id = financeRepository.processGlTransNew();
        return Transaction.builder().id(id).build();
    }
}
