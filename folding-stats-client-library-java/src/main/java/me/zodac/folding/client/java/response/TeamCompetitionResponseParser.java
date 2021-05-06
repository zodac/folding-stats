package me.zodac.folding.client.java.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import me.zodac.folding.client.java.request.TeamCompetitionRequestSender;
import me.zodac.folding.rest.api.tc.CompetitionResult;

import java.net.http.HttpResponse;

/**
 * {@link UtilityClass} used to parse a {@link HttpResponse} returned from {@link TeamCompetitionRequestSender}.
 */
@UtilityClass
public final class TeamCompetitionResponseParser {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Returns the {@link CompetitionResult} retrieved by {@link TeamCompetitionRequestSender#get()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link CompetitionResult}
     */
    public static CompetitionResult get(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), CompetitionResult.class);
    }
}
