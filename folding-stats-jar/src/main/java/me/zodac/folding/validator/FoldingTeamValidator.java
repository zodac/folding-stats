package me.zodac.folding.validator;

import me.zodac.folding.api.FoldingTeam;

import java.util.ArrayList;
import java.util.List;

public class FoldingTeamValidator {

    private FoldingTeamValidator() {

    }

    public static ValidationResponse isValid(final FoldingTeam foldingTeam) {
        final List<String> failureMessages = new ArrayList<>(6);

//        if (Category.INVALID == Category.get(foldingUser.getCategory())) {
//            failureMessages.add(String.format("Attribute 'category' must be one of: %s", Category.getValuesAsList()));
//        }
//
//        if (StringUtils.isBlank(foldingUser.getFoldingUserName())) {
//            failureMessages.add("Attribute 'foldingUserName' must not be empty");
//        }
//
//        if (StringUtils.isBlank(foldingUser.getDisplayName())) {
//            failureMessages.add("Attribute 'displayName' must not be empty");
//        }
//
//        if (StringUtils.isBlank(foldingUser.getPasskey())) {
//            failureMessages.add("Attribute 'passkey' must not be empty");
//        }
//
//        if (foldingUser.getFoldingTeamNumber() <= 0) {
//            failureMessages.add("Attribute 'foldingTeamNumber' is invalid (EHW: 239902, OCN: 37726, etc)");
//        }
//
//        if (foldingUser.getHardwareId() <= 0 || !HardwareCache.getInstance().contains(foldingUser.getHardwareId())) {
//            final List<String> availableHardware = HardwareCache.getInstance()
//                    .getAll()
//                    .stream()
//                    .map(hardware -> String.format("%s: %s", hardware.getId(), hardware.getHardwareName()))
//                    .collect(toList());
//
//            failureMessages.add(String.format("Attribute 'hardwareId' must be one of: %s", availableHardware));
//        }

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(foldingTeam, failureMessages);
    }
}
