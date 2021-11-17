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

package me.zodac.folding.test.stub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

/**
 * {@link SpringBootTest.WebEnvironment} integration test for stubbed endpoints, until the testsuite is migrated.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StubIntegrationTest {

    @Autowired
    private TestRestTemplate template;

    @Test
    void lars() {
        final ResponseEntity<String> response = template.getForEntity("/gpu_ppd/overall_ranks/", String.class);
        assertThat(response.getBody())
            .isEqualTo("<html><table id=\"primary-database\"></table></html>");
    }

    @Test
    void points() {
        final ResponseEntity<String> response = template.getForEntity("/user/foldingUserName/stats?passkey=passkey", String.class);
        assertThat(response.getBody())
            .isEqualTo("{\"earned\":0}");
    }

    @Test
    void units() {
        final ResponseEntity<String> response = template.getForEntity("/bonus?user=user&passkey=passkey", String.class);
        assertThat(response.getBody())
            .isEqualTo("[]");
    }
}