package com.jk.common.param.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.jk.common.dao.ProxyDAO;
import com.jk.common.param.ParamContextException;

/**
 * 
 * Database param handler which has two arrtibutes, sqlName is the statement
 * configured in sql map, keyName is the index field in target table.
 * 
 * @author LICHAO844
 *
 */

public class LoadDBParamHandler extends AbstractParamHandler {

    private static final Logger LOG = Logger.getLogger(LoadDBParamHandler.class);
    private String sqlName;
    private String keyName;

    @Override
    public void load() throws ParamContextException {
        ProxyDAO dao = getParamProxyDAO();

        List<Map<String, Object>> results = (List<Map<String, Object>>) dao.invokeQueryForList(sqlName, Collections.emptyMap());
        if (CollectionUtils.isEmpty(results)) {
            LOG.error("param [" + name + "], size [0]");
            return;
        }

        for (Map<String, Object> result : results) {
            if (!result.containsKey(keyName)) {
                throw new ParamContextException("keyName [" + keyName + "] not exist in DB result");
            }

            Object keyValue = result.get(keyName);
            this.valueObj.put(keyValue, JSONObject.fromObject(result));
        }
    }

    private ProxyDAO getParamProxyDAO() {
        return (ProxyDAO) beanFactory.getBean("paramProxyDAO");
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}
