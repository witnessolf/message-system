package com.web;

import com.alibaba.fastjson.JSON;
import com.web.dao.MessageTemplateDao;
import com.web.domain.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 3y
 * @date 2022/6/5
 */

@RestController
@Slf4j
public class TestController {

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @RequestMapping("/test")
    private String test() {
        System.out.println("sout:我真的是醉了，这都没人给三连吗？");
        log.info("log:我真的是醉了，这都没人给三连吗？");
        return "请给我三连好吗？";
    }

    @RequestMapping("/database")
    private String testDataBase() {
        List<MessageTemplate> list = messageTemplateDao.findAllByIsDeletedEquals(0, PageRequest.of(0, 10));
        return JSON.toJSONString(list);
    }

}
