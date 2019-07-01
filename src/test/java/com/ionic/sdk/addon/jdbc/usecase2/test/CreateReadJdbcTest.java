package com.ionic.sdk.addon.jdbc.usecase2.test;

import com.ionic.sdk.addon.jdbc.usecase2.employee.Employee;
import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeIonic;
import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeIonicFields;
import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeIonicUtil;
import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeUtil;
import com.ionic.sdk.addon.jdbc.usecase2.jdbc.EmployeeIonicJdbcUtil;
import com.ionic.sdk.addon.jdbc.usecase2.jdbc.JdbcUtil;
import com.ionic.sdk.addon.jdbc.usecase2.jdbc.RowSet;
import com.ionic.sdk.addon.jdbc.usecase2.jdbc.SampleResultSetHandler;
import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.res.Resource;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorPlainText;
import com.ionic.sdk.device.profile.persistor.ProfilePersistor;
import com.ionic.sdk.error.IonicException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.InputStream;
import java.net.URL;
import java.security.Security;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateReadJdbcTest {

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
     * @throws Exception on failure to read the test configuration
     */
    @Before
    public void setUp() throws Exception {
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
     * Store some newly fabricated records into the Employee and EmployeeIonic database tables.  Sensitive data is
     * filtered out of the data written to table Employee.  The Ionic-protected representation of these fields is
     * written to EmployeeIonic.
     *
     * @throws SQLException   on failure to read from/write to the SQL data store
     * @throws IonicException on test case initialization failure; on failure to Ionic protect sensitive data
     */
    @Test
    public final void testJdbc_1_CreateRecords() throws SQLException, IonicException {
        logger.entering(null, null);
        try (Connection connection = JdbcUtil.getConnection(properties)) {
            Assert.assertNotNull(connection);
            final int recordsToCreate = 2;
            for (int i = 0; (i < recordsToCreate); ++i) {
                // make employee record
                final Employee e = EmployeeUtil.generate();
                logger.fine(e.getPersonalIdentifier());
                final EmployeeIonicFields eif = EmployeeIonicUtil.toEmployeeIonicFields(e, agent);
                final EmployeeIonic ei = new EmployeeIonic(e, eif);
                // store employee record (to get record key)
                final String sqlInsertEmployee = properties.getProperty("sql.insert.employee");
                final QueryRunner queryRunner = new QueryRunner();
                final ScalarHandler<Integer> scalarHandler = new ScalarHandler<Integer>();
                final int idEmployee = queryRunner.insert(connection, sqlInsertEmployee, scalarHandler,
                        ei.getFirstName(), ei.getLastName(),
                        ei.getPersonalIdentifier(), ei.getSalary(), ei.getCountry());
                // store employeeionic record
                final String sqlInsertEmployeeIonic = properties.getProperty("sql.insert.employeeionic");
                final int inserts = queryRunner.update(connection, sqlInsertEmployeeIonic,
                        idEmployee, eif.getPersonalIdentifier(), eif.getSalary());
                Assert.assertEquals(1, inserts);
            }
        }
        logger.exiting(null, null);
    }

    /**
     * Read the records previously stored in the SQL data store.  Sensitive data in table EmployeeIonic is unprotected
     * and recombined with the corresponding record in table Employee.  We then verify that the unprotected data is as
     * expected.
     *
     * @throws IonicException on test case initialization failure; on failure to Ionic unprotect sensitive data
     * @throws SQLException   on failure to read from the SQL data store
     */
    @Test
    public final void testJdbc_2_ReadRecords() throws IonicException, SQLException {
        logger.entering(null, null);
        try (Connection connection = JdbcUtil.getConnection(properties)) {
            Assert.assertNotNull(connection);
            // retrieve employeeionic records
            final Map<Integer, EmployeeIonicFields> mapIonicFields =
                    EmployeeIonicJdbcUtil.getIonicFields(connection, properties);
            // retrieve employee records
            final QueryRunner queryRunner = new QueryRunner();
            final ResultSetHandler<RowSet> handler = new SampleResultSetHandler();
            final String sqlSelectEmployee = properties.getProperty("sql.select.employee");
            final RowSet rowSet = queryRunner.query(connection, sqlSelectEmployee, handler);
            // iterate through records
            for (Object[] row : rowSet) {
                // this will hold the values from the database
                final Employee employeeDB = new Employee(
                        (Integer) row[0], (String) row[1], (String) row[2],
                        (String) row[3], (Integer) row[4], (String) row[5]);
                // we re-derive the personal identifier to verify it against the unwrapped Ionic-protected value
                final String personalIdentifier = EmployeeUtil.getPersonalIdentifier(
                        employeeDB.getFirstName(), employeeDB.getLastName());
                // find ionic data for this employee record
                final EmployeeIonicFields ionicFields = mapIonicFields.get(employeeDB.getId());
                Assert.assertNotNull(ionicFields);
                // merge the two records together
                final EmployeeIonic employeeIonic = new EmployeeIonic(employeeDB, ionicFields);
                final Employee employee = EmployeeIonicUtil.toEmployee(employeeIonic, agent);
                // verify data expectations; we can recover original values from ciphertext
                Assert.assertEquals(personalIdentifier, employee.getPersonalIdentifier());
                logger.info(String.format("PERSONAL IDENTIFIER RECOVERED FOR RECORD %d", employee.getId()));
                Assert.assertEquals(EmployeeUtil.SALARY, employee.getSalary());
                logger.info(String.format("SALARY RECOVERED FOR RECORD %d", employee.getId()));
            }
        }
        logger.exiting(null, null);
    }

    /**
     * Write the records previously stored in the SQL data store to the console.
     *
     * @throws SQLException on failure to read from the SQL data store
     */
    @Test
    public final void testJdbc_3_OutputRecords() throws IonicException, SQLException {
        try (Connection connection = JdbcUtil.getConnection(properties)) {
            Assert.assertNotNull(connection);
            logger.info("SHOW TABLE EMPLOYEE");
            readRecords(connection, properties.getProperty("sql.select.employee"));
            logger.info("SHOW TABLE EMPLOYEEIONIC");
            readRecords(connection, properties.getProperty("sql.select.employeeionic"));
        }
    }

    private void readRecords(final Connection connection, final String dbSqlSelect) throws SQLException {
        final QueryRunner queryRunner = new QueryRunner();
        final ResultSetHandler<RowSet> handler = new SampleResultSetHandler();
        final RowSet rowSet = queryRunner.query(connection, dbSqlSelect, handler);
        for (Object[] row : rowSet) {
            final StringBuilder buffer = new StringBuilder();
            for (Object cell : row) {
                buffer.append(cell);
                buffer.append(" ");
            }
            logger.info(buffer.toString());
        }
    }
}
