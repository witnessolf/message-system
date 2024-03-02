package com.web.handler;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author 17131
 * @Date 2024/3/2
 * @Description:渠道channel->发送消息Handler的映射关系
 */
@Component
public class HandlerHolder {

    private Map<Integer, Handler> handlers = new HashMap<>(128);

    public void putHandler(Integer channel, Handler handler) {
        handlers.put(channel, handler);
    }

    public Handler route(Integer channel) {
        return handlers.get(channel);
    }

}
