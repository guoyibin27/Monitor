package com.zte.monitor.app.codec;

import com.zte.monitor.app.handler.ResponseHandler;
import com.zte.monitor.app.handler.chain.ResponseHandlerChain;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-11.
 */
public class CodecHandler {
    private ResponseHandlerChain chain;
    private static CodecHandler handler = null;

    private CodecHandler() {
        chain = new ResponseHandlerChain();
    }

    public static CodecHandler getHandler() {
        if (handler == null) {
            synchronized (CodecHandler.class) {
                if (handler == null)
                    handler = new CodecHandler();
            }
        }
        return handler;
    }

    public ResponseHandlerChain getChain() {
        return chain;
    }

    /**
     * 根据交互协议的MsgId，调用相关的处理类进行处理
     * @param message
     */
    public void handleMessage(IoBuffer message) {
        if (message == null)
            return;
        byte messageId = message.get();
        ResponseHandler handler = chain.get(messageId);
        if (handler != null) {
            handler.handleMessage(message);
        }
    }
}
