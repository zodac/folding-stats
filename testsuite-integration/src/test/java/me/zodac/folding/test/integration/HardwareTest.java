/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.test.integration;

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.rest.api.util.RestUtilConstants.GSON;
import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.INVALID_PASSWORD;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.integration.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.integration.util.SystemCleaner;
import me.zodac.folding.test.integration.util.TestConstants;
import me.zodac.folding.test.integration.util.TestGenerator;
import me.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import me.zodac.folding.test.integration.util.rest.request.UserUtils;
import me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the {@link Hardware} REST endpoint at {@code /folding/hardware}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HardwareTest {

    @BeforeAll
    static void setUp() throws FoldingRestException {
        SystemCleaner.cleanSystemForSimpleTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        SystemCleaner.cleanSystemForSimpleTests();
    }

    // Negative/alternative test cases

    @Test
    void whenCreatingHardware_givenHardwareWithTheSameNameAlreadyExists_then409ResponseIsReturned() throws FoldingRestException {
        final HardwareRequest hardwareToCreate = TestGenerator.generateHardware();
        HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(),
            ADMIN_USER.password()); // Send one request and ignore it (even if the user already exists, we can verify the conflict with the next one)
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingHardware_givenNonExistingHardwareId_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(TestConstants.NON_EXISTING_ID);

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: " + response.body())
            .isEmpty();
    }

    @Test
    void whenGettingHardware_givenNonExistingHardwareName_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get("nonExistingName");

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: " + response.body())
            .isEmpty();
    }

    @Test
    void whenGettingHardware_givenInvalidHardwareId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenCreatingHardware_givenTeamWithInvalidMultiplier_thenJsonResponseWithErrorIsReturned_andHas400Status() throws FoldingRestException {
        final String hardwareName = TestGenerator.nextHardwareName();
        final HardwareRequest hardwareRequest = HardwareRequest.builder()
            .hardwareName(hardwareName)
            .displayName(hardwareName)
            .hardwareMake(HardwareMake.NVIDIA.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(-1.00D)
            .averagePpd(1L)
            .build();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareRequest, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive expected error message in response")
            .contains("multiplier");
    }

    @Test
    void whenUpdatingHardware_givenNonExistingHardwareId_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final HardwareRequest updatedHardware = TestGenerator.generateHardware();
        final HttpResponse<String> response =
            HARDWARE_REQUEST_SENDER.update(TestConstants.NON_EXISTING_ID, updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: " + response.body())
            .isEmpty();
    }

    @Test
    void whenUpdatingHardware_givenInvalidHardwareId_thenResponseHas400Status() throws IOException, InterruptedException, FoldingRestException {
        final Hardware createdHardware = HardwareUtils.create(TestGenerator.generateHardware());

        final HardwareRequest updatedHardware = HardwareRequest.builder()
            .hardwareName(createdHardware.hardwareName())
            .displayName(createdHardware.displayName())
            .multiplier(createdHardware.multiplier())
            .build();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(updatedHardware)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenDeletingHardware_givenNonExistingHardwareId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<Void> response =
            HARDWARE_REQUEST_SENDER.delete(TestConstants.NON_EXISTING_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenDeletingHardware_givenInvalidHardwareId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenUpdatingHardware_givenValidHardwareId_andPayloadHasNoChanges_thenOriginalHardwareIsReturned_andHas200Status()
        throws FoldingRestException {
        final Hardware createdHardware = HardwareUtils.create(TestGenerator.generateHardware());

        final HardwareRequest updatedHardware = HardwareRequest.builder()
            .hardwareName(createdHardware.hardwareName())
            .displayName(createdHardware.displayName())
            .hardwareMake(createdHardware.hardwareMake().toString())
            .hardwareType(createdHardware.hardwareType().toString())
            .multiplier(createdHardware.multiplier())
            .averagePpd(createdHardware.averagePpd())
            .build();

        final HttpResponse<String> updateResponse =
            HARDWARE_REQUEST_SENDER.update(createdHardware.id(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(updateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareResponseParser.update(updateResponse);
        assertThat(actual.isEqualRequest(updatedHardware))
            .as("Did not receive the original hardware in response")
            .isTrue();
    }

    @Test
    void whenDeletingHardware_givenTheHardwareIsLinkedToUser_thenResponseHas409Status() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(TestGenerator.generateHardware()).id();
        final UserRequest user = TestGenerator.generateUserWithHardwareId(hardwareId);
        UserUtils.create(user);

        final HttpResponse<Void> deleteHardwareResponse = HARDWARE_REQUEST_SENDER.delete(hardwareId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(deleteHardwareResponse.statusCode())
            .as("Expected to fail due to a 409_CONFLICT: " + deleteHardwareResponse)
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingHardwareById_givenRequestUsesPreviousEntityTag_andHardwareHasNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(TestGenerator.generateHardware()).id();

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(hardwareId);
        assertThat(response.statusCode())
            .as("Expected first request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = HttpResponseHeaderUtils.getEntityTag(response);

        final HttpResponse<String> cachedResponse = HARDWARE_REQUEST_SENDER.get(hardwareId, eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HardwareResponseParser.get(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingAllHardware_givenRequestUsesPreviousEntityTag_andHardwareHasNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        HardwareUtils.create(TestGenerator.generateHardware());

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
            .as("Expected first GET request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = HttpResponseHeaderUtils.getEntityTag(response);

        final HttpResponse<String> cachedResponse = HARDWARE_REQUEST_SENDER.getAll(eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HardwareResponseParser.getAll(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenCreatingHardware_givenNoAuthentication_thenRequestFails_andResponseHas401Status() throws IOException, InterruptedException {
        final HardwareRequest hardwareToCreate = TestGenerator.generateHardware();

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(hardwareToCreate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenUpdatingHardware_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final Hardware createdHardware = HardwareUtils.create(TestGenerator.generateHardware());

        final HardwareRequest updatedHardware = HardwareRequest.builder()
            .hardwareName(createdHardware.hardwareName())
            .displayName(createdHardware.displayName())
            .multiplier(createdHardware.multiplier())
            .build();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(updatedHardware)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + createdHardware.id()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenDeletingHardware_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final int hardwareId = HardwareUtils.create(TestGenerator.generateHardware()).id();

        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingHardware_givenAuthentication_andAuthenticationHasInvalidUser_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final HardwareRequest hardwareToCreate = TestGenerator.generateHardware();
        final HttpResponse<String> response =
            HARDWARE_REQUEST_SENDER.create(hardwareToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingHardware_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final HardwareRequest hardwareToCreate = TestGenerator.generateHardware();
        final HttpResponse<String> response =
            HARDWARE_REQUEST_SENDER.create(hardwareToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingHardware_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHas403Status()
        throws FoldingRestException {
        final HardwareRequest hardwareToCreate = TestGenerator.generateHardware();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void whenCreatingHardware_givenEmptyPayload_thenRequestFails_andResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenUpdatingHardware_givenEmptyPayload_thenRequestFails_andResponseHas400Status()
        throws FoldingRestException, IOException, InterruptedException {
        final int hardwareId = HardwareUtils.create(TestGenerator.generateHardware()).id();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingHardware_andContentTypeIsNotJson_thenResponse415Status() throws IOException, InterruptedException {
        final HardwareRequest hardwareToCreate = TestGenerator.generateHardware();

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(hardwareToCreate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.TEXT.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 415_UNSUPPORTED_MEDIA_TYPE HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
    }
}
