package uk.gov.register.resources;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.serialization.AddItemCommand;
import uk.gov.register.serialization.AppendEntryCommand;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandWriterTest {
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;


    private final Entry entry1 = new Entry(1, "entry1sha", Instant.parse("2016-07-24T16:55:00Z"));
    private final Entry entry2 = new Entry(2, "entry2sha", Instant.parse("2016-07-24T16:56:00Z"));

    private final Item item1 = new Item("entry1sha", jsonFactory.objectNode()
            .put("field-1", "entry1-field-1-value")
            .put("field-2", "entry1-field-2-value"));

    private final Item item2 = new Item("entry2sha", jsonFactory.objectNode()
            .put("field-1", "entry2-field-1-value")
            .put("field-2", "entry2-field-2-value"));


    @Mock
    private MultivaluedMap<String, Object> httpHeadersMock;

    @Test
    public void writeTo_shouldCreateRegisterRepresentationWithEntriesAndItems() throws IOException {
        Iterator<RegisterCommand> registerCommandsIterator = Arrays.asList(
                new AddItemCommand(item1),
                new AddItemCommand(item2),
                new AppendEntryCommand(entry1),
                new AppendEntryCommand(entry2)).iterator();


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RegisterCommandWriter sutCommandWriter = new RegisterCommandWriter();
        sutCommandWriter.writeTo(
                registerCommandsIterator,
                registerCommandsIterator.getClass(),
                registerCommandsIterator.getClass(),
                new Annotation[]{},
                ExtraMediaType.APPLICATION_RSF_TYPE,
                httpHeadersMock,
                outputStream);

        String expectedRSF =
                "add-item\t{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}\n" +
                "add-item\t{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}\n" +
                "append-entry\t2016-07-24T16:55:00Z\tsha-256:entry1sha\n" +
                "append-entry\t2016-07-24T16:56:00Z\tsha-256:entry2sha\n";
        String actualRSF = outputStream.toString();

        assertThat(actualRSF, equalTo(expectedRSF));
    }

    @Test
    public void writeTo_shouldAddContentDispositionHeader() throws IOException {
        Iterator<RegisterCommand> registerCommandsIterator = Arrays.asList(
                new AddItemCommand(item1),
                new AppendEntryCommand(entry1)).iterator();

        final String[] actualContentDisposition = {""};
        Mockito.doAnswer(invocation -> {
            actualContentDisposition[0] = invocation.getArgument(1);
            return null;
        }).when(httpHeadersMock).add(eq("Content-Disposition"), anyObject());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RegisterCommandWriter sutCommandWriter = new RegisterCommandWriter();
        sutCommandWriter.writeTo(
                registerCommandsIterator,
                registerCommandsIterator.getClass(),
                registerCommandsIterator.getClass(),
                new Annotation[]{},
                ExtraMediaType.APPLICATION_RSF_TYPE,
                httpHeadersMock,
                outputStream);

        verify(httpHeadersMock, times(1)).add(eq("Content-Disposition"), anyString());
        assertThat(actualContentDisposition[0], startsWith("attachment; filename="));
        assertThat(actualContentDisposition[0], endsWith(".tsv"));
    }

}
