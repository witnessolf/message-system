package web.service;

/**
 * @Author 17131
 * @Date 2024/3/17
 * @Description:具体处理定时任务逻辑的Handler
 */
public interface TaskService {

    /**
     * 处理的具体逻辑
     * @param messageTemplateId
     */
    void handle(Long messageTemplateId);
}
