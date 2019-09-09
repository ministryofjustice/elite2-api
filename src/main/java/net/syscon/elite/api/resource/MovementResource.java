package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;

import javax.ws.rs.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = {"/movements"})
@SuppressWarnings("unused")
public interface MovementResource {

    @SuppressWarnings("RestParamTypeInspection")
    @GET
    @Path("/")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Returns a list of recently released or moved offender nos and the associated timestamp.", notes = "Returns a list of recently released or moved offender nos and the associated timestamp.", nickname = "getRecentMovementsByDate")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Movement.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Movement> getRecentMovementsByDate(
            @ApiParam(value = "A timestamp that indicates the earliest record required", required = true) @QueryParam("fromDateTime") LocalDateTime fromDateTime,
            @ApiParam(value = "The date for which movements are searched", required = true) @QueryParam("movementDate") LocalDate movementDate,
            @ApiParam(value = "Filter to just movements to or from this agency.") @QueryParam("agencyId") String agencyId,
            @ApiParam(value = "movement type codes to filter by, defaults to TRN, REL, ADM") @QueryParam("movementTypes") List<String> movementTypes);

    @GET
    @Path("/rollcount/{agencyId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Current establishment rollcount numbers.", notes = "Current establishment rollcount numbers.", nickname = "getRollcount")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = RollCount.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<RollCount> getRollcount(
            @ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
            @ApiParam(value = "If false return data for prisoners in cell locations, if true return unassigned prisoners, i.e. those in non-cell locations.", defaultValue = "false") @QueryParam("unassigned") boolean unassigned);

    @SuppressWarnings("RestParamTypeInspection")
    @GET
    @Path("/rollcount/{agencyId}/movements")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Rollcount movement numbers.", notes = "Rollcount movement numbers.", nickname = "getRollcountMovements")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    MovementCount getRollcountMovements(
            @ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
            @ApiParam(value = "The date for which movements are counted, default today.", required = true) @QueryParam("movementDate") LocalDate movementDate);

    @POST
    @Path("/offenders")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "", nickname = "getMovementsByOffenders")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = Movement.class, responseContainer = "List")})
    List<Movement> getMovementsByOffenders(
            @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
            @ApiParam(value = "movement type codes to filter by") @QueryParam("movementTypes") List<String> movementTypes,
            @ApiParam(value = "Returns only the assessments for the current sentence if true, otherwise all previous sentences are included", defaultValue = "true") @QueryParam("latestOnly") Boolean latestOnly);

    @SuppressWarnings("RestParamTypeInspection")
    @GET
    @Path("/{agencyId}/enroute")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Enroute prisoner movement details.", notes = "Enroute to reception", nickname = "getEnrouteOffenderMovements")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderMovement> getEnrouteOffenderMovements(
            @ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
            @ApiParam(value = "Optional filter on date of movement") @QueryParam("movementDate") LocalDate movementDate);

    @SuppressWarnings("RestParamTypeInspection")
    @GET
    @Path("/rollcount/{agencyId}/enroute")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Enroute prisoner movement count.", notes = "Enroute to reception count", nickname = "getEnrouteOffenderMovementCount")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    int getEnrouteOffenderMovementCount(
            @ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId, @ApiParam(value = "Optional filter on date of movement.", required = true) @QueryParam("movementDate") LocalDate movementDate);

    @SuppressWarnings("RestParamTypeInspection")
    @GET
    @Path("/{agencyId}/in/{isoDate}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Information on offenders in today.", notes = "Information on offenders in on given date.", nickname = "getMovementsIn")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderIn> getMovementsIn(
            @ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
            @ApiParam(value = "date", required = true) @PathParam("isoDate") LocalDate movementsDate);

    @GET
    @Path("/livingUnit/{livingUnitId}/currently-out")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Information on offenders currently out.", notes = "Information on offenders currently out.", nickname = "getOffendersCurrentlyOut")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderOut> getOffendersCurrentlyOut(
            @ApiParam(value = "The identifier of a living unit, otherwise known as an internal location.", required = true) @PathParam("livingUnitId") Long livingUnitId);

    @GET
    @Path("/agency/{agencyId}/currently-out")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Information on offenders currently out.", notes = "Information on offenders currently out.", nickname = "getOffendersCurrentlyOut")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MovementCount.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderOut> getOffendersCurrentlyOut(
            @ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId);

    @SuppressWarnings("RestParamTypeInspection")
    @GET
    @Path("/{agencyId}/out/{isoDate}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "", nickname = "getOffendersOut")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderOutTodayDto.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderOutTodayDto> getOffendersOutToday(
            @ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId,
            @ApiParam(value = "date", required = true) @PathParam("isoDate") LocalDate movementsDate);

    @GET
    @Path("/rollcount/{agencyId}/in-reception")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "", nickname = "getOffendersInReception")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderInReception.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderInReception> getOffendersInReception(@ApiParam(value = "The prison id", required = true) @PathParam("agencyId") String agencyId);

    @SuppressWarnings("RestParamTypeInspection")
    @GET
    @Path("/transfers")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Information on scheduled court, transfer and release events, and confirmed movements between two dates/times for a specified number of agencies.",
            notes = "Planned movements are recorded as events of type court, release or transfers/appointments. When these events are started they are actualised as external movements.",
            nickname = "getTransfers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TransferSummary.class),
            @ApiResponse(code = 400, message = "Invalid agency identifiers, or from time after the to time, or a time period greater than 24 hours specified, or parameter format not correct.", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "The token presented did not contain the necessary role to access this resource.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "The token presented has expired.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    public TransferSummary getTransfers(
            @ApiParam(value = "One or more agencyId values eg.agencyId=LEI&agencyId=MDI", required = true) @QueryParam("agencyId") List<String> agencyIds,
            @ApiParam(value = "From date and time ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS", required = true)   @QueryParam("fromDateTime")LocalDateTime fromDateTime,
            @ApiParam(value = "To date and time in ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS", required = true)  @QueryParam("toDateTime") LocalDateTime toDateTime,
            @ApiParam(value = "Set to true to include planned court events", required=false, defaultValue = "false") @QueryParam("courtEvents")  boolean courtEvents,
            @ApiParam(value = "Set to true to include planned release events", required=false, defaultValue = "false") @QueryParam("releaseEvents") boolean releaseEvents,
            @ApiParam(value = "Set to true to include planned transfer/appointment events", required=false, defaultValue = "false") @QueryParam("transferEvents") boolean transferEvents,
            @ApiParam(value = "Set to true to include confirmed movements", required=false, defaultValue = "false") @QueryParam("movements") boolean movements);

}
