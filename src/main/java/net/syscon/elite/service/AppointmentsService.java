package net.syscon.elite.service;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.model.bulkappointments.AppointmentDefaults;
import net.syscon.elite.api.model.bulkappointments.AppointmentDetails;
import net.syscon.elite.api.model.bulkappointments.AppointmentsToCreate;
import net.syscon.elite.api.model.bulkappointments.Repeat;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.jpa.repository.ScheduledAppointmentRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.support.ReferenceDomain;
import net.syscon.util.CalcDateRanges;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.syscon.elite.security.AuthenticationFacade.hasRoles;

@Service
@Validated
@Transactional(readOnly = true)
public class AppointmentsService {

    // Maximum of 1000 values in an Oracle 'IN' clause is current hard limit. (See #validateBookingIds below).
    private static final int MAXIMUM_NUMBER_OF_APPOINTMENTS = 1000;
    private static final int APPOINTMENT_TIME_LIMIT_IN_DAYS = 365;

    private final BookingRepository bookingRepository;
    private final AuthenticationFacade authenticationFacade;
    private final LocationService locationService;
    private final ReferenceDomainService referenceDomainService;
    private final TelemetryClient telemetryClient;
    private final ScheduledAppointmentRepository scheduledAppointmentRepository;

    public AppointmentsService(
            final BookingRepository bookingRepository,
            final AuthenticationFacade authenticationFacade,
            final LocationService locationService,
            final ReferenceDomainService referenceDomainService,
            final TelemetryClient telemetryClient,
            final ScheduledAppointmentRepository scheduledAppointmentRepository) {
        this.bookingRepository = bookingRepository;
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.referenceDomainService = referenceDomainService;
        this.telemetryClient = telemetryClient;
        this.scheduledAppointmentRepository = scheduledAppointmentRepository;
    }

    /**
     * Create multiple appointments (ScheduledEvents?).
     * This implementation creates each appointment using BookingService#createBookingAppointment.
     *
     * @param appointments Details of the new appointments to be created.
     */
    @PreAuthorize("#oauth2.hasScope('write')")
    @Transactional
    public void createAppointments(@NotNull @Valid final AppointmentsToCreate appointments) {

        assertThatRequestHasPermission(appointments);
        assertFewerThanMaximumNumberOfBookingIds(appointments);

        final var defaults = appointments.getAppointmentDefaults();

        final var agencyId = findLocationInUserLocations(defaults.getLocationId())
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Location does not exist or is not in your caseload."))
                .getAgencyId();

        assertValidAppointmentType(defaults.getAppointmentType());
        assertAllBookingIdsInCaseload(appointments.getAppointments(), agencyId);

        final var flattenedDetails = appointments.withDefaults();

        assertAdditionalAppointmentConstraints(flattenedDetails);

        final var withRepeats = withRepeats(appointments.getRepeat(), flattenedDetails);

        assertThatAppointmentsFallWithin(withRepeats, appointmentTimeLimit());
        createAppointments(withRepeats, defaults, agencyId);
    }

    @Transactional
    @VerifyBookingAccess(overrideRoles = "GLOBAL_APPOINTMENT")
    public ScheduledEvent createBookingAppointment(final Long bookingId, final String username, @Valid final NewAppointment newAppointment) {
        validateStartTime(newAppointment);
        validateEndTime(newAppointment);
        validateEventType(newAppointment);

        final var agencyId = validateLocationAndGetAgency(username, newAppointment);
        final var eventId = bookingRepository.createBookingAppointment(bookingId, newAppointment, agencyId);

        trackSingleAppointmentCreation(username, newAppointment);

        return bookingRepository.getBookingAppointment(bookingId, eventId);
    }

