package net.syscon.prison.repository;

import net.syscon.prison.api.model.CaseNote;
import net.syscon.prison.api.model.CaseNoteEvent;
import net.syscon.prison.api.model.CaseNoteStaffUsage;
import net.syscon.prison.api.model.CaseNoteUsage;
import net.syscon.prison.api.model.CaseNoteUsageByBookingId;
import net.syscon.prison.api.model.NewCaseNote;
import net.syscon.prison.api.model.ReferenceCode;
import net.syscon.prison.api.support.Order;
import net.syscon.prison.api.support.Page;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CaseNoteRepository {

    Page<CaseNote> getCaseNotes(long bookingId, String query, LocalDate from, LocalDate to, String orderBy, Order order, long offset, long limit);

    Optional<CaseNote> getCaseNote(long bookingId, long caseNoteId);

    Long createCaseNote(long bookingId, NewCaseNote caseNote, String sourceCode, String username, Long staffId);

    void updateCaseNote(long bookingId, long caseNoteId, @Size(max = 4000, message = "{caseNoteTextTooLong}") String updatedText, String userId);

    Long getCaseNoteCount(long bookingId, String type, String subType, LocalDate fromDate, LocalDate toDate);

    List<ReferenceCode> getCaseNoteTypesByCaseLoadType(String caseLoadType);

    List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(String caseLoadType);

    List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes();

    List<CaseNoteUsage> getCaseNoteUsage(@NotNull LocalDate fromDate, @NotNull LocalDate toDate, String agencyId, List<String> offenderNos, Integer staffId, String type, String subType);

    List<CaseNoteStaffUsage> getCaseNoteStaffUsage(String type, String subType, List<Integer> staffIds, LocalDate fromDate, LocalDate toDate);

    List<CaseNoteUsageByBookingId> getCaseNoteUsageByBookingId(String type, String subType, List<Integer> bookingIds, LocalDate fromDateToUse, LocalDate toDateToUse);

    List<CaseNoteEvent> getCaseNoteEvents(LocalDateTime fromDate, final Set<String> events, final long limit);
}