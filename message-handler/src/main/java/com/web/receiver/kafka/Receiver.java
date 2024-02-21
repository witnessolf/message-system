package com.web.receiver.kafka;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.web.service.ConsumeService;
import com.web.utils.GroupIdMappingUtils;
import com.web.constant.MessageQueuePipeline;
import com.web.domain.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @Author 17131
 * @Date 2024/2/20
 * @Description: 消费mq数据
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ConditionalOnProperty(name = "austin.mq.pipeline", havingValue = MessageQueuePipeline.KAFKA)
public class Receiver {

    @Autowired
    private ConsumeService consumeService;

    @KafkaListener(topics = "#{'austin.mq.topic}'", containerFactory = "filterContainerFactory")
    public void consumer(ConsumerRecord<?, String> consumerRecord, @Header(KafkaHeaders.GROUP_ID) String topicGroupId) {
        // 1.判断ConsumerRecord是否为空
        Optional<String> kafkaMessage = Optional.ofNullable(consumerRecord.value());
        if (kafkaMessage.isPresent()) {
            // 2.将ConsumerRecord的值转换为TaskInfo
            List<TaskInfo> taskInfos = JSON.parseArray(kafkaMessage.get(), TaskInfo.class);
            // 3.根据TaskInfo生成消息的GroupId
            String messageGroupId = GroupIdMappingUtils.getGroupIdByTaskInfo(CollUtil.getFirst(taskInfos.iterator()));
            // 4.每个消费者组 只消费 他们自身关心的消息
            if (messageGroupId.equals(topicGroupId)) {
                log.info("groupId:{},params:{}", messageGroupId, JSON.toJSONString(taskInfos));
                consumeService.consume2Send(taskInfos);
            }
        }
    }
}
