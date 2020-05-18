package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.LanguageReferenceCode;
import net.syscon.elite.repository.jpa.model.OffenderLanguage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderLanguageRepositoryTest {

    @Autowired
    private OffenderLanguageRepository repository;

    @Test
    public void testGetLanguages() {
        assertThat(repository.findByOffenderBookId(-1L))
                .containsExactly(
                        OffenderLanguage.builder().offenderBookId(-1L).type("PREF_SPEAK").code("POL").writeSkill("N").readSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("POL", "Polish")).build()
                );

        assertThat(repository.findByOffenderBookId(-3L))
                .containsAnyOf(
                        OffenderLanguage.builder().offenderBookId(-3L).type("PREF_SPEAK").code("TUR").readSkill("N").readSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("TUR","Turkish")).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("PREF_SPEAK").code("ENG").readSkill("N").writeSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("ENG", "English")).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("SEC").code("ENG").readSkill("Y").writeSkill("Y").speakSkill("Y").referenceCode(new LanguageReferenceCode("EMG","English")).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("SEC").code("KUR").readSkill("N").writeSkill("N").speakSkill("Y").referenceCode(new LanguageReferenceCode("KUR", "Kurdish")).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("SEC").code("SPA").readSkill("N").writeSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("SPA","Spanish; Castilian")).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("PREF_WRITE").code("TUR").readSkill("N").writeSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("TUR","Turkish")).build()
                );
    }
}