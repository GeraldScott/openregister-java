package uk.gov;

import com.google.common.base.Strings;
import uk.gov.mint.LoadHandler;
import uk.gov.mint.MintService;
import uk.gov.mint.RabbitMQConnector;
import uk.gov.store.DataStore;
import uk.gov.store.LocalDataStoreApplication;
import uk.gov.store.LogStream;
import uk.gov.store.PostgresDataStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Application {

    @SuppressWarnings("FieldCanBeLocal")
    private static Application notToBeGCed;
    private final Properties configuration;
    private RabbitMQConnector mqConnector;
    private LogStream logStream;
    private DataStore dataStore;
    private MintService mintService;

    public Application(String... args) throws IOException {
        Map<String, String> propertiesMap = createConfigurationMap(args);

        Properties properties = new Properties();
        properties.load(configurationPropertiesStream(propertiesMap.get("config.file")));
        properties.putAll(propertiesMap);
        configuration = properties;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        notToBeGCed = new Application(args);
        notToBeGCed.startup();

        Thread.currentThread().join();
    }

    public void startup() {
        String pgConnectionString = configuration.getProperty("postgres.connection.string");
        String storeName = configuration.getProperty("store.name");
        consoleLog("Connecting to Postgres database: " + pgConnectionString);
        dataStore = new PostgresDataStore(pgConnectionString, storeName);

        String kafkaString = configuration.getProperty("kafka.bootstrap.servers");
        consoleLog("Connecting to Kafka: " + kafkaString);
        logStream = new LogStream(kafkaString);

        mqConnector = new RabbitMQConnector(new LocalDataStoreApplication(dataStore, logStream));
        mqConnector.connect(configuration);

        LoadHandler loadHandler = new LoadHandler(dataStore, logStream);
        mintService = new MintService(loadHandler);
        mintService.init();

        consoleLog("Application started...");
    }

    public void shutdown() throws Exception {
        consoleLog("Shutting application down...");

        mqConnector.close();
        dataStore.close();
        logStream.close();
    }

    private InputStream configurationPropertiesStream(String fileName) throws IOException {
        if (Strings.isNullOrEmpty(fileName)) {
            consoleLog("Configuration properties file not provided, using default application.properties file");
            return Application.class.getResourceAsStream("/application.properties");
        } else {
            consoleLog("Loading properties file: " + fileName);
            return new FileInputStream(new File(fileName));
        }
    }

    private Map<String, String> createConfigurationMap(String[] args) {
        Map<String, String> appParams = new HashMap<>();
        for (int i = 0; args != null && i < args.length; i++) {
            if (args[i].contains("=")) {
                String[] kv = args[i].split("=", 2);
                appParams.put(kv[0], kv[1]);
            }
        }
        return appParams;
    }

    private void consoleLog(String logMessage) {
        System.out.println(logMessage);
    }
}

