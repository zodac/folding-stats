package me.zodac.folding.test.util;

/**
 * Constants class for test convenience.
 */
public class TestConstants {

    // URL
    public static final String TEST_IP_ADDRESS = System.getProperty("testIpAddress");
    public static final String TEST_SERVICE_URL = "http://" + TEST_IP_ADDRESS + ":8081";
    public static final String FOLDING_URL = TEST_SERVICE_URL + "/folding";

    // ID
    public static final int NON_EXISTING_ID = 9_999;
    public static final int OUT_OF_RANGE_ID = -1;
    public static final String INVALID_FORMAT_ID = "id";
}
