package net.syscon.elite.service;

import net.syscon.elite.api.model.Contact;
import net.syscon.elite.api.model.ContactDetail;
import net.syscon.elite.api.model.OffenderRelationship;

import java.util.List;

public interface ContactService {
    String EXTERNAL_REL = "EXTERNAL_REL";

    ContactDetail getContacts(Long bookingId);

    List<Contact> getRelationships(Long bookingId, String relationshipType);

    List<Contact> getRelationshipsByOffenderNo(String offenderNo, String relationshipType);

    Contact createRelationship(Long bookingId, OffenderRelationship relationshipDetail);

    Contact createRelationshipByOffenderNo(String offenderNo, OffenderRelationship relationshipDetail);
}
