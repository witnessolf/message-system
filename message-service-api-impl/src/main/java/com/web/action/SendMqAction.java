package com.web.action;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Throwables;
import com.web.domain.SendTaskModel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.web.enums.BusinessCode;
import com.web.enums.BusinessEnum;
import com.web.enums.RespStatusEnum;
import com.web.mq.SendMqService;
import com.web.mq.kafka.KafkaSendMqServiceImpl;
import com.web.pipline.BusinessProcess;
import com.web.pipline.ProcessContext;
import com.web.service.SendService;
import com.web.vo.BasicResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author 17131
 * @Date 2024/1/22
 * @Description:
 */
@Slf4j
@Service
public class SendMqAction implements BusinessProcess<SendTaskModel> {

    @Autowired
    private KafkaSendMqServiceImpl sendMqService;

    @Value("${austin.business.topic.name}")
    private String sendMessageTopic;

    @Value("${austin.business.recall.topic.name}")
    private String austinRecall;

    @Value("${austin.business.tagId.value}")
    private String tagId;

    @Value("${austin.mq.pipeline}")
    private String mqPipeline;

    @Override
    public void process(ProcessContext<SendTaskModel> context) {
        SendTaskModel sendTaskModel = context.getProcessModel();
        try {
            if (BusinessCode.COMMON_SEND.getCode().equals(context.getCode())) {
                //toJSONString(a,b),b在序列化时json数据中会写入一个 @type 选项,json数据在反序列化时会被转成@type指定的对象
                String message = JSON.toJSONString(sendTaskModel.getTaskInfo(), new SerializerFeature[]{SerializerFeature.WriteClassName});
                sendMqService.send(sendMessageTopic, message, tagId);
            } else if (BusinessCode.RECALL.getCode().equals(context.getCode())) {
                String message = JSON.toJSONString(sendTaskModel.getMessageTemplate(), new SerializerFeature[]{SerializerFeature.WriteClassName});
                sendMqService.send(austinRecall, message, tagId);
            }
        }catch (Exception e) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR));
            log.error("send {} fail! e:{},params:{}", mqPipeline, Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(CollUtil.getFirst(sendTaskModel.getTaskInfo().listIterator())));
        }

    }
}
