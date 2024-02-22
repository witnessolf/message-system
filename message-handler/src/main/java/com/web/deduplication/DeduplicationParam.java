package com.web.deduplication;

import com.alibaba.fastjson.annotation.JSONField;
import com.web.domain.TaskInfo;
import com.web.enums.AnchorState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author 17131
 * @Date 2024/2/22
 * @Description:去重所需要的参数
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeduplicationParam {

    private TaskInfo taskInfo;

    /**
     * 去重时间
     * 单位：秒
     */
    @JSONField(name = "time")
    private Long deduplicationTime;

    /**
     * 需达到的次数去重
     */
    @JSONField(name = "num")
    private Integer countNum;

    /**
     * 标识属于哪种去重(数据埋点)
     */
    private AnchorState anchorState;
}
