package com.codexa.analysis.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CsvFormulaSanitizerTest {

    @Test
    void shouldPrefixFormulaTriggersWithSingleQuote() {
        assertThat(CsvFormulaSanitizer.sanitize("=CMD('calc')")).isEqualTo("'=CMD('calc')");
        assertThat(CsvFormulaSanitizer.sanitize("+1234")).isEqualTo("'+1234");
        assertThat(CsvFormulaSanitizer.sanitize("-SUM(A1:A10)")).isEqualTo("'-SUM(A1:A10)");
        assertThat(CsvFormulaSanitizer.sanitize("@SUM(B1:B10)")).isEqualTo("'@SUM(B1:B10)");
    }

    @Test
    void shouldLeaveSafeStringsUntouched() {
        assertThat(CsvFormulaSanitizer.sanitize("Clean Finding Title")).isEqualTo("Clean Finding Title");
        assertThat(CsvFormulaSanitizer.sanitize("")).isEqualTo("");
        assertThat(CsvFormulaSanitizer.sanitize(null)).isEqualTo("");
    }
}
