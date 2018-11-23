package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.LocationGroup;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.LocationGroupService;
import org.springframework.stereotype.Service;

import java.beans.ConstructorProperties;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An implementation of LocationGroupService backed by a properties file.
 */
@Service
@Slf4j
public class LocationGroupServiceImpl implements LocationGroupService {

    private final Properties groupsProperties;

    private final Map<String, List<Pattern>> cellPatternsByGroup = new HashMap<>();

    @ConstructorProperties({"groupsProperties"})
    public LocationGroupServiceImpl(Properties groupsProperties) {
        this.groupsProperties = groupsProperties;
    }

    /**
     * Return the set of Location Groups for an agency, including any nested sub-groups.
     *
     * @param agencyId The agency identifier
     * @return A list of LocationGroup, sorted by name, with each item containing its nested LocationGroups, also sorted by name.
     */
    @Override
    @VerifyAgencyAccess
    public List<LocationGroup> getLocationGroupsForAgency(String agencyId) {

        final Set<String> fullKeys = groupsProperties.stringPropertyNames();

        return fullKeys.stream()
                .filter(key -> key.startsWith(agencyId))
                .map(key -> key.substring(agencyId.length() + 1))
                .filter(key -> !key.contains("_"))
                .sorted()
                .map(key -> new LocationGroup(Collections.emptyMap(), key, getAvailableSubGroups(agencyId, key)))
                .collect(Collectors.toList());
    }

    /**
     * Get the available sub-groups (sub-locations) for the named group/agency.
     * @param agencyId The agency identifier
     * @param groupName The  name of a group
     * @return Alphabetically sorted List of subgroups matching the criteria
     */
    private List<LocationGroup> getAvailableSubGroups(String agencyId, String groupName) {

        final Set<String> fullKeys = groupsProperties.stringPropertyNames();

        final String agencyAndGroupName = agencyId + '_' + groupName + '_';

        return fullKeys.stream()
                .filter(key -> key.startsWith(agencyAndGroupName))
                .map(key -> key.substring(agencyAndGroupName.length()))
                .sorted()
                .map(key -> new LocationGroup(Collections.emptyMap(), key, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    /**
     *  Return a Predicate which will filter Locations by agencyId and group/sub-group name.
     *  For a group the compoundGroupName is the name of a group, while for a sub-group the compoundGoupName is
     *  the group name followed by a '_' followed by the sub-group name.
     * @param agencyId
     * @param compoundGroupName The group name or compounded group / sub-group name.
     * @return A list of Predicates that is true for any Location which is a member of the named group or sub-group and false
     * for any other Location.
     */
    public List<Predicate<Location>> locationGroupFilters(String agencyId, String compoundGroupName) {
        final String patternStrings = groupsProperties.getProperty(agencyId + '_' + compoundGroupName);
        if (patternStrings == null) {
            throw new EntityNotFoundException(
                    "Group '" + compoundGroupName + "' does not exist for agencyId '" + agencyId + "'.");
        }
        String[]  patternString = patternStrings.split(",");
        return Arrays.stream(patternString)
                .map(Pattern::compile)
                .map(p -> (Predicate<Location>) ((Location l) -> p.matcher(l.getLocationPrefix()).matches()))
                .collect(Collectors.toList());
    }

}
