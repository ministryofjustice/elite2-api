package net.syscon.elite.api.resource.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.CourtHearings;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.api.model.PrisonToPrisonMove;
import net.syscon.elite.api.model.ScheduledPrisonToPrisonMove;
import net.syscon.elite.api.resource.OffenderMovementsResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.service.CourtHearingsService;
import net.syscon.elite.service.MovementUpdateService;
import net.syscon.elite.service.PrisonToPrisonMoveSchedulingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@RestController
@RequestMapping("${api.base.path}/bookings")
@Validated
@Slf4j
public class OffenderMovementsResourceImpl implements OffenderMovementsResource {

    private final CourtHearingsService courtHearingsService;
    private final MovementUpdateService movementUpdateService;
    private final PrisonToPrisonMoveSchedulingService prisonToPrisonMoveSchedulingService;

    public OffenderMovementsResourceImpl(final CourtHearingsService courtHearingsService,
                                         final MovementUpdateService movementUpdateService,
                                         final PrisonToPrisonMoveSchedulingService prisonToPrisonMoveSchedulingService) {
        this.courtHearingsService = courtHearingsService;
        this.movementUpdateService = movementUpdateService;
        this.prisonToPrisonMoveSchedulingService = prisonToPrisonMoveSchedulingService;
    }

    @ProxyUser
    @Override
    public CourtHearing prisonToCourt(final Long bookingId, final Long courtCaseId, final @Valid PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, courtCaseId, hearing);
    }

    @ProxyUser
    @Override
    public CourtHearing prisonToCourt(final Long bookingId, final @Valid PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, hearing);
    }

    @Override
    public CourtHearings getCourtHearings(final Long bookingId, final LocalDate fromDate, final LocalDate toDate) {
        return courtHearingsService.getCourtHearingsFor(bookingId, fromDate, toDate);
    }

    @ProxyUser
    @Override
    public OffenderBooking moveToCell(final Long bookingId, final String internalLocationDescription, final String reasonCode, final LocalDateTime dateTime) {
        log.debug("Received moveToCell request for booking id {}, cell location {}, reasonCode {}, date/time {}",
                bookingId,
                internalLocationDescription,
                reasonCode,
                dateTime != null ? dateTime.format(ISO_DATE_TIME) : "null");

        return movementUpdateService.moveToCell(bookingId, internalLocationDescription, reasonCode, dateTime);
    }

    @Override
    public ScheduledPrisonToPrisonMove prisonToPrison(final  Long bookingId, final @Valid PrisonToPrisonMove prisonMove) {
        return prisonToPrisonMoveSchedulingService.schedule(bookingId, prisonMove);
    }
}