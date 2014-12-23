package com.jk.common.param.config;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jk.common.param.handler.AbstractParamHandler;

/**
 * 
 * The root params node, include all param node and group node
 * 
 * @author LICHAO844
 *
 */

public class ParamsNode {

	private static final Logger LOG = Logger.getLogger(ParamsNode.class);

	private List<AbstractParamHandler> handlers;
	private JSONObject paramCtx;

	public ParamsNode() {
		this.handlers = new ArrayList<AbstractParamHandler>();
		this.paramCtx = new JSONObject();
	}

	public void addHandler(AbstractParamHandler handler) {
		handler.setLock(new ReentrantReadWriteLock());
		handler.setParamCtx(paramCtx);
		handlers.add(handler);
	}

	public int size() {
		return handlers.size();
	}

	public AbstractParamHandler getHandler(int index) {
		return handlers.get(index);
	}

	public AbstractParamHandler getHandler(String name) {
		for (int i = 0; i < handlers.size(); ++i) {
			if (StringUtils.equals(name, handlers.get(i).getName())) {
				return handlers.get(i);
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("ParamHandler with name [" + name + "] not exist");
		}
		return null;
	}

	public JSONObject getParamCtx() {
		return paramCtx;
	}

	public void setParamCtx(JSONObject paramCtx) {
		this.paramCtx = paramCtx;
	}
}
