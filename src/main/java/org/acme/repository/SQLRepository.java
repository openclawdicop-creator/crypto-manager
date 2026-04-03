package org.acme.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.acme.dto.SQLExecutionResult;
import org.hibernate.Session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SQLRepository {

    @Inject
    EntityManager entityManager;

    public SQLExecutionResult execute(String sql) {
        return entityManager.unwrap(Session.class).doReturningWork(connection -> executeSql(connection, sql));
    }

    private SQLExecutionResult executeSql(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            boolean hasResultSet = statement.execute(sql);

            SQLExecutionResult result = new SQLExecutionResult();
            result.sql = sql;

            if (hasResultSet) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    ResultSetMetaData metadata = resultSet.getMetaData();
                    int columnCount = metadata.getColumnCount();
                    List<String> columns = new ArrayList<>(columnCount);
                    List<Map<String, Object>> rows = new ArrayList<>();

                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metadata.getColumnLabel(i));
                    }

                    while (resultSet.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(columns.get(i - 1), resultSet.getObject(i));
                        }
                        rows.add(row);
                    }

                    result.executionType = "RESULT_SET";
                    result.columns = columns;
                    result.rows = rows;
                    result.rowCount = rows.size();
                }
            } else {
                int updateCount = statement.getUpdateCount();
                result.executionType = "UPDATE_COUNT";
                result.updateCount = updateCount;
                result.rowCount = Math.max(updateCount, 0);
            }

            return result;
        }
    }
}
