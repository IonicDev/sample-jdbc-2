package com.ionic.sdk.addon.jdbc.usecase2.jdbc;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Implementation of commons-dbutils interface {@link ResultSetHandler}.
 * <p>
 * Business logic for loading data from a {@link ResultSet}.
 */
public class SampleResultSetHandler implements ResultSetHandler<RowSet> {

    /**
     * Turn the ResultSet into an Object.
     *
     * @param resultSet the JDBC {@link ResultSet} from the database
     * @return the Ionic-filtered representation of the input {@link ResultSet}
     * @throws SQLException on errors reading from the {@link ResultSet}
     */
    @Override
    public RowSet handle(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final RowSet rowSet = new RowSet(metaData);
        final int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            final Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; ++i) {
                //final String columnName = metaData.getColumnName(i + 1);
                row[i] = resultSet.getObject(i + 1);
            }
            rowSet.add(row);
        }
        return rowSet;
    }
}
