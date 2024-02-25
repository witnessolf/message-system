package com.web.deduplication.limit;

import cn.hutool.core.util.IdUtil;
import com.web.deduplication.DeduplicationParam;
import com.web.deduplication.service.AbstractDeduplicationService;
import com.web.domain.TaskInfo;
import com.web.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author 17131
 * @Date 2024/2/25
 * @Description:滑动窗口去重器（内容去重采用基于redis中zset的滑动窗口去重，可以做到严格控制单位时间内的频次。）
 */
@Service(value = "SlideWindowLimitService")
public class SlideWindowLimitService extends AbstractLimitService{

    private static final String LIMIT_TAG = "SW_";

    @Autowired
    private RedisUtils redisUtils;

    // 获取lua脚本
    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("limit.lua")));
    }

    /**
     * @param service  去重器对象
     * @param taskInfo
     * @param param    去重参数
     * @return 返回不符合条件的手机号码
     */
    @Override
    public Set<String> limitFilter(AbstractDeduplicationService service, TaskInfo taskInfo, DeduplicationParam param) {
        Set<String> filterReceiver = new HashSet<>(taskInfo.getReceiver().size());
        long nowTime = System.currentTimeMillis();
        for (String receiver : taskInfo.getReceiver()) {
            String key = LIMIT_TAG + deduplicationSingleKey(service, taskInfo, receiver);
            // 我们只需要保证每次add进窗口内的成员是唯一的，那么就不会触发有更新的操作
            String scoreValue = String.valueOf(IdUtil.getSnowflake().nextId());
            String score = String.valueOf(nowTime);
            if (redisUtils.execLimitLua(redisScript, Collections.singletonList(key),
                    String.valueOf(param.getDeduplicationTime() * 1000),
                    score, String.valueOf(param.getCountNum()), scoreValue)) {
                filterReceiver.add(receiver);
            }

        }
        return filterReceiver;
    }
}
