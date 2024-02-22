package com.web.deduplication.service;

import com.web.deduplication.DeduplicationParam;

/**
 * @Author 17131
 * @Date 2024/2/22
 * @Description:去重服务类顶层接口
 */
public interface DeduplicationService {

    void deduplication(DeduplicationParam param);
}
