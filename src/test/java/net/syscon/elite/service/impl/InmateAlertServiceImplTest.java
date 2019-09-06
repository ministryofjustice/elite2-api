package net.syscon.elite.service.impl;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.UpdateAlert;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InmateAlertServiceImplTest {
    @Mock
    private InmateAlertRepository inmateAlertRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private UserService userService;

    @Mock
    private TelemetryClient telemetryClient;

    @InjectMocks
    private InmateAlertServiceImpl serviceToTest;

    @Test
    public void testCorrectNumberAlertReturned() {
        final var alerts = createAlerts();

        when(inmateAlertRepository.getAlerts(eq(-1L), any(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = serviceToTest.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).hasSize(alerts.getItems().size());
    }

    @Test
    public void testCorrectExpiredAlerts() {
        final var alerts = createAlerts();

        when(inmateAlertRepository.getAlerts(eq(-1L), isNull(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = serviceToTest.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).extracting("expired").containsSequence(false, false, true, true, false);
    }

    @Test
    public void testThatAlertRepository_CreateAlertIsCalledWithCorrectParams() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(userService.getUserByUsername("ITAG_USER")).thenReturn(UserDetail.builder().activeCaseLoadId("LEI").build());
        when(inmateAlertRepository.createNewAlert(anyLong(), any(), anyString(), anyString())).thenReturn(1L);

        final var alertId = serviceToTest.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());

        assertThat(alertId).isEqualTo(1L);

        verify(inmateAlertRepository).createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build(), "ITAG_USER", "LEI");
    }

    @Test
    public void testThatAlertDate_SevenDaysInThePastThrowsException() {
        assertThat(catchThrowable(() -> {
            serviceToTest.createNewAlert(-1L, CreateAlert
                    .builder().alertDate(LocalDate.now().minusDays(8)).build());
        })).as("Alert date cannot go back more than seven days.").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testThatAlertDate_InTheFutureThrowsException() {
        assertThat(catchThrowable(() -> {
            serviceToTest.createNewAlert(-1L, CreateAlert
                    .builder().alertDate(LocalDate.now().plusDays(1)).build());
        })).as("Alert date cannot be in the future.").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testThatAlertRepository_UpdateAlertIsCalledWithCorrectParams() {
        final var updateAlert = UpdateAlert
                .builder()
                .expiryDate(LocalDate.now())
                .alertStatus("INACTIVE")
                .build();

        final var alert = Alert.builder()
                .alertId(4L)
                .bookingId(-1L)
                .alertType(format("ALERTYPE%d", 1L))
                .alertCode(format("ALERTCODE%d", 1L))
                .active(false)
                .comment(format("This is a comment %d", 1L))
                .dateCreated(LocalDate.now())
                .dateExpires(LocalDate.now())
                .build();

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.updateAlert(eq("ITAG_USER"), eq(-1L), eq(4L), eq(updateAlert))).thenReturn(Optional.of(alert));

        final var updatedAlert = serviceToTest.updateAlert(-1L, 4L, updateAlert);

        assertThat(updatedAlert).isEqualTo(alert);

        verify(inmateAlertRepository).updateAlert("ITAG_USER", -1L, 4L, updateAlert);
    }

    @Test
    public void testThatYouCannotCreateDuplicateAlerts() {
        final var originalAlert = Alert.builder().alertCode("X").alertType("XX").build();

        when(inmateAlertRepository.getActiveAlerts(anyLong())).thenReturn(List.of(originalAlert));

        assertThatThrownBy(() -> serviceToTest.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build())).as("Alert already exists for this offender.").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testThatTelemetryEventHasBeenRaised_OnAlertCreation() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(userService.getUserByUsername("ITAG_USER")).thenReturn(UserDetail.builder().activeCaseLoadId("LEI").build());
        when(inmateAlertRepository.createNewAlert(anyLong(), any(), anyString(), anyString())).thenReturn(1L);

        final var alertId = serviceToTest.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());

        verify(telemetryClient).trackEvent("Alert created", Map.of(
                "alertSeq", String.valueOf(alertId),
                "alertDate",  LocalDate.now().atStartOfDay().toLocalDate().toString(),
                "alertCode", "X",
                "alertType", "XX",
                "bookingId", "-1",
                "created_by", "ITAG_USER"
        ), null);
    }

    @Test
    public void testThatTelemetryEventHasBeenRaised_OnAlertUpdate() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.updateAlert(anyString(), anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(Alert.builder().build()));

        serviceToTest.updateAlert(-1L,-2L,  UpdateAlert
                .builder()
                .alertStatus("INACTIVE")
                .expiryDate(LocalDate.now())
                .build());

        verify(telemetryClient).trackEvent("Alert updated", Map.of(
                "bookingId", "-1",
                "alertSeq", "-2",
                "expiryDate",  LocalDate.now().atStartOfDay().toLocalDate().toString(),
                "updated_by", "ITAG_USER"
        ), null);
    }

    private Page<Alert> createAlerts() {
        final var now = LocalDate.now();

        final var alerts = Arrays.asList(
                buildAlert(-1L, now.minusMonths(1), now.plusDays(2)),
                buildAlert(-2L, now.minusMonths(2), now.plusDays(1)),
                buildAlert(-3L, now.minusMonths(3), now),
                buildAlert(-4L, now.minusMonths(4), now.minusDays(1)),
                buildAlert(-5L, now.minusMonths(5), null)
            );

        return new Page<>(alerts, 5, 0, 10);
    }

    private Alert buildAlert(final long id, final LocalDate dateCreated, final LocalDate dateExpires) {
        return Alert.builder()
                .alertId(id)
                .alertType(format("ALERTYPE%d", id))
                .alertCode(format("ALERTCODE%d", id))
                .comment(format("This is a comment %d", id))
                .dateCreated(dateCreated)
                .dateExpires(dateExpires)
                .build();
    }
}
