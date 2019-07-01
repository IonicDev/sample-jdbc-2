package com.ionic.sdk.addon.jdbc.usecase2.jdbc;

import com.ionic.sdk.addon.jdbc.usecase2.employee.EmployeeIonicFields;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Load the database table containing Ionic-protected data.
 * <p>
 * This sample demonstrates the usage of a second database table to hold Ionic-protected content.  The rationale is to
 * provide guidance to help integrate Ionic protection with database tables where the database schema is locked.
 * <p>
 * The tests in this code sample cache the Ionic-protected content in memory, in order to simplify the needed logic.
 * In a real-world scenario, this caching strategy may be impractical, due to memory limitations.  An alternate (and
 * more complex) strategy would involve the introduction of stored procedure layer, or of an object-relational mapping
 * tool, in order to present these two database tables as a single logical table.
 */
public class EmployeeIonicJdbcUtil {

    public static Map<Integer, EmployeeIonicFields> getIonicFields(
            final Connection connection, final Properties properties) throws SQLException {
        final Map<Integer, EmployeeIonicFields> ionicFields = new TreeMap<Integer, EmployeeIonicFields>();
        final String sqlSelectEmployeeIonic = properties.getProperty("sql.select.employeeionic");
        final QueryRunner queryRunner = new QueryRunner();
        final ResultSetHandler<RowSet> handler = new SampleResultSetHandler();
        final RowSet rowSet = queryRunner.query(connection, sqlSelectEmployeeIonic, handler);
        for (Object[] row : rowSet) {
            final EmployeeIonicFields eif = new EmployeeIonicFields(
                    (Integer) row[0], (String) row[1], (String) row[2]);
            ionicFields.put(eif.getId(), eif);
        }
        return ionicFields;
    }
}
