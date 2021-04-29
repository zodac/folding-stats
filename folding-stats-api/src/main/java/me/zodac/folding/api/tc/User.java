package me.zodac.folding.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.Identifiable;


@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class User implements Identifiable {

    public static final int EMPTY_USER_ID = 0;
    private static final String PASSKEY_MASK = new String(new char[24]).replace('\0', '*');

    private int id;
    private String foldingUserName;
    private String displayName;
    private String passkey;
    private String category;
    private int hardwareId;
    private String liveStatsLink;
    private boolean isRetired;

    public static User create(final int userId, final String foldingUserName, final String displayName, final String passkey, final Category category, final int hardwareId, final String liveStatsLink, final boolean isRetired) {
        return new User(userId, foldingUserName, displayName, passkey, category.displayName(), hardwareId, liveStatsLink, isRetired);
    }

    public static User createWithoutId(final String foldingUserName, final String displayName, final String passkey, final Category category, final int hardwareId, final String liveStatsLink, final boolean isRetired) {
        return new User(EMPTY_USER_ID, foldingUserName, displayName, passkey, category.displayName(), hardwareId, liveStatsLink, isRetired);
    }

    public static User updateWithId(final int userId, final User user) {
        return new User(userId, user.foldingUserName, user.displayName, user.passkey, user.category, user.hardwareId, user.liveStatsLink, user.isRetired);
    }

    public static User unretireUser(final User user) {
        return new User(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, user.hardwareId, user.liveStatsLink, false);
    }

    public static User retireUser(final User user) {
        return new User(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, user.hardwareId, user.liveStatsLink, true);
    }

    public static User hidePasskey(final User user) {
        return new User(user.id, user.foldingUserName, user.displayName, hidePasskey(user.passkey), user.category, user.hardwareId, user.liveStatsLink, user.isRetired);
    }

    public boolean isActive() {
        return !isRetired;
    }

    private static String hidePasskey(final String passkey) {
        final int endIndex = Math.min(8, passkey.length()); // In case passkey has fewer than 8 characters
        return passkey.substring(0, endIndex).concat(PASSKEY_MASK);
    }
}
