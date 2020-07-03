package net.syscon.prison.service;

import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.api.model.Agency;
import net.syscon.prison.api.model.IepLevel;
import net.syscon.prison.api.model.Location;
import net.syscon.prison.api.model.OffenderCell;
import net.syscon.prison.api.model.OffenderCellAttribute;
import net.syscon.prison.api.model.PrisonContactDetail;
import net.syscon.prison.api.model.ReferenceCode;
import net.syscon.prison.api.support.Order;
import net.syscon.prison.api.support.Page;
import net.syscon.prison.api.support.TimeSlot;
import net.syscon.prison.repository.AgencyRepository;
import net.syscon.prison.repository.jpa.model.ActiveFlag;
import net.syscon.prison.repository.jpa.model.LivingUnit;
import net.syscon.prison.repository.jpa.model.LivingUnitProfile;
import net.syscon.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import net.syscon.prison.repository.jpa.repository.AgencyLocationFilter;
import net.syscon.prison.repository.jpa.repository.AgencyLocationRepository;
import net.syscon.prison.repository.jpa.repository.LivingUnitProfileRepository;
import net.syscon.prison.repository.jpa.repository.LivingUnitRepository;
import net.syscon.prison.repository.jpa.transform.LocationTransformer;
import net.syscon.prison.repository.support.StatusFilter;
import net.syscon.prison.security.AuthenticationFacade;
import net.syscon.prison.security.VerifyAgencyAccess;
import net.syscon.prison.service.support.AlphaNumericComparator;
import net.syscon.prison.service.support.LocationProcessor;
import net.syscon.prison.service.support.ReferenceDomain;
import net.syscon.prison.service.transformers.AgencyTransformer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.syscon.prison.repository.support.StatusFilter.ACTIVE_ONLY;
import static net.syscon.prison.repository.support.StatusFilter.INACTIVE_ONLY;
import static net.syscon.prison.web.config.CacheConfig.GET_AGENCY_LOCATIONS_BOOKED;

/**
 * Agency API service implementation.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class AgencyServiceImpl implements AgencyService {

    private static final Comparator<Location> LOCATION_DESCRIPTION_COMPARATOR = Comparator.comparing(
            Location::getDescription,
            new AlphaNumericComparator());

    private final AuthenticationFacade authenticationFacade;
    private final AgencyRepository agencyRepository;
    private final AgencyLocationRepository agencyLocationRepository;
    private final ReferenceDomainService referenceDomainService;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final LivingUnitRepository livingUnitRepository;
    private final LivingUnitProfileRepository livingUnitProfileRepository;

    @Override
    public Agency getAgency(final String agencyId, final StatusFilter filter, final String agencyType) {
        final var criteria = AgencyLocationFilter.builder()
                .id(agencyId)
                .type(agencyType)
                .activeFlag(filter == ACTIVE_ONLY ? ActiveFlag.Y : filter == INACTIVE_ONLY ? ActiveFlag.N : null)
                .build();

       return agencyLocationRepository.findAll(criteria)
          .stream()
                .findFirst()
                .map(AgencyTransformer::transform).orElseThrow(EntityNotFoundException.withId(agencyId));
    }

    @Override
    public List<Agency> getAgenciesByType(final String agencyType, final boolean activeOnly) {

        final var filter = AgencyLocationFilter.builder()
                .activeFlag(activeOnly ? ActiveFlag.Y : null)
                .type(agencyType)
                .build();

        return agencyLocationRepository.findAll(filter)
                .stream()
                .map(AgencyTransformer::transform)
                .collect(Collectors.toList());
    }

    @Override
    public void checkAgencyExists(final String agencyId) {
        Objects.requireNonNull(agencyId, "agencyId is a required parameter");

        if (agencyRepository.findAgency(agencyId, ACTIVE_ONLY, null).isEmpty()) {
            throw EntityNotFoundException.withId(agencyId);
        }
    }

    @Override
    public Page<Agency> getAgencies(final long offset, final long limit) {
        return agencyRepository.getAgencies("agencyId", Order.ASC, offset, limit);
    }

    @Override
    public List<Agency> findAgenciesByUsername(final String username) {
        final var agenciesByUsername = agencyRepository.findAgenciesByUsername(username);
        agenciesByUsername.forEach(a -> a.setDescription(LocationProcessor.formatLocation(a.getDescription())));
        return agenciesByUsername;
    }

    /**
     * Gets set of agency location ids accessible to current authenticated user. This governs access to bookings - a user
     * cannot have access to an offender unless they are in a location that the authenticated user is also associated with.
     *
     * @return set of agency location ids accessible to current authenticated user.
     */
    @Override
    public Set<String> getAgencyIds() {
        return findAgenciesByUsername(authenticationFacade.getCurrentUsername())
                .stream()
                .map(Agency::getAgencyId)
                .collect(Collectors.toSet());
    }

    /**
     * Verifies that current user is authorised to access specified agency. If this
     * agency location is not part of any caseload accessible to the current user, a 'Resource Not Found'
     * exception is thrown.
     *
     * @param agencyId the agency.
     * @throws EntityNotFoundException if current user does not have access to this agency.
     */
    @Override
    public void verifyAgencyAccess(final String agencyId) {
        Objects.requireNonNull(agencyId, "agencyId is a required parameter");

        final var agencyIds = getAgencyIds();
        if (AuthenticationFacade.hasRoles("INACTIVE_BOOKINGS")) {
            agencyIds.addAll(Set.of("OUT", "TRN"));
        }
        if (!agencyIds.contains(agencyId)) {
            throw EntityNotFoundException.withId(agencyId);
        }
    }

    @Override
    public List<Location> getAgencyLocations(final String agencyId, final String eventType, final String sortFields, final Order sortOrder) {
        // If no sort fields defined, sort in ascending order of user description then description (by default)
        final var orderBy = StringUtils.defaultIfBlank(sortFields, "userDescription,description");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        final List<String> eventTypes = StringUtils.isBlank(eventType) ? Collections.emptyList() : Collections.singletonList(eventType);
        final var rawLocations = agencyRepository.getAgencyLocations(agencyId, eventTypes, orderBy, order);

        return LocationProcessor.processLocations(rawLocations);
    }

    @Override
    @VerifyAgencyAccess
    public List<Location> getAgencyLocationsByType(final String agencyId, final String type) {
        final var agencyInternalLocations = agencyInternalLocationRepository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag(agencyId, type, ActiveFlag.Y);

        if (agencyInternalLocations.size() == 0) {
            throw EntityNotFoundException.withMessage(String.format("Locations of type %s in agency %s not found", type, agencyId));
        }

        return agencyInternalLocations.stream().map(LocationTransformer::fromAgencyInternalLocation).collect(Collectors.toList());
    }

    @Override
    public List<Location> getAgencyEventLocations(final String agencyId, final String sortFields, final Order sortOrder) {
        final var orderBy = StringUtils.defaultIfBlank(sortFields, "userDescription,description");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        // Get all location usages for locations that an event could possibly be held in. (reference domain ILOC_USG )
        // Note this should be cached. Also assuming small number of values
        final var allEventLocationUsages = referenceDomainService
                .getReferenceCodesByDomain(ReferenceDomain.INTERNAL_LOCATION_USAGE.getDomain(), false, null, null, 0, 1000)
                .getItems().stream().map(ReferenceCode::getCode).collect(Collectors.toList());

        final var rawLocations = agencyRepository.getAgencyLocations(agencyId, allEventLocationUsages, orderBy, order);

        return LocationProcessor.processLocations(rawLocations);
    }

    @Override
    @Cacheable(value = GET_AGENCY_LOCATIONS_BOOKED, key = "#agencyId + '-' + #bookedOnDay + '-' + #bookedOnPeriod")
    public List<Location> getAgencyEventLocationsBooked(final String agencyId, @NotNull final LocalDate bookedOnDay, final TimeSlot bookedOnPeriod) {
        return getAgencyLocationsOnDayAndPeriod(agencyId, bookedOnDay, bookedOnPeriod);
    }

    private List<Location> getAgencyLocationsOnDayAndPeriod(final String agencyId, @NotNull final LocalDate bookedOnDay, final TimeSlot bookedOnPeriod) {
        Objects.requireNonNull(bookedOnDay, "bookedOnDay must be specified.");

        final var locations = agencyRepository.getAgencyLocationsBooked(agencyId, bookedOnDay, bookedOnPeriod);
        final var processedLocations = LocationProcessor.processLocations(locations, true);
        processedLocations.sort(LOCATION_DESCRIPTION_COMPARATOR);
        return processedLocations;
    }

    @Override
    public List<IepLevel> getAgencyIepLevels(final String agencyId) {
        return agencyRepository.getAgencyIepLevels(agencyId);
    }

    @Override
    public List<PrisonContactDetail> getPrisonContactDetail() {
        return removeBlankAddresses(agencyRepository.getPrisonContactDetails(null));
    }

    @Override
    public PrisonContactDetail getPrisonContactDetail(final String agencyId) {

        final var prisonContactDetailList = removeBlankAddresses(agencyRepository.getPrisonContactDetails(agencyId));
        if (prisonContactDetailList.isEmpty()) {
            throw EntityNotFoundException.withMessage(String.format("Contact details not found for Prison %s", agencyId));
        }
        return prisonContactDetailList.get(0);
    }

    @Override
    public List<Agency> getAgenciesByCaseload(final String caseload) {
        final var agenciesByCaseload = agencyRepository.findAgenciesByCaseload(caseload);
        agenciesByCaseload.forEach(a -> a.setDescription(LocationProcessor.formatLocation(a.getDescription())));
        return agenciesByCaseload;
    }

    //It is possible for invalid/empty address records to be persisted
    @VisibleForTesting
    List<PrisonContactDetail> removeBlankAddresses(final List<PrisonContactDetail> list) {
        return list.stream().filter(pcd -> !isBlankAddress(pcd)).collect(Collectors.toList());
    }

    private boolean isBlankAddress(final PrisonContactDetail pcd) {
        return pcd.getPremise() == null && pcd.getCity() == null && pcd.getLocality() == null && pcd.getPostCode() == null;
    }

    @Override
    public Page<OffenderIepReview> getPrisonIepReview(final OffenderIepReviewSearchCriteria criteria) {
        return agencyRepository.getPrisonIepReview(criteria);
    }

    @Override
    public List<OffenderCell> getCellsWithCapacityInAgency(@NotNull final String agencyId, String attribute) {
        final var livingUnits = livingUnitRepository.findAllByAgencyLocationId(agencyId);
        return livingUnits.stream()
                .filter(LivingUnit::isActiveCellWithSpace)
                .map(livingUnit -> OffenderCell.builder()
                    .capacity(livingUnit.getOperationalCapacity())
                    .noOfOccupants(livingUnit.getNoOfOccupants())
                    .id(livingUnit.getLivingUnitId())
                    .description(livingUnit.getUserDescription() != null ?  livingUnit.getUserDescription() : livingUnit.getDescription())
                    .attributes(livingUnitProfileRepository
                                .findAllByLivingUnitIdAndAgencyLocationIdAndDescription(livingUnit.getLivingUnitId(), livingUnit.getAgencyLocationId(), livingUnit.getDescription())
                                .stream()
                                .filter(LivingUnitProfile::isAttribute)
                                .map(profile -> OffenderCellAttribute.builder()
                                     .code(profile.getHousingAttributeReferenceCode().getCode())
                                     .description(profile.getHousingAttributeReferenceCode().getDescription())
                                     .build())
                                .collect(Collectors.toList()))
                    .build())
                .filter(cell -> attribute == null || cell.getAttributes().stream().map(OffenderCellAttribute::getCode).collect(Collectors.toList()).contains(attribute))
                .collect(Collectors.toList());
    }
}
