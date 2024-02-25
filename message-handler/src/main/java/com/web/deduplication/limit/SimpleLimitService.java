package com.web.deduplication.limit;

import cn.hutool.core.collection.CollUtil;
import com.web.deduplication.DeduplicationParam;
import com.web.deduplication.service.AbstractDeduplicationService;
import com.web.domain.TaskInfo;
import com.web.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author 17131
 * @Date 2024/2/25
 * @Description:采用普通的计数去重方法，限制的是每天发送的条数。
 */
@Service(value = "SimpleLimitService")
public class SimpleLimitService extends AbstractLimitService {

    private static final String LIMIT_TAG = "SP_";

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Set<String> limitFilter(AbstractDeduplicationService service, TaskInfo taskInfo, DeduplicationParam param) {
        // 1.构造redis的key
        Set<String> filterReceiver = new HashSet<>(taskInfo.getReceiver().size());
        Map<String, String> readyPutRedisReceiver = new HashMap<>(taskInfo.getReceiver().size());
        List<String> keys = deduplicationAllKey(service, taskInfo).stream().map(obj -> LIMIT_TAG + obj).collect(Collectors.toList());
        // 2.获取redis数据
        Map<String, String> inRedisValue = redisUtils.mGet(keys);
        // 3.遍历接收者列表，判断是否符合去重条件
        for (String receiver : taskInfo.getReceiver()) {
            String key = LIMIT_TAG + deduplicationSingleKey(service, taskInfo, receiver);
            String value = inRedisValue.get(key);

            if (Objects.nonNull(value) && Integer.parseInt(value) >= param.getCountNum()) {
                filterReceiver.add(receiver);
            } else {
                readyPutRedisReceiver.put(receiver, key);
            }
        }
        // 4.不符合条件的用户：需要更新Redis(无记录添加，有记录则累加次数)
        putInRedis(readyPutRedisReceiver, inRedisValue, param.getDeduplicationTime());

        return filterReceiver;
    }

    /**
     * 对于不满足去重条件的用户，若在redis中存在则+1，不在则创建一个
     * @param readyPutRedisReceiver
     * @param inRedisValue
     * @param deduplicationTime
     */
    private void putInRedis(Map<String, String> readyPutRedisReceiver, Map<String, String> inRedisValue, Long deduplicationTime) {
        Map<String, String> keyValues = new HashMap<>(readyPutRedisReceiver.size());
        for (Map.Entry<String, String> entry : readyPutRedisReceiver.entrySet()) {
            String key = entry.getValue();
            String value = inRedisValue.get(key);
            if (Objects.nonNull(value)) {
                keyValues.put(key, String.valueOf(Integer.parseInt(inRedisValue.get(key)) + 1));
            } else {
                keyValues.put(key, String.valueOf(1));
            }
        }
        if (CollUtil.isNotEmpty(keyValues)) {
            redisUtils.pipelineSetEx(keyValues, deduplicationTime);
        }
    }
}
