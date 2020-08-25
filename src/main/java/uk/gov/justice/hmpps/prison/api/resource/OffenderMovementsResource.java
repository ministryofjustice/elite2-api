package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.justice.hmpps.prison.api.model.CourtHearing;
import uk.gov.justice.hmpps.prison.api.model.CourtHearingDateAmendment;
import uk.gov.justice.hmpps.prison.api.model.CourtHearings;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.model.PrisonMoveCancellation;
import uk.gov.justice.hmpps.prison.api.model.PrisonToCourtHearing;
import uk.gov.justice.hmpps.prison.api.model.PrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.api.model.ScheduledPrisonToPrisonMove;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Api(tags = {"/bookings"})
public interface OffenderMovementsResource {

    @PostMapping("/{bookingId}/court-cases/{courtCaseId}/prison-to-court-hearings")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Schedules a prison to court hearing for an offender and given court case.", notes = "Schedules a prison to court hearing for an offender and given court case.", nickname = "prisonToCourt")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Court hearing created.", response = CourtHearing.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    CourtHearing prisonToCourt(@ApiParam(value = "The offender booking to associate the court hearing with.", required = true) @PathVariable("bookingId") Long bookingId,
                               @ApiParam(value = "The court case to associate the hearing with.", required = true) @PathVariable("courtCaseId") Long courtCaseId,
                               @ApiParam(value = "The prison to court hearing to be scheduled for the offender booking.", required = true) @RequestBody @Valid PrisonToCourtHearing hearing);

    @PostMapping("/{bookingId}/prison-to-court-hearings")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Schedules a prison to court hearing for an offender.", notes = "Schedules a prison to court hearing for an offender.", nickname = "prisonToCourt")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Court hearing created.", response = CourtHearing.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    CourtHearing prisonToCourt(@ApiParam(value = "The offender booking to associate the court hearing with.", required = true) @PathVariable("bookingId") Long bookingId,
                               @ApiParam(value = "The prison to court hearing to be scheduled for the offender booking.", required = true) @RequestBody @Valid PrisonToCourtHearing hearing);

    @GetMapping("{bookingId}/court-hearings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CourtHearings.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    CourtHearings getCourtHearings(
            @ApiParam(value = "The offender booking linked to the court hearings.", required = true) @PathVariable("bookingId") Long bookingId,
            @ApiParam(value = "Return court hearings on or after this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @ApiParam(value = "Return court hearings on or before this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate
    );

    @PutMapping("/{bookingId}/living-unit/{internalLocationDescription}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    OffenderBooking moveToCell(
            @ApiParam(value = "The offender booking id", example = "1200866", required = true) @PathVariable("bookingId") Long bookingId,
            @ApiParam(value = "The cell location the offender has been moved to", example = "MDI-1-1", required = true) @PathVariable("internalLocationDescription") String internalLocationDescription,
            @ApiParam(value = "The reason code for the move (from reason code domain CHG_HOUS_RSN)", example = "ADM", required = true) @RequestParam("reasonCode") String reasonCode,
            @ApiParam(value = "The date / time of the move (defaults to current)", example = "2020-03-24T12:13:40") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "dateTime", required = false) LocalDateTime dateTime
    );


    @PutMapping("/{bookingId}/move-to-cell-swap")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    OffenderBooking moveToCellSwap(
            @ApiParam(value = "The offender booking id", example = "1200866", required = true) @PathVariable("bookingId") Long bookingId,
            @ApiParam(value = "The reason code for the move (from reason code domain CHG_HOUS_RSN)", example = "ADM", required = true) @RequestParam("reasonCode") String reasonCode,
            @ApiParam(value = "The date / time of the move (defaults to current)", example = "2020-03-24T12:13:40") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "dateTime", required = false) LocalDateTime dateTime
    );

    @PostMapping("/{bookingId}/prison-to-prison")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Schedules a future prison to prison move for an offender.", notes = "Schedules a future prison to prison move for an offender.", nickname = "prisonToPrison")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The scheduled prison move.", response = ScheduledPrisonToPrisonMove.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ScheduledPrisonToPrisonMove prisonToPrison(@ApiParam(value = "The offender booking to associate the prison to prison move with.", required = true) @PathVariable("bookingId") Long bookingId,
                                               @ApiParam(value = "The prison to prison move to be scheduled for the offender booking.", required = true) @RequestBody @Valid PrisonToPrisonMove prisonMove);

    @PutMapping("/{bookingId}/prison-to-prison/{eventId}/cancel")
    @ApiOperation(value = "Cancels a scheduled prison to prison move for an offender.", notes = "Cancels a scheduled prison to prison move for an offender.", nickname = "cancelPrisonToPrisonMove")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<Void> cancelPrisonToPrisonMove(@ApiParam(value = "The offender booking linked to the scheduled event.", required = true) @PathVariable("bookingId") Long bookingId,
                                                  @ApiParam(value = "The identifier of the scheduled event to be cancelled.", required = true) @PathVariable("eventId") Long eventId,
                                                  @ApiParam(value = "The cancellation details.", required = true) @RequestBody @Valid PrisonMoveCancellation cancellation);

    @PutMapping("/{bookingId}/court-hearings/{hearingId}/hearing-date")
    @ApiOperation(value = "Amends the scheduled court hearing date and/or time for an offender.", notes = "Amends the scheduled court hearing date and/or time for an offender.", nickname = "courtHearingDateAmendment")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    CourtHearing courtHearingDateAmendment(@ApiParam(value = "The offender booking to associate the update with.", required = true) @PathVariable("bookingId") Long bookingId,
                                           @ApiParam(value = "The  court hearing to be updated.", required = true) @PathVariable Long hearingId,
                                           @ApiParam(value = "The amendments for the scheduled court hearing.", required = true) @RequestBody @Valid CourtHearingDateAmendment amendment);

    @DeleteMapping("/{bookingId}/court-hearings/{hearingId}/cancel")
    @ApiOperation(value = "Cancels the scheduled court hearing for an offender.", notes = "Cancels the scheduled court hearing for an offender.", nickname = "cancelCourtHearing")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<Void> cancelCourtHearing(@ApiParam(value = "The offender booking to linked to the scheduled event.", required = true) @PathVariable("bookingId") final Long bookingId,
                                            @ApiParam(value = "The identifier of the scheduled event to be cancelled.", required = true) @PathVariable("hearingId") final Long hearingId);
}
