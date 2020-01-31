package net.syscon.elite.repository;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.filters.NameFilter;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.syscon.elite.service.UserService.STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaseLoadRepository caseLoadRepository;

    @Test
    public void testFindUserByUsername() {
        final var user = userRepository.findByUsername("ITAG_USER").orElseThrow(EntityNotFoundException.withId("ITAG_USER"));

        assertThat(user.getLastName()).isEqualTo("User");
    }

    @Test
    public void testFindUserByUsernameNotExists() {
        final var user = userRepository.findByUsername("XXXXXXXX");
        assertThat(user).isNotPresent();
    }


    @Test
    public void testFindRolesByUsername() {
        final var roles = userRepository.findRolesByUsername("ITAG_USER", null);
        assertThat(roles).isNotEmpty();
        assertThat(roles).extracting("roleCode").contains("LEI_WING_OFF");
    }


    @Test
    public void testFindRolesByUsernameAndCaseloadAdmin() {
        final var roles = userRepository.findAccessRolesByUsernameAndCaseload("ITAG_USER", "NWEB", true);
        assertThat(roles).isNotEmpty();
        assertThat(roles).extracting("roleCode").contains("ACCESS_ROLE_ADMIN");
    }

    @Test
    public void testFindRolesByUsernameAndCaseloadGeneral() {
        final var roles = userRepository.findAccessRolesByUsernameAndCaseload("ITAG_USER", "NWEB", false);
        assertThat(roles).isNotEmpty();
        assertThat(roles).extracting("roleCode").doesNotContain("ACCESS_ROLE_ADMIN");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testFindUserByStaffIdAndStaffUserTypeUnknownStaffId() {
        final var staffId = -99L;

        userRepository.findByStaffIdAndStaffUserType(staffId, STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION).orElseThrow(EntityNotFoundException.withId(staffId));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testFindUserByStaffIdAndStaffUserTypeInvalidUserType() {
        final var staffId = -1L;
        final var staffUserType = "INVALID";

        userRepository.findByStaffIdAndStaffUserType(staffId, staffUserType).orElseThrow(EntityNotFoundException.withId(staffId));
    }

    @Test
    public void testFindUserByStaffIdAndStaffUserType() {
        final Long staffId = -1L;

        final var user = userRepository.findByStaffIdAndStaffUserType(staffId, STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION).orElseThrow(EntityNotFoundException.withId(staffId));

        assertThat(user.getUsername()).isEqualTo("ELITE2_API_USER");
    }

    @Test
    public void testFindUsersByCaseload() {

        final var page = userRepository.findUsersByCaseload("LEI", null, new NameFilter(), new PageRequest("last_name", Order.ASC, 0L, 5L));
        final var items = page.getItems();

        assertThat(items).hasSize(5);
        assertThat(items).extracting("username").first().isEqualTo("CA_USER");

    }

    @Test
    public void testFindUsersByCaseloadAndNameFilter() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUserByFullNameSearch() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("User Api"), new PageRequest());

        assertThat(usersByCaseload.getItems()).hasSize(1);
        assertThat(usersByCaseload.getItems().get(0).getUsername()).isEqualTo("ITAG_USER");
    }

    @Test
    public void testFindUserByFullNameSearchNoresults() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("Other Api"), new PageRequest());

        assertThat(usersByCaseload.getItems()).hasSize(0);
    }

    @Test
    public void testFindUsersByCaseloadAndNameFilterUsername() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("ITAG_USER"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndNameFilterAndAccessRoleFilter() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", "OMIC_ADMIN", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilter() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", "OMIC_ADMIN", new NameFilter("User Api"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterRoleNotAssigned() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", "ACCESS_ROLE_GENERAL", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterRoleNotAnAccessRole() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", "WING_OFF", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterNonExistantRole() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", "OMIC_ADMIN_DOESNT_EXIST", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindLocalAdministratorUsersByCaseload() {

        final var page = userRepository.getUsersAsLocalAdministrator("LAA_USER", null, new NameFilter(), new PageRequest("last_name", Order.ASC, 0L, 5L));
        final var items = page.getItems();

        assertThat(items).hasSize(3);
        assertThat(items).extracting("username").first().isEqualTo("CA_USER");
    }

    @Test
    public void testFindLocalAdministratorUsersByCaseloadAndNameFilter() {
        final var usersByCaseload = userRepository.getUsersAsLocalAdministrator("LAA_USER", null, new NameFilter("ITAG_USER"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").containsOnly("ITAG_USER");
    }

    @Test
    public void testFindLocalAdministratorUsersByCaseloadAndAccessRoleFilter() {

        final var usersByCaseload = userRepository.getUsersAsLocalAdministrator("LAA_USER", "OMIC_ADMIN", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }


    @Test
    public void testIsRoleAssigned() {

        assertThat(userRepository.isRoleAssigned("ITAG_USER", "LEI", -2)).isTrue();
        assertThat(userRepository.isRoleAssigned("ITAG_USERR", "LEI", -2)).isFalse();
        assertThat(userRepository.isRoleAssigned("ITAG_USER", "XXX", -2)).isFalse();
        assertThat(userRepository.isRoleAssigned("ITAG_USER", "LEI", -3)).isFalse();
    }

    @Test
    public void testGetRoleByCode() {
        final var roleOptional = userRepository.getRoleByCode("MAINTAIN_ACCESS_ROLES");
        assertThat(roleOptional).isPresent();
        assertThat(roleOptional.get().getRoleName()).isEqualTo("Maintain access roles");
    }

    @Test
    public void testUpdateWorkingCaseLoad() {
        // STAFF_USER_ACCOUNTS for genUsername and admUsername have the same staff_id (-1).
        final var genUsername = "ELITE2_API_USER";
        final var admUsername = "ELITE2_API_USER_ADM";

        assertThat(caseLoadRepository.getWorkingCaseLoadByUsername(genUsername).map(CaseLoad::getCaseLoadId)).contains("LEI");
        assertThat(caseLoadRepository.getWorkingCaseLoadByUsername(admUsername).map(CaseLoad::getCaseLoadId)).contains("CADM_I");
        userRepository.updateWorkingCaseLoad(genUsername, "NWEB");
        assertThat(caseLoadRepository.getWorkingCaseLoadByUsername(genUsername).map(CaseLoad::getCaseLoadId)).contains("NWEB");
        assertThat(caseLoadRepository.getWorkingCaseLoadByUsername(admUsername).map(CaseLoad::getCaseLoadId)).contains("CADM_I");
        // Restore the original so we don't break other tests
        userRepository.updateWorkingCaseLoad(genUsername, "LEI");
    }

    @Test
    public void testGetBasicInmateDetailsByBookingIds() {
        final var users = userRepository.getUserListByUsernames(List.of("JBRIEN", "RENEGADE"));
        assertThat(users).extracting("username", "firstName", "staffId").contains(
                Tuple.tuple("JBRIEN", "Jo", -12L),
                Tuple.tuple("RENEGADE", "Renegade", -11L));
    }
}
