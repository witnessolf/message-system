package com.web.deduplication.build;

import com.web.deduplication.DeduplicationParam;
import com.web.domain.TaskInfo;

/**
 * @Author 17131
 * @Date 2024/2/22
 * @Description:构建去重参数接口
 */
public interface Builder {

    String DEDUPLICATION_CONFIG_PRE = "deduplication_";

    DeduplicationParam build(String deduplication, TaskInfo taskInfo);


}
