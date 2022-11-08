/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.api.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EnvironmentVariableUtils}.
 */
class EnvironmentVariablesUtilsTest {

    private static int systemPropertyNameIncrementer = 1;

    @Test
    void whenGetVariable_givenVariableIsSetAsSystemProperty_thenValueIsReturned() {
        final String propertyName = getSystemPropertyName();

        System.setProperty(propertyName, "value");
        final String result = EnvironmentVariableUtils.get(propertyName);
        assertThat(result)
            .isEqualTo("value");
    }

    @Test
    void whenGetVariable_givenVariableIsNotSetAsSystemProperty_andIsSetAsEnvVariable_thenValueIsReturned() {
        final Map.Entry<String, String> environmentVariable = getEnvironmentVariableThatIsNotAlsoSystemProperty();

        final String result = EnvironmentVariableUtils.get(environmentVariable.getKey());
        assertThat(result)
            .isEqualTo(environmentVariable.getValue());
    }

    @Test
    void whenGetVariable_givenVariableIsNotSetAsSystemProperty_andIsNotSetAsEnvVariable_thenNullIsReturned() {
        final String result = EnvironmentVariableUtils.get("invalid");
        assertThat(result)
            .isNull();
    }

    @Test
    void whenGetVariableWithDefault_givenVariableIsSetAsSystemProperty_thenValueIsReturned() {
        final String propertyName = getSystemPropertyName();

        System.setProperty(propertyName, "value");
        final String result = EnvironmentVariableUtils.getOrDefault(propertyName, "defaultValue");
        assertThat(result)
            .isEqualTo("value");
    }

    @Test
    void whenGetVariableWithDefault_givenVariableIsNotSetAsSystemProperty_andIsSetAsEnvVariable_thenValueIsReturned() {
        final Map.Entry<String, String> environmentVariable = getEnvironmentVariableThatIsNotAlsoSystemProperty();

        final String result = EnvironmentVariableUtils.getOrDefault(environmentVariable.getKey(), "defaultValue");
        assertThat(result)
            .isEqualTo(environmentVariable.getValue());
    }

    @Test
    void whenGetVariableWithDefault_givenVariableIsNotSetAsSystemProperty_andIsNotSetAsEnvVariable_thenDefaultValueIsReturned() {
        final String result = EnvironmentVariableUtils.getOrDefault("invalid", "defaultValue");
        assertThat(result)
            .isEqualTo("defaultValue");
    }

    @Test
    void whenIsEnabled_givenVariableIsNotSet_thenFalseIsReturned() {
        final String propertyName = getSystemPropertyName();

        final boolean result = EnvironmentVariableUtils.isEnabled(propertyName);
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsEnabled_givenVariableIsSet_andValueIsNotValidBoolean_thenFalseIsReturned() {
        final String propertyName = getSystemPropertyName();

        System.setProperty(propertyName, "value");
        final boolean result = EnvironmentVariableUtils.isEnabled(propertyName);
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsEnabled_givenVariableIsSet_andValueIsPositiveBoolean_thenTrueIsReturned() {
        final String propertyName = getSystemPropertyName();

        System.setProperty(propertyName, "tRuE");
        final boolean result = EnvironmentVariableUtils.isEnabled(propertyName);
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsDisabled_givenVariableIsNotSet_thenTrueIsReturned() {
        final String propertyName = getSystemPropertyName();

        final boolean result = EnvironmentVariableUtils.isDisabled(propertyName);
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsDisabled_givenVariableIsSet_andValueIsNotValidBoolean_thenTrueIsReturned() {
        final String propertyName = getSystemPropertyName();

        System.setProperty(propertyName, "value");
        final boolean result = EnvironmentVariableUtils.isDisabled(propertyName);
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsDisabled_givenVariableIsSet_andValueIsPositiveBoolean_thenFalseIsReturned() {
        final String propertyName = getSystemPropertyName();

        System.setProperty(propertyName, "tRuE");
        final boolean result = EnvironmentVariableUtils.isDisabled(propertyName);
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenGetIntOrDefault_givenVariableIsNotSet_thenDefaultIsReturned() {
        final String propertyName = getSystemPropertyName();

        final int result = EnvironmentVariableUtils.getIntOrDefault(propertyName, 5);
        assertThat(result)
            .isEqualTo(5);
    }

    @Test
    void whenGetIntOrDefault_givenVariableIsSet_andValueIsNotValidInt_thenDefaultIsReturned() {
        final String propertyName = getSystemPropertyName();

        System.setProperty(propertyName, "value");
        final int result = EnvironmentVariableUtils.getIntOrDefault(propertyName, 5);
        assertThat(result)
            .isEqualTo(5);
    }

    @Test
    void whenGetBoolean_givenVariableIsSet_andValueIsValidInt_thenValueIsReturned() {
        final String propertyName = getSystemPropertyName();

        System.setProperty(propertyName, "20");
        final int result = EnvironmentVariableUtils.getIntOrDefault(propertyName, 5);
        assertThat(result)
            .isEqualTo(20);
    }

    private static Map.Entry<String, String> getEnvironmentVariableThatIsNotAlsoSystemProperty() {
        final Set<String> systemProperties = System.getProperties().keySet().stream().map(Object::toString).collect(Collectors.toSet());
        final Set<String> environmentVariables = new HashSet<>(System.getenv().keySet());

        environmentVariables.removeAll(systemProperties);

        final String environmentVariableName = environmentVariables.iterator().next();
        return Map.of(
                environmentVariableName, System.getenv(environmentVariableName)
            )
            .entrySet().iterator().next();
    }

    private static String getSystemPropertyName() {
        return "property_" + systemPropertyNameIncrementer++;
    }
}
