package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.OffenderBookingSearchRequest;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.SearchOffenderService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.support.SearchOffenderRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchOffenderServiceImplTest {

    @Mock
    BookingService bookingService;
    @Mock
    UserService userService;
    @Mock
    InmateRepository inmateRepository;
    @Mock
    AuthenticationFacade authenticationFacade;

    @Test
    public void testFindOffenders_findAssessmentsCorrectlyBatchesQueries() {
        final var offenderNoRegex = "^[A-Za-z]\\d{4}[A-Za-z]{2}$}";
        final int maxBatchSize = 1;

        final var bookings = List.of(
                OffenderBooking.builder().firstName("firstName1").bookingId(1L).bookingNo("1").build(),
                OffenderBooking.builder().firstName("firstName2").bookingId(2L).bookingNo("2").build()
        );

        when(inmateRepository.searchForOffenderBookings(isA(OffenderBookingSearchRequest.class))).thenReturn(new Page<>(bookings, bookings.size(), 0, bookings.size()));

        final var service = new SearchOffenderService(bookingService, userService, inmateRepository, authenticationFacade, offenderNoRegex, maxBatchSize);

        service.findOffenders(SearchOffenderRequest.builder().keywords("firstName").locationPrefix("LEI").returnCategory(true).build());

        verify(inmateRepository).findAssessments(eq(List.of(1L)), anyString(), anySet());
        verify(inmateRepository).findAssessments(eq(List.of(2L)), anyString(), anySet());
    }
}
