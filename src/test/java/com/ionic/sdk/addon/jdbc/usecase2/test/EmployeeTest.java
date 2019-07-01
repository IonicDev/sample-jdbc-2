package com.ionic.sdk.addon.jdbc.usecase2.test;

import com.ionic.sdk.addon.jdbc.usecase2.employee.Employee;
import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeIonic;
import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeIonicFields;
import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeIonicUtil;
import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeUtil;
import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.res.Resource;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorPlainText;
import com.ionic.sdk.device.profile.persistor.ProfilePersistor;
import com.ionic.sdk.error.IonicException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Security;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Test cases for manipulating unprotected sensitive data in object representations of a database table record.
 */
public class EmployeeTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Test configuration.
     */
    private final Properties properties = new Properties();

    /**
     * Test Ionic agent, used to protect data on insert into database, and unprotect data on fetch from database.
     */
    private final Agent agent = new Agent();

    /**
     * Set up for each test case to be run.
     *
     * @throws IOException    on failure to access test resources
     * @throws IonicException on failure to initialize JRE cryptography
     */
    @Before
    public void setUp() throws IOException, IonicException {
        // load test configuration: "src/test/resources/test.properties.xml"
        final URL urlTestProperties = Resource.resolve("test.properties.xml");
        Assert.assertNotNull(urlTestProperties);
        try (InputStream is = urlTestProperties.openStream()) {
            properties.loadFromXML(is);
        }
        // initialize Ionic agent for use
        if (!agent.isInitialized()) {
            final String ionicProfile = properties.getProperty("ionic.profile");
            Assert.assertNotNull(ionicProfile);
            AgentSdk.initialize(Security.getProvider("SunJCE"));
            final URL urlIonicProfile = Resource.resolve(ionicProfile);
            final ProfilePersistor profilePersistor = new DeviceProfilePersistorPlainText(urlIonicProfile);
            agent.initialize(profilePersistor);
        }
    }

    /**
     * The class {@link Employee} represents a record in an existing relational database table.  Sensitive fields in
     * {@link Employee} are Ionic-protected, and the protected representations are stored in
     * {@link EmployeeIonicFields}.  That content is then combined into {@link EmployeeIonic}, where the unprotected
     * sensitive content is filtered out of the data object.
     *
     * @throws IonicException on failure to Ionic protect sensitive data
     */
    @Test
    public final void testEmployee_ConstructorChain() throws IonicException {
        final Employee employee = EmployeeUtil.generate();
        final EmployeeIonicFields ionicFields = EmployeeIonicUtil.toEmployeeIonicFields(employee, agent);
        final EmployeeIonic employeeIonic = new EmployeeIonic(employee, ionicFields);
        logger.info(String.format("IDENTIFIER=%s, SALARY=%s",
                employeeIonic.getIonicFields().getPersonalIdentifier(),
                employeeIonic.getIonicFields().getSalary()));
    }
}
