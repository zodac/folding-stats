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

package me.zodac.folding.test.util;

/**
 * Constants class for test convenience.
 */
public class TestConstants {

    // URL
    public static final String TEST_IP_ADDRESS = System.getProperty("testIpAddress", "127.0.0.1"); // NOPMD - IP address is only for tests
    public static final String TEST_SERVICE_URL = "http://" + TEST_IP_ADDRESS + ":8081";
    public static final String FOLDING_URL = TEST_SERVICE_URL + "/folding";

    // ID
    public static final int NON_EXISTING_ID = 9_999;
    public static final int OUT_OF_RANGE_ID = -1;
    public static final String INVALID_FORMAT_ID = "id";
}
