package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.OffenderSentenceAdjustment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderSentenceAdjustmentRepositoryTest {

    @Autowired
    private OffenderSentenceAdjustmentRepository repository;

    @Test
    public void findAllForBooking() {
        final var expected = List.of(
                    OffenderSentenceAdjustment.builder()
                            .id(-8L)
                            .offenderBookId(-6L)
                            .sentenceAdjustCode("RSR")
                            .activeFlag(ActiveFlag.Y)
                            .adjustDays(4)
                            .build(),
                    OffenderSentenceAdjustment.builder()
                            .id(-9L)
                            .offenderBookId(-6L)
                            .sentenceAdjustCode("RST")
                            .activeFlag(ActiveFlag.N)
                            .adjustDays(4)
                            .build(),
                    OffenderSentenceAdjustment.builder()
                            .id(-10L)
                            .offenderBookId(-6L)
                            .sentenceAdjustCode("RX")
                            .activeFlag(ActiveFlag.Y)
                            .adjustDays(4)
                            .build(),
                    OffenderSentenceAdjustment.builder()
                            .id(-11L)
                            .offenderBookId(-6L)
                            .sentenceAdjustCode("S240A")
                            .activeFlag(ActiveFlag.N)
                            .adjustDays(4)
                            .build(),
                    OffenderSentenceAdjustment.builder()
                            .id(-12L)
                            .offenderBookId(-6L)
                            .sentenceAdjustCode("UR")
                            .activeFlag(ActiveFlag.Y)
                            .adjustDays(4)
                            .build(),
                    OffenderSentenceAdjustment.builder()
                            .id(-13L)
                            .offenderBookId(-6L)
                            .sentenceAdjustCode("RX")
                            .activeFlag(ActiveFlag.Y)
                            .adjustDays(4)
                            .build()
                );

        final var sentenceAdjustments = repository.findAllByOffenderBookId(-6L);

        assertThat(sentenceAdjustments).isEqualTo(expected);
    }
}
