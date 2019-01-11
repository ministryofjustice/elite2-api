package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.OffenderIn;
import net.syscon.elite.api.model.OffenderInReception;
import net.syscon.elite.api.model.OffenderOutTodayDto;
import net.syscon.elite.executablespecification.steps.MovementsSteps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Arrays;

public class MovementsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private MovementsSteps movementsSteps;

    @When("^a request is made to retrieve recent movements$")
    public void aRequestIsMadeToRetrieveAllRecords() {
        final String fromDateTime = "2017-02-20T13:56:00";
        final String movementDate = "2017-08-16";
        movementsSteps.retrieveAllMovementRecords(fromDateTime, movementDate);
    }

    @Then("^a correct list of records are returned$")
    public void aListOfRecordsAreReturned() {
        movementsSteps.verifyListOfRecords();
    }

    @When("^a request is made to retrieve the establishment roll count for an agency$")
    public void aRequestIsMadeToRetrieveRollCount() {
        movementsSteps.retrieveRollCounts("LEI");
    }

    @When("^a request is made to retrieve the establishment unassigned roll count for an agency$")
    public void aRequestIsMadeToRetrieveUnassignedRollCount() {
        movementsSteps.retrieveUnassignedRollCounts("LEI");
    }

    @Then("^a valid list of roll count records are returned$")
    public void aListOfRollCountRecordsAreReturned() {
        movementsSteps.verifyListOfRollCounts();
    }

    @Then("^a valid list of unassigned roll count records are returned$")
    public void aListOfUnassignedRollCountRecordsAreReturned() {
        movementsSteps.verifyListOfUnassignedRollCounts();
    }


    @When("^a request is made to retrieve the movement counts for an \"([^\"]*)\" on \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveTheMovementCountsForAnOn(String agency, String date) throws Throwable {
        movementsSteps.retrieveMovementCounts(agency, date.equals("today") ? LocalDate.now().toString() : date);

    }

    @When("^a make a request for recent movements for \"([^\"]*)\" and \"([^\"]*)\"$")
    public void aMakeARequestForRecentMovementsForAnd(String offenderNo1, String offenderNo2) {
        movementsSteps.retrieveMovementsByOffenders(Arrays.asList(offenderNo1, offenderNo2));
    }

    @Then("^the records should contain a entry for \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theRecordsShouldContainAEntryFor(String movementType, String fromDescription, String toDescription, String movementReason, String movementTime)  {
        movementsSteps.verifyMovements(movementType, fromDescription, toDescription, movementReason, movementTime);
    }

    @Then("^a total count of out today as \"([^\"]*)\" offender numbers that are out today matching \"([^\"]*)\" and a count of in today as \"([^\"]*)\"\"$")
    public void aTotalCountOfOutTodayAsOffenderNumbersThatAreOutTodayMatchingAndACountOfInTodayAs(Integer outToday, String offenderNumbers, Integer inToday) {
        movementsSteps.verifyMovementCounts(outToday, inToday);
    }

    @Then("^the following rows should be returned:$")
    public void theFollowingFieldsShouldBeReturned(DataTable table) throws Throwable {
        movementsSteps.verifyOutToday(table.asList(OffenderOutTodayDto.class));
    }

    @Then("^\"([^\"]*)\" offenders are out today and \"([^\"]*)\" are in$")
    public void offendersOutTodayAndAreIn(Integer outToday, Integer inToday) throws Throwable {
        movementsSteps.verifyMovementCounts(outToday, inToday);
    }

    @When("^a request is made for en-route offenders for agency \"([^\"]*)\" on movement date \"([^\"]*)\"$")
    public void aMakeARequestForEnRouteOffendersForAgencyOnMovementDate(String agencyId, String date) {
        movementsSteps.retrieveEnrouteOffenders(agencyId, date);
    }

    @Then("^the records should contain a entry for \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theRecordsShouldContainAEntryFor(String offenderNo, String lastName, String fromAgency, String toAgency, String reason, String time) {
       movementsSteps.verifyOffenderMovements(offenderNo, lastName, fromAgency, toAgency, reason, time);
    }
    @When("^a request is made to retrieve the 'offenders in' for agency \"([^\"]*)\" on date \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveTheOffendersInForAgencyOnDate(String agencyId, String isoDateString) {
        movementsSteps.getOffendersIn(agencyId, LocalDate.parse(isoDateString));
    }

    @Then("^information about 'offenders in' is returned as follows:$")
    public void informationAboutOffendersInIsReturnedAsFollows(DataTable table) {
        var offendersIn = table.asList(OffenderIn.class);

        offendersIn.forEach(offender -> {
            if(StringUtils.isBlank(offender.getLocation()))
                offender.setLocation(null);
        });

        movementsSteps.verifyOffendersIn(offendersIn);
    }

    @When("^a request is made to retrieve the 'offenders out' for agency \"([^\"]*)\" for \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveTheOffendersOutForAgencyFor(String agencyId, String isoDateString) throws Throwable {
        movementsSteps.getOffendersOut(agencyId, LocalDate.parse(isoDateString));
    }

    @When("^a request is made to retrieve 'offenders in reception' for agency \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveOffendersInReceptionForAgency(String agencyId) throws Throwable {
        movementsSteps.getOffendersInReception(agencyId);
    }

    @Then("^information about 'offenders in reception' is returned as follows:$")
    public void informationAboutOffendersInReceptionIsReturnedAsFollows(DataTable table) {
        var offendersInReception = table.asList(OffenderInReception.class);
        movementsSteps.verifyOffendersInReception(offendersInReception);
    }
}