    public List<ScheduledAppointmentDto> getAppointments(final String agencyId, final LocalDate date, final Long locationId, final TimeSlot timeSlot) {
        final var appointmentStream = locationId != null ?
                scheduledAppointmentRepository.findByAgencyIdAndEventDateAndLocationId(agencyId, date, locationId).stream() :
                scheduledAppointmentRepository.findByAgencyIdAndEventDate(agencyId, date).stream();

        final var appointmentDtos =  appointmentStream
                .map(scheduledAppointment ->
                        ScheduledAppointmentDto
                                .builder()
                                .id(scheduledAppointment.getEventId())
                                .offenderNo(scheduledAppointment.getOffenderNo())
                                .firstName(scheduledAppointment.getFirstName())
                                .lastName(scheduledAppointment.getLastName())
                                .date(scheduledAppointment.getEventDate())
                                .startTime(scheduledAppointment.getStartTime())
                                .endTime(scheduledAppointment.getEndTime())
                                .appointmentTypeDescription(scheduledAppointment.getAppointmentTypeDescription())
                                .appointmentTypeCode(scheduledAppointment.getAppointmentTypeCode())
                                .locationDescription(scheduledAppointment.getLocationDescription())
                                .locationId(scheduledAppointment.getLocationId())
                                .auditUserId(scheduledAppointment.getAuditUserId())
                                .agencyId(scheduledAppointment.getAgencyId())
                                .build()
                );

        return timeSlot != null ?
                appointmentDtos.filter(appointment -> CalcDateRanges.eventStartsInTimeslot(appointment.getStartTime(), timeSlot)).collect(Collectors.toList()) :
                appointmentDtos.collect(Collectors.toList());
    }


    private void validateStartTime(final NewAppointment newAppointment) {
        if (newAppointment.getStartTime().isBefore(LocalDateTime.now())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Appointment time is in the past.");
        }
    }

    private void validateEndTime(final NewAppointment newAppointment) {
        if (newAppointment.getEndTime() != null
                && newAppointment.getEndTime().isBefore(newAppointment.getStartTime())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Appointment end time is before the start time.");
        }
    }

    private void validateEventType(final NewAppointment newAppointment) {
        Optional<ReferenceCode> result;

        try {
            result = referenceDomainService.getReferenceCodeByDomainAndCode(
                    ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(), false);
        } catch (final EntityNotFoundException ex) {
            result = Optional.empty();
        }

        if (result.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Event type not recognised.");
        }
    }

    private String validateLocationAndGetAgency(final String username, final NewAppointment newAppointment) {

        try {
            final var appointmentLocation = locationService.getLocation(newAppointment.getLocationId());
            final var skipLocationAgencyCheck = authenticationFacade.isOverrideRole("SYSTEM_USER", "GLOBAL_APPOINTMENT");

            if (skipLocationAgencyCheck) return appointmentLocation.getAgencyId();

            final var userLocations = locationService.getUserLocations(username);
            final var isValidLocation = userLocations.stream()
                    .anyMatch(loc -> loc.getAgencyId().equals(appointmentLocation.getAgencyId()));

            if (isValidLocation) return appointmentLocation.getAgencyId();

        } catch (final EntityNotFoundException ignored) {
        }

        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Location does not exist or is not in your caseload.");
    }


