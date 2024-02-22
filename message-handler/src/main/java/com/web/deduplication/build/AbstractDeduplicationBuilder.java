package com.web.deduplication.build;

import com.alibaba.fastjson.JSONObject;
import com.web.deduplication.DeduplicationHolder;
import com.web.deduplication.DeduplicationParam;
import com.web.domain.TaskInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @Author 17131
 * @Date 2024/2/22
 * @Description:去重参数构造器的抽象类，定义具体构造器的公有方法
 */

public abstract class AbstractDeduplicationBuilder implements Builder {

    protected Integer deduplicationType;

    @Autowired
    private DeduplicationHolder deduplicationHolder;

    /**
     * 子类会在构造方法里设置具体的deduplicationType
     * this代表了当前类的实例，@PostConstruct使得具体构造器类会在构造方法结束后调用init(),将对应关系存入holder
     */
    @PostConstruct
    public void init() {
        deduplicationHolder.putBuilder(deduplicationType, this);
    }

    public DeduplicationParam getParamsFromConfig(Integer key, String duplicationConfig, TaskInfo taskInfo) {
        JSONObject object = JSONObject.parseObject(duplicationConfig);
        if (Objects.isNull(object)) return null;

        DeduplicationParam deduplicationParam = JSONObject.parseObject(object.getString(DEDUPLICATION_CONFIG_PRE + "key"), DeduplicationParam.class);
        if (deduplicationParam == null) return null;

        deduplicationParam.setTaskInfo(taskInfo);
        return deduplicationParam;
    }
}
