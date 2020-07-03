package net.syscon.prison.repository;

import net.syscon.prison.api.model.CaseLoad;
import net.syscon.prison.web.config.PersistenceConfigs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class CaseLoadRepositoryTest {
    private static final String TEST_USERNAME = "ITAG_USER";

    @Autowired
    private CaseLoadRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(TEST_USERNAME, "password"));
    }

    @Test
    public void testGetCaseLoad() {
        final var caseLoad = repository.getCaseLoad("LEI");

        assertThat(caseLoad).get().extracting(CaseLoad::getDescription).isEqualTo("Leeds (HMP)");
    }

    @Test
    public void testGetAllCaseLoadsByUsername() {
        final var caseLoads = repository.getAllCaseLoadsByUsername(TEST_USERNAME);
        assertThat(caseLoads).extracting(CaseLoad::getCaseLoadId).containsOnly("LEI", "BXI", "MDI", "RNI", "SYI", "WAI", "NWEB");
    }

    @Test
    public void testGetCaseLoadsByUsername() {
        final var caseLoads = repository.getCaseLoadsByUsername(TEST_USERNAME);
        assertThat(caseLoads).extracting(CaseLoad::getCaseLoadId).containsOnly("LEI", "BXI", "MDI", "RNI", "SYI", "WAI");
    }

    @Test
    public void testGetCaseLoadsByUsername_CurrentActive() {
        final var caseLoads = repository.getCaseLoadsByUsername(TEST_USERNAME);
        final var activeCaseLoads = caseLoads.stream().filter(CaseLoad::isCurrentlyActive).collect(Collectors.toList());
        assertThat(activeCaseLoads).extracting(CaseLoad::getCaseLoadId).containsOnly("LEI");
    }

    @Test
    public void testGetCaseLoadsByUsername_CurrentInactive() {
        final var caseLoads = repository.getCaseLoadsByUsername(TEST_USERNAME);
        final var activeCaseLoads = caseLoads.stream().filter(cl -> !cl.isCurrentlyActive()).collect(Collectors.toList());
        assertThat(activeCaseLoads).extracting(CaseLoad::getCaseLoadId).containsOnly("BXI", "MDI", "RNI", "SYI", "WAI");
    }

    @Test
    public void testGetWorkingCaseLoadByUsername() {
        final var caseLoad = repository.getWorkingCaseLoadByUsername(TEST_USERNAME);

        assertThat(caseLoad).get().extracting(CaseLoad::getDescription).isEqualTo("Leeds (HMP)");
    }

}