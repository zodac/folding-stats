package me.zodac.folding.rest.provider;

import java.net.URI;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * {@link Provider} used to handle invalid URL requests. Redirects to the main <code>Team Competition</code> homepage.
 */
@Provider
public class InvalidUrlRedirecter implements ExceptionMapper<NotFoundException> {

    private static final String TEAM_COMPETITION_HOME_PAGE = "http://teamcomp.axihub.ca/";

    @Override
    public Response toResponse(final NotFoundException e) {
        return Response
            .seeOther(URI.create(TEAM_COMPETITION_HOME_PAGE))
            .build();
    }
}