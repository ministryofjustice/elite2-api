package uk.gov.justice.hmpps.prison.api.resource.impl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction;
import uk.gov.justice.hmpps.prison.service.FinanceService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static uk.gov.justice.hmpps.prison.api.resource.v1.NomisApiV1Resource.NOMS_ID_REGEX_PATTERN;

@Slf4j
@RestController
@Api(tags = {"/finance"})
@RequestMapping("${api.base.path}/finance")
@AllArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @PostMapping("/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings")
    @ApiOperation(value = "Post a financial transaction to NOMIS.",
            notes = "Notes:<br/><ul>" +
                    "<li>If the field X-Client-Name is present in the request header then the value is prepended to the client_unique_ref separated by a dash</li>" +
                    "<li>The client_unique_ref can have a maximum of 64 characters, only alphabetic, numeric, ‘-’ and ‘_’ characters are allowed</li></ul>")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Transaction Created", response = TransferTransaction.class),
            @ApiResponse(code = 400, message = "One of: <ul><li>Insufficient Funds - The prisoner has insufficient funds in the required account to cover the cost of the debit transaction</li>" +
                    "<li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li>" +
                    "<li>Finance Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Duplicate post - The unique_client_ref has been used before", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Transaction transferToSavings(
            @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") @RequestHeader(value = "X-Client-Name", required = false) String clientName,
            @ApiParam(name = "prisonId", value = "Prison ID", example = "BMI", required = true) @PathVariable("prisonId") @NotNull @Size(max = 3) String prisonId,
            @ApiParam(name = "offenderNo", value = "Offender Noms Id", example = "A1417AE", required = true) @PathVariable("offenderNo") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String offenderNo,
            @ApiParam(value = "Saving Transfer Transaction Details", required = true) @RequestBody @NotNull @Valid TransferTransaction transferTransaction) {

        return financeService.transferToSavings(prisonId, offenderNo, transferTransaction);

    }


}