    private void assertThatRequestHasPermission(final AppointmentsToCreate appointments) {
        if (appointments.moreThanOneOffender() && !hasRoles("BULK_APPOINTMENTS")) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You do not have the 'BULK_APPOINTMENTS' role. Creating appointments for more than one offender is not permitted without this role.");
        }
    }

    private void assertThatAppointmentsFallWithin(final List<AppointmentDetails> appointments, final LocalDateTime limit) {
        for (final var appointment : appointments) {
            assertThatAppointmentFallsWithin(appointment, limit);
        }
    }

    private void assertThatAppointmentFallsWithin(final AppointmentDetails appointment, final LocalDateTime limit) {
        if (appointment.getStartTime().isAfter(limit)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "An appointment startTime is later than the limit of " + limit);
        }
        if (appointment.getEndTime() == null) return;
        if (appointment.getEndTime().isAfter(limit)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "An appointment endTime is later than the limit of " + limit);
        }
    }

    private static LocalDateTime appointmentTimeLimit() {
        return LocalDateTime.now().plusDays(APPOINTMENT_TIME_LIMIT_IN_DAYS);
    }

    private void assertFewerThanMaximumNumberOfBookingIds(final AppointmentsToCreate appointments) {
        final var numberOfAppointments = appointments.getAppointments().size();

        if (numberOfAppointments > MAXIMUM_NUMBER_OF_APPOINTMENTS) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Request to create " + numberOfAppointments + " appointments exceeds limit of " + MAXIMUM_NUMBER_OF_APPOINTMENTS);
        }
    }

    private void assertAllBookingIdsInCaseload(final List<AppointmentDetails> appointments, final String agencyId) {
        final var bookingIds = appointments.stream().map(AppointmentDetails::getBookingId).collect(Collectors.toList());
        final var bookingIdsInAgency = bookingRepository.findBookingsIdsInAgency(bookingIds, agencyId);
        if (bookingIdsInAgency.size() < bookingIds.size()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "A BookingId does not exist in your caseload");
        }
    }

    private void assertAdditionalAppointmentConstraints(final List<AppointmentDetails> appointments) {
        appointments.forEach(AppointmentsService::assertStartTimePrecedesEndTime);
    }

    private static void assertStartTimePrecedesEndTime(final AppointmentDetails appointment) {
        if (appointment.getEndTime() != null
                && appointment.getEndTime().isBefore(appointment.getStartTime())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Appointment end time is before the start time.");
        }
    }

    private void assertValidAppointmentType(final String appointmentType) {
        findEventType(appointmentType).orElseThrow(() -> new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Event type not recognised."));
    }

    private Optional<ReferenceCode> findEventType(final String appointmentType) {
        return referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(),
                appointmentType,
                false);
    }

    private Optional<Location> findLocationInUserLocations(final long locationId) {

        final var appointmentLocation = locationService.getLocation(locationId);
        final var userLocations = locationService.getUserLocations(authenticationFacade.getCurrentUsername());

        for (final var location : userLocations) {
            if (location.getAgencyId().equals(appointmentLocation.getAgencyId())) {
                return Optional.of(location);
            }
        }
        return Optional.empty();
    }

    private void trackAppointmentsCreated(final List<AppointmentDetails> appointments, final AppointmentDefaults defaults) {
        if (appointments.size() < 1) return;

        final Map<String, String> logMap = new HashMap<>();
        logMap.put("type", defaults.getAppointmentType());
        logMap.put("defaultStart", defaults.getStartTime().toString());
        logMap.put("location", defaults.getLocationId().toString());
        logMap.put("user", authenticationFacade.getCurrentUsername());
        if (defaults.getEndTime() != null) {
            logMap.put("defaultEnd", defaults.getEndTime().toString());
        }
        logMap.put("count", Integer.toString(appointments.size()));

        telemetryClient.trackEvent("AppointmentsCreated", logMap, null);
    }

    private void trackSingleAppointmentCreation(String username, @Valid NewAppointment newAppointment) {
        final Map<String, String> logMap = new HashMap<>();
        logMap.put("type", newAppointment.getAppointmentType());
        logMap.put("start", newAppointment.getStartTime().toString());
        logMap.put("location", newAppointment.getLocationId().toString());
        logMap.put("user", username);
        if (newAppointment.getEndTime() != null) {
            logMap.put("end", newAppointment.getEndTime().toString());
        }
        telemetryClient.trackEvent("AppointmentCreated", logMap, null);
    }

    public static List<AppointmentDetails> withRepeats(final Repeat repeat, final List<AppointmentDetails> details) {
        if (repeat == null) return details;
        return details.stream()
                .flatMap(d -> withRepeats(repeat, d))
                .collect(Collectors.toList());
    }

    public static Stream<AppointmentDetails> withRepeats(final Repeat repeat, final AppointmentDetails details) {
        final var appointmentDuration = Optional
                .ofNullable(details.getEndTime())
                .map(endTime -> Duration.between(details.getStartTime(), endTime));

        return repeat
                .dateTimeStream(details.getStartTime())
                .map(startTime -> buildFromPrototypeWithStartTimeAndDuration(details, startTime, appointmentDuration));
    }

    private static AppointmentDetails buildFromPrototypeWithStartTimeAndDuration(final AppointmentDetails prototype,
                                                                                 final LocalDateTime startTime,
                                                                                 final Optional<Duration> appointmentDuration) {
        final var builder = prototype.toBuilder().startTime(startTime);
        appointmentDuration.ifPresent(d -> builder.endTime(startTime.plus(d)));
        return builder.build();
    }

    private void createAppointments(final List<AppointmentDetails> details, final AppointmentDefaults defaults, final String agencyId) {
        bookingRepository.createMultipleAppointments(details, defaults, agencyId);
        trackAppointmentsCreated(details, defaults);
    }

}
