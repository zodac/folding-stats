package me.zodac.folding.validator;

import me.zodac.folding.api.Category;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class FoldingUserValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoldingUserValidator.class);

    private FoldingUserValidator() {

    }

    public static ValidationResponse isValid(final FoldingUser foldingUser) {
        final List<String> failureMessages = new ArrayList<>(6);

        if (Category.INVALID == Category.get(foldingUser.getCategory())) {
            failureMessages.add(String.format("Attribute 'category' must be one of: %s", Category.getValuesAsList()));
        }

        if (StringUtils.isBlank(foldingUser.getFoldingUserName())) {
            failureMessages.add("Attribute 'foldingUserName' must not be empty");
        }

        if (StringUtils.isBlank(foldingUser.getDisplayName())) {
            failureMessages.add("Attribute 'displayName' must not be empty");
        }

        if (StringUtils.isBlank(foldingUser.getPasskey())) {
            failureMessages.add("Attribute 'passkey' must not be empty");
        }

        if (foldingUser.getFoldingTeamNumber() <= 0) {
            failureMessages.add("Attribute 'foldingTeamNumber' is invalid (EHW: 239902, OCN: 37726, etc)");
        }

        if (foldingUser.getHardwareId() <= Hardware.EMPTY_HARDWARE_ID || !HardwareCache.get().contains(foldingUser.getHardwareId())) {
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
                final int unitsForUserAndPasskey = FoldingStatsParser.getUnitsForUser(foldingUser.getFoldingUserName(), foldingUser.getPasskey());

                if (unitsForUserAndPasskey == 0) {
                    failureMessages.add(String.format("User '%s' has 0 completed Work Units with passkey '%s', there must be at least one valid Work Unit submitted on the passkey before adding the user",
                            foldingUser.getFoldingUserName(),
                            foldingUser.getPasskey()
                    ));
                }
            } catch (final Exception e) {
                LOGGER.warn("Unable to get Folding stats for user {}", foldingUser, e);
                failureMessages.add("Unable to check stats for user");
            }
        }

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(foldingUser, failureMessages);
    }
}
