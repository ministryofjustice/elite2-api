package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${api.base.path}/cell")
@Validated
public interface CellResource {
    @GetMapping("/{locationId}/history")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<BedAssignment> getBedAssignmentsHistory(
            @ApiParam(value = "The offender booking linked to the court hearings.", required = true) @PathVariable("locationId") Long locationId,
            @ApiParam(value = "From date", example = "2020-03-24", required = true) @RequestParam(value = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @ApiParam(value = "To date", example = "2020-03-24", required = true) @RequestParam(value = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate);
}
