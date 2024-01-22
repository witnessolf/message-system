package com.web.pipline;

import java.util.List;

/**
 * @Author 17131
 * @Date 2024/1/14
 * @Description:业务执行模板（将责任链中的各个节点的业务逻辑传起来）
 */
public class ProcessTemplate {

    private List<BusinessProcess> processList;

    public List<BusinessProcess> getProcessList() {
        return processList;
    }
    public void setProcessList(List<BusinessProcess> processList) {
        this.processList = processList;
    }
}
