/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
