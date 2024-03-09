package com.web.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.web.constant.AustinConstant;
import com.web.constant.CommonConstant;
import com.web.dao.MessageTemplateDao;
import com.web.domain.MessageTemplate;
import com.web.enums.AuditStatus;
import com.web.enums.MessageStatus;
import com.web.enums.RespStatusEnum;
import com.web.enums.TemplateType;
import com.web.service.MessageTemplateService;
import com.web.vo.BasicResultVO;
import com.web.vo.MessageTemplateParam;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import web.entity.XxlJobInfo;
import web.service.CronTaskService;
import web.utils.XxlJobUtil;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Author 17131
 * @Date 2024/3/4
 * @Description:
 */
@Service
public class MessageTemplateServiceImpl implements MessageTemplateService {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Autowired
    private CronTaskService cronTaskService;

    @Autowired
    private XxlJobUtil xxlJobUtil;

    @Override
    public Page<MessageTemplate> queryList(MessageTemplateParam param) {
        PageRequest pageRequest = PageRequest.of(param.getPage() - 1, param.getPerPage());
        String creator = StrUtil.isBlank(param.getCreator()) ? AustinConstant.DEFAULT_CREATOR : param.getCreator();
        return messageTemplateDao.findAll((Specification<MessageTemplate>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (StrUtil.isNotBlank(param.getKeywords())) {
                predicateList.add(cb.like(root.get("name").as(String.class), "%" + param.getKeywords() + "%"));
            }
            predicateList.add(cb.equal(root.get("isDeleted").as(Integer.class), CommonConstant.FALSE));
            predicateList.add(cb.equal(root.get("creator").as(String.class), creator));
            javax.persistence.criteria.Predicate[] p = new javax.persistence.criteria.Predicate[predicateList.size()];
            // 查询
            query.where(cb.and(predicateList.toArray(p)));
            // 排序
            query.orderBy(cb.desc(root.get("updated")));
            return query.getRestriction();
        }, pageRequest);
    }

    @Override
    public Long count() {
        return messageTemplateDao.countByIsDeletedEquals(CommonConstant.FALSE);
    }

    @Override
    public MessageTemplate saveOrUpdate(MessageTemplate messageTemplate) {
        if (Objects.isNull(messageTemplate.getId())) {
            initStatus(messageTemplate);
        } else {
            resetStatus(messageTemplate);
        }

        messageTemplate.setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
        return messageTemplateDao.save(messageTemplate);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        List<MessageTemplate> messageTemplates = messageTemplateDao.findAllById(ids);
        messageTemplates.stream().forEach(messageTemplate -> messageTemplate.setIsDeleted(CommonConstant.TRUE));
        messageTemplateDao.saveAll(messageTemplates);
    }

    @Override
    public MessageTemplate queryById(Long id) {
        return messageTemplateDao.findById(id).orElse(null);
    }

    @Override
    public void copy(Long id) {
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).orElse(null);
        if (Objects.nonNull(messageTemplate)) {
            MessageTemplate clone = ObjectUtil.clone(messageTemplate).setId(null).setCronTaskId(null);
            messageTemplateDao.save(clone);
        }
    }

    /**
     * 初始化状态信息
     *
     * @param messageTemplate
     */
    private void initStatus(MessageTemplate messageTemplate) {
        messageTemplate.setFlowId(StrUtil.EMPTY)
                .setMsgStatus(MessageStatus.INIT.getCode()).setAuditStatus(AuditStatus.WAIT_AUDIT.getCode())
                .setCreator(StrUtil.isBlank(messageTemplate.getCreator()) ? AustinConstant.DEFAULT_CREATOR : messageTemplate.getCreator())
                .setUpdator(StrUtil.isBlank(messageTemplate.getUpdator()) ? AustinConstant.DEFAULT_UPDATOR : messageTemplate.getUpdator())
                .setTeam(StrUtil.isBlank(messageTemplate.getTeam()) ? AustinConstant.DEFAULT_TEAM : messageTemplate.getTeam())
                .setAuditor(StrUtil.isBlank(messageTemplate.getAuditor()) ? AustinConstant.DEFAULT_AUDITOR : messageTemplate.getAuditor())
                .setCreated(Math.toIntExact(DateUtil.currentSeconds()))
                .setIsDeleted(CommonConstant.FALSE);

    }

    /**
     * 1. 重置模板的状态
     * 2. 修改定时任务信息(如果存在)
     *
     * @param messageTemplate
     */
    private void resetStatus(MessageTemplate messageTemplate) {
        messageTemplate.setUpdator(messageTemplate.getUpdator())
                .setMsgStatus(MessageStatus.INIT.getCode()).setAuditStatus(AuditStatus.WAIT_AUDIT.getCode());
        MessageTemplate dbMsg = queryById(messageTemplate.getId());
        if (Objects.nonNull(dbMsg) && Objects.nonNull(dbMsg.getCronTaskId())) {
            messageTemplate.setCronTaskId(dbMsg.getCronTaskId());
        }

        // 如果存在定时任务,则更新任务信息
        if (Objects.nonNull(messageTemplate.getCronTaskId()) && TemplateType.CLOCKING.getCode().equals(messageTemplate.getTemplateType())) {
            XxlJobInfo xxlJobInfo = xxlJobUtil.buildXxlJobInfo(messageTemplate);
            cronTaskService.saveCronTask(xxlJobInfo);
            cronTaskService.stopCronTask(messageTemplate.getCronTaskId());
        }
    }


    @Override
    public BasicResultVO startCronTask(Long id) {
        // 1.获取消息模板的具体信息
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).orElse(null);
        if (Objects.isNull(messageTemplate)) {
            return BasicResultVO.fail();
        }
        // 2.动态创建或更新定时任务
        XxlJobInfo xxlJobInfo = xxlJobUtil.buildXxlJobInfo(messageTemplate);

        // 3.获取taskId(如果本身存在则复用原有任务，如果不存在则得到新建后任务ID)
        Integer taskId = messageTemplate.getCronTaskId();
        BasicResultVO basicResultVO = cronTaskService.saveCronTask(xxlJobInfo);
        if (Objects.isNull(taskId) && RespStatusEnum.SUCCESS.getCode().equals(basicResultVO.getStatus())
                && Objects.nonNull(basicResultVO.getData())) {
            taskId = Integer.valueOf(String.valueOf(basicResultVO.getData()));
        }

        // 4.启动定时任务,并更新消息模板的状态
        if (Objects.nonNull(taskId)) {
            cronTaskService.startCronTask(taskId);
            MessageTemplate copy = ObjectUtil.clone(messageTemplate).setMsgStatus(MessageStatus.RUN.getCode())
                    .setCronTaskId(taskId).setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
            messageTemplateDao.save(copy);
            return BasicResultVO.success();
        }
        return BasicResultVO.fail();
    }

    @Override
    public BasicResultVO stopCronTask(Long id) {
        // 1.获取消息模板
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).orElse(null);
        if (ObjectUtil.isNull(messageTemplate)) return BasicResultVO.fail();
        // 2.更新消息模板状态
        MessageTemplate copy = ObjectUtil.clone(messageTemplate).setMsgStatus(MessageStatus.STOP.getCode())
                .setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
        messageTemplateDao.save(copy);
        // 3.暂停定时任务
        return cronTaskService.stopCronTask(copy.getCronTaskId());
    }
}
