package me.zodac.folding.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path("/home/")
@RequestScoped
public class SiteEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteEndpoint.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String viewHomePage() {
        final File file = new File("/media/frontend/index.html");
        try {
            return readFile(file);
        } catch (final IOException e) {
            LOGGER.error("Error loading site home page", e);
            return "";
        }
    }

    private static String readFile(final File file) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        try (final InputStream fileInputStream = new FileInputStream(file);
             final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append(System.lineSeparator());
            }
        }

        return stringBuilder.toString();
    }
}
