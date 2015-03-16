package com.zte.monitor.app.handler.chain;

import com.zte.monitor.app.handler.ResponseHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sylar on 14-9-13.
 * 响应处理连，将所有的相应存储再HashMap中，根据msgId获取，ResponseHandler。
 */
public class ResponseHandlerChain {

    private Map<Byte, ResponseHandler> internalMap;

    public ResponseHandlerChain() {
        internalMap = new HashMap<Byte, ResponseHandler>();
    }

    public void add(byte name, ResponseHandler handler) {
        internalMap.put(name, handler);
    }

    public ResponseHandler get(byte name) {
        return internalMap.get(name);
    }


}
