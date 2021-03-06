package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Record;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.db.*;
import uk.gov.register.util.HashValue;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class LoadSerializedFunctionalTest {
    private static final TestRegister testRegister = TestRegister.register;

    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    private static TestItemCommandDAO testItemDAO;
    private static TestEntryDAO testEntryDAO;
    private static RecordQueryDAO testRecordDAO;
    private static String schema = testRegister.getSchema();

    @BeforeClass
    public static void setUp() throws Exception {
        Handle handle = register.handleFor(testRegister);
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        testRecordDAO = handle.attach(RecordQueryDAO.class);
    }

    @Before
    public void setup() {
        register.wipe();
    }

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-rsf.tsv")));
        Response r = send(input);
        System.out.println(r.readEntity(String.class));
        assertThat(r.getStatus(), equalTo(200));

        TestDBItem expectedItem1 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "955a84bcec7dad1a4d9b05e28ebfa21b17ac9552cc0aabbc459c73d63ab530b0"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"register\",\"phase\":\"alpha\",\"register\":\"register\",\"text\":\"A register name.\"}"));
        TestDBItem expectedItem2 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "ceae38992b310fba3ae77fd84e21cdb6838c90b36bcb558de02acd2f6589bd3f"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"text\",\"field\":\"text\",\"phase\":\"alpha\",\"text\":\"Description of register entry.\"}"));
        TestDBItem expectedItem3 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "4624c413d90e125141a92f28c9ea4300a568d9b5d9c1c7ad13623433c4a370f2"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"registry\",\"phase\":\"alpha\",\"text\":\"Body responsible for maintaining one or more registers\"}"));
        TestDBItem expectedItem4 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "1c5a799079c97f1dcea1b244d9962b0de248ba1282145c2e815839815db1d0a4"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"phase\",\"phase\":\"alpha\",\"text\":\"Phase of a register or service as defined by the [digital service manual](https://www.gov.uk/service-manual).\"}"));
        TestDBItem expectedItem5 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "c7e5a90c020f7686d9a275cb0cc164636745b10ae168a72538772692cc90d633"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"text\",\"field\":\"copyright\",\"phase\":\"alpha\",\"text\":\"Copyright for the data in the register.\"}"));
        TestDBItem expectedItem6 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "61138002a7ae8a53f3ad16bb91ee41fe73cc7ab7c8b24a8afd2569eb0e6a1c26"),
                nodeOf("{\"cardinality\":\"n\",\"datatype\":\"string\",\"field\":\"fields\",\"phase\":\"alpha\",\"register\":\"field\",\"text\":\"Set of field names.\"}"));
        TestDBItem expectedItem7 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "f404b4739b51afeb39bba26f3bbf1aa8c6f7d25f0d54444992fc00f24587ef77"),
                nodeOf("{\"fields\":[\"register\",\"text\",\"registry\",\"phase\",\"copyright\",\"fields\"],\"phase\":\"alpha\",\"register\":\"register\",\"registry\":\"cabinet-office\",\"text\":\"Register of registers\"}"));
        TestDBItem expectedItem8 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"),
                nodeOf("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test\"}"));
        TestDBItem expectedItem9 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "b8b56d0329b4a82ce55217cfbb3803c322bf43711f82649757e9c2df5f5b8371"),
                nodeOf("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test2\"}"));


        List<TestDBItem> storedItems = testItemDAO.getItems(schema);
        assertThat(storedItems, containsInAnyOrder(expectedItem1, expectedItem2, expectedItem3, expectedItem4, expectedItem5, expectedItem6, expectedItem7, expectedItem8, expectedItem9));

        List<Entry> systemEntries = testEntryDAO.getAllSystemEntries(schema);
        assertThat(systemEntries.get(0).getEntryNumber(), is(1));
        assertThat(systemEntries.get(0).getV1ItemHash().getValue(), is("955a84bcec7dad1a4d9b05e28ebfa21b17ac9552cc0aabbc459c73d63ab530b0"));
        assertThat(systemEntries.get(1).getEntryNumber(), is(2));
        assertThat(systemEntries.get(1).getV1ItemHash().getValue(), is("ceae38992b310fba3ae77fd84e21cdb6838c90b36bcb558de02acd2f6589bd3f"));
        assertThat(systemEntries.get(2).getEntryNumber(), is(3));
        assertThat(systemEntries.get(2).getV1ItemHash().getValue(), is("4624c413d90e125141a92f28c9ea4300a568d9b5d9c1c7ad13623433c4a370f2"));
        assertThat(systemEntries.get(3).getEntryNumber(), is(4));
        assertThat(systemEntries.get(3).getV1ItemHash().getValue(), is("1c5a799079c97f1dcea1b244d9962b0de248ba1282145c2e815839815db1d0a4"));
        assertThat(systemEntries.get(4).getEntryNumber(), is(5));
        assertThat(systemEntries.get(4).getV1ItemHash().getValue(), is("c7e5a90c020f7686d9a275cb0cc164636745b10ae168a72538772692cc90d633"));
        assertThat(systemEntries.get(5).getEntryNumber(), is(6));
        assertThat(systemEntries.get(5).getV1ItemHash().getValue(), is("61138002a7ae8a53f3ad16bb91ee41fe73cc7ab7c8b24a8afd2569eb0e6a1c26"));
        assertThat(systemEntries.get(6).getEntryNumber(), is(7));
        assertThat(systemEntries.get(6).getV1ItemHash().getValue(), is("f404b4739b51afeb39bba26f3bbf1aa8c6f7d25f0d54444992fc00f24587ef77"));

        List<Entry> userEntries = testEntryDAO.getAllEntries(schema);

        assertThat(userEntries.get(0).getEntryNumber(), is(1));
        assertThat(userEntries.get(0).getV1ItemHash().getValue(), is("3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"));
        assertThat(userEntries.get(1).getEntryNumber(), is(2));
        assertThat(userEntries.get(1).getV1ItemHash().getValue(), is("b8b56d0329b4a82ce55217cfbb3803c322bf43711f82649757e9c2df5f5b8371"));

        Record record1 = testRecordDAO.getRecord("ft_openregister_test", schema, "entry").get();
        assertThat(record1.getEntry().getEntryNumber(), equalTo(1));
        assertThat(record1.getEntry().getKey(), equalTo("ft_openregister_test"));
        Record record2 = testRecordDAO.getRecord("ft_openregister_test2", schema, "entry").get();
        assertThat(record2.getEntry().getEntryNumber(), equalTo(2));
        assertThat(record2.getEntry().getKey(), equalTo("ft_openregister_test2"));
    }

    @Test
    public void shouldReturnBadRequestWhenNotValidRsf() {
        String entry = "foo bar";
        Response response = send(entry);

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"String is empty or is in incorrect format\"}"));
    }

    @Test
    public void shouldReturnBadRequestForOrphanItems() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-orphan-rsf.tsv")));
        Response response = send(input);
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Orphan add item (line:16): sha-256:d00d4b610e9b5af160a7e5e836eec9e12626cac61823eda1c3ec9a59a78eefaa\"}"));
    }

    @Test
    public void shouldReturnBadRequestForNonCanonicalItems() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-non-canonical-item.tsv")));

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(400));
        assertThat(r.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Non canonical JSON: { \\\"register\\\":\\\"ft_openregister_test\\\",   \\\"text\\\":\\\"SomeText\\\" }\"}"));
    }

    @Test
    public void shouldReturnBadRequestWhenLoadingDuplicateItemForExistingRecord() throws IOException {
        String metadataRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/local-authority-eng-metadata.rsf")));
        register.loadRsf(TestRegister.local_authority_eng, metadataRsf);

        Response initialResponse = register.loadRsf(TestRegister.local_authority_eng,
            "add-item\t{\"local-authority-eng\":\"Notts\",\"local-authority-type\":\"MD\"}\n" +
            "add-item\t{\"local-authority-eng\":\"London\",\"local-authority-type\":\"UA\"}\n" +
            "add-item\t{\"local-authority-eng\":\"Leics\",\"local-authority-type\":\"CTY\"}\n" +
            "append-entry\tuser\tNotts\t2016-04-05T13:23:05Z\tsha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125\n" +
            "append-entry\tuser\tLondon\t2016-04-05T13:23:05Z\tsha-256:7c9cadb17dbdf51ac8d5da5b6f5b55d3ea5332e8eb064c5c7ef7404f08fe74f6\n" +
            "append-entry\tuser\tLeics\t2016-04-05T13:23:05Z\tsha-256:a726c24e895a56f15699068ea48d61297eed4fb8cc73c019701fd3e8dd26c15e");

        assertThat(initialResponse.getStatus(), equalTo(200));

        Response duplicateItemResponse = register.loadRsf(TestRegister.local_authority_eng,
            "add-item\t{\"local-authority-eng\":\"Notts\",\"local-authority-type\":\"MD\"}\n" +
            "append-entry\tuser\tNotts\t2016-04-05T13:23:05Z\tsha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125");

        assertThat(duplicateItemResponse.getStatus(), equalTo(400));
        assertThat(duplicateItemResponse.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[user, Notts, 2016-04-05T13:23:05Z, sha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125]}: Failed to append entry with entry-number 4 and key 'Notts': Failed to index entry #4 with key 'Notts': Cannot contain identical items to previous entry\"}"));
    }

    @Test
    public void shouldReturnBadRequestWhenLoadingDuplicateItemForNewUserRecord() throws IOException {
        String metadataRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/local-authority-eng-metadata.rsf")));
        register.loadRsf(TestRegister.local_authority_eng, metadataRsf);

        Response response = register.loadRsf(TestRegister.local_authority_eng,
            "add-item\t{\"local-authority-eng\":\"Notts\",\"local-authority-type\":\"MD\"}\n" +
            "append-entry\tuser\tNotts\t2016-04-05T13:23:05Z\tsha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125\n" +
            "append-entry\tuser\tNotts\t2016-04-05T13:23:05Z\tsha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125");

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[user, Notts, 2016-04-05T13:23:05Z, sha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125]}: Failed to append entry with entry-number 2 and key 'Notts': Failed to index entry #2 with key 'Notts': Cannot contain identical items to previous entry\"}"));
    }

    @Test
    public void shouldReturnBadRequestWhenLoadingDuplicateItemForNewMetadataRecord() throws IOException {
        String metadataRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/local-authority-eng-metadata.rsf")));
        register.loadRsf(TestRegister.local_authority_eng, metadataRsf);

        Response response = register.loadRsf(TestRegister.local_authority_eng,
                "add-item\t{\"local-authority-eng\":\"Notts\",\"local-authority-type\":\"MD\"}\n" +
                        "append-entry\tsystem\tregister-name\t2016-04-05T13:23:05Z\tsha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125\n" +
                        "append-entry\tsystem\tregister-name\t2016-04-05T13:23:05Z\tsha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125");

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, register-name, 2016-04-05T13:23:05Z, sha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125]}: Failed to append entry with entry-number 9 and key 'register-name': Failed to index entry #9 with key 'register-name': Cannot contain identical items to previous entry\"}"));
    }

    @Test
    public void shouldReturnBadRequestWhenLoadingDuplicateItemsForExistingRecord() throws IOException {
        String metadataRsf = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/local-authority-eng-metadata.rsf")));
        register.loadRsf(TestRegister.local_authority_eng, metadataRsf);

        Response initialResponse = register.loadRsf(TestRegister.local_authority_eng,
            "add-item\t{\"local-authority-eng\":\"Notts\",\"local-authority-type\":\"MD\"}\n" +
            "append-entry\tuser\tEastMidlands\t2016-04-05T13:23:05Z\tsha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125");

        assertThat(initialResponse.getStatus(), equalTo(200));

        Response duplicateItemResponse = register.loadRsf(TestRegister.local_authority_eng,
            "add-item\t{\"local-authority-eng\":\"Notts\",\"local-authority-type\":\"MD\"}\n" +
            "append-entry\tuser\tEastMidlands\t2016-04-05T13:23:05Z\tsha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125");

        assertThat(duplicateItemResponse.getStatus(), equalTo(400));
        assertThat(duplicateItemResponse.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[user, EastMidlands, 2016-04-05T13:23:05Z, sha-256:d57e3435709718d26dcc527676bca19583c4309fc1e4c116b2a6ca528f62c125]}: Failed to append entry with entry-number 2 and key 'EastMidlands': Failed to index entry #2 with key 'EastMidlands': Cannot contain identical items to previous entry\"}"));
    }

    @Test
    public void shouldReturnBadRequestForRegisterDefinitionWhenBeforeFieldDefinitions() throws IOException {
        String input = new String(RsfRegisterDefinition.ADDRESS_REGISTER + RsfRegisterDefinition.ADDRESS_FIELDS);

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(400));
        assertThat(r.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, register:address, 2017-06-06T09:54:11Z, sha-256:b9f80885b11cbc9064970214387571ebfae6795f62bd79723163a3a91162537e]}: Failed to append entry with entry-number 8 and key 'register:address': Field undefined: register - address\"}"));
    }

    @Test
    public void shouldAllowRegisterTextToBeUpdatedAfterUserEntry() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-valid-entry-ordering.rsf")));

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(200));
        Response response = register.getRequest(testRegister, "/register.json");
        Map registerResourceMapFromRegisterRegister = response.readEntity(Map.class);
        Map<?, ?> registerRecordMapFromRegisterRegister = (Map) registerResourceMapFromRegisterRegister.get("register-record");
        assertThat(registerRecordMapFromRegisterRegister.get("text"), equalTo("Register of registers X"));
    }

    @Test
    public void shouldNotAllowRegisterNonTextValuesToBeUpdatedAfterUserEntry() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-invalid-entry-ordering.rsf")));

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(400));
        assertThat(r.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, register:register, 2017-06-06T09:54:11Z, sha-256:e46ec43a2a9734d26834c2e75fab0c547aa4fccdec8da29c29f331d629ad0e71]}: Failed to append entry with entry-number 11 and key 'register:register': Definition of register register does not match Register Register\"}"));
    }

    @Test
    public void shouldRollbackIfCheckedRootHashDoesNotMatchExpectedOne() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-rsf-invalid-root-hash.tsv")));

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(400));
        assertThat(testItemDAO.getItems(schema), empty());
        assertThat(testEntryDAO.getAllEntries(schema), empty());
    }

	@Test
	public void shouldReturnBadRequestWhenRegisterIsMissingFieldsDefinedInEnvironment() throws Exception {
		Response response = register.loadRsf(TestRegister.postcode,
				"add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"text\":\"UK Postcodes.\"}\n" +
						"append-entry\tsystem\tfield:postcode\t2017-06-09T12:59:51Z\tsha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a\n" +
						"add-item\t{\"fields\":[\"postcode\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}\n" +
						"append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:323fb3d9167d55ea8173172d756ddbc653292f8debbb13f251f7057d5cb5e450\n");
		assertThat(response.getStatus(), equalTo(400));
		assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, field:postcode, 2017-06-09T12:59:51Z, sha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a]}: Failed to append entry with entry-number 1 and key 'field:postcode': Definition of field postcode does not match Field Register\"}"));
	}

    @Test
    public void shouldReturnBadRequestWhenLocalFieldDoesNotExistInEnvironment() throws Exception {
        Response response = register.loadRsf(TestRegister.postcode,
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"test-postcode\",\"phase\":\"alpha\",\"text\":\"UK Postcodes.\"}\n" +
            "append-entry\tsystem\tfield:test-postcode\t2017-06-09T12:59:51Z\tsha-256:eb0381c0c768767e60b3edf140e6bdf241f5e6f01a98c3751da488c3e6ffb3fe\n" +
            "add-item\t{\"fields\":[\"test-postcode\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}\n" +
            "append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:323fb3d9167d55ea8173172d756ddbc653292f8debbb13f251f7057d5cb5e450\n");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, field:test-postcode, 2017-06-09T12:59:51Z, sha-256:eb0381c0c768767e60b3edf140e6bdf241f5e6f01a98c3751da488c3e6ffb3fe]}: Failed to append entry with entry-number 1 and key 'field:test-postcode': Field test-postcode does not exist in Field Register\"}"));
    }

    @Test
    public void shouldReturnBadRequestWhenLocalFieldDoesNotMatchEnvironmentField() throws Exception {
        String rsf =
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"text\":\"UK Postcodes.\"}\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"point\",\"phase\":\"alpha\",\"text\":\"A geographical point\"}\n" +
            "append-entry\tsystem\tfield:postcode\t2017-06-09T12:59:51Z\tsha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a\n" +
            "append-entry\tsystem\tfield:point\t2017-06-09T12:59:51Z\tsha-256:48d0ad5afa2502674a2253c62e5af3f9bc10f5c6fbc5d16784c9dcfbc60d066b\n" +
            "add-item\t{\"fields\":[\"postcode\",\"point\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}\n" +
            "append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:ee2fd6546a8362d98e3cd63d914ca55d93f15801c35fdd108b9294c4f0a1d01e\n";

        Response response = register.loadRsf(TestRegister.postcode, rsf);
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Failed to load RSF\",\"details\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, field:postcode, 2017-06-09T12:59:51Z, sha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a]}: Failed to append entry with entry-number 1 and key 'field:postcode': Definition of field postcode does not match Field Register\"}"));
    }

    private Response send(String payload) {
        return register.loadRsf(testRegister, RsfRegisterDefinition.REGISTER_REGISTER + payload);
    }

    private JsonNode nodeOf(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, JsonNode.class);
    }
}
