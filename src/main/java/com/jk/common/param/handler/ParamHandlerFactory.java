package com.jk.common.param.handler;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.xml.sax.Attributes;

import com.jk.common.param.ParamContextException;

/**
 * 
 * Param handler factory using by apache digester
 * 
 * @author LICHAO844
 *
 */

public class ParamHandlerFactory extends AbstractObjectCreationFactory {

    @Override
    public Object createObject(Attributes attributes) throws Exception {
        String className = attributes.getValue("class");
        if (className != null) {
            Class<?> clazz = digester.getClassLoader().loadClass(className);
            Object instance = clazz.newInstance();
            return instance;
        }

        throw new ParamContextException("class is empty");
    }

}
