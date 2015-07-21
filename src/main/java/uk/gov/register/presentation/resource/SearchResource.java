package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import uk.gov.register.presentation.Entry;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.view.ListResultView;
import uk.gov.register.presentation.view.SingleResultView;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;


@Path("/")
public class SearchResource extends ResourceBase {

    private final RecentEntryIndexQueryDAO queryDAO;

    public SearchResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonNode> search(@Context UriInfo uriInfo) {
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        return queryParameters.entrySet()
                                .stream()
                                .findFirst()
                                .map(e -> queryDAO.findAllByKeyValue(e.getKey(), e.getValue().get(0)))
                                .orElseGet(() -> queryDAO.getAllEntries(getRegisterPrimaryKey(), ENTRY_LIMIT));
    }

    @GET
    @Path("search")
    @Produces(MediaType.TEXT_HTML)
    public ListResultView searchHtml(@Context UriInfo uriInfo) {
        return new ListResultView("/templates/entries.mustache",
                        search(uriInfo));
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public SingleResultView findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        String registerPrimaryKey = getRegisterPrimaryKey();
        if (key.equals(registerPrimaryKey)) {
            Optional<Entry> entry = queryDAO.findByKeyValue(key, value);
            if (entry.isPresent()) {
                return new SingleResultView("/templates/entry.mustache", entry.get());
            }
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/hash/{hash}")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public SingleResultView findByHash(@PathParam("hash") String hash) {
        Optional<Entry> entry = queryDAO.findByHash(hash);
        if (entry.isPresent()) {
            return new SingleResultView("/templates/entry.mustache", entry.orNull());
        }
        throw new NotFoundException();
    }
}
