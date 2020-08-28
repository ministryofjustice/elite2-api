package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class BedAssignmentHistoriesRepositoryTest {

    @Autowired
    private BedAssignmentHistoriesRepository repository;

    @Test
    public void getMaxSeqForBookingId_noRecords() {
        assertThat(repository.getMaxSeqForBookingId(-2L)).isEqualTo(0);
    }

    @Test
    public void getMaxSeqForBookingId_singleRecord() {
        createBedAssignmentHistories(-3L, 1);

        assertThat(repository.getMaxSeqForBookingId(-3L)).isEqualTo(1);
    }

    @Test
    public void getMaxSeqForBookingId_severalRecords() {
        createBedAssignmentHistories(-4L, 4);

        assertThat(repository.getMaxSeqForBookingId(-4L)).isEqualTo(4);
    }

    @Test
    public void findBadAssignmentHistory_forLocationAndDatePeriod() {
        final var cellHistory =
                repository.findByLivingUnitIdAndDateRange(
                        -16,
                        LocalDate.of(2000,10,16),
                        LocalDate.of(2020,10,10)
                );

        assertThat(cellHistory).containsExactlyInAnyOrder(
                BedAssignmentHistory.builder()
                        .livingUnitId(-16L)
                        .assignmentDate(LocalDate.of(2019,10,17))
                        .assignmentDateTime(LocalDateTime.of(LocalDate.of(2019,10,17), LocalTime.of(11,0)))
                        .assignmentEndDate(LocalDate.of(2020,1,1))
                        .assignmentReason("ADM")
                        .build(),
                BedAssignmentHistory.builder()
                        .livingUnitId(-16L)
                        .assignmentDate(LocalDate.of(2020,4,3))
                        .assignmentDateTime(LocalDateTime.of(LocalDate.of(2020,4,3), LocalTime.of(11,0)))
                        .assignmentReason("ADM")
                        .build());
    }

    private void createBedAssignmentHistories(Long bookingId, Integer numberRecords) {
        IntStream.rangeClosed(1, numberRecords).forEach(seq -> {
            final var bookingAndSequence = new BedAssignmentHistory.BedAssignmentHistoryPK(bookingId, seq);
            final var bedAssignmentHistory =
                    BedAssignmentHistory.builder()
                            .bedAssignmentHistoryPK(bookingAndSequence)
                            .livingUnitId(2L)
                            .assignmentDate(LocalDate.now())
                            .assignmentDateTime(LocalDateTime.now())
                            .build();
            repository.save(bedAssignmentHistory);
        });
    }
}
