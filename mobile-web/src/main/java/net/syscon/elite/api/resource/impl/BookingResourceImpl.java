package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.BookingResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.*;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;
import static net.syscon.util.ResourceUtils.nvl;

/**
 * Implementation of Booking (/bookings) endpoint.
 */
@RestResource
@Path("/bookings")
public class BookingResourceImpl implements BookingResource {
    private final BookingService bookingService;
    private final InmateService inmateService;
    private final CaseNoteService caseNoteService;
    private final InmatesAlertService inmateAlertService;
    private final FinanceService financeService;
    private final ContactService contactService;
    private final AdjudicationService adjudicationService;

    public BookingResourceImpl(BookingService bookingService, InmateService inmateService,
            CaseNoteService caseNoteService, InmatesAlertService inmateAlertService, FinanceService financeService,
            ContactService contactService, AdjudicationService adjudicationService) {
        this.bookingService = bookingService;
        this.inmateService = inmateService;
        this.caseNoteService = caseNoteService;
        this.inmateAlertService = inmateAlertService;
        this.financeService = financeService;
        this.contactService = contactService;
        this.adjudicationService = adjudicationService;
    }

    @Override
    public GetOffenderBookingsResponse getOffenderBookings(String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<OffenderBooking> allInmates = inmateService.findAllInmates(
                query,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return GetOffenderBookingsResponse.respond200WithApplicationJson(allInmates);
    }

    @Override
    public GetOffenderBookingResponse getOffenderBooking(Long bookingId) {
        InmateDetail inmate = inmateService.findInmate(bookingId);

        return GetOffenderBookingResponse.respond200WithApplicationJson(inmate);
    }

    @Override
    public GetBookingActivitiesResponse getBookingActivities(Long bookingId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder, String fromDate, String toDate) {
        Page<ScheduledEvent> activities =  bookingService.getBookingActivities(
                bookingId,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate),
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return GetBookingActivitiesResponse.respond200WithApplicationJson(activities);
    }

    @Override
    public GetBookingActivitiesForTodayResponse getBookingActivitiesForToday(Long bookingId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        LocalDate today = LocalDate.now();

        Page<ScheduledEvent> activities =  bookingService.getBookingActivities(
                bookingId,
                today,
                today,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return GetBookingActivitiesForTodayResponse.respond200WithApplicationJson(activities);
    }

    @Override
    public GetOffenderAlertsResponse getOffenderAlerts(Long bookingId, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<Alert> inmateAlerts = inmateAlertService.getInmateAlerts(
                bookingId,
                query,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetOffenderAlertsResponse.respond200WithApplicationJson(inmateAlerts);
    }

    @Override
    public GetOffenderAlertResponse getOffenderAlert(Long bookingId, Long alertId) {
        Alert inmateAlert = inmateAlertService.getInmateAlert(bookingId, alertId);

        return GetOffenderAlertResponse.respond200WithApplicationJson(inmateAlert);
    }

    @Override
    public GetOffenderAliasesResponse getOffenderAliases(Long bookingId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<Alias> aliases = inmateService.findInmateAliases(
                bookingId,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 5L));

        return GetOffenderAliasesResponse.respond200WithApplicationJson(aliases);
    }

    @Override
    public GetAssessmentByCodeResponse getAssessmentByCode(Long bookingId, String assessmentCode) {
        Optional<Assessment> inmateAssessmentByCode = inmateService.getInmateAssessmentByCode(bookingId, assessmentCode);

        if (!inmateAssessmentByCode.isPresent()) {
            throw EntityNotFoundException.withMessage("Offender does not have a [" + assessmentCode + "] assessment on record.");
        }

        return GetAssessmentByCodeResponse.respond200WithApplicationJson(inmateAssessmentByCode.get());
    }

    @Override
    public GetBookingsBookingIdCaseNotesResponse getBookingsBookingIdCaseNotes(Long bookingId, String from, String to,
            String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<CaseNote> caseNotes = caseNoteService.getCaseNotes(
                bookingId,
                query,
                fromISO8601DateString(from),
                fromISO8601DateString(to),
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetBookingsBookingIdCaseNotesResponse.respond200WithApplicationJson(caseNotes);
    }

    @Override
    public GetBookingsBookingIdCaseNotesCaseNoteIdResponse getBookingsBookingIdCaseNotesCaseNoteId(Long bookingId, Long caseNoteId) {
        CaseNote caseNote = caseNoteService.getCaseNote(bookingId, caseNoteId);

        return GetBookingsBookingIdCaseNotesCaseNoteIdResponse.respond200WithApplicationJson(caseNote);
    }

    @Override
    public GetBookingIEPSummaryResponse getBookingIEPSummary(Long bookingId, boolean withDetails) {
        PrivilegeSummary privilegeSummary = bookingService.getBookingIEPSummary(bookingId, withDetails);

        return GetBookingIEPSummaryResponse.respond200WithApplicationJson(privilegeSummary);
    }

    @Override
    public GetBookingSentenceDetailResponse getBookingSentenceDetail(Long bookingId) {
        SentenceDetail sentenceDetail = bookingService.getBookingSentenceDetail(bookingId);

        return GetBookingSentenceDetailResponse.respond200WithApplicationJson(sentenceDetail);
    }

    @Override
    public PostBookingsBookingIdCaseNotesResponse postBookingsBookingIdCaseNotes(Long bookingId, NewCaseNote body) {
        CaseNote caseNote = caseNoteService.createCaseNote(bookingId, body);

        return PostBookingsBookingIdCaseNotesResponse.respond201WithApplicationJson(caseNote);
    }

    @Override
    public PutBookingsBookingIdCaseNotesCaseNoteIdResponse putBookingsBookingIdCaseNotesCaseNoteId(Long bookingId, Long caseNoteId, UpdateCaseNote body) {
        CaseNote caseNote = caseNoteService.updateCaseNote(bookingId, caseNoteId, body.getText());

        return PutBookingsBookingIdCaseNotesCaseNoteIdResponse.respond201WithApplicationJson(caseNote);
    }

    @Override
    public GetBalancesResponse getBalances(Long bookingId) {
        Account account = financeService.getBalances(bookingId);

        return GetBalancesResponse.respond200WithApplicationJson(account);
    }

    @Override
    public GetMainOffenceResponse getMainOffence(Long bookingId) {
        List<OffenceDetail> offenceDetails = bookingService.getMainOffenceDetails(bookingId);

        return GetMainOffenceResponse.respond200WithApplicationJson(offenceDetails);
    }

    @Override
    public GetContactsResponse getContacts(Long bookingId) {
        final ContactDetail contacts = contactService.getContacts(bookingId);

        return GetContactsResponse.respond200WithApplicationJson(contacts);
    }

    @Override
    public GetCaseNoteCountResponse getCaseNoteCount(Long bookingId, String type, String subType, String fromDate, String toDate) {
        CaseNoteCount caseNoteCount = caseNoteService.getCaseNoteCount(
                bookingId,
                type,
                subType,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate));

        return GetCaseNoteCountResponse.respond200WithApplicationJson(caseNoteCount);
    }

    @Override
    public GetAdjudicationsResponse getAdjudications(Long bookingId, String fromDate) {
        final AdjudicationDetail adjudicationDetail = adjudicationService.getAdjudications(bookingId, fromISO8601DateString(fromDate));

        return GetAdjudicationsResponse.respond200WithApplicationJson(adjudicationDetail);
    }

    @Override
    public GetBookingVisitsResponse getBookingVisits(Long bookingId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder, String fromDate, String toDate) {
        Page<ScheduledEvent> visits =  bookingService.getBookingVisits(
                bookingId,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate),
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return GetBookingVisitsResponse.respond200WithApplicationJson(visits);
    }

    @Override
    public GetBookingVisitsForTodayResponse getBookingVisitsForToday(Long bookingId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        LocalDate today = LocalDate.now();

        Page<ScheduledEvent> visits =  bookingService.getBookingVisits(
                bookingId,
                today,
                today,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return GetBookingVisitsForTodayResponse.respond200WithApplicationJson(visits);
    }
}
