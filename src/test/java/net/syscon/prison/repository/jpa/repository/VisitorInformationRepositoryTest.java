package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.VisitorInformation;
import net.syscon.prison.security.AuthenticationFacade;
import net.syscon.prison.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class VisitorInformationRepositoryTest {

    @Autowired
    private VisitorRepository repository;

    @Test
    public void findAllByVisitIdAndBookingId() {
        var visits = repository.findAllByVisitIdAndBookingId(-15L, -1L);

        assertThat(visits).hasSize(2);
        assertThat(visits).extracting(VisitorInformation::getPersonId).containsOnly(-1L, -2L);
        assertThat(visits).extracting(VisitorInformation::getFirstName).containsOnly("JESSY", "John");
        assertThat(visits).extracting(VisitorInformation::getLastName).containsOnly("SMITH1", "Smith");
        assertThat(visits).extracting(VisitorInformation::getRelationship).containsOnly("Uncle", "Community Offender Manager");
    }
}

