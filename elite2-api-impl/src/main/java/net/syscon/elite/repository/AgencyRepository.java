package net.syscon.elite.repository;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Agency API repository interface.
 */
public interface AgencyRepository {
    Page<Agency> getAgencies(String orderByField, Order order, long offset, long limit);
    List<Agency> findAgenciesByUsername(String username);
    List<Agency> findAgenciesForCurrentCaseloadByUsername(String username);
    List<Agency> findAgenciesByCaseload(String caseload);
    Optional<Agency> getAgency(String agencyId);
    List<PrisonContactDetail> getPrisonContactDetails(String agencyId);
    List<Location> getAgencyLocations(String agencyId, List<String> eventTypes, String sortFields, Order sortOrder);
    List<Location> getAgencyLocationsBooked(String agencyId, LocalDate bookedOnDay, TimeSlot bookedOnPeriod);
}
