package com.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Author 17131
 * @Date 2024/3/4
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageTemplateParam {

    /**
     * 页码
     */
    @NotNull
    private Integer page = 1;

    /**
     * 当前页大小
     */
    @NotNull
    private Integer perPage = 10;

    /**
     * 模板ID
     */
    private Long id;

    /**
     * 当前用户
     */
    private String creator;

    /**
     * 消息接收者(测试发送时使用)
     */
    private String receiver;

    /**
     * 下发参数信息
     */
    private String msgContent;

    /**
     * 模版名称
     */
    private String keywords;
}
