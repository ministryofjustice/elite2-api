package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.CourtEvent.CourtEventBuilder;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.EventType;
import net.syscon.elite.repository.jpa.model.OffenderCharge;
import net.syscon.elite.repository.jpa.model.OffenderCourtCase;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static net.syscon.elite.repository.jpa.model.EventStatus.COMPLETED;
import static net.syscon.elite.repository.jpa.model.EventStatus.SCHEDULED_APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
@ContextConfiguration(classes = CourtEventRepositoryTest.TestClock.class)
public class CourtEventRepositoryTest {

    @TestConfiguration
    static class TestClock {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.now(), ZoneId.systemDefault());
        }
    }

    @Autowired
    private Clock clock;

    private static final long BOOKING_WITH_COURT_CASE = -1L;

    private static final long BOOKING_WITHOUT_COURT_CASE = -31L;

    @Autowired
    private CourtEventRepository courtEventRepository;

    @Autowired
    private CourtEventChargeRepository courtEventChargeRepository;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Autowired
    private AgencyLocationRepository agencyRepository;

    @Autowired
    private ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Autowired
    private ReferenceCodeRepository<EventType> eventTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final CourtEventBuilder builder = CourtEvent.builder();

    @BeforeEach
    void setup() {
        final var eventDate = LocalDate.now(clock).plusDays(1);
        final var startTime = eventDate.atTime(12, 0);
        final var bookingWithCourtCase = offenderBookingRepository.findById(BOOKING_WITH_COURT_CASE).orElseThrow();

        builder
                .commentText("Comment text for court event")
                .courtEventType(eventTypeRepository.findById(EventType.COURT).orElseThrow())
                .courtLocation(agencyRepository.findById("COURT1").orElseThrow())
                .directionCode("OUT")
                .eventDate(eventDate)
                .eventStatus(eventStatusRepository.findById(SCHEDULED_APPROVED).orElseThrow())
                .offenderBooking(bookingWithCourtCase)
                .offenderCourtCase(bookingWithCourtCase.getCourtCases().stream().findFirst().orElseThrow())
                .startTime(startTime);
    }

    @Test
    void court_event_can_be_saved_and_retrieved_with_defaults_populated() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder.build());

        entityManager.flush();

        assertThat(courtEventRepository.findById(savedCourtEventWithCourtCase.getId()).orElseThrow()).isEqualTo(savedCourtEventWithCourtCase);

        // defaults populated
        assertThat(savedCourtEventWithCourtCase.getNextEventRequestFlag()).isEqualTo("N");
        assertThat(savedCourtEventWithCourtCase.getOrderRequestedFlag()).isEqualTo("N");
    }

    @Test
    void court_event_retrieved_by_booking_and_hearing_id() {
        final var persistedCourtEvent = courtEventRepository.save(builder.build());

        entityManager.flush();

        assertThat(courtEventRepository.findByOffenderBooking_BookingIdAndId(persistedCourtEvent.getOffenderBooking().getBookingId(), persistedCourtEvent.getId())).isNotEmpty();
    }

    @Test
    void court_event_can_be_saved_and_retrieved_with_defaults_overridden() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder
                .nextEventRequestFlag("X")
                .orderRequestedFlag("Y")
                .build());

        entityManager.flush();

        assertThat(courtEventRepository.findById(savedCourtEventWithCourtCase.getId()).orElseThrow()).isEqualTo(savedCourtEventWithCourtCase);

        // defaults overridden
        assertThat(savedCourtEventWithCourtCase.getNextEventRequestFlag()).isEqualTo("X");
        assertThat(savedCourtEventWithCourtCase.getOrderRequestedFlag()).isEqualTo("Y");
    }

    @Test
    void court_event_only_active_charges_are_carried_over_from_court_case_on_creation() {
        final var prePersistCourtEvent = builder.build();

        addInactiveChargeTo(prePersistCourtEvent.getOffenderCourtCase().get());

        assertThat(prePersistCourtEvent.getCharges()).isEmpty();
        assertThat(prePersistCourtEvent.getOffenderCourtCase().get().getCharges()).hasSize(2);

        final var savedCourtEventWithCourtCase = courtEventRepository.save(prePersistCourtEvent);

        entityManager.flush();

        final var postPersistCourtEvent = courtEventRepository.findById(savedCourtEventWithCourtCase.getId()).orElseThrow();

        assertThat(postPersistCourtEvent.getCharges()).hasSize(1);
        assertThat(postPersistCourtEvent.getCharges())
                .extracting(charge -> charge.getEventAndCharge().getOffenderCharge().isActive())
                .containsExactly(true);
    }

    private void addInactiveChargeTo(final OffenderCourtCase courtCase) {
        final var inactiveCharge = OffenderCharge.builder().chargeStatus("I").build();

        assertThat(inactiveCharge.isActive()).isFalse();

        courtCase.add(inactiveCharge);
    }

    @Test
    void court_event_without_court_case_retrieved() {
        final var bookingWithoutCourtCase = offenderBookingRepository.findById(BOOKING_WITHOUT_COURT_CASE).orElseThrow();

        assertThat(bookingWithoutCourtCase.getCourtCases()).isEmpty();

        final var savedCourtEventWithoutCourtCase = courtEventRepository.save(builder
                .offenderBooking(bookingWithoutCourtCase)
                .offenderCourtCase(null)
                .build());

        assertThat(savedCourtEventWithoutCourtCase.getOffenderBooking().getCourtCases()).isEmpty();
        assertThat(savedCourtEventWithoutCourtCase.getOffenderCourtCase()).isEmpty();
        assertThat(savedCourtEventWithoutCourtCase.getCharges()).isEmpty();

        assertThat(courtEventRepository.findById(savedCourtEventWithoutCourtCase.getId()).orElseThrow()).isEqualTo(savedCourtEventWithoutCourtCase);
    }

    @Test
    void court_event_in_future_and_charges_deleted() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder.build());

        entityManager.flush();

        final var chargeIdentifier = savedCourtEventWithCourtCase.getCharges().stream().findFirst().orElseThrow().getEventAndCharge();

        assertThat(courtEventChargeRepository.findById(chargeIdentifier)).isNotEmpty();

        final var id = savedCourtEventWithCourtCase.getId();

        courtEventRepository.delete(savedCourtEventWithCourtCase);

        entityManager.flush();

        assertThat(courtEventRepository.findById(id)).isEmpty();

        assertThat(courtEventChargeRepository.findById(chargeIdentifier)).isEmpty();
    }

    @Test
    void court_event_in_past_cannot_be_deleted() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder
                .eventDate(LocalDate.now(clock).minusDays(1))
                .build());

        entityManager.flush();

        final var id = savedCourtEventWithCourtCase.getId();

        assertThatThrownBy(() -> courtEventRepository.deleteById(id))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Court hearing '%s' cannot be deleted as its start date/time is in the past.", id);
    }

    @Test
    void court_event_that_is_not_scheduled_cannot_be_deleted() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder
                .eventStatus(eventStatusRepository.findById(COMPLETED).orElseThrow())
                .build());

        entityManager.flush();

        final var id = savedCourtEventWithCourtCase.getId();

        assertThatThrownBy(() -> courtEventRepository.deleteById(id))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Court hearing '%s' must be in a scheduled state to delete.", id);
    }
}
