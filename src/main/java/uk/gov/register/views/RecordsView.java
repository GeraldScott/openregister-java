package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterables;
import io.dropwizard.jackson.Jackson;
import net.logstash.logback.encoder.org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.*;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.representations.CsvRepresentation;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.representations.turtle.RecordsTurtleWriter;

import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RecordsView implements CsvRepresentationView {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsView.class);

    private static final String END_OF_LINE = "\n";

    private final boolean displayEntryKeyColumn;
    private final boolean resolveAllItemLinks;

    private final Map<String, Field> fieldsByName;
    private final Map<Entry, List<ItemView>> recordMap;

    private final ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();
    private final ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());

    public RecordsView(final List<Record> records, final Map<String, Field> fieldsByName, final ItemConverter itemConverter,
                       final boolean resolveAllItemLinks, final boolean displayEntryKeyColumn) {
        this.displayEntryKeyColumn = displayEntryKeyColumn;
        this.resolveAllItemLinks = resolveAllItemLinks;
        this.fieldsByName = fieldsByName;
        recordMap = getItemViews(records, itemConverter);
    }

    public Map<Entry, List<ItemView>> getRecords() {
        return recordMap;
    }

    @SuppressWarnings("unused, used by the template")
    public List<ItemView> getRecordsSimple() {
        return recordMap.entrySet()
                .stream()
                .map(e -> new ItemSimpleView(e.getKey().getKey(), e.getValue().iterator().next()))
                .collect(Collectors.toList());

    }

    public Iterable<Field> getFields() {
        return fieldsByName.values();
    }

    @SuppressWarnings("unused, used by JSON renderer")
    @JsonValue
    public Map<String, JsonNode> getNestedRecordJson() {
        final Map<String, JsonNode> records = new HashMap<>();
        recordMap.forEach((key, value) -> {
            final ObjectNode jsonNode = getEntryJson(key);
            final ArrayNode items = jsonNode.putArray("item");
            value.forEach(item -> items.add(getItemJson(item)));
            records.put(key.getKey(), jsonNode);
        });

        return records;
    }

    @Override
    public CsvRepresentation<ArrayNode> csvRepresentation() {
        final Iterable<String> fieldNames = Iterables.transform(getFields(), f -> f.fieldName);
        return new CsvRepresentation<>(Record.csvSchema(fieldNames), getFlatRecordsJson());
    }

    protected ArrayNode getFlatRecordsJson() {
        final ArrayNode flatRecords = jsonObjectMapper.createArrayNode();
        recordMap.forEach((key, value) -> value.forEach(item -> {
            final ObjectNode jsonNodes = getEntryJson(key);
            jsonNodes.setAll(getItemJson(item));
            flatRecords.add(jsonNodes);
        }));

        return flatRecords;
    }

    @SuppressWarnings("unused, used by template")
    public boolean displayEntryKeyColumn() {
        return displayEntryKeyColumn;
    }

    @SuppressWarnings("unused, used by template")
    public boolean resolveAllItemLinks() {
        return resolveAllItemLinks;
    }

    public String recordsTo(final String mediaType, final Provider<RegisterId> registerIdProvider, final RegisterResolver registerResolver) {
        final ByteArrayOutputStream outputStream;
        final RecordsTurtleWriter recordsTurtleWriter;
        String registerInTextFormatted = StringUtils.EMPTY;

        try {
            if (ExtraMediaType.TEXT_TTL_TYPE.getSubtype().equals(mediaType)) {
                outputStream = new ByteArrayOutputStream();
                recordsTurtleWriter = new RecordsTurtleWriter(registerIdProvider, registerResolver);

                recordsTurtleWriter.writeTo(this, RecordsView.class, RecordsView.class, null, ExtraMediaType.TEXT_TTL_TYPE, null, outputStream);

                registerInTextFormatted = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            } else if (ExtraMediaType.TEXT_YAML_TYPE.getSubtype().equals(mediaType)) {
                registerInTextFormatted = yamlObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getNestedRecordJson());
            } else {
                registerInTextFormatted = jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getNestedRecordJson());
            }
        } catch (final IOException ex) {
            LOGGER.error("Error processing preview request. Impossible process the register items");
        }

        return StringEscapeUtils.escapeHtml(registerInTextFormatted.isEmpty() ? registerInTextFormatted : END_OF_LINE + registerInTextFormatted);
    }

    private Map<Entry, List<ItemView>> getItemViews(final Collection<Record> records, final ItemConverter itemConverter) {
        final Map<Entry, List<ItemView>> map = new LinkedHashMap<>();
        records.forEach(record -> {
            map.put(record.getEntry(), record.getItems().stream().map(item ->
                    new ItemView(item.getSha256hex(), itemConverter.convertItem(item, fieldsByName), getFields()))
                    .collect(Collectors.toList()));
        });
        return map;
    }

    private ObjectNode getEntryJson(final Entry entry) {
        final ObjectNode jsonNode = jsonObjectMapper.convertValue(entry, ObjectNode.class);
        jsonNode.remove("item-hash");
        return jsonNode;
    }

    private ObjectNode getItemJson(final ItemView itemView) {
        return jsonObjectMapper.convertValue(itemView, ObjectNode.class);
    }
}
