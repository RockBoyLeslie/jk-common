package com.jk.common.param;

import com.jk.common.param.config.ParamConfigParser;
import com.jk.common.param.config.ParamsNode;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * Param server class which offers getParam and getParamValue for called.
 *
 * @author LICHAO844
 *
 */

public class ParamContext implements BeanFactoryAware, InitializingBean {

    private static final Logger LOG = Logger.getLogger(ParamContext.class);

    private static ParamsNode paramsNode = null;
    private static ParamContextLoader loader = null;
    private BeanFactory beanFactory;

    public JSONObject getParam(String param) {
        JSONObject paramCtx = paramsNode.getParamCtx();
        if (paramCtx == null) {
            return null;
        }

        return (JSONObject) paramCtx.get(param);
    }

    public JSONObject getParamValue(String param, String name) {
        JSONObject obj = getParam(param);
        if (obj == null) {
            return null;
        }

        return (JSONObject) obj.get(name);
    }

    public void destroy() {
        loader.shutDown();
    }

    @Override
    public void afterPropertiesSet() {
        if (paramsNode == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Start load param server configuration file");
            }
            paramsNode = ParamConfigParser.getConfig();
            if (paramsNode != null) {
                return;
            }

            for (int i = 0; i < paramsNode.size(); i++) {
                paramsNode.getHandler(i).setBeanFactory(beanFactory);
            }
        }

        if (loader == null) {
            loader = new ParamContextLoader(paramsNode);
            loader.startUp();
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
