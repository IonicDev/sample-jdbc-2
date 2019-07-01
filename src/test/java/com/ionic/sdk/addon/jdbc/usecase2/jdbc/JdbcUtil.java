package com.ionic.sdk.addon.jdbc.usecase2.jdbc;

import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database utilities used by this sample use case.
 */
public class JdbcUtil {

    /**
     * Open a connection to the database specified in the sample project properties.
     *
     * @param propertiesTest the sample project test properties
     * @return a {@link Connection} object, suitable for database table inserts and queries
     * @throws IonicException on project misconfiguration
     * @throws SQLException   on database connectivity failures
     */
    public static Connection getConnection(final Properties propertiesTest) throws IonicException, SQLException {
        final String dbUser = propertiesTest.getProperty("jdbc.user");
        final String dbPassword = propertiesTest.getProperty("jdbc.password");
        final String dbDriver = propertiesTest.getProperty("jdbc.driver");
        final String dbUrl = propertiesTest.getProperty("jdbc.url");

        final Properties propertiesJDBC = new Properties();
        propertiesJDBC.setProperty("user", dbUser);
        propertiesJDBC.setProperty("password", dbPassword);
        try {
            final Class<?> driverClass = Class.forName(dbDriver);
            final Driver driver = (Driver) driverClass.newInstance();
            return driver.connect(dbUrl, propertiesJDBC);
        } catch (ReflectiveOperationException e) {
            throw new IonicException(SdkError.ISAGENT_INVALIDVALUE, e);
        }
    }
}
