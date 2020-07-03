package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderKeyDateAdjustment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderKeyDateAdjustmentRepositoryTest {

    @Autowired
    private OffenderKeyDateAdjustmentRepository repository;

    @Test
    public void findAllForBooking() {
        final var expected = List.of(
                            OffenderKeyDateAdjustment
                                    .builder()
                                    .id(-8L)
                                    .sentenceAdjustCode("ADA")
                                    .activeFlag(ActiveFlag.Y)
                                    .offenderBookId(-6L)
                                    .adjustDays(4)
                                    .build(),
                            OffenderKeyDateAdjustment
                                    .builder()
                                    .id(-9L)
                                    .sentenceAdjustCode("ADA")
                                    .activeFlag(ActiveFlag.N)
                                    .offenderBookId(-6L)
                                    .adjustDays(9)
                                    .build(),
                            OffenderKeyDateAdjustment
                                    .builder()
                                    .id(-10L)
                                    .sentenceAdjustCode("ADA")
                                    .activeFlag(ActiveFlag.Y)
                                    .offenderBookId(-6L)
                                    .adjustDays(13)
                                    .build(),
                            OffenderKeyDateAdjustment
                                    .builder()
                                    .id(-11L)
                                    .sentenceAdjustCode("UAL")
                                    .activeFlag(ActiveFlag.N)
                                    .offenderBookId(-6L)
                                    .adjustDays(1)
                                    .build(),
                            OffenderKeyDateAdjustment
                                    .builder()
                                    .id(-12L)
                                    .sentenceAdjustCode("RADA")
                                    .activeFlag(ActiveFlag.Y)
                                    .offenderBookId(-6L)
                                    .adjustDays(2)
                                    .build(),
                            OffenderKeyDateAdjustment
                                    .builder()
                                    .id(-13L)
                                    .sentenceAdjustCode("UAL")
                                    .activeFlag(ActiveFlag.Y)
                                    .offenderBookId(-6L)
                                    .adjustDays(7)
                                    .build()
                            );

        final var keyDateAdjustments = repository.findAllByOffenderBookId(-6L);

        assertThat(keyDateAdjustments).isEqualTo(expected);
    }
}
