package com.jk.common.param.config;

public class ParamConfigParserTest {

    public static void main(String[] args) {
        ParamsNode node = ParamConfigParser.getConfig();
        System.out.println(node.size());
    }

}
