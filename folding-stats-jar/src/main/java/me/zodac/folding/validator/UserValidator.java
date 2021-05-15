package me.zodac.folding.validator;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class UserValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserValidator.class);
    private static final UrlValidator URL_VALIDATOR = new UrlValidator();

    private UserValidator() {

    }

    public static ValidationResponse isValid(final User user) {
        final List<String> failureMessages = new ArrayList<>(6);

        if (Category.INVALID == Category.get(user.getCategory())) {
            failureMessages.add(String.format("Attribute 'category' must be one of: %s", Category.getAllValues()));
        }

        if (StringUtils.isBlank(user.getFoldingUserName())) {
            failureMessages.add("Attribute 'foldingUserName' must not be empty");
        }

        if (StringUtils.isBlank(user.getDisplayName())) {
            failureMessages.add("Attribute 'displayName' must not be empty");
        }

        if (StringUtils.isBlank(user.getPasskey())) {
            failureMessages.add("Attribute 'passkey' must not be empty");
        }

        if (StringUtils.isNotEmpty(user.getLiveStatsLink())) {
            if (!URL_VALIDATOR.isValid(user.getLiveStatsLink())) {
                failureMessages.add(String.format("Attribute 'liveStatsLink' is not a valid link: '%s'", user.getLiveStatsLink()));
            }
        }

        if (user.getHardwareId() <= Hardware.EMPTY_HARDWARE_ID || HardwareCache.get().doesNotContain(user.getHardwareId())) {
            final List<String> availableHardware = HardwareCache.get()
                    .getAll()
                    .stream()
                    .map(hardware -> String.format("%s: %s", hardware.getId(), hardware.getHardwareName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'hardwareId' must be one of: %s", availableHardware));
        }

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            try {
                final int unitsForUserAndPasskey = FoldingStatsParser.getUnitsForUser(user.getFoldingUserName(), user.getPasskey());

                if (unitsForUserAndPasskey == 0) {
                    failureMessages.add(String.format("User '%s' has 0 completed Work Units with passkey '%s', there must be at least one valid Work Unit submitted on the passkey before adding the user",
                            user.getFoldingUserName(),
                            user.getPasskey()
                    ));
                }
            } catch (final Exception e) {
                LOGGER.warn("Unable to get Folding stats for user {}", user, e);
                failureMessages.add("Unable to check stats for user");
            }
        }

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(user, failureMessages);
    }
}
