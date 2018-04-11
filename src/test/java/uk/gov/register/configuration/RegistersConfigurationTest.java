package uk.gov.register.configuration;

import org.junit.Test;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterMetadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

public class RegistersConfigurationTest {
    @Test
    public void configuration_shouldReturnRegisterData_whenRegisterExists() throws Exception {
        RegistersConfiguration configuration = new RegistersConfiguration("src/main/resources/config/registers.yaml");
        RegisterMetadata data = configuration.getRegisterMetadata(new RegisterId("register"));

        assertThat("register", equalTo(data.getRegisterId().toString()));
    }

    @Test
    public void configuration_shouldThrowException_whenRegisterDoesNotExist() throws Exception {
        try {
            RegistersConfiguration configuration = new RegistersConfiguration("src/main/resources/config/registers.yaml");
            configuration.getRegisterMetadata(new RegisterId("register-that-does-not-exist"));
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), equalTo("Cannot get register data for register-that-does-not-exist"));
        }
    }
}
