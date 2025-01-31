/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.test.integration;

import static net.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;
import static net.zodac.folding.rest.api.util.RestUtilConstants.GSON;
import static net.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static net.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static net.zodac.folding.test.integration.util.DummyAuthenticationData.INVALID_PASSWORD;
import static net.zodac.folding.test.integration.util.DummyAuthenticationData.INVALID_USERNAME;
import static net.zodac.folding.test.integration.util.DummyAuthenticationData.READ_ONLY_USER;
import static net.zodac.folding.test.integration.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.HardwareMake;
import net.zodac.folding.api.tc.HardwareType;
import net.zodac.folding.client.java.response.HardwareResponseParser;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.rest.api.header.ContentType;
import net.zodac.folding.rest.api.header.RestHeader;
import net.zodac.folding.rest.api.tc.request.HardwareRequest;
import net.zodac.folding.rest.api.tc.request.UserRequest;
import net.zodac.folding.test.integration.util.DummyDataGenerator;
import net.zodac.folding.test.integration.util.SystemCleaner;
import net.zodac.folding.test.integration.util.TestConstants;
import net.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import net.zodac.folding.test.integration.util.rest.request.UserUtils;
import net.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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

    @Test
    @Order(1)
    void whenGettingAllHardware_givenNoHardwareHasBeenCreated_thenEmptyJsonResponseIsReturned_andHas200Status() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<Hardware> allHardware = HardwareResponseParser.getAll(response);
        final int xTotalCount = HttpResponseHeaderUtils.getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(allHardware.size());

        assertThat(allHardware)
            .isEmpty();
    }

    @Test
    void whenGetAllHardware_givenHardwareHasBeenCreated_thenAllAreReturned_andHas200Status() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(DummyDataGenerator.generateHardware());
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<Hardware> allHardware = HardwareResponseParser.getAll(response);
        final int xTotalCount = HttpResponseHeaderUtils.getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(allHardware.size());

        assertThat(allHardware)
            .contains(hardware);
    }

    @Test
    void whenCreatingHardware_givenPayloadIsValid_thenTheCreatedHardwareIsReturnedInResponse_andHasId_andHas201Status()
        throws FoldingRestException {
        final HardwareRequest hardwareToCreate = DummyDataGenerator.generateHardware();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 201_CREATED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final Hardware actual = HardwareResponseParser.create(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: %s", response.body())
            .extracting("hardwareName", "displayName", "multiplier")
            .containsExactly(hardwareToCreate.hardwareName(), hardwareToCreate.displayName(), hardwareToCreate.multiplier());
    }

    @Test
    void whenGetHardware_givenValidHardwareId_thenHardwareIsReturned_andHas200Status() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(DummyDataGenerator.generateHardware()).id();

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(hardwareId);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware hardware = HardwareResponseParser.get(response);
        assertThat(hardware.id())
            .as("Did not receive the expected hardware: %s", response.body())
            .isEqualTo(hardwareId);
    }

    @Test
    void whenGetHardware_givenValidHardwareName_thenHardwareIsReturned_andHas200Status() throws FoldingRestException {
        final String hardwareName = HardwareUtils.create(DummyDataGenerator.generateHardware()).hardwareName();

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(hardwareName);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware hardware = HardwareResponseParser.get(response);
        assertThat(hardware.hardwareName())
            .as("Did not receive the expected hardware: %s", response.body())
            .isEqualTo(hardwareName);
    }

    @Test
    void whenUpdatingHardware_givenValidHardwareIdAndPayload_thenUpdatedHardwareIsReturned_andNoNewHardwareIsCreated_andHas200Status()
        throws FoldingRestException {
        final Hardware createdHardware = HardwareUtils.create(DummyDataGenerator.generateHardware());
        final int initialSize = HardwareUtils.getNumberOfHardware();

        final HardwareRequest updatedHardware = generateHardwareRequest(
            createdHardware.hardwareName(),
            createdHardware.hardwareMake(),
            createdHardware.hardwareType(),
            createdHardware.multiplier(),
            createdHardware.averagePpd()
        );

        final HttpResponse<String> response =
            HARDWARE_REQUEST_SENDER.update(createdHardware.id(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareResponseParser.update(response);
        assertThat(actual.isEqualRequest(updatedHardware))
            .as("Did not receive created object as JSON response: %s", response.body())
            .isTrue();

        final int allHardwareAfterUpdate = HardwareUtils.getNumberOfHardware();
        assertThat(allHardwareAfterUpdate)
            .as("Expected no new hardware instances to be created")
            .isEqualTo(initialSize);
    }

    @Test
    void whenDeletingHardware_givenValidId_thenHardwareIsDeleted_andHas200Status_andCountIsReduced_andHardwareCannotBeRetrievedAgain()
        throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(DummyDataGenerator.generateHardware()).id();
        final int initialSize = HardwareUtils.getNumberOfHardware();

        final HttpResponse<Void> response = HARDWARE_REQUEST_SENDER.delete(hardwareId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final HttpResponse<String> getResponse = HARDWARE_REQUEST_SENDER.get(hardwareId);
        assertThat(getResponse.statusCode())
            .as("Was able to retrieve the hardware instance, despite deleting it")
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        final int newSize = HardwareUtils.getNumberOfHardware();
        assertThat(newSize)
            .as("Get all response did not return (initial hardware - deleted hardware)")
            .isEqualTo(initialSize - 1);
    }

    // Negative/alternative test cases

    @Test
    void whenCreatingHardware_givenHardwareWithTheSameNameAlreadyExists_then409ResponseIsReturned() throws FoldingRestException {
        final HardwareRequest hardwareToCreate = DummyDataGenerator.generateHardware();
        HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(),
            ADMIN_USER.password()); // Send one request and ignore it (even if the user already exists, we can verify the conflict with the next one)
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 409_CONFLICT HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingHardware_givenNonExistingHardwareId_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(TestConstants.NON_EXISTING_ID);

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: %s", response.body())
            .isEmpty();
    }

    @Test
    void whenGettingHardware_givenNonExistingHardwareName_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get("nonExistingName");

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: %s", response.body())
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
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: %s", response.body())
            .contains("not a valid format");
    }

    @Test
    void whenCreatingHardware_givenTeamWithInvalidMultiplier_thenJsonResponseWithErrorIsReturned_andHas400Status() throws FoldingRestException {
        final String hardwareName = DummyDataGenerator.nextHardwareName();
        final HardwareRequest hardwareRequest = generateHardwareRequest(
            hardwareName,
            HardwareMake.NVIDIA,
            HardwareType.GPU,
            -1.00D,
            1L
        );
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareRequest, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive expected error message in response")
            .contains("multiplier");
    }

    @Test
    void whenUpdatingHardware_givenNonExistingHardwareId_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final HardwareRequest updatedHardware = DummyDataGenerator.generateHardware();
        final HttpResponse<String> response =
            HARDWARE_REQUEST_SENDER.update(TestConstants.NON_EXISTING_ID, updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: %s", response.body())
            .isEmpty();
    }

    @Test
    void whenUpdatingHardware_givenInvalidHardwareId_thenResponseHas400Status() throws IOException, InterruptedException, FoldingRestException {
        final Hardware createdHardware = HardwareUtils.create(DummyDataGenerator.generateHardware());

        final HardwareRequest updatedHardware = generateHardwareRequest(
            createdHardware.hardwareName(),
            createdHardware.hardwareMake(),
            createdHardware.hardwareType(),
            createdHardware.multiplier(),
            createdHardware.averagePpd()
        );

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(updatedHardware)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: %s", response.body())
            .contains("not a valid format");
    }

    @Test
    void whenDeletingHardware_givenNonExistingHardwareId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<Void> response =
            HARDWARE_REQUEST_SENDER.delete(TestConstants.NON_EXISTING_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
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
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: %s", response.body())
            .contains("not a valid format");
    }

    @Test
    void whenUpdatingHardware_givenValidHardwareId_andPayloadHasNoChanges_thenOriginalHardwareIsReturned_andHas200Status()
        throws FoldingRestException {
        final Hardware createdHardware = HardwareUtils.create(DummyDataGenerator.generateHardware());

        final HardwareRequest updatedHardware = generateHardwareRequest(
            createdHardware.hardwareName(),
            createdHardware.hardwareMake(),
            createdHardware.hardwareType(),
            createdHardware.multiplier(),
            createdHardware.averagePpd()
        );

        final HttpResponse<String> updateResponse =
            HARDWARE_REQUEST_SENDER.update(createdHardware.id(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(updateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", updateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareResponseParser.update(updateResponse);
        assertThat(actual.isEqualRequest(updatedHardware))
            .as("Did not receive the original hardware in response")
            .isTrue();
    }

    @Test
    void whenDeletingHardware_givenTheHardwareIsLinkedToUser_thenResponseHas409Status() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(DummyDataGenerator.generateHardware()).id();
        final UserRequest user = DummyDataGenerator.generateUserWithHardwareId(hardwareId);
        UserUtils.create(user);

        final HttpResponse<Void> deleteHardwareResponse = HARDWARE_REQUEST_SENDER.delete(hardwareId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(deleteHardwareResponse.statusCode())
            .as("Expected to fail due to a 409_CONFLICT: %s", deleteHardwareResponse)
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingHardwareById_givenRequestUsesPreviousEntityTag_andHardwareHasNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(DummyDataGenerator.generateHardware()).id();

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
        HardwareUtils.create(DummyDataGenerator.generateHardware());

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
        final HardwareRequest hardwareToCreate = DummyDataGenerator.generateHardware();

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(hardwareToCreate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenUpdatingHardware_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final Hardware createdHardware = HardwareUtils.create(DummyDataGenerator.generateHardware());

        final HardwareRequest updatedHardware = generateHardwareRequest(
            createdHardware.hardwareName(),
            createdHardware.hardwareMake(),
            createdHardware.hardwareType(),
            createdHardware.multiplier(),
            createdHardware.averagePpd()
        );

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(updatedHardware)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + createdHardware.id()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenDeletingHardware_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final int hardwareId = HardwareUtils.create(DummyDataGenerator.generateHardware()).id();

        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingHardware_givenAuthentication_andAuthenticationHasInvalidUser_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final HardwareRequest hardwareToCreate = DummyDataGenerator.generateHardware();
        final HttpResponse<String> response =
            HARDWARE_REQUEST_SENDER.create(hardwareToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingHardware_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final HardwareRequest hardwareToCreate = DummyDataGenerator.generateHardware();
        final HttpResponse<String> response =
            HARDWARE_REQUEST_SENDER.create(hardwareToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingHardware_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHas403Status()
        throws FoldingRestException {
        final HardwareRequest hardwareToCreate = DummyDataGenerator.generateHardware();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 403_FORBIDDEN HTTP response: %s", response.body())
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
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenUpdatingHardware_givenEmptyPayload_thenRequestFails_andResponseHas400Status()
        throws FoldingRestException, IOException, InterruptedException {
        final int hardwareId = HardwareUtils.create(DummyDataGenerator.generateHardware()).id();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware/" + hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingHardware_andContentTypeIsNotJson_thenResponse415Status() throws IOException, InterruptedException {
        final HardwareRequest hardwareToCreate = DummyDataGenerator.generateHardware();

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(hardwareToCreate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/hardware"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.TEXT.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 415_UNSUPPORTED_MEDIA_TYPE HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
    }

    private static HardwareRequest generateHardwareRequest(final String hardwareName,
                                                           final HardwareMake hardwareMake,
                                                           final HardwareType hardwareType,
                                                           final double multiplier,
                                                           final long averagePpd
    ) {
        return new HardwareRequest(
            hardwareName,
            hardwareName,
            hardwareMake.toString(),
            hardwareType.toString(),
            multiplier,
            averagePpd
        );
    }
}
