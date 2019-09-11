package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Contact;
import net.syscon.elite.api.model.ContactDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;

import java.util.List;

import static org.junit.Assert.*;

public class ContactSteps extends CommonSteps {
    private static final String BOOKING_CONTACTS_API_URL = API_PREFIX + "bookings/{bookingId}/contacts";
    private ContactDetail details;
    private int index;

    @Step("Get offender contact details")
    public void getContacts(final Long bookingId) {
        doSingleResultApiCall(bookingId);
    }

    @Step("Verify value of next of kin field in details")
    public void verifyNextOfKinField(final String field, final String value) throws ReflectiveOperationException {
        verifyField(details.getNextOfKin().get(index), field, value);
    }

    private void doSingleResultApiCall(final long bookingId) {
        init();
        try {
            final var response = restTemplate.exchange(BOOKING_CONTACTS_API_URL, HttpMethod.GET,
                    createEntity(), ContactDetail.class, bookingId);
            details = response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        details = null;
    }

    public void verifyNoNextOfKin() {
        assertTrue("There is " + details.getNextOfKin().size() + " next of kin", details.getNextOfKin().isEmpty());
    }

    public void verifyNextOfKinList(final List<Contact> expected) {
        final var expectedIterator = expected.iterator();
        final var awardsIterator = details.getNextOfKin().iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = awardsIterator.next();
            assertEquals(expectedThis.getLastName(), actualThis.getLastName());
            assertEquals(expectedThis.getFirstName(), actualThis.getFirstName());
            assertEqualsBlankIsNull(expectedThis.getMiddleName(), actualThis.getMiddleName());
            assertEqualsBlankIsNull(expectedThis.getContactType(), actualThis.getContactType());
            assertEqualsBlankIsNull(expectedThis.getContactTypeDescription(), actualThis.getContactTypeDescription());
            assertEqualsBlankIsNull(expectedThis.getRelationship(), actualThis.getRelationship());
            assertEqualsBlankIsNull(expectedThis.getRelationshipDescription(), actualThis.getRelationshipDescription());
            assertEquals(expectedThis.isEmergencyContact(), actualThis.isEmergencyContact());
        }
        assertFalse("Too many actual results", awardsIterator.hasNext());
    }
}
