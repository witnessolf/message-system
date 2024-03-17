package web.pending;

import cn.hutool.core.util.StrUtil;
import com.web.pending.AbstractLazyPending;
import com.web.pending.PendingParam;
import com.web.service.SendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import web.VO.CrowdInfoVo;
import web.config.CronAsyncThreadPoolConfig;
import web.constants.PendingConstant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Author 17131
 * @Date 2024/3/17
 * @Description: * 延迟批量处理人群信息
 *  * 调用 batch 发送接口 进行消息推送
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CrowdBatchTaskPending extends AbstractLazyPending<CrowdInfoVo> {

    @Autowired
    private SendService sendService;

    // 构造函数的时候设置批量延迟任务参数
    public CrowdBatchTaskPending() {
        PendingParam<CrowdInfoVo> pendingParam = new PendingParam<>();
        pendingParam.setQueue(new LinkedBlockingDeque<>(PendingConstant.QUEUE_SIZE))
                .setExecutorService(CronAsyncThreadPoolConfig.getConsumePendingThreadPool())
                .setNumThreshold(PendingConstant.NUM_THRESHOLD)
                .setTimeThreshold(PendingConstant.TIME_THRESHOLD);
        this.pendingParam = pendingParam;
    }


    @Override
    public void doHandle(List<CrowdInfoVo> list) {
        // 1. 如果参数相同，组装成同一个MessageParam发送
        Map<Map<String, String>, String> paramMap = new HashMap<>();
        for (CrowdInfoVo crowdInfoVo : list) {
            String receiver = crowdInfoVo.getReceiver();
            Map<String, String> params = crowdInfoVo.getParams();
            if (Objects.isNull(paramMap.get(params))) {
                paramMap.put(params, receiver);
            } else {
                String newReceiver = StringUtils.join(new String[]{paramMap.get(params), receiver}, StrUtil.COMMA);
                paramMap.put(params, newReceiver);
            }
        }
    }
}
