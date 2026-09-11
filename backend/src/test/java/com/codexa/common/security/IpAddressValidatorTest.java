package com.codexa.common.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class IpAddressValidatorTest {

    @Test
    void shouldDetectLoopbackHosts() {
        assertThat(IpAddressValidator.isLoopback("localhost")).isTrue();
        assertThat(IpAddressValidator.isLoopback("127.0.0.1")).isTrue();
        assertThat(IpAddressValidator.isLoopback("::1")).isTrue();
        assertThat(IpAddressValidator.isLoopback("google.com")).isFalse();
    }

    @Test
    void shouldDetectPrivateAndCloudMetadataIps() {
        assertThat(IpAddressValidator.isPrivateIp("10.0.0.1")).isTrue();
        assertThat(IpAddressValidator.isPrivateIp("172.16.5.4")).isTrue();
        assertThat(IpAddressValidator.isPrivateIp("192.168.1.1")).isTrue();
        assertThat(IpAddressValidator.isPrivateIp("169.254.169.254")).isTrue(); // AWS metadata
        assertThat(IpAddressValidator.isPrivateIp("8.8.8.8")).isFalse(); // Public DNS
    }
}
