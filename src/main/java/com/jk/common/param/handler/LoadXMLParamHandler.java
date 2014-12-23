package com.jk.common.param.handler;

import com.jk.common.param.ParamContextException;

public class LoadXMLParamHandler extends AbstractParamHandler {

    private String file;
    private String keyName;

    @Override
    public void load() throws ParamContextException {
        // TODO Auto-generated method stub

    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

}
