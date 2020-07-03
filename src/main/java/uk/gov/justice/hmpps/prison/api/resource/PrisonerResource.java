package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria;
import uk.gov.justice.hmpps.prison.api.support.Order;

import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/prisoners"})
@SuppressWarnings("unused")
public interface PrisonerResource {

    @GetMapping
    @ApiOperation(value = "List of offenders matching specified criteria.", notes = "List of offenders matching specified criteria.", nickname = "getPrisoners")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<PrisonerDetail>> getPrisoners(@ApiParam(value = "If true the result set should include a row for every matched alias.  If the request includes some combination of firstName, lastName and dateOfBirth then this will be a subset of the OFFENDERS records for one or more offenders. Otherwise it will be every OFFENDERS record for each match on the other search criteria. Default is false.") @RequestParam(value = "includeAliases", required = false, defaultValue = "false") boolean includeAliases,
                                      @ApiParam(value = "List of offender NOMS numbers. NOMS numbers have the format:<b>ANNNNAA</b>", allowMultiple = true) @RequestParam(value = "offenderNo", required = false) List<String> offenderNos,
                                      @ApiParam(value = "The offender's PNC (Police National Computer) number.") @RequestParam(value = "pncNumber", required = false) String pncNumber,
                                      @ApiParam(value = "The offender's CRO (Criminal Records Office) number.") @RequestParam(value = "croNumber", required = false) String croNumber,
                                      @ApiParam(value = "The first name of the offender.") @RequestParam(value = "firstName", required = false) String firstName,
                                      @ApiParam(value = "The middle name(s) of the offender.") @RequestParam(value = "middleNames", required = false) String middleNames,
                                      @ApiParam(value = "The last name of the offender.") @RequestParam(value = "lastName", required = false) String lastName,
                                      @ApiParam(value = "The offender's date of birth. Cannot be used in conjunction with <i>dobFrom</i> or <i>dobTo</i>. Must be specified using YYYY-MM-DD format.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "dob", required = false) LocalDate dob,
                                      @ApiParam(value = "Start date for offender date of birth search. If <i>dobTo</i> is not specified, an implicit <i>dobTo</i> value of <i>dobFrom</i> + 10 years will be applied. If <i>dobTo</i> is specified, it will be adjusted, if necessary, to ensure it is not more than 10 years after <i>dobFrom</i>. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "dobFrom", required = false) LocalDate dobFrom,
                                      @ApiParam(value = "End date for offender date of birth search. If <i>dobFrom</i> is not specified, an implicit <i>dobFrom</i> value of <i>dobTo</i> - 10 years will be applied. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "dobTo", required = false) LocalDate dobTo,
                                      @ApiParam(value = "Offender's location filter (IN, OUT or ALL) - defaults to ALL.", defaultValue = "ALL") @RequestParam(value = "location", required = false) String location,
                                      @ApiParam(value = "Offender's gender code (F - Female, M - Male, NK - Not Known or NS - Not Specified).") @RequestParam(value = "gender", required = false) String genderCode,
                                      @ApiParam(value = "If <i>true</i>, the search will use partial, start-of-name matching of offender names (where provided). For example, if <i>lastName</i> criteria of 'AD' is specified, this will match an offender whose last name is 'ADAMS' but not an offender whose last name is 'HADAD'. This will typically increase the number of matching offenders found. This parameter can be used with any other search processing parameter (e.g. <i>prioritisedMatch</i> or <i>anyMatch</i>).") @RequestParam(value = "partialNameMatch", required = false, defaultValue = "false") boolean partialNameMatch,
                                      @ApiParam(value = "If <i>true</i>, search criteria prioritisation is used and searching/matching will stop as soon as one or more matching offenders are found. The criteria priority is:<br/><br/>1. <i>offenderNo</i><br/> 2. <i>pncNumber</i><br/>3. <i>croNumber</i><br/>4. <i>firstName</i>, <i>lastName</i>, <i>dob</i> <br/>5. <i>dobFrom</i>, <i>dobTo</i><br/><br/>As an example of how this works, if this parameter is set <i>true</i> and an <i>offenderNo</i> is specified and an offender having this offender number is found, searching will stop and that offender will be returned immediately. If no offender matching the specified <i>offenderNo</i> is found, the search will be repeated using the next priority criteria (<i>pncNumber</i>) and so on. Note that offender name and date of birth criteria have the same priority and will be used together to search for matching offenders.") @RequestParam(value = "prioritisedMatch", required = false, defaultValue = "false") boolean prioritisedMatch,
                                      @ApiParam(value = "If <i>true</i>, offenders that match any of the specified criteria will be returned. The default search behaviour is to only return offenders that match <i>all</i> of the specified criteria. If the <i>prioritisedMatch</i> parameter is also set <i>true</i>, this parameter will only impact the behaviour of searching using offender name and date of birth criteria.") @RequestParam(value = "anyMatch", required = false, defaultValue = "false") boolean anyMatch,
                                      @ApiParam(value = "Requested offset of first record in returned collection of prisoner records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                      @ApiParam(value = "Requested limit to number of prisoner records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>offenderNo, pncNumber, croNumber, firstName, lastName, dob</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{offenderNo}")
    @ApiOperation(value = "List of offenders globally matching the offenderNo.", notes = "List of offenders globally matching the offenderNo.", nickname = "getPrisonersOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PrisonerDetail> getPrisonersOffenderNo(@ApiParam(value = "The offenderNo to search for", required = true) @PathVariable("offenderNo") String offenderNo);


    @PostMapping
    @ApiOperation(value = "List of offenders matching specified criteria. (POST version)", notes = "List of offenders matching specified criteria.", nickname = "getPrisoners")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<PrisonerDetail>> getPrisoners(@ApiParam(value = "", required = true) @RequestBody PrisonerDetailSearchCriteria criteria,
                                                      @ApiParam(value = "Requested offset of first record in returned collection of prisoner records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                      @ApiParam(value = "Requested limit to number of prisoner records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>offenderNo, pncNumber, croNumber, firstName, lastName, dob</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);


}
