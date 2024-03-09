package com.web.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.web.domain.MessageParam;
import com.web.domain.MessageTemplate;
import com.web.domain.SendRequest;
import com.web.domain.SendResponse;
import com.web.enums.BusinessCode;
import com.web.enums.RespStatusEnum;
import com.web.exception.CommonException;
import com.web.service.MessageTemplateService;
import com.web.service.SendService;
import com.web.utils.Convert4Amis;
import com.web.vo.BasicResultVO;
import com.web.vo.MessageTemplateParam;
import com.web.vo.MessageTemplateVo;
import com.web.vo.amis.CommonAmisVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author 17131
 * @Date 2024/3/4
 * @Description:消息模板管理controller
 */
@Slf4j
@RestController
@RequestMapping("/messageTemplate")
public class MessageTemplateController {


    @Autowired
    private MessageTemplateService messageTemplateService;

    @Autowired
    private SendService sendService;

//    @Autowired
//    private RecallService recallService;

//    @Autowired
//    private LoginUtils loginUtils;

    @Value("${austin.business.upload.crowd.path}")
    private String dataPath;

    /**
     * 如果Id存在，则修改
     * 如果Id不存在，则保存
     */
    @PostMapping("/save")
    public MessageTemplate saveOrUpdate(@RequestBody MessageTemplate messageTemplate) {
//        if (loginUtils.needLogin() && StrUtil.isBlank(messageTemplate.getCreator())) {
//            throw new CommonException(RespStatusEnum.NO_LOGIN.getCode(), RespStatusEnum.NO_LOGIN.getMsg());
//        }
        return messageTemplateService.saveOrUpdate(messageTemplate);
    }

    /**
     * 列表数据
     */
    @GetMapping("/list")
    public MessageTemplateVo queryList(@Validated MessageTemplateParam messageTemplateParam) {
//        if (loginUtils.needLogin() && StrUtil.isBlank(messageTemplateParam.getCreator())) {
//            throw new CommonException(RespStatusEnum.NO_LOGIN.getCode(), RespStatusEnum.NO_LOGIN.getMsg());
//        }
        Page<MessageTemplate> messageTemplates = messageTemplateService.queryList(messageTemplateParam);
        List<Map<String, Object>> result = Convert4Amis.flatListMap(messageTemplates.toList());
        return MessageTemplateVo.builder().count(messageTemplates.getTotalElements()).rows(result).build();
    }

    /**
     * 根据Id查找
     */
    @GetMapping("query/{id}")
    public Map<String, Object> queryById(@PathVariable("id") Long id) {
        return Convert4Amis.flatSingleMap(messageTemplateService.queryById(id));
    }

    /**
     * 根据Id复制
     */
    @PostMapping("copy/{id}")
    public void copyById(@PathVariable("id") Long id) {
        messageTemplateService.copy(id);
    }


    /**
     * 根据Id删除
     * id多个用逗号分隔开
     */
    @DeleteMapping("delete/{id}")
    public void deleteByIds(@PathVariable("id") String id) {
        if (StrUtil.isNotBlank(id)) {
            List<Long> idList = Arrays.stream(id.split(StrUtil.COMMA)).map(Long::valueOf).collect(Collectors.toList());
            messageTemplateService.deleteByIds(idList);
        }
    }


    /**
     * 测试发送接口
     */
    @PostMapping("test")
    public SendResponse test(@RequestBody MessageTemplateParam messageTemplateParam) {

        Map<String, String> variables = JSON.parseObject(messageTemplateParam.getMsgContent(), Map.class);
        MessageParam messageParam = MessageParam.builder().receiver(messageTemplateParam.getReceiver()).variables(variables).build();
        SendRequest sendRequest = SendRequest.builder().code(BusinessCode.COMMON_SEND.getCode()).messageTemplateId(messageTemplateParam.getId()).messageParam(messageParam).build();
        SendResponse response = sendService.send(sendRequest);
        if (!Objects.equals(response.getCode(), RespStatusEnum.SUCCESS.getCode())) {
            throw new CommonException(response.getMsg());
        }
        return response;
    }

    /**
     * 获取需要测试的模板占位符，透出给Amis
     */
    @PostMapping("test/content")
    public CommonAmisVo test(Long id) {
        MessageTemplate messageTemplate = messageTemplateService.queryById(id);
        return Convert4Amis.getTestContent(messageTemplate.getMsgContent());
    }


    @PostMapping("start/{id}")
    @ApiOperation("启动模板定时任务")
    public BasicResultVO start(@RequestBody @PathVariable("id") Long id) {
        BasicResultVO basicResultVO = messageTemplateService.startCronTask(id);
        return basicResultVO;
    }

    /**
     * 暂停模板的定时任务
     */
    @PostMapping("stop/{id}")
    @ApiOperation("/暂停模板的定时任务")
    public BasicResultVO stop(@RequestBody @PathVariable("id") Long id) {
        return messageTemplateService.stopCronTask(id);
    }
}
