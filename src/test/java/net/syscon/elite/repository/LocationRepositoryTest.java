package net.syscon.elite.repository;

import lombok.val;
import net.syscon.elite.api.model.Location;
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
import java.util.Set;

import static net.syscon.elite.repository.support.StatusFilter.ACTIVE_ONLY;
import static net.syscon.elite.repository.support.StatusFilter.ALL;
import static net.syscon.elite.repository.support.StatusFilter.INACTIVE_ONLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class LocationRepositoryTest {

    @Autowired
    private LocationRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void findLocationsByAgencyAndType() {
        final var result = repository.findLocationsByAgencyAndType("LEI", "CELL", false);
        assertThat(result).hasSize(30);
        assertThat(result.get(0).getLocationPrefix()).isEqualTo("LEI-A-1-1");
    }

    @Test
    public void findLocationIdWithFilter() {

        val activeAgencyLocationId = -1L;

        assertThat(repository.findLocation(activeAgencyLocationId, ALL)).isPresent();
        assertThat(repository.findLocation(activeAgencyLocationId, INACTIVE_ONLY)).isEmpty();
        assertThat(repository.findLocation(activeAgencyLocationId, ACTIVE_ONLY)).isPresent();

        val inactiveAgencyLocationId = -31L;

        assertThat(repository.findLocation(inactiveAgencyLocationId, ALL)).isPresent();
        assertThat(repository.findLocation(inactiveAgencyLocationId, INACTIVE_ONLY)).isPresent();
        assertThat(repository.findLocation(inactiveAgencyLocationId, ACTIVE_ONLY)).isEmpty();
    }

    @Test
    public void getLocationGroupData() {
        assertThat(repository.getLocationGroupData("LEI"))
                .contains(
                        Location.builder().locationId(-1L).locationType("WING").description("LEI-A").userDescription("Block A").internalLocationCode("A").build(),
                        Location.builder().locationId(-13L).locationType("WING").description("LEI-H").internalLocationCode("H").build()
                );
    }

    @Test
    public void getSubLocationGroupData() {
        final List<Location> subLocationGroupData = repository.getSubLocationGroupData(Set.of(-1L, -13L));
        assertThat(subLocationGroupData)
                .contains(
                        Location.builder().locationId(-14L).locationType("LAND").description("LEI-H-1").parentLocationId(-13L).userDescription("Landing H/1").internalLocationCode("1").build(),
                        Location.builder().locationId(-2L).locationType("LAND").description("LEI-A-1").parentLocationId(-1L).userDescription("Landing A/1").internalLocationCode("1").build(),
                        Location.builder().locationId(-32L).locationType("LAND").description("LEI-A-2").parentLocationId(-1L).userDescription("Landing A/2").internalLocationCode("2").build()
                );
    }
}
