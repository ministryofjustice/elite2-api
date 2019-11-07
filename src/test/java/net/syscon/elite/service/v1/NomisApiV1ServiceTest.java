package net.syscon.elite.service.v1;

import net.syscon.elite.repository.v1.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NomisApiV1ServiceTest {
    @Mock
    private BookingV1Repository bookingV1Repository;
    @Mock
    private OffenderV1Repository offenderV1Repository;
    @Mock
    private LegalV1Repository legalV1Repository;
    @Mock
    private FinanceV1Repository financeV1Repository;
    @Mock
    private AlertV1Repository alertV1Repository;
    @Mock
    private EventsV1Repository eventsV1Repository;
    @Mock
    private PrisonV1Repository prisonV1Repository;
    @Mock
    private CoreV1Repository coreV1Repository;
    @Mock
    private VisitV1Repository visitV1Repository;

    private NomisApiV1Service service;

    @Before
    public void setUp() {
        service = new NomisApiV1Service(bookingV1Repository, offenderV1Repository, legalV1Repository, financeV1Repository, alertV1Repository, eventsV1Repository, prisonV1Repository, coreV1Repository, visitV1Repository);
    }

    @Test
    public void getVisitAvailableDates_fromInPast() {
        final var from = LocalDate.now().minusDays(1);
        final var to = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> service.getVisitAvailableDates(12345L, from, to))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start date cannot be in the past");
    }

    @Test
    public void getVisitAvailableDates_fromTooFarInFuture() {
        final var from = LocalDate.now().plusDays(5);
        final var to = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> service.getVisitAvailableDates(12345L, from, to))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End date cannot be before the start date");
    }

    @Test
    public void getVisitAvailableDates_TooFarInFuture() {
        final var from = LocalDate.now().plusDays(60);
        final var to = LocalDate.now().plusDays(61);
        assertThatThrownBy(() -> service.getVisitAvailableDates(12345L, from, to))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End date cannot be more than 60 days in the future");
    }

    @Test
    public void getVisitAvailableDates() {
        final var from = LocalDate.now();
        final var to = LocalDate.now().plusDays(5);
        service.getVisitAvailableDates(12345L, from, to);
        verify(visitV1Repository).getAvailableDates(12345L, from, to);
    }
}