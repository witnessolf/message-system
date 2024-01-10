package com.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author 17131
 * @Date 2024/1/10
 * @Description:
 */
@Getter
@ToString
@AllArgsConstructor
public enum BusinessEnum {
    /** 普通发送流程 */
    COMMON_SEND("send", "普通发送"),

    /** 撤回流程 */
    RECALL("recall", "撤回消息");


    /** code 关联着责任链的模板 */
    private String code;

    /** 类型说明 */
    private String description;

}
