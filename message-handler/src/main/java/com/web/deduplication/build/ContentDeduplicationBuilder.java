package com.web.deduplication.build;

import com.web.deduplication.DeduplicationParam;
import com.web.domain.TaskInfo;
import com.web.enums.AnchorState;
import com.web.enums.DeduplicationType;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author 17131
 * @Date 2024/2/22
 * @Description:相同内容的消息去重参数构造器
 */
@Service
public class ContentDeduplicationBuilder extends AbstractDeduplicationBuilder {

    public ContentDeduplicationBuilder() {
        this.deduplicationType = DeduplicationType.CONTENT.getCode();
    }

    @Override
    public DeduplicationParam build(String deduplication, TaskInfo taskInfo) {
        // 1.从配置中提取参数，构造参数类
        DeduplicationParam deduplicationParam = getParamsFromConfig(deduplicationType, deduplication, taskInfo);
        if (Objects.isNull(deduplicationParam)) {
            return null;
        }
        // 2.设置埋点信息
        deduplicationParam.setAnchorState(AnchorState.CONTENT_DEDUPLICATION);
        return deduplicationParam;
    }
}
