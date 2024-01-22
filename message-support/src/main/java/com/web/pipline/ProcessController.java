package com.web.pipline;

import cn.hutool.core.util.StrUtil;
import com.web.enums.RespStatusEnum;
import com.web.exception.ProcessException;
import com.web.vo.BasicResultVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @Author 17131
 * @Date 2024/1/14
 * @Description:责任链流程控制器
 */
@Data
@Slf4j
public class ProcessController {

    /**
     * 模板映射
     */
    private Map<String, ProcessTemplate> templateConfig = null;

    /**
     * 执行责任链
     *
     * @param context
     * @return 返回上下文内容
     */
    public ProcessContext process(ProcessContext context) {
        /**
         * 使用责任链之前需要进行检查
         */
        try {
            preCheck(context);
        } catch (ProcessException e) {
            return e.getProcessContext();
        }
        /**
         * 遍历责任链中的所有节点，并调用对应的process()
         */
        List<BusinessProcess> processList = templateConfig.get(context.getCode()).getProcessList();
        for (BusinessProcess businessProcess : processList) {
            businessProcess.process(context);
            if (context.getNeedBreak()) break;
        }
        return context;

    }

    /**
     * 执行前检查，出错则抛出异常
     *
     * @param context 执行上下文
     * @throws ProcessException 异常信息
     */
    private void preCheck(ProcessContext context) throws ProcessException {
        // 1.检查上下文是否为空
        if (context == null) {
            context = new ProcessContext();
            context.setResponse(BasicResultVO.fail(RespStatusEnum.CONTEXT_IS_NULL));
            throw new ProcessException(context);
        }

        // 2.检查上下文的责任链标识code是否为空
        String businessCode = context.getCode();
        if (StrUtil.isBlank(businessCode)) {
            context.setResponse(BasicResultVO.fail(RespStatusEnum.BUSINESS_CODE_IS_NULL));
            throw new ProcessException(context);
        }

        // 3.检查根据责任链标识获得的责任链模板是否为空
        ProcessTemplate processTemplate = templateConfig.get(businessCode);
        if (processTemplate == null) {
            context.setResponse(BasicResultVO.fail(RespStatusEnum.PROCESS_TEMPLATE_IS_NULL));
            throw new ProcessException(context);
        }

        // 4.检查ProcessTemplate中节点list是否为空
        List<BusinessProcess> processList = processTemplate.getProcessList();
        if (processList == null) {
            context.setResponse(BasicResultVO.fail(RespStatusEnum.PROCESS_LIST_IS_NULL));
            throw new ProcessException(context);
        }
    }

}
