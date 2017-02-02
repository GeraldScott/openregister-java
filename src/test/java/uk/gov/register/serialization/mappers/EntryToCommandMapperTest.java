package uk.gov.register.serialization.mappers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class EntryToCommandMapperTest {
    private EntryToCommandMapper sutMapper;

    @Before
    public void setUp() throws Exception {
        sutMapper = new EntryToCommandMapper();
    }

    @Test
    public void apply_returnsAppendEntryCommandForEntry() {
        Entry entryToMap = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "item-sha"), Instant.parse("2016-07-24T16:55:00Z"), "entry1-field-1-value");

        RegisterCommand mapResult = sutMapper.apply(entryToMap);

        assertThat(mapResult.getCommandName(), equalTo("append-entry"));
        assertThat(mapResult.getCommandArguments(), equalTo(Arrays.asList("2016-07-24T16:55:00Z", "sha-256:item-sha", "entry1-field-1-value")));
    }
}