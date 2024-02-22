package com.web.deduplication.build;

import cn.hutool.core.date.DateUtil;
import com.web.deduplication.DeduplicationParam;
import com.web.domain.TaskInfo;
import com.web.enums.AnchorState;
import com.web.enums.DeduplicationType;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @Author 17131
 * @Date 2024/2/22
 * @Description:根据频次去重参数构造器
 */
@Service
public class FrequencyDeduplicationBuilder extends AbstractDeduplicationBuilder{

    public FrequencyDeduplicationBuilder() {
        this.deduplicationType = DeduplicationType.FREQUENCY.getCode();
    }

    @Override
    public DeduplicationParam build(String deduplication, TaskInfo taskInfo) {
        DeduplicationParam deduplicationParam = getParamsFromConfig(deduplicationType, deduplication, taskInfo);
        if (Objects.isNull(deduplicationParam)) {
            return null;
        }
        // 从当前时间到当天结束的时间（以秒为单位）的差值
        deduplicationParam.setDeduplicationTime((DateUtil.endOfDay(new Date()).getTime() - DateUtil.current()) / 1000);
        deduplicationParam.setAnchorState(AnchorState.RULE_DEDUPLICATION);

        return deduplicationParam;
    }
}
