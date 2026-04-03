package org.acme.dto;

import java.util.List;
import java.util.Map;

public class SQLExecutionResult {

    public String sql;
    public String executionType;
    public Integer rowCount;
    public Integer updateCount;
    public List<String> columns;
    public List<Map<String, Object>> rows;
}
