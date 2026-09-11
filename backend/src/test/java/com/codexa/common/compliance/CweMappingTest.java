package com.codexa.common.compliance;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CweMappingTest {

    @Test
    void shouldFormatCweIdAndTaxonomyUri() {
        CweMappingRecord record = CweMappingRecord.of(89, "Improper Neutralization of Special Elements used in an SQL Command", OwaspTop10Mapping.A03_INJECTION);
        assertThat(record.getFormattedId()).isEqualTo("CWE-89");
        assertThat(record.taxonomyUri()).isEqualTo("https://cwe.mitre.org/data/definitions/89.html");
        assertThat(record.owaspCategory()).isEqualTo(OwaspTop10Mapping.A03_INJECTION);
    }
}
