package web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.util.StrUtil;
import com.web.dao.MessageTemplateDao;
import com.web.domain.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import web.VO.CrowdInfoVo;
import web.csv.CountFileRowHandler;
import web.pending.CrowdBatchTaskPending;
import web.service.TaskService;
import web.utils.ReadFileUtil;

import java.util.HashMap;
import java.util.Objects;

/**
 * @Author 17131
 * @Date 2024/3/17
 * @Description:具体处理定时任务逻辑的Handler
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Autowired
    private ApplicationContext context;


    @Override
    public void handle(Long messageTemplateId) {
        MessageTemplate messageTemplate = messageTemplateDao.findById(messageTemplateId).orElse(null);
        if (Objects.isNull(messageTemplate)) {
            return;
        }
        if (Objects.isNull(messageTemplate.getCronTaskId())) {
            log.error("TaskHandler#handle crowdPath empty! messageTemplateId:{}", messageTemplateId);
            return;
        }

        // 1. 获取文件行数大小
        long countCSVRow = ReadFileUtil.countCsvRow(messageTemplate.getCronCrowdPath(), new CountFileRowHandler());

        // 2. 读取文件得到每一行记录给到队列做lazy batch延迟批量处理
        CrowdBatchTaskPending crowdBatchTaskPending = context.getBean(CrowdBatchTaskPending.class);
        ReadFileUtil.getCsvRow(messageTemplate.getCronCrowdPath(), row -> {
            if (CollUtil.isEmpty(row.getFieldMap())
                    || StrUtil.isBlank(row.getFieldMap().get(ReadFileUtil.RECEIVER_KEY))) {
                return;
            }
            // 3. 每一行处理交给LazyPending
            HashMap<String, String> params = ReadFileUtil.getParamFromLine(row.getFieldMap());
            CrowdInfoVo crowdInfoVo = CrowdInfoVo.builder().receiver(row.getFieldMap().get(ReadFileUtil.RECEIVER_KEY))
                    .params(params).messageTemplateId(messageTemplateId).build();
            crowdBatchTaskPending.pending(crowdInfoVo);
            // 4. 判断是否读取文件完成回收资源且更改状态
            onComplete(row, countCSVRow, crowdBatchTaskPending, messageTemplateId);
        });


    }

    /**
     * 文件遍历结束时
     * 1. 暂停单线程池消费(最后会回收线程池资源)
     * 2. 更改消息模板的状态(暂未实现)
     * @param row
     * @param countCSVRow
     * @param crowdBatchTaskPending
     * @param messageTemplateId
     */
    private void onComplete(CsvRow row, long countCSVRow, CrowdBatchTaskPending crowdBatchTaskPending, Long messageTemplateId) {
        if (row.getOriginalLineNumber() == countCSVRow) {
            crowdBatchTaskPending.setStop(true);
            log.info("messageTemplate:[{}] read csv file complete!", messageTemplateId);
        }
    }
}
