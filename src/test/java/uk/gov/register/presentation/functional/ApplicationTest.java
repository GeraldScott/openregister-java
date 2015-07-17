package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class ApplicationTest extends FunctionalTestBase {
    @Test
    public void appSupportsCORS() {
        String origin = "http://originfortest.com";
        Response response = client.target("http://localhost:" + APPLICATION_PORT + "/feed")
                .request()
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Requested-With")
                .options();


        MultivaluedMap<String, Object> headers = response.getHeaders();

        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), equalTo(ImmutableList.of(origin)));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), equalTo((ImmutableList.of("true"))));
        assertNotNull(headers.get(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS), equalTo((ImmutableList.of("OPTIONS,GET,PUT,POST,DELETE,HEAD"))));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), equalTo((ImmutableList.of("X-Requested-With,Content-Type,Accept,Origin"))));
    }

}
