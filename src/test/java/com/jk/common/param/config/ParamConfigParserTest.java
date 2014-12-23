package com.jk.common.param.config;

import org.junit.Assert;
import org.junit.Test;

public class ParamConfigParserTest {

    private static final int EXEPECTED_PARAMNODE_SIZE = 2;
    
    @Test
    public void testParser() {
        ParamsNode node = ParamConfigParser.getConfig();
        Assert.assertNotEquals(null, node);
    }
    
    @Test
    public void testParserNode() {
        ParamsNode node = ParamConfigParser.getConfig();
        Assert.assertEquals(EXEPECTED_PARAMNODE_SIZE, node.size());
    }
    
}
