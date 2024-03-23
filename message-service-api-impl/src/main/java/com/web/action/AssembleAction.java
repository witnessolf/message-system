package com.web.action;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.web.constant.CommonConstant;
import com.web.dao.MessageTemplateDao;
import com.web.domain.MessageParam;
import com.web.domain.MessageTemplate;
import com.web.domain.SendTaskModel;
import com.web.domain.TaskInfo;
import com.web.enums.BusinessCode;
import com.web.enums.ChannelType;
import com.web.enums.RespStatusEnum;
import com.web.dto.model.ContentModel;
import com.web.pipline.BusinessProcess;
import com.web.pipline.ProcessContext;
import com.web.utils.ContentHolderUtil;
import com.web.utils.TaskInfoUtils;
import com.web.vo.BasicResultVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @Author 17131
 * @Date 2024/1/14
 * @Description:
 */
@Data
@Service
public class AssembleAction implements BusinessProcess<SendTaskModel> {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Override
    public void process(ProcessContext<SendTaskModel> context) {
        SendTaskModel sendTaskModel = context.getProcessModel();
        Long messageTemplateId = sendTaskModel.getMessageTemplateId();

        // 根据messageTemplateId从数据库中获取信息模板
        Optional<MessageTemplate> messageTemplate = messageTemplateDao.findById(messageTemplateId);
        if (!messageTemplate.isPresent() || messageTemplate.get().getIsDeleted().equals(CommonConstant.TRUE)) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.TEMPLATE_NOT_FOUND));
            return;
        }
        if (BusinessCode.COMMON_SEND.getCode().equals(context.getCode())) {
            List<TaskInfo> taskInfos = assembleTaskInfo(sendTaskModel, messageTemplate.get());
            sendTaskModel.setTaskInfo(taskInfos);
        } else if (BusinessCode.RECALL.getCode().equals(context.getCode())) {
            sendTaskModel.setMessageTemplate(messageTemplate.get());
        }
    }

    /**
     * 组装 TaskInfo 任务消息
     * @param sendTaskModel
     * @param messageTemplate
     * @return
     */
    private List<TaskInfo> assembleTaskInfo(SendTaskModel sendTaskModel, MessageTemplate messageTemplate) {
        List<MessageParam> messageParamList = sendTaskModel.getMessageParamList();
        List<TaskInfo> taskInfoList = new ArrayList<>();

        for (MessageParam messageParam : messageParamList) {
            TaskInfo taskInfo = TaskInfo.builder()
                    .messageTemplateId(TaskInfoUtils.generateBusinessId(messageTemplate.getId(), messageTemplate.getMsgType()))
                    .receiver(new HashSet<>(Arrays.asList(messageParam.getReceiver().split(String.valueOf(StrUtil.C_COMMA)))))
                    .idType(messageTemplate.getIdType())
                    .sendChannel(messageTemplate.getSendChannel())
                    .templateType(messageTemplate.getTemplateType())
                    .msgType(messageTemplate.getMsgType())
                    .shieldType(messageTemplate.getShieldType())
                    .sendAccount(messageTemplate.getSendAccount())
                    .contentModel(getContentModelValue(messageTemplate, messageParam)).build();
            taskInfoList.add(taskInfo);
        }
        return taskInfoList;
    }

    /**
     * 获取 contentModel，替换模板msgContent中占位符信息
     * @param messageTemplate
     * @param messageParam
     * @return
     */
    private ContentModel getContentModelValue(MessageTemplate messageTemplate, MessageParam messageParam) {
        // 得到真正的ContentModel 类型
        Integer sendChannel = messageTemplate.getSendChannel();
        Class contentModelClass = ChannelType.getChanelModelClassByCode(sendChannel);

        // 得到模板的 msgContent 和 入参
        Map<String, String> variables = messageParam.getVariables();
        JSONObject jsonObject = JSON.parseObject(messageTemplate.getMsgContent());

        // 通过反射 组装出 contentModel
        Field[] fields = ReflectUtil.getFields(contentModelClass);
        ContentModel contentModel = (ContentModel) ReflectUtil.newInstance(contentModelClass);

        for (Field field : fields) {
            String originValue = jsonObject.getString(field.getName());
            if (StrUtil.isNotBlank(originValue)) {
                String resultValue = ContentHolderUtil.replacePlaceHolder(originValue, variables);
                Object resultObj = JSONUtil.isJsonObj(resultValue) ? JSONUtil.toBean(resultValue, field.getType()) : resultValue;
                ReflectUtil.setFieldValue(contentModel, field, resultObj);
            }
        }

        // 如果 url 字段存在，则在url拼接对应的埋点参数
        String url = (String) ReflectUtil.getFieldValue(contentModel, "url");
        if (StrUtil.isNotBlank(url)) {
            String resultUrl = TaskInfoUtils.generateUrl(url, messageTemplate.getId(), messageTemplate.getTemplateType());
            ReflectUtil.setFieldValue(contentModel, "url", resultUrl);
        }

        return contentModel;
    }
}
