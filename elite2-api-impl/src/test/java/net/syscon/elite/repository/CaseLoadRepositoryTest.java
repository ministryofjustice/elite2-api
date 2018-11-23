package net.syscon.elite.repository;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.web.config.PersistenceConfigs;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
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
        Optional<CaseLoad> caseLoad = repository.getCaseLoad("LEI");

        assertThat(caseLoad.isPresent()).isTrue();
        assertThat(caseLoad.get().getDescription()).isEqualTo("LEEDS (HMP)");
    }
    
    @Test
    public void testGetCaseLoadsByUsername() {
        List<CaseLoad> caseLoads = repository.getCaseLoadsByUsername(TEST_USERNAME, "type:eq:'INST'");

        assertThat(caseLoads).isNotEmpty();
        assertThat(caseLoads).hasSize(5);
        assertThat(caseLoads).extracting("caseLoadId").contains("LEI", "BXI", "MDI", "SYI", "WAI");
    }
    
    @Test
    public void testGetWorkingCaseLoadByUsername() {
        Optional<CaseLoad> caseLoad = repository.getWorkingCaseLoadByUsername(TEST_USERNAME);

        assertThat(caseLoad.isPresent()).isTrue();
        assertThat(caseLoad.get().getDescription()).isEqualTo("LEEDS (HMP)");
    }

}
