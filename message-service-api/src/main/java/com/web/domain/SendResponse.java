package com.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author 17131
 * @Date 2024/1/10
 * @Description:
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class SendResponse {
    /**
     * 响应状态
     */
    private String code;

    /**
     * 响应编码
     */
    private String msg;
}
