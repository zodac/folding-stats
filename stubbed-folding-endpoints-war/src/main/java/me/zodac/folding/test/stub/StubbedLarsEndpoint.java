package me.zodac.folding.test.stub;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import me.zodac.folding.api.tc.lars.LarsGpu;

/**
 * Stubbed endpoint for the LARS PPD DB. Used to retrieve metadata about a piece of hardware, rather than going to the real API.
 *
 * <p>
 * Since the LARS DB does not have an API, the production code scrapes the HTML page itself. To correctly test, we will need to reconstruct the HTML
 * structure (at least the parts that the production code expected), then populate our configured hardware in a similar way to the LARS DB itself.
 *
 * <p>
 * We expose additional endpoints to allow the tests to provide {@link LarsGpu}s to the system. The endpoint is {@link ApplicationScoped} so we can
 * store updates across multiple HTTP requests and tests.
 *
 * @see <a href="https://folding.lar.systems/">LARS PPD DB</a>
 */
@Path("/")
@ApplicationScoped
public class StubbedLarsEndpoint {

    private static final Collection<LarsGpu> gpuEntries = new ArrayList<>();

    /**
     * Endpoint that allows tests to <b>POST</b> a {@link Collection} of {@link LarsGpu} to be added to the stubbed response.
     *
     * @param larsGpus the {@link LarsGpu}s to add
     * @return an {@link Response#ok()} {@link Response}
     */
    @POST
    @Path("/gpu_ppd/overall_ranks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGpus(final Collection<LarsGpu> larsGpus) {
        gpuEntries.addAll(larsGpus);

        return Response
            .ok()
            .build();
    }

    /**
     * Endpoint that returns the configured hardware in the same format as the LARS DB HTML output.
     *
     * <p>
     * Used by production code, not by tests directly, though tests should populate the hardware using {@link #addGpus(Collection)}.
     *
     * @return an {@link Response#ok()} {@link Response} with the HTML output
     * @see StubbedLarsEndpoint#addGpus(Collection)
     */
    @GET
    @Path("/gpu_ppd/overall_ranks")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getGpus() {
        return Response
            .ok()
            .entity(createGpuPage())
            .build();
    }

    private static String createGpuPage() {
        final StringBuilder htmlPage = new StringBuilder()
            .append("<html>")
            .append("<table id=\"primary-datatable\"")
            .append("<tbody>");

        final Collection<String> trElementsForHardwares = createTrElementsForHardwares();
        for (final String trElementForHardware : trElementsForHardwares) {
            htmlPage.append(trElementForHardware);
        }

        return htmlPage
            .append("</tbody>")
            .append("</table>")
            .append("</html>")
            .toString();
    }

    private static Collection<String> createTrElementsForHardwares() {
        return gpuEntries
            .stream()
            .map(StubbedLarsEndpoint::createTrElementForHardware)
            .collect(toList());
    }

    private static String createTrElementForHardware(final LarsGpu larsGpu) {
        return "<tr>"
            + String.format("<td class=\"rank-num\">%s</td>", larsGpu.getRank())
            + String.format("<td><span class=\"model-name\">%s</span><span class=\"model-info\">%s</span></td>", larsGpu.getDisplayName(),
            larsGpu.getModelInfo())
            + String.format("<td>%s</td>", larsGpu.getAveragePpd())
            + "<td>linuxAveragePpd_ignored</td>"
            + "<td>windowsAveragePpd_ignored</td>"
            + String.format("<td><span>%s</span></td>", larsGpu.getManufacturer())
            + "<td>model_ignored</td>"
            + "</tr>";
    }
}
