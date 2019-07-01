package com.ionic.sdk.addon.jdbc.usecase2.jdbc;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;

/**
 * Container for data loaded from a {@link java.sql.ResultSet}.  This sample application applies the Ionic SDK decrypt
 * transformation to the data as it is loaded from the ResultSet.
 */
public class RowSet extends ArrayList<Object[]> {

    /**
     * Metadata associated with the source result set of this RowSet.
     */
    private final ResultSetMetaData metaData;

    /**
     * Constructor.
     *
     * @param metaData metadata associated with the source result set of this RowSet
     */
    public RowSet(final ResultSetMetaData metaData) {
        super();
        this.metaData = metaData;
    }

    /**
     * @return metadata associated with the source result set of this RowSet
     */
    public ResultSetMetaData getMetaData() {
        return metaData;
    }
}
