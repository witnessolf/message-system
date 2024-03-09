package web.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.web.constant.CommonConstant;
import com.web.domain.MessageTemplate;
import com.web.enums.RespStatusEnum;
import com.web.vo.BasicResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import web.constants.XxlJobConstant;
import web.entity.XxlJobGroup;
import web.entity.XxlJobInfo;
import web.enums.*;
import web.service.CronTaskService;

import java.util.Date;
import java.util.Objects;

/**
 * @Author 17131
 * @Date 2024/3/9
 * @Description:xxljob工具类
 */
@Component
@Slf4j
public class XxlJobUtil {
    @Value("${xxl.job.executor.appname}")
    private String appName;

    @Value("${xxl.job.executor.jobHandlerName}")
    private String jobHandlerName;

    @Autowired
    private CronTaskService cronTaskService;


    public XxlJobInfo buildXxlJobInfo(MessageTemplate messageTemplate) {
        // 如果没有指定cron表达式，说明立即执行(给到xxl-job延迟5秒的cron表达式)
        String scheduleConfig = messageTemplate.getExpectPushTime();
        if (messageTemplate.getExpectPushTime().equals(String.valueOf(CommonConstant.FALSE))) {
            scheduleConfig = DateUtil.format(DateUtil.offsetSecond(new Date(), XxlJobConstant.DELAY_TIME), CommonConstant.CRON_FORMAT);
        }

        XxlJobInfo xxlJobInfo = XxlJobInfo.builder().jobGroup(queryGroupId()).jobDesc(messageTemplate.getName())
                .author(messageTemplate.getCreator())
                .scheduleConf(scheduleConfig)
                .scheduleType(ScheduleTypeEnum.CRON.name())
                .misfireStrategy(MisfireStrategyEnum.DO_NOTHING.name())
                .executorRouteStrategy(ExecutorRouteStrategyEnum.CONSISTENT_HASH.name())
                .executorHandler(XxlJobConstant.JOB_HANDLER_NAME)
                .executorParam(String.valueOf(messageTemplate.getId()))
                .executorBlockStrategy(ExecutorBlockStrategyEnum.SERIAL_EXECUTION.name())
                .executorTimeout(XxlJobConstant.TIME_OUT)
                .executorFailRetryCount(XxlJobConstant.RETRY_COUNT)
                .glueType(GlueTypeEnum.BEAN.name())
                .triggerStatus(CommonConstant.FALSE)
                .glueRemark(StrUtil.EMPTY)
                .glueSource(StrUtil.EMPTY)
                .alarmEmail(StrUtil.EMPTY)
                .childJobId(StrUtil.EMPTY).build();

        if (Objects.nonNull(messageTemplate.getCronTaskId())) {
            xxlJobInfo.setId(messageTemplate.getCronTaskId());
        }
        return xxlJobInfo;
    }

    /**
     * 根据就配置文件的内容获取jobGroupId，没有则创建
     * @return
     */
    private Integer queryGroupId() {
        BasicResultVO<Integer> basicResultVO = cronTaskService.getGroupId(appName, jobHandlerName);
        if (Objects.isNull(basicResultVO.getData())) {
            XxlJobGroup xxlJobGroup = XxlJobGroup.builder().appname(appName).title(jobHandlerName)
                    .addressType(CommonConstant.FALSE).build();
            if (RespStatusEnum.SUCCESS.getCode().equals(cronTaskService.createGroup(xxlJobGroup).getStatus())) {
                return (int) cronTaskService.getGroupId(appName, jobHandlerName).getData();
            }
        }
        return (Integer) basicResultVO.getData();
    }

}
