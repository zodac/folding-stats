package me.zodac.folding.rest.provider;

import java.net.URI;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import me.zodac.folding.api.util.EnvironmentVariableUtils;

/**
 * {@link Provider} used to handle invalid URL requests. Redirects to the main <code>Team Competition</code> homepage.
 */
// TODO: [zodac] Add integration test for this
@Provider
public class InvalidUrlRedirecter implements ExceptionMapper<NotFoundException> {

    private static final String TC_HOMEPAGE_URL = EnvironmentVariableUtils.get("TC_HOMEPAGE_URL", "https://etf.axihub.ca/");

    @Override
    public Response toResponse(final NotFoundException e) {
        return Response
            .seeOther(URI.create(TC_HOMEPAGE_URL))
            .build();
    }
}