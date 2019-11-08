package net.syscon.elite.repository.v1;

import org.junit.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class NomisV1SQLErrorCodeTranslatorTest {
    private final NomisV1SQLErrorCodeTranslator translator = new NomisV1SQLErrorCodeTranslator();

    @Test
    public void customTranslate_errorCode() {
        final var accessException = translator.customTranslate("hello", "sql", new SQLException("reason", "state", 20040));
        assertThat(accessException).hasMessage("Sum of sub account balances not equal to current balance; nested exception is java.sql.SQLException: reason");

    }

    @Test
    public void customTranslate_notMapped() {
        final var accessException = translator.customTranslate("hello", "sql", new SQLException("reason"));
        assertThat(accessException).isNull();
    }
}
