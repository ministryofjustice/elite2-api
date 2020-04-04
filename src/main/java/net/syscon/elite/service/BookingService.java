package net.syscon.elite.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.BookingActivity;
import net.syscon.elite.api.model.CourtCase;
import net.syscon.elite.api.model.IepLevelAndComment;
import net.syscon.elite.api.model.MilitaryRecord;
import net.syscon.elite.api.model.MilitaryRecords;
import net.syscon.elite.api.model.Offence;
import net.syscon.elite.api.model.OffenceDetail;
import net.syscon.elite.api.model.OffenceHistoryDetail;
import net.syscon.elite.api.model.OffenderSentenceCalculation;
import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.model.OffenderSentenceDetailDto;
import net.syscon.elite.api.model.OffenderSentenceTerms;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.PrivilegeDetail;
import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.api.model.UpdateAttendance;
import net.syscon.elite.api.model.Visit;
import net.syscon.elite.api.model.VisitBalances;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.HasWriteScope;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.SentenceRepository;
import net.syscon.elite.repository.jpa.model.AgencyInternalLocation;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.model.ReferenceCode;
import net.syscon.elite.repository.jpa.repository.AgencyInternalLocationRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.support.LocationProcessor;
import net.syscon.elite.service.support.NonDtoReleaseDate;
import net.syscon.elite.service.transformers.CourtCaseTransformer;
import net.syscon.elite.service.validation.AttendanceTypesValid;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;
import static net.syscon.elite.service.ContactService.EXTERNAL_REL;

/**
 * Bookings API service interface.
 */
@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class BookingService {

    private static final String AGENCY_LOCATION_ID_KEY = "agencyLocationId";
    private static final String IEP_LEVEL_DOMAIN = "IEP_LEVEL";

    private final Comparator<ScheduledEvent> startTimeComparator = Comparator.comparing(ScheduledEvent::getStartTime, nullsLast(naturalOrder()));

    private final BookingRepository bookingRepository;
    private final OffenderBookingRepository offenderBookingRepository;
    private final SentenceRepository sentenceRepository;
    private final AgencyService agencyService;
    private final CaseLoadService caseLoadService;
    private final ReferenceDomainService referenceDomainService;
    private final CaseloadToAgencyMappingService caseloadToAgencyMappingService;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final AuthenticationFacade securityUtils;
    private final AuthenticationFacade authenticationFacade;
    private final String defaultIepLevel;
    private final int maxBatchSize;

    public BookingService(final BookingRepository bookingRepository,
                          final OffenderBookingRepository offenderBookingRepository,
                          final SentenceRepository sentenceRepository,
                          final AgencyService agencyService,
                          final CaseLoadService caseLoadService,
                          final ReferenceDomainService referenceDomainService,
                          final CaseloadToAgencyMappingService caseloadToAgencyMappingService,
                          final AgencyInternalLocationRepository agencyInternalLocationRepository,
                          final AuthenticationFacade securityUtils,
                          final AuthenticationFacade authenticationFacade,
                          @Value("${api.bookings.iepLevel.default:Unknown}") final String defaultIepLevel,
                          @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.bookingRepository = bookingRepository;
        this.offenderBookingRepository = offenderBookingRepository;
        this.sentenceRepository = sentenceRepository;
        this.agencyService = agencyService;
        this.caseLoadService = caseLoadService;
        this.referenceDomainService = referenceDomainService;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.agencyInternalLocationRepository = agencyInternalLocationRepository;
        this.securityUtils = securityUtils;
        this.authenticationFacade = authenticationFacade;
        this.defaultIepLevel = defaultIepLevel;
        this.maxBatchSize = maxBatchSize;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public SentenceDetail getBookingSentenceDetail(final Long bookingId) {

        final var sentenceDetail = getSentenceDetail(bookingId);

        final var confirmedReleaseDate = sentenceRepository.getConfirmedReleaseDate(bookingId);
        sentenceDetail.setConfirmedReleaseDate(confirmedReleaseDate.orElse(null));

        return deriveSentenceDetail(sentenceDetail);
    }

    private SentenceDetail getSentenceDetail(final Long bookingId) {
        final var optSentenceDetail = bookingRepository.getBookingSentenceDetail(bookingId);

        return optSentenceDetail.orElse(emptySentenceDetail(bookingId));
    }

    private SentenceDetail emptySentenceDetail(final Long bookingId) {
        return SentenceDetail.sentenceDetailBuilder().bookingId(bookingId).build();
    }

    private SentenceDetail deriveSentenceDetail(final SentenceDetail sentenceDetail) {

        // Determine non-DTO release date
        final var nonDtoReleaseDate = deriveNonDtoReleaseDate(sentenceDetail);

        if (Objects.nonNull(nonDtoReleaseDate)) {
            sentenceDetail.setNonDtoReleaseDate(nonDtoReleaseDate.getReleaseDate());
            sentenceDetail.setNonDtoReleaseDateType(nonDtoReleaseDate.getReleaseDateType());
        }

        // Determine offender release date
        final var releaseDate = deriveOffenderReleaseDate(sentenceDetail);

        sentenceDetail.setReleaseDate(releaseDate);

        return sentenceDetail;
    }

    public PrivilegeSummary getBookingIEPSummary(final Long bookingId, final boolean withDetails) {
        final var bookingIEPSummary = getBookingIEPSummary(Collections.singletonList(bookingId), withDetails);
        final var privilegeSummary = bookingIEPSummary.get(bookingId);
        if (privilegeSummary == null) {
            throw EntityNotFoundException.withId(bookingId);
        }
        return privilegeSummary;
    }

    @VerifyBookingAccess
    @Transactional
    public void addIepLevel(final Long bookingId, final String username, @Valid final IepLevelAndComment iepLevel) {

        if (!referenceDomainService.isReferenceCodeActive(IEP_LEVEL_DOMAIN, iepLevel.getIepLevel())) {
            throw new IllegalArgumentException(format("IEP Level '%1$s' is not a valid NOMIS value.", iepLevel.getIepLevel()));
        }

        if (!activeIepLevelForAgencySelectedByBooking(bookingId, iepLevel.getIepLevel())) {
            throw new IllegalArgumentException(format("IEP Level '%1$s' is not active for this booking's agency: Booking Id %2$d.", iepLevel.getIepLevel(), bookingId));
        }

        bookingRepository.addIepLevel(bookingId, username, iepLevel);
    }

    private boolean activeIepLevelForAgencySelectedByBooking(final long bookingId, final String iepLevel) {
        final var iepLevels = bookingRepository.getIepLevelsForAgencySelectedByBooking(bookingId);
        return iepLevels.contains(iepLevel);
    }

    public Map<Long, PrivilegeSummary> getBookingIEPSummary(final List<Long> bookingIds, final boolean withDetails) {
        if (withDetails || !isViewAllBookings()) {
            bookingIds.forEach(this::verifyBookingAccess);
        }
        final Map<Long, PrivilegeSummary> mapOfEip = new HashMap<>();

        final var bookingIdBatches = Lists.partition(bookingIds, maxBatchSize);
        bookingIdBatches.forEach(bookingIdBatch -> {
            final var mapOfIEPResults = bookingRepository.getBookingIEPDetailsByBookingIds(bookingIdBatch);
            mapOfIEPResults.forEach((key, iepDetails) -> {

                // Extract most recent detail from list
                final var currentDetail = mostRecentDetail(iepDetails);

                // Determine number of days since current detail became effective
                final var daysSinceReview = daysSinceDetailBecameEffective(currentDetail);

                mapOfEip.put(key, PrivilegeSummary.builder()
                        .bookingId(currentDetail.getBookingId())
                        .iepDate(currentDetail.getIepDate())
                        .iepTime(currentDetail.getIepTime())
                        .iepLevel(currentDetail.getIepLevel())
                        .daysSinceReview(Long.valueOf(daysSinceReview).intValue())
                        .iepDetails(withDetails ? iepDetails : Collections.emptyList())
                        .build());
            });
        });

        // If no IEP details exist for offender, cannot derive an IEP summary.
        bookingIds.stream()
                .filter(bookingId -> !mapOfEip.containsKey(bookingId))
                .collect(toList())
                .forEach(bookingId -> mapOfEip.put(bookingId, PrivilegeSummary.builder()
                        .bookingId(bookingId)
                        .iepLevel(defaultIepLevel)
                        .iepDetails(Collections.emptyList())
                        .build()));

        return mapOfEip;
    }

    private PrivilegeDetail mostRecentDetail(final List<PrivilegeDetail> iepDetails) {
        return iepDetails.get(0);
    }

    private long daysSinceDetailBecameEffective(final PrivilegeDetail currentDetail) {
        return DAYS.between(currentDetail.getIepDate(), now());
    }

    public Map<Long, List<String>> getBookingAlertSummary(final List<Long> bookingIds, final LocalDateTime now) {
        final Map<Long, List<String>> alerts = new HashMap<>();

        if (!bookingIds.isEmpty()) {
            final var batch = Lists.partition(bookingIds, maxBatchSize);
            batch.forEach(bookingIdList -> alerts.putAll(bookingRepository.getAlertCodesForBookings(bookingIdList, now)));
        }

        return alerts;
    }

    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingActivities(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_PAY')")
    public void updateAttendance(final String offenderNo, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        updateAttendance(activityId, updateAttendance, getLatestBookingByOffenderNo(offenderNo));
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_PAY')")
    public void updateAttendance(final Long bookingId, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        updateAttendance(activityId, updateAttendance, getLatestBookingByBookingId(bookingId));
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_PAY')")
    public void updateAttendanceForMultipleBookingIds(final Set<BookingActivity> bookingActivities, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        bookingActivities.forEach(bookingActivity -> updateAttendance(bookingActivity.getActivityId(), updateAttendance, getLatestBookingByBookingId(bookingActivity.getBookingId())));
    }

    private void updateAttendance(final Long activityId, final UpdateAttendance updateAttendance, final OffenderSummary offenderSummary) {
        validateActivity(activityId);

        // Copy flags from the PAYABLE_ATTENDANCE_OUTCOME reference table
        final var activityOutcome = bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", updateAttendance.getEventOutcome());
        bookingRepository.updateAttendance(offenderSummary.getBookingId(), activityId, updateAttendance, activityOutcome.isPaid(), activityOutcome.isAuthorisedAbsence());
    }


    private void validateActivity(final Long activityId) {
        // Find details for activities for same offender and same day as this one
        final var attendanceEventDate = bookingRepository.getAttendanceEventDate(activityId);
        if (attendanceEventDate == null) {
            throw EntityNotFoundException.withMessage("Activity Id %d not found", activityId);
        }
    }

    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @VerifyBookingAccess
    public Optional<VisitBalances> getBookingVisitBalances(final Long bookingId) {
        return bookingRepository.getBookingVisitBalances(bookingId);
    }

    private List<ScheduledEvent> getBookingVisits(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Visit getBookingVisitLast(final Long bookingId) {
        return bookingRepository.getBookingVisitLast(bookingId, LocalDateTime.now());
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Visit getBookingVisitNext(final Long bookingId) {
        return bookingRepository.getBookingVisitNext(bookingId, LocalDateTime.now());
    }

    public List<OffenderSummary> getBookingsByExternalRefAndType(final String externalRef, final String relationshipType) {
        return bookingRepository.getBookingsByRelationship(externalRef, relationshipType, EXTERNAL_REL);
    }

    public List<OffenderSummary> getBookingsByPersonIdAndType(final Long personId, final String relationshipType) {
        return bookingRepository.getBookingsByRelationship(personId, relationshipType);
    }

    public void verifyCanViewSensitiveBookingInfo(final String offenderNo, final String... rolesAllowed) {
        getBookingIdByOffenderNo(offenderNo, rolesAllowed);
    }

    public Long getBookingIdByOffenderNo(final String offenderNo, final String... rolesAllowed) {
        final var bookingId = bookingRepository.getBookingIdByOffenderNo(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
        if (!isViewAllBookings()) {
            try {
                verifyBookingAccess(bookingId, rolesAllowed);
            } catch (final EntityNotFoundException e) {
                throw EntityNotFoundException.withId(offenderNo);
            }
        }
        return bookingId;
    }


    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingAppointments(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    // FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER
    public OffenderSummary getLatestBookingByBookingId(final Long bookingId) {
        return bookingRepository.getLatestBookingByBookingId(bookingId).orElse(null);
    }

    // FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER
    public OffenderSummary getLatestBookingByOffenderNo(final String offenderNo) {
        return bookingRepository.getLatestBookingByOffenderNo(offenderNo).orElse(null);
    }


    private void validateScheduledEventsRequest(final LocalDate fromDate, final LocalDate toDate) {
        // Validate date range
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid date range: toDate is before fromDate.");
        }
    }

    private NonDtoReleaseDate deriveNonDtoReleaseDate(final SentenceDetail sentenceDetail) {
        final List<NonDtoReleaseDate> nonDtoReleaseDates = new ArrayList<>();

        if (Objects.nonNull(sentenceDetail)) {
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getAutomaticReleaseDate(), SentenceDetail.NonDtoReleaseDateType.ARD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getAutomaticReleaseOverrideDate(), SentenceDetail.NonDtoReleaseDateType.ARD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getConditionalReleaseDate(), SentenceDetail.NonDtoReleaseDateType.CRD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getConditionalReleaseOverrideDate(), SentenceDetail.NonDtoReleaseDateType.CRD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getNonParoleDate(), SentenceDetail.NonDtoReleaseDateType.NPD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getNonParoleOverrideDate(), SentenceDetail.NonDtoReleaseDateType.NPD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getPostRecallReleaseDate(), SentenceDetail.NonDtoReleaseDateType.PRRD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getPostRecallReleaseOverrideDate(), SentenceDetail.NonDtoReleaseDateType.PRRD, true);

            Collections.sort(nonDtoReleaseDates);
        }

        return nonDtoReleaseDates.isEmpty() ? null : nonDtoReleaseDates.get(0);
    }

    private void addReleaseDate(final List<NonDtoReleaseDate> nonDtoReleaseDates, final LocalDate releaseDate,
                                final SentenceDetail.NonDtoReleaseDateType releaseDateType, final boolean isOverride) {

        if (Objects.nonNull(releaseDate)) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(releaseDateType, releaseDate, isOverride));
        }
    }

    private LocalDate deriveOffenderReleaseDate(final SentenceDetail sentenceDetail) {
        // Offender release date is determined according to algorithm.
        //
        // 1. If there is a confirmed release date, the offender release date is the confirmed release date.
        //
        // 2. If there is no confirmed release date for the offender, the offender release date is either the actual
        //    parole date or the home detention curfew actual date.
        //
        // 3. If there is no confirmed release date, actual parole date or home detention curfew actual date for the
        //    offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both
        //    are present).
        //
        final LocalDate releaseDate;

        if (Objects.nonNull(sentenceDetail.getConfirmedReleaseDate())) {
            releaseDate = sentenceDetail.getConfirmedReleaseDate();
        } else if (Objects.nonNull(sentenceDetail.getActualParoleDate())) {
            releaseDate = sentenceDetail.getActualParoleDate();
        } else if (Objects.nonNull(sentenceDetail.getHomeDetentionCurfewActualDate())) {
            releaseDate = sentenceDetail.getHomeDetentionCurfewActualDate();
        } else {
            final var nonDtoReleaseDate = sentenceDetail.getNonDtoReleaseDate();
            final var midTermDate = sentenceDetail.getMidTermDate();

            if (Objects.isNull(midTermDate)) {
                releaseDate = nonDtoReleaseDate;
            } else if (Objects.isNull(nonDtoReleaseDate)) {
                releaseDate = midTermDate;
            } else {
                releaseDate = midTermDate.isAfter(nonDtoReleaseDate) ? midTermDate : nonDtoReleaseDate;
            }
        }

        return releaseDate;
    }

    /**
     * Verifies that current user is authorised to access specified offender booking. If offender booking is in an
     * agency location that is not part of any caseload accessible to the current user, a 'Resource Not Found'
     * exception is thrown.
     *
     * @param bookingId offender booking id.
     * @throws EntityNotFoundException if current user does not have access to specified booking.
     */
    public void verifyBookingAccess(final Long bookingId, final String... rolesAllowed) {
        // system user has access to everything
        if (securityUtils.isOverrideRole(rolesAllowed)) return;

        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var agencyIds = agencyService.getAgencyIds();
        if (AuthenticationFacade.hasRoles("INACTIVE_BOOKINGS")) {
            agencyIds.addAll(Set.of("OUT", "TRN"));
        }
        if (agencyIds.isEmpty()) {
            throw EntityNotFoundException.withId(bookingId);
        }
        if (!bookingRepository.verifyBookingAccess(bookingId, agencyIds)) {
            throw EntityNotFoundException.withId(bookingId);
        }
    }

    public void checkBookingExists(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        if (!bookingRepository.checkBookingExists(bookingId)) {
            throw EntityNotFoundException.withId(bookingId);
        }
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<OffenceDetail> getMainOffenceDetails(final Long bookingId) {
        return sentenceRepository.getMainOffenceDetails(bookingId);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER','SYSTEM_READ_ONLY')")
    public List<Offence> getMainOffenceDetails(final Set<Long> bookingIds) {

        final List<Offence> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bookingIds)) {
            final var batch = Lists.partition(new ArrayList<>(bookingIds), maxBatchSize);
            batch.forEach(bookingBatch -> {
                final var offences = sentenceRepository.getMainOffenceDetails(bookingBatch);
                results.addAll(offences);
            });
        }
        return results;
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER','SYSTEM_READ_ONLY','CREATE_CATEGORISATION','APPROVE_CATEGORISATION')")
    public List<OffenceHistoryDetail> getOffenceHistory(final String offenderNo) {
        return sentenceRepository.getOffenceHistory(offenderNo);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<ScheduledEvent> getEventsToday(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today, today);
    }

    public List<ScheduledEvent> getEventsOnDay(final Collection<Long> bookingIds, final LocalDate day) {
        return getEvents(bookingIds, day, day);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<ScheduledEvent> getEventsThisWeek(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today, today.plusDays(6));
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<ScheduledEvent> getEventsNextWeek(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today.plusDays(7), today.plusDays(13));
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<ScheduledEvent> getEvents(final Long bookingId, final LocalDate from, final LocalDate to) {
        final var activities = getBookingActivities(bookingId, from, to, null, null);
        final var visits = getBookingVisits(bookingId, from, to, null, null);
        final var appointments = getBookingAppointments(bookingId, from, to, null, null);
        return Stream.of(activities, visits, appointments)
                .flatMap(Collection::stream)
                .sorted(startTimeComparator)
                .collect(Collectors.toList());
    }

    private List<ScheduledEvent> getEvents(final Collection<Long> bookingIds, final LocalDate from, final LocalDate to) {
        final var activities = getBookingActivities(bookingIds, from, to, null, null);
        final var visits = getBookingVisits(bookingIds, from, to, null, null);
        final var appointments = getBookingAppointments(bookingIds, from, to, null, null);

        return Stream.of(activities, visits, appointments)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<OffenderSentenceCalculation> getOffenderSentenceCalculationsForAgency(final Set<String> agencyIds) {

        final var offenderSentenceSummaryRaw = bookingRepository.getOffenderSentenceCalculations(agencyIds);

        final var identifyLatest = offenderSentenceSummaryRaw.parallelStream()
                .collect(Collectors.groupingBy(OffenderSentenceCalculation::getBookingId,
                        Collectors.maxBy(Comparator.comparing(OffenderSentenceCalculation::getOffenderSentCalculationId))));

        return identifyLatest.values().stream().filter(Optional::isPresent).map(Optional::get).collect(toList());
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<OffenderSentenceTerms> getOffenderSentenceTerms(final Long bookingId) {
        return bookingRepository.getOffenderSentenceTerms(bookingId, "IMP");
    }

    public List<OffenderSentenceDetail> getOffenderSentencesSummary(final String agencyId, final List<String> offenderNos) {

        final var offenderSentenceSummary = offenderSentenceSummaries(agencyId, offenderNos);
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    public List<OffenderSentenceDetail> getBookingSentencesSummary(final List<Long> bookingIds) {
        final var offenderSentenceSummary = bookingSentenceSummaries(bookingIds, caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false), !isViewAllBookings());
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    public Optional<OffenderSentenceDetail> getOffenderSentenceDetail(final String offenderNo) {
        final var query = format("offenderNo:eq:'%s'", offenderNo);
        return getOffenderSentenceDetails(bookingRepository.getOffenderSentenceSummary(query, getCaseLoadIdForUserIfRequired(),
                !isViewAllBookings(), isViewInactiveBookings()))
                .stream()
                .findFirst();
    }

    @VerifyBookingAccess
    public MilitaryRecords getMilitaryRecords(final Long bookingId) {
        return offenderBookingRepository.findById(bookingId).map(b ->
                new MilitaryRecords(b.getMilitaryRecords().stream().map(mr ->
                        MilitaryRecord.builder()
                                .warZoneCode(ReferenceCode.getCodeOrNull(mr.getWarZone()))
                                .warZoneDescription(ReferenceCode.getDescriptionOrNull(mr.getWarZone()))
                                .startDate(mr.getStartDate())
                                .endDate(mr.getEndDate())
                                .militaryDischargeCode(ReferenceCode.getCodeOrNull(mr.getMilitaryDischarge()))
                                .militaryDischargeDescription(ReferenceCode.getDescriptionOrNull(mr.getMilitaryDischarge()))
                                .militaryBranchCode(ReferenceCode.getCodeOrNull(mr.getMilitaryBranch()))
                                .militaryBranchDescription(ReferenceCode.getDescriptionOrNull(mr.getMilitaryBranch()))
                                .description(mr.getDescription())
                                .unitNumber(mr.getUnitNumber())
                                .enlistmentLocation(mr.getEnlistmentLocation())
                                .dischargeLocation(mr.getDischargeLocation())
                                .selectiveServicesFlag(mr.getSelectiveServicesFlag())
                                .militaryRankCode(ReferenceCode.getCodeOrNull(mr.getMilitaryRank()))
                                .militaryRankDescription(ReferenceCode.getDescriptionOrNull(mr.getMilitaryRank()))
                                .serviceNumber(mr.getServiceNumber())
                                .disciplinaryActionCode(ReferenceCode.getCodeOrNull(mr.getDisciplinaryAction()))
                                .disciplinaryActionDescription(ReferenceCode.getDescriptionOrNull(mr.getDisciplinaryAction()))
                                .build())
                        .collect(Collectors.toUnmodifiableList())
                )).orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    @VerifyBookingAccess
    public List<CourtCase> getOffenderCourtCases(final Long bookingId, final boolean activeOnly) {
        return offenderBookingRepository.findById(bookingId)
                .map(booking -> activeOnly ? booking.getActiveCourtCases() : booking.getCourtCases())
                .map(CourtCaseTransformer::transform)
                .orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    @Transactional
    @VerifyBookingAccess
    @HasWriteScope
    public void updateLivingUnit(final Long bookingId, final Long livingUnitId) {
        var offenderBooking = offenderBookingRepository.findById(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(format("Offender booking for booking id %d not found", bookingId)));
        final var location = agencyInternalLocationRepository.findById(livingUnitId)
                .orElseThrow(EntityNotFoundException.withMessage(format("Living unit with id %d not found", livingUnitId)));

        validateUpdateLivingUnit(offenderBooking, location);

        offenderBooking.setAssignedLivingUnitId(livingUnitId);
        offenderBookingRepository.save(offenderBooking);
        log.info("Updated offender {} booking id {} to living unit id {}", offenderBooking.getOffender().getNomsId(), offenderBooking.getBookingId(), livingUnitId);
    }

    private void validateUpdateLivingUnit(final OffenderBooking offenderBooking, final AgencyInternalLocation location) {
        checkArgument(
                offenderBooking.getLocation().getId().equals(location.getAgencyId()),
                "Move to living unit in prison %s invalid for offender %s in prison %s",
                location.getAgencyId(), offenderBooking.getOffender().getNomsId(), offenderBooking.getLocation().getId()
        );
        checkArgument(
                location.getLocationType().equals("CELL"),
                "Living unit %d of type %s is not a cell",
                location.getLocationId(), location.getLocationType()
        );
    }

    private Set<String> getCaseLoadIdForUserIfRequired() {
        return isViewAllBookings() ? Set.of() : caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false);
    }

    private List<OffenderSentenceDetail> getOffenderSentenceDetails(final List<OffenderSentenceDetailDto> offenderSentenceSummary) {
        final var offenderSentenceDetails = offenderSentenceSummary.stream()
                .map(this::mapper)
                .collect(toList());

        offenderSentenceDetails.forEach(s -> deriveSentenceDetail(s.getSentenceDetail()));

        final Comparator<OffenderSentenceDetail> compareDate = Comparator.comparing(
                s -> s.getSentenceDetail().getReleaseDate(),
                nullsLast(naturalOrder())
        );

        return offenderSentenceDetails.stream().sorted(compareDate).collect(toList());
    }

    private OffenderSentenceDetail mapper(final OffenderSentenceDetailDto os) {
        return OffenderSentenceDetail.offenderSentenceDetailBuilder()
                .bookingId(os.getBookingId())
                .offenderNo(os.getOffenderNo())
                .firstName(os.getFirstName())
                .lastName(os.getLastName())
                .dateOfBirth(os.getDateOfBirth())
                .agencyLocationId(os.getAgencyLocationId())
                .agencyLocationDesc(os.getAgencyLocationDesc())
                .facialImageId(os.getFacialImageId())
                .internalLocationDesc(LocationProcessor.stripAgencyId(os.getInternalLocationDesc(), os.getAgencyLocationId()))
                .sentenceDetail(SentenceDetail.sentenceDetailBuilder()
                        .bookingId(os.getBookingId())
                        .sentenceStartDate(os.getSentenceStartDate())
                        .additionalDaysAwarded(os.getAdditionalDaysAwarded())
                        .sentenceExpiryDate(os.getSentenceExpiryDate())
                        .automaticReleaseDate(os.getAutomaticReleaseDate())
                        .automaticReleaseOverrideDate(os.getAutomaticReleaseOverrideDate())
                        .conditionalReleaseDate(os.getConditionalReleaseDate())
                        .conditionalReleaseOverrideDate(os.getConditionalReleaseOverrideDate())
                        .nonParoleDate(os.getNonParoleDate())
                        .nonParoleOverrideDate(os.getNonParoleOverrideDate())
                        .postRecallReleaseDate(os.getPostRecallReleaseDate())
                        .postRecallReleaseOverrideDate(os.getPostRecallReleaseOverrideDate())
                        .nonDtoReleaseDate(os.getNonDtoReleaseDate())
                        .licenceExpiryDate(os.getLicenceExpiryDate())
                        .homeDetentionCurfewEligibilityDate(os.getHomeDetentionCurfewEligibilityDate())
                        .paroleEligibilityDate(os.getParoleEligibilityDate())
                        .homeDetentionCurfewActualDate(os.getHomeDetentionCurfewActualDate())
                        .actualParoleDate(os.getActualParoleDate())
                        .releaseOnTemporaryLicenceDate(os.getReleaseOnTemporaryLicenceDate())
                        .earlyRemovalSchemeEligibilityDate(os.getEarlyRemovalSchemeEligibilityDate())
                        .earlyTermDate(os.getEarlyTermDate())
                        .midTermDate(os.getMidTermDate())
                        .lateTermDate(os.getLateTermDate())
                        .topupSupervisionExpiryDate(os.getTopupSupervisionExpiryDate())
                        .confirmedReleaseDate(os.getConfirmedReleaseDate())
                        .releaseDate(os.getReleaseDate())
                        .tariffDate(os.getTariffDate())
                        .build())
                .build();
    }


    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final String agencyId, final List<String> offenderNos) {

        final var viewAllBookings = isViewAllBookings();
        final var caseLoadIdsForUser = getCaseLoadIdForUserIfRequired();

        if (offenderNos == null || offenderNos.isEmpty()) {
            return offenderSentenceSummaries(agencyId, caseLoadIdsForUser, !viewAllBookings);
        } else {
            return offenderSentenceSummaries(offenderNos, caseLoadIdsForUser, !viewAllBookings);
        }
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final String agencyId, final Set<String> caseloads, final boolean filterByCaseloads) {
        final var query = buildAgencyQuery(agencyId, authenticationFacade.getCurrentUsername());
        if (StringUtils.isEmpty(query) && caseloads.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Request must be restricted to either a caseload, agency or list of offenders");
        }
        return bookingRepository.getOffenderSentenceSummary(query, caseloads, filterByCaseloads, isViewInactiveBookings());
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final List<String> offenderNos, final Set<String> caseloads, final boolean filterByCaseloads) {

        return Lists
                .partition(offenderNos, maxBatchSize)
                .stream()
                .flatMap(numbers -> {
                    var query = "offenderNo:in:" + quotedAndPipeDelimited(numbers.stream());
                    return bookingRepository.getOffenderSentenceSummary(query, caseloads, filterByCaseloads, isViewInactiveBookings()).stream();
                })
                .collect(Collectors.toList());
    }

    private List<OffenderSentenceDetailDto> bookingSentenceSummaries(final List<Long> bookingIds, final Set<String> caseloads, final boolean filterByCaseloads) {

        return Lists
                .partition(bookingIds, maxBatchSize)
                .stream()
                .flatMap(numbers -> {
                    var query = "bookingId:in:" + numbers.stream().map(String::valueOf).collect(Collectors.joining("|"));
                    return bookingRepository.getOffenderSentenceSummary(query, caseloads, filterByCaseloads, isViewInactiveBookings()).stream();
                })
                .collect(Collectors.toList());
    }

    private boolean isViewAllBookings() {
        return securityUtils.isOverrideRole("SYSTEM_USER", "GLOBAL_SEARCH", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION");
    }

    private boolean isViewInactiveBookings() {
        return isOverrideRole("INACTIVE_BOOKINGS");
    }

    private boolean isOverrideRole(final String otherRole) {
        return securityUtils.isOverrideRole(otherRole, "SYSTEM_USER");
    }


    private static String quotedAndPipeDelimited(final Stream<String> values) {
        return values.collect(Collectors.joining("'|'", "'", "'"));
    }

    private String buildAgencyQuery(final String agencyId, final String username) {
        return StringUtils.isBlank(agencyId) ?
                forAgenciesInWorkingCaseload(username) :
                forAgency(agencyId);
    }

    private String forAgenciesInWorkingCaseload(final String username) {

        final var agencies = caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(username);

        return agencies.isEmpty() ? "" : AGENCY_LOCATION_ID_KEY + ":in:" +
                quotedAndPipeDelimited(
                        agencies
                                .stream()
                                .map(Agency::getAgencyId));
    }

    private static String forAgency(final String agencyId) {
        return AGENCY_LOCATION_ID_KEY + ":eq:'" + agencyId + "'";
    }
}
