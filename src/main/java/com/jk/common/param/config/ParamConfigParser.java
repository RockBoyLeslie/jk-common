package com.jk.common.param.config;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;

/**
 * Common param server configuration file parser， parse configuration to param
 * node by apache digester rules
 * 
 * @author LICHAO844
 * 
 */

public class ParamConfigParser {

    private static final Logger LOG = Logger.getLogger(ParamConfigParser.class);
    private static final String PARAM_SERVER_CONFIG = "sys-param-server.xml";
    private static ParamsNode paramsNode = null;

    private ParamConfigParser() {

    }

    public static ParamsNode getConfig() {
        if (paramsNode == null) {
            synchronized (PARAM_SERVER_CONFIG) {
                if (paramsNode == null) {
                    try {
                        paramsNode = (ParamsNode) getDigester().parse(getInputStream());
                    } catch (Exception e) {
                        LOG.error("Load param server configuration file failed", e);
                    }
                }
            }
        }

        return paramsNode;
    }

    private static InputStream getInputStream() throws IOException {
        return ParamConfigParser.class.getClassLoader().getResourceAsStream(PARAM_SERVER_CONFIG);
    }

    private static Digester getDigester() {
        Digester digester = new Digester();
        digester.setClassLoader(Thread.currentThread().getContextClassLoader());
        digester.setValidating(false);

        // parse Params node
        digester.addObjectCreate("Params",
                "com.jk.common.param.config.ParamsNode");
        digester.addSetProperties("Params");

        // loop parse Params/Group node
        digester.addObjectCreate("Params/Group",
                "com.jk.common.param.config.ParamGroupNode");
        digester.addSetProperties("Params/Group");
        digester.addSetNext("Params/Group", "addHandler",
                "com.jk.common.param.config.ParamGroupNode");

        // loop parse */Param node
        digester.addFactoryCreate("*/Param",
                "com.jk.common.param.handler.ParamHandlerFactory");
        digester.addSetProperties("*/Param");
        digester.addSetProperty("*/Param/Arg", "name", "value");
        digester.addSetNext("*/Param", "addHandler");
        return digester;
    }
}
