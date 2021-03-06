package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class UserCaseloadAndRoleMaintenanceIntTest {

    @Autowired
    private UserService userService;

    @Test
    @WithUserDetails("ITAG_USER")
    public void testGetSpecificRoles() {
        final var users = userService.getAllUsernamesForCaseloadAndRole("NWEB", "KW_ADMIN");
        assertThat(users).containsExactly("API_TEST_USER", "ITAG_USER");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"MAINTAIN_ACCESS_ROLES"})
    public void addAndRemoveRoleFromStaffMember() {
        var users = userService.getAllUsernamesForCaseloadAndRole("NWEB", "LICENCE_CA");
        assertThat(users).hasSize(1);
        assertThat(users).containsExactly("CA_USER");

        final var added = userService.addAccessRole("ITAG_USER", "LICENCE_CA");
        assertThat(added).isTrue();

        users = userService.getAllUsernamesForCaseloadAndRole("NWEB", "LICENCE_CA");
        assertThat(users).hasSize(2);
        assertThat(users).containsExactly("CA_USER", "ITAG_USER");

        userService.removeUsersAccessRoleForCaseload("ITAG_USER", "NWEB", "LICENCE_CA");

        users = userService.getAllUsernamesForCaseloadAndRole("NWEB", "LICENCE_CA");
        assertThat(users).hasSize(1);
        assertThat(users).containsExactly("CA_USER");

    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"MAINTAIN_ACCESS_ROLES"})
    public void addUsersToNwebCaseload() {

        var numberAlloc = userService.addDefaultCaseloadForPrison("LEI");
        assertThat(numberAlloc.getNumUsersEnabled()).isEqualTo(2);

        numberAlloc = userService.addDefaultCaseloadForPrison("LEI");
        assertThat(numberAlloc.getNumUsersEnabled()).isEqualTo(0);
    }

}
