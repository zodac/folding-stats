package me.zodac.folding.client.java.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * Due to issues with the {@link LocalDateTime} class not exposing the internal {@code date} or {@code time} implementation, we need to
 * implement a custom {@link JsonDeserializer}.
 *
 * <p>
 * In this case, we need to construct a {@link LocalDateTime} by extracting the {@code year}, {@code month}, and {@code day} from the {@code date},
 * and extracting the {@code hour}, {@code minute}, {@code second}, and {@code nano} from the {@code time}. We do this by manually parsing the
 * following JSON format:
 * <pre>
 * {
 *     "date": {
 *         "year": 2021,
 *         "month": 5,
 *         "day": 12
 *     },
 *     "time": {
 *         "hour": 0,
 *         "minute": 0,
 *         "second": 0,
 *         "nano": 0
 *     }
 * }
 * </pre>
 */
public final class GsonLocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

    private static final GsonLocalDateTimeDeserializer INSTANCE = new GsonLocalDateTimeDeserializer();

    private GsonLocalDateTimeDeserializer() {

    }

    /**
     * Return a singleton implementation of {@link GsonLocalDateTimeDeserializer}.
     *
     * @return the {@link GsonLocalDateTimeDeserializer} singleton instance
     */
    public static GsonLocalDateTimeDeserializer getInstance() {
        return INSTANCE;
    }

    @Override
    public LocalDateTime deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
        final JsonObject date = json.getAsJsonObject().get("date").getAsJsonObject();
        final JsonObject time = json.getAsJsonObject().get("time").getAsJsonObject();

        final int year = date.get("year").getAsInt();
        final int month = date.get("month").getAsInt();
        final int dayOfMonth = date.get("day").getAsInt();

        final int hour = time.get("hour").getAsInt();
        final int minute = time.get("minute").getAsInt();
        final int second = time.get("second").getAsInt();
        final int nano = time.get("nano").getAsInt();

        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nano);
    }
}
