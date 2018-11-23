package net.syscon.elite.repository;

import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.impl.NameFilter;
import net.syscon.elite.web.config.PersistenceConfigs;
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
import java.util.Optional;

import static net.syscon.elite.service.UserService.STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    public void testFindUserByUsername() {
        UserDetail user = repository.findByUsername("ITAG_USER").orElseThrow(EntityNotFoundException.withId("ITAG_USER"));

        assertThat(user.getLastName()).isEqualTo("User");
    }

    @Test
    public void testFindUserByUsernameNotExists() {
        Optional<UserDetail> user = repository.findByUsername("XXXXXXXX");
        assertThat(user).isNotPresent();
    }


    @Test
    public void testFindRolesByUsername() {
        List<UserRole> roles = repository.findRolesByUsername("ITAG_USER", null);
        assertThat(roles).isNotEmpty();
        assertThat(roles).extracting("roleCode").contains("LEI_WING_OFF");
    }


    @Test
    public void testFindRolesByUsernameAndCaseloadAdmin() {
        List<AccessRole> roles = repository.findAccessRolesByUsernameAndCaseload("ITAG_USER", "NWEB", true);
        assertThat(roles).isNotEmpty();
        assertThat(roles).extracting("roleCode").contains("ACCESS_ROLE_ADMIN");
    }

    @Test
    public void testFindRolesByUsernameAndCaseloadGeneral() {
        List<AccessRole> roles = repository.findAccessRolesByUsernameAndCaseload("ITAG_USER", "NWEB", false);
        assertThat(roles).isNotEmpty();
        assertThat(roles).extracting("roleCode").doesNotContain("ACCESS_ROLE_ADMIN");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testFindUserByStaffIdAndStaffUserTypeUnknownStaffId() {
        final long staffId = -99L;

        repository.findByStaffIdAndStaffUserType(staffId, STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION).orElseThrow(EntityNotFoundException.withId(staffId));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testFindUserByStaffIdAndStaffUserTypeInvalidUserType() {
        final long staffId = -1L;
        final String staffUserType = "INVALID";

        repository.findByStaffIdAndStaffUserType(staffId, staffUserType).orElseThrow(EntityNotFoundException.withId(staffId));
    }

    @Test
    public void testFindUserByStaffIdAndStaffUserType() {
        final Long staffId = -1L;

        UserDetail user = repository.findByStaffIdAndStaffUserType(staffId, STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION).orElseThrow(EntityNotFoundException.withId(staffId));

        assertThat(user.getUsername()).isEqualTo("ELITE2_API_USER");
    }

    @Test
    public void testFindUsersByCaseload() {

        final Page<UserDetail> page = repository.findUsersByCaseload("LEI", null, new NameFilter(), new PageRequest("last_name", Order.ASC, 0L, 5L));
        final List<UserDetail> items = page.getItems();

        assertThat(items).hasSize(5);
        assertThat(items).extracting("username").first().isEqualTo("CA_USER");

    }

    @Test
    public void testFindUsersByCaseloadAndNameFilter() {

        final Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", null, new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUserByFullNameSearch() {

        final Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", null, new NameFilter("User Api"), new PageRequest());

        assertThat(usersByCaseload.getItems()).hasSize(1);
        assertThat(usersByCaseload.getItems().get(0).getUsername()).isEqualTo("ITAG_USER");
    }

    @Test
    public void testFindUserByFullNameSearchNoresults() {

        final Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", null, new NameFilter("Other Api"), new PageRequest());

        assertThat(usersByCaseload.getItems()).hasSize(0);
    }

    @Test
    public void testFindUsersByCaseloadAndNameFilterUsername() {

        final Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", null, new NameFilter("ITAG_USER"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndNameFilterAndAccessRoleFilter() {

        final Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", "OMIC_ADMIN", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilter() {

        Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", "OMIC_ADMIN", new NameFilter("User Api"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterRoleNotAssigned() {

        Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", "ACCESS_ROLE_GENERAL", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterRoleNotAnAccessRole() {

        Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", "WING_OFF", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterNonExistantRole() {

        Page<UserDetail> usersByCaseload = repository.findUsersByCaseload("LEI", "OMIC_ADMIN_DOESNT_EXIST", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindLocalAdministratorUsersByCaseload() {

        final Page<UserDetail> page = repository.findLocalAdministratorUsersByCaseload("LEI", null, new NameFilter(), new PageRequest("last_name", Order.ASC, 0L, 5L));
        final List<UserDetail> items = page.getItems();

        assertThat(items).hasSize(2);
        assertThat(items).extracting("username").first().isEqualTo("CA_USER");
    }

    @Test
    public void testFindLocalAdministratorUsersByCaseloadAndNameFilter() {

        final Page<UserDetail> usersByCaseload = repository.findLocalAdministratorUsersByCaseload("LEI", null, new NameFilter("ITAG_USER"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").containsOnly("ITAG_USER");
    }

    @Test
    public void testFindLocalAdministratorUsersByCaseloadAndAccessRoleFilter() {

        Page<UserDetail> usersByCaseload = repository.findLocalAdministratorUsersByCaseload("LEI", "OMIC_ADMIN", new NameFilter("User"), new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }


    @Test
    public void testIsRoleAssigned() {

        assertThat(repository.isRoleAssigned("ITAG_USER", "LEI", -2)).isTrue();
        assertThat(repository.isRoleAssigned("ITAG_USERR", "LEI", -2)).isFalse();
        assertThat(repository.isRoleAssigned("ITAG_USER", "XXX", -2)).isFalse();
        assertThat(repository.isRoleAssigned("ITAG_USER", "LEI", -3)).isFalse();
    }

    @Test
    public void testGetRoleByCode() {
        Optional<AccessRole> roleOptional = repository.getRoleByCode("MAINTAIN_ACCESS_ROLES");
        assertThat(roleOptional).isPresent();
        assertThat(roleOptional.get().getRoleName()).isEqualTo("Maintain access roles");
    }


}
