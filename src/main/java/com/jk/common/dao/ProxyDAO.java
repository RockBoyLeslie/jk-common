package com.jk.common.dao;

import java.util.List;
import java.util.Map;

public interface ProxyDAO {
    public List<Map<String, Object>> invokeQueryForList(String sqlName, Object parameterObject);
}
