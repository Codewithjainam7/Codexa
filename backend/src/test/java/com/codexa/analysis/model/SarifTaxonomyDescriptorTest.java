package com.codexa.analysis.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SarifTaxonomyDescriptorTest {

    @Test
    void shouldProduceStandardTaxonomies() {
        SarifTaxonomyDescriptor cwe = SarifTaxonomyDescriptor.cwe();
        assertThat(cwe.name()).isEqualTo("CWE");
        assertThat(cwe.organization()).isEqualTo("MITRE");

        SarifTaxonomyDescriptor owasp = SarifTaxonomyDescriptor.owasp();
        assertThat(owasp.name()).isEqualTo("OWASP Top 10");
        assertThat(owasp.organization()).isEqualTo("OWASP");
    }
}
