package com.web.handler.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.sun.mail.util.MailSSLSocketFactory;
import com.web.domain.MessageTemplate;
import com.web.domain.TaskInfo;
import com.web.handler.AbstractHandler;
import com.web.handler.Handler;
import com.web.handler.HandlerHolder;
import com.web.model.EmailContentModel;
import com.web.utils.AustinFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Objects;

/**
 * @Author 17131
 * @Date 2024/3/2
 * @Description:
 */
@Service
@Slf4j
public class EmailHandler extends AbstractHandler implements Handler {

    @Value("${austin.business.upload.crowd.path}")
    private String dataPath;

    @Override
    public boolean handler(TaskInfo taskInfo) {
        EmailContentModel emailContentModel = (EmailContentModel) taskInfo.getContentModel();
        MailAccount mailAccount = getAccountConfig(taskInfo.getSendAccount());
        try {
            File file = StrUtil.isNotBlank(emailContentModel.getUrl()) ? AustinFileUtils.getRemoteUrl2File(dataPath, emailContentModel.getUrl()) : null;
            String result = Objects.isNull(file) ? MailUtil.send(mailAccount, taskInfo.getReceiver(), emailContentModel.getTitle(), emailContentModel.getContent(), true) :
                    MailUtil.send(mailAccount, taskInfo.getReceiver(), emailContentModel.getTitle(), emailContentModel.getContent(), true, file);
        } catch (Exception e) {
            log.error("EmailHandler#handler fail!{},params:{}", Throwables.getStackTraceAsString(e), taskInfo);
            return false;
        }
        return true;
    }

    /**
     * 获取账号配置信息
     * @param sendAccount
     * @return
     */
    private MailAccount getAccountConfig(Integer sendAccount) {
        /**
         * 修改 user/from/pass
         */
        String defaultConfig = "{\"host\":\"smtp.qq.com\",\"port\":465,\"user\":\"1713157566@qq.com\",\"pass\":\"deruupvqlevibjbi\",\"from\":\"1713157566@qq.com\",\"starttlsEnable\":\"true\",\"auth\":true,\"sslEnable\":true}";
        MailAccount account = JSON.parseObject(defaultConfig, MailAccount.class);
        try {
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            account.setAuth(account.isAuth()).setStarttlsEnable(account.isStarttlsEnable()).setSslEnable(account.isSslEnable()).setCustomProperty("mail.smtp.ssl.socketFactory", sf);
            account.setTimeout(25000).setConnectionTimeout(25000);
        } catch (Exception e) {
            log.error("EmailHandler#getAccount fail!{}", Throwables.getStackTraceAsString(e));
        }
        return account;

    }

    @Override
    public void recall(MessageTemplate messageTemplate) {

    }
}
