package com.web.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.web.dao.ChannelAccountDao;
import com.web.domain.ChannelAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * @Author 17131
 * @Date 2024/3/23
 * @Description:获取账户信息工具类
 */
@Slf4j
@Configuration
public class AccountUtils {

    @Autowired
    private ChannelAccountDao channelAccountDao;

    public <T> T getAccountById(Integer sendAccount, Class<T> clazz) {
        try {
            Optional<ChannelAccount> optionalChannelAccount = channelAccountDao.findById(Long.valueOf(sendAccount));
            if (optionalChannelAccount.isPresent()) {
                ChannelAccount channelAccount = optionalChannelAccount.get();
                return JSON.parseObject(String.valueOf(channelAccount), clazz);
            }
        } catch (Exception e) {
            log.error("AccountUtils#getAccount fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }
}
