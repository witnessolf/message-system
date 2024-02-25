package com.web.deduplication.limit;

import com.web.deduplication.DeduplicationParam;
import com.web.deduplication.service.AbstractDeduplicationService;
import com.web.deduplication.service.DeduplicationService;
import com.web.domain.TaskInfo;

import java.util.Set;

/**
 * @Author 17131
 * @Date 2024/2/25
 * @Description:去重逻辑实现类顶层接口
 */
public interface LimitService {
    /**
     *  具体去重实现
     * @param service
     * @param taskInfo
     * @param param
     * @return 返回不符合条件的手机号码
     */
    Set<String> limitFilter(AbstractDeduplicationService service, TaskInfo taskInfo, DeduplicationParam param);
}
