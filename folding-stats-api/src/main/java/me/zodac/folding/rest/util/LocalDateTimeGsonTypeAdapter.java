package me.zodac.folding.rest.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * Due to issues with the {@link LocalDateTime} class not exposing the internal {@code date} or {@code time} implementation, we need to
 * implement a custom type adapter to use with {@link com.google.gson.GsonBuilder#registerTypeAdapter(Type, Object)}. We will need to implement both
 * a {@link JsonDeserializer} and a {@link JsonSerializer}.
 *
 * <p>
 * For the {@link JsonDeserializer}, we need to construct a {@link LocalDateTime} by extracting the {@code year}, {@code month}, and {@code day} from
 * the {@code date}, and extracting the {@code hour}, {@code minute}, {@code second}, and {@code nano} from the {@code time}. We do this by manually
 * parsing a JSON representation of the {@link LocalDateTime} in the following format:
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
 *
 * <p>
 * For the {@link JsonSerializer}, we do the reverse and build up the same JSON output.
 */
public final class LocalDateTimeGsonTypeAdapter implements JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {

    private static final LocalDateTimeGsonTypeAdapter INSTANCE = new LocalDateTimeGsonTypeAdapter();

    private LocalDateTimeGsonTypeAdapter() {

    }

    /**
     * Return a singleton implementation of {@link LocalDateTimeGsonTypeAdapter}.
     *
     * @return the {@link LocalDateTimeGsonTypeAdapter} singleton instance
     */
    public static LocalDateTimeGsonTypeAdapter getInstance() {
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

    @Override
    public JsonElement serialize(final LocalDateTime localDateTime, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject output = new JsonObject();

        final JsonObject date = new JsonObject();
        date.addProperty("year", localDateTime.getYear());
        date.addProperty("month", localDateTime.getMonthValue());
        date.addProperty("day", localDateTime.getDayOfMonth());

        final JsonObject time = new JsonObject();
        time.addProperty("hour", localDateTime.getHour());
        time.addProperty("minute", localDateTime.getMinute());
        time.addProperty("second", localDateTime.getSecond());
        time.addProperty("nano", localDateTime.getNano());

        output.add("date", date);
        output.add("time", time);
        return output;
    }
}