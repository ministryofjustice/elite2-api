package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import net.syscon.elite.Elite2ApiServer;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { Elite2ApiServer.class })
public class OffenderPendingDeletionRepositoryTest {

    private static final LocalDate SENTENCE_END_DATE = LocalDate.of(2020, 3, 24);
    private static final LocalDate DELETION_DUE_DATE = SENTENCE_END_DATE.plusYears(7);

    @Autowired
    private OffenderPendingDeletionRepository repository;

    @Test
    public void getOffendersDueForDeletion() {

        var offenders = repository.getOffendersDueForDeletionBetween(DELETION_DUE_DATE.minusDays(1), DELETION_DUE_DATE.plusDays(1));

        assertThat(offenders).hasSize(1);
        assertThat(offenders).extracting(OffenderPendingDeletion::getOffenderNumber).containsOnly("Z0020ZZ");
    }

    @Test
    public void getOffendersDueForDeletionUsesDatesInclusively() {

        var offenders = repository.getOffendersDueForDeletionBetween(DELETION_DUE_DATE, DELETION_DUE_DATE);

        assertThat(offenders).hasSize(1);
        assertThat(offenders).extracting(OffenderPendingDeletion::getOffenderNumber).containsOnly("Z0020ZZ");
    }

    @Test
    public void getOffendersDueForDeletionReturnsEmpty() {
        assertThat(repository.getOffendersDueForDeletionBetween(LocalDate.of(1970, 1, 1), LocalDate.of(1970, 1, 1))).isEmpty();
    }

    @Test
    public void getOffendersDueForDeletionReturnsEmptyForDatesWrongWayRound() {
        assertThat(repository.getOffendersDueForDeletionBetween(DELETION_DUE_DATE.plusDays(1), DELETION_DUE_DATE.minusDays(1))).isEmpty();
    }
}