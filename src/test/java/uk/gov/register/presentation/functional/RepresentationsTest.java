package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RepresentationsTest extends FunctionalTestBase {
    @BeforeClass
    public static void publishTestMessages() {
        publishMessagesToDB(ImmutableList.of(
                "{\"hash\":\"someHash1\",\"entry\":{\"name\":\"The Entry 1\", \"key1\":\"value1\", \"ft_test_pkey\":\"12345\"}}",
                "{\"hash\":\"someHash2\",\"entry\":{\"name\":\"The Entry 2\", \"key1\":\"value2\", \"ft_test_pkey\":\"67890\"}}"
        ));
    }

    public static final String EXPECTED_SINGLE_RECORD = "<http://localhost:9000/hash/someHash1>;\n" +
            " key1 \"value1\" ;\n" +
            " name \"The Entry 1\" ;\n" +
            " ft_test_pkey \"12345\" .\n";



    public static final String TEXT_TURTLE = "text/turtle;charset=utf-8";

    @Test
    public void turtleRepresentationIsSupportedForSingleEntryView() {
        Response response = getRequest("/hash/someHash1.ttl");

        assertThat(response.getHeaderString("Content-Type"), equalTo(TEXT_TURTLE));
        assertThat(response.readEntity(String.class), equalTo(EXPECTED_SINGLE_RECORD));
    }
}
