package com.web.handler.Impl;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.web.domain.MessageTemplate;
import com.web.domain.TaskInfo;
import com.web.domain.wechat.robot.EnterpriseWeChatRobotParam;
import com.web.domain.wechat.robot.EnterpriseWeChatRootResult;
import com.web.dto.account.EnterpriseWeChatRobotAccount;
import com.web.dto.model.EnterpriseWeChatRobotContentModel;
import com.web.enums.ChannelType;
import com.web.enums.SendMessageType;
import com.web.handler.AbstractHandler;
import com.web.handler.Handler;
import com.web.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author 17131
 * @Date 2024/3/23
 * @Description:
 */
@Slf4j
@Component
public class EnterpriseWeChatRobotHandler extends AbstractHandler implements Handler {

    @Autowired
    private AccountUtils accountUtils;

    public EnterpriseWeChatRobotHandler() {
        channelCode = ChannelType.ENTERPRISE_WE_CHAT_ROBOT.getCode();
    }

    @Override
    public boolean handler(TaskInfo taskInfo) {
        try {
            EnterpriseWeChatRobotAccount account = accountUtils.getAccountById(taskInfo.getSendAccount(), EnterpriseWeChatRobotAccount.class);
            EnterpriseWeChatRobotParam enterpriseWeChatRobotParam = assembleParam(taskInfo);
            String result = HttpRequest.post(account.getWebhook()).header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                    .body(JSON.toJSONString(enterpriseWeChatRobotParam))
                    .timeout(2000)
                    .execute().body();
            EnterpriseWeChatRootResult weChatRootResult = JSON.parseObject(result, EnterpriseWeChatRootResult.class);
            if (weChatRootResult.getErrcode() == 0) {
                return true;
            }
            log.error("EnterpriseWeChatRobotHandler#handler fail! result:{},params:{}", JSON.toJSONString(weChatRootResult), JSON.toJSONString(taskInfo));
        } catch (Exception e) {
            log.error("EnterpriseWeChatRobotHandler#handler fail!e:{},params:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(taskInfo));
        }
        return false;
    }

    private EnterpriseWeChatRobotParam assembleParam(TaskInfo taskInfo) {
        EnterpriseWeChatRobotContentModel contentModel = (EnterpriseWeChatRobotContentModel) taskInfo.getContentModel();
        EnterpriseWeChatRobotParam param = EnterpriseWeChatRobotParam.builder()
                .msgType(SendMessageType.getEnterpriseWeChatRobotTypeByCode(contentModel.getSendType())).build();

        if (SendMessageType.TEXT.getCode().equals(contentModel.getSendType())) {
            param.setText(EnterpriseWeChatRobotParam.TextDTO.builder().content(contentModel.getContent()).build());
        }
        if (SendMessageType.MARKDOWN.getCode().equals(contentModel.getSendType())) {
            param.setMarkdown(EnterpriseWeChatRobotParam.MarkdownDTO.builder().content(contentModel.getContent()).build());
        }
        if (SendMessageType.IMAGE.getCode().equals(contentModel.getSendType())) {
            param.setImage(EnterpriseWeChatRobotParam.ImageDTO.builder().base64(contentModel.getBase64()).md5(contentModel.getMd5()).build());
        }
        if (SendMessageType.FILE.getCode().equals(contentModel.getSendType())) {
            param.setFile(EnterpriseWeChatRobotParam.FileDTO.builder().mediaId(contentModel.getMediaId()).build());
        }
        if (SendMessageType.NEWS.getCode().equals(contentModel.getSendType())) {
            List<EnterpriseWeChatRobotParam.NewsDTO.ArticlesDTO> articlesDtoS = JSON.parseArray(contentModel.getArticles(), EnterpriseWeChatRobotParam.NewsDTO.ArticlesDTO.class);
            param.setNews(EnterpriseWeChatRobotParam.NewsDTO.builder().articles(articlesDtoS).build());
        }
        if (SendMessageType.TEMPLATE_CARD.getCode().equals(contentModel.getSendType())) {
            //
        }
        return param;
    }

    @Override
    public void recall(MessageTemplate messageTemplate) {

    }
}
