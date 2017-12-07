package net.syscon.elite.executablespecification.steps;

import static org.assertj.core.api.Assertions.assertThat;

import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BDD step implementations for Booking Appointments feature.
 */
public class BookingAppointmentSteps extends ScheduledEventSteps {
    private static final String BOOKING_APPOINTMENTS_API_URL = API_PREFIX + "bookings/{bookingId}/appointments";

    @Override
    protected String getResourcePath() {
        return BOOKING_APPOINTMENTS_API_URL;
    }

    @Step("Get appointments for booking")
    public void getBookingAppointments(Long bookingId, String fromDate, String toDate, String sortFields, Order sortOrder) {
        dispatchRequest(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Step("Get appointments for booking for current day only")
    public void getBookingAppointmentsForCurrentDay(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.TODAY);
    }

    @Step("Get appointments for booking for 7 days ahead starting from current day")
    public void getBookingAppointmentsForThisWeek(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.THISWEEK);
    }

    @Step("Get appointments for booking for 7 days ahead starting from a week from current day")
    public void getBookingAppointmentsForNextWeek(Long bookingId) {
        dispatchRequestForPeriod(bookingId, ScheduledEventPeriod.NEXTWEEK);
    }

    @Step("Create appointment")
    public void createAppointment(Long bookingId, String eventType, LocalDateTime startDateTime, Long locationId) {
        dispatchCreateRequest(bookingId, NewAppointment.builder()
                .appointmentType(eventType)
                .startTime(startDateTime)
                .locationId(locationId)
                .build());
    }

    private void dispatchCreateRequest(Long bookingId, NewAppointment newAppointment) {
        init();
        try {
            ResponseEntity<ScheduledEvent> response = restTemplate.exchange(BOOKING_APPOINTMENTS_API_URL,
                    HttpMethod.POST, createEntity(newAppointment), ScheduledEvent.class, bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            scheduledEvent = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Verify created appointment")
    public void verifyCreatedAppointment() {
        assertThat(scheduledEvent).isNotNull();
        assertThat(scheduledEvent.getBookingId()).isEqualTo(-4L);
        assertThat(scheduledEvent.getEventDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(scheduledEvent.getEventLocation()).isEqualTo("Visiting Room");
        assertThat(scheduledEvent.getEventType()).isEqualTo("APP");
        assertThat(scheduledEvent.getEventSubType()).isEqualTo("MEDE");
        assertThat(scheduledEvent.getEventStatus()).isEqualTo("SCH");
        assertThat(scheduledEvent.getEventClass()).isEqualTo("INT_MOV");
        assertThat(scheduledEvent.getEventSource()).isEqualTo("APP");
        assertThat(scheduledEvent.getEventSourceCode()).isEqualTo("APP");
        assertThat(scheduledEvent.getStartTime().toString().substring(11)).isEqualTo("16:00");
        assertThat(scheduledEvent.getEndTime()).isNull();
    }
}
