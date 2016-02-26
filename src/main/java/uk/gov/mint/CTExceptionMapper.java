package uk.gov.mint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CTExceptionMapper implements ExceptionMapper<CTException> {
    private static final Logger LOG = LoggerFactory.getLogger(CTExceptionMapper.class);

    public Response toResponse(CTException ex) {
        LOG.error("Problem talking to CT server: {}", ex.getMessage());
        return Response.serverError().entity(ex.getMessage()).type("text/plain").build();
    }
}