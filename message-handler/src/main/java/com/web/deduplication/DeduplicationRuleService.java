package com.web.deduplication;

import com.web.domain.TaskInfo;
import com.web.enums.DeduplicationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @Author 17131
 * @Date 2024/2/25
 * @Description:去重服务入口
 */
@Service
public class DeduplicationRuleService {
    public static final String DEDUPLICATION_RULE_KEY = "deduplicationRule";

    @Autowired
    private DeduplicationHolder deduplicationHolder;

    public void duplication(TaskInfo taskInfo) {
        // 配置样例：{"deduplication_10":{"num":1,"time":300},"deduplication_20":{"num":5}}
        //String deduplicationConfig = config.getProperty(DEDUPLICATION_RULE_KEY, CommonConstant.EMPTY_JSON_OBJECT);
        String deduplicationConfig = "{\"deduplication_10\":{\"num\":1,\"time\":300},\"deduplication_20\":{\"num\":5}}";

        List<Integer> deduplicationList = DeduplicationType.getDeduplicationList();
        for (Integer deduplicationType : deduplicationList) {
            DeduplicationParam param = deduplicationHolder.selectBuilder(deduplicationType).build(deduplicationConfig, taskInfo);
            if (Objects.nonNull(param)) {
                deduplicationHolder.selectService(deduplicationType).deduplication(param);
            }
        }

    }
}
