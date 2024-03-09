package web.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.shaded.com.google.common.base.Throwables;
import com.web.enums.RespStatusEnum;
import com.web.vo.BasicResultVO;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import web.constants.XxlJobConstant;
import web.entity.XxlJobGroup;
import web.entity.XxlJobInfo;
import web.service.CronTaskService;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @Author 17131
 * @Date 2024/3/9
 * @Description:
 */
@Service
@Slf4j
public class CronTaskServiceImpl implements CronTaskService {

    @Value("${xxl.job.admin.username}")
    private String xxlUserName;

    @Value("${xxl.job.admin.password}")
    private String xxlPassword;

    @Value("${xxl.job.admin.addresses}")
    private String xxlAddresses;

    @Override
    public BasicResultVO saveCronTask(XxlJobInfo xxlJobInfo) {
        // 1.将xxlJobInfo转换成请求参数
        Map<String, Object> params = JSON.parseObject(JSON.toJSONString(xxlJobInfo), Map.class);
        // 2.构造请求URL(更新和插入是不一样的url)
        String path = xxlAddresses + (Objects.nonNull(xxlJobInfo.getId()) ? XxlJobConstant.UPDATE_URL : XxlJobConstant.INSERT_URL);

        HttpResponse response;
        ReturnT returnT = null;

        try {
            // 3.发送请求
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            // 4.插入要返回id,更新不需要
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                if (path.contains(XxlJobConstant.INSERT_URL)) {
                    Integer taskId = Integer.parseInt(String.valueOf(returnT.getContent()));
                    return BasicResultVO.success(taskId);
                } else if (path.contains(XxlJobConstant.UPDATE_URL)) {
                    return BasicResultVO.success();
                }
            }
        } catch (Exception e) {
            log.error("CronTaskService#saveTask fail,e:{},param:{},response:{}", com.google.common.base.Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(xxlJobInfo), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }

    @Override
    public BasicResultVO deleteCronTask(Integer taskId) {
        String path = xxlAddresses + XxlJobConstant.DELETE_URL;

        HashMap<String, Object> params = MapUtil.newHashMap();
        params.put("id", taskId);

        HttpResponse response;
        ReturnT returnT = null;
        try {
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                return BasicResultVO.success();
            }
        } catch (Exception e) {
            log.error("CronTaskService#deleteCronTask fail,e:{},param:{},response:{}", com.google.common.base.Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(params), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }

    @Override
    public BasicResultVO startCronTask(Integer taskId) {
        // 1.构造xxl-job规定的api url
        String path = xxlAddresses + XxlJobConstant.RUN_URL;
        // 2.构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("id", taskId);

        HttpResponse response;
        ReturnT returnT = null;
        // 3.http远程调用(需要先登录获取用户cookie)
        try {
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                return BasicResultVO.success();
            }
        } catch (Exception e) {
            log.error("CronTaskService#startCronTask fail,e:{},param:{},response:{}", Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(params), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }

    private String getCookie() {
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("userName", xxlUserName);
        params.put("password", xxlPassword);
        params.put("randomCode", IdUtil.fastSimpleUUID());

        String path = xxlAddresses + XxlJobConstant.LOGIN_URL;
        HttpResponse response = null;
        try {
            response = HttpRequest.post(path).form(params).execute();
            // 如果登陆成功，获取用户的cookie
            if (response.isOk()) {
                List<HttpCookie> cookies = response.getCookies();
                StringBuilder builder = new StringBuilder();
                for (HttpCookie cookie : cookies) {
                    builder.append(cookie.toString());
                }
                return builder.toString();
            }
        } catch (Exception e) {
            log.error("CronTaskService#createGroup getCookie,e:{},param:{},response:{}", com.google.common.base.Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(params), JSON.toJSONString(response));
        }
        return null;
    }

    @Override
    public BasicResultVO stopCronTask(Integer taskId) {
        String path = xxlAddresses + XxlJobConstant.STOP_URL;

        HashMap<String, Object> params = MapUtil.newHashMap();
        params.put("id", taskId);

        HttpResponse response;
        ReturnT returnT = null;
        try {
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                return BasicResultVO.success();
            }
        } catch (Exception e) {
            log.error("CronTaskService#stopCronTask fail,e:{},param:{},response:{}", com.google.common.base.Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(params), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }

    @Override
    public BasicResultVO getGroupId(String appName, String title) {
        String path = xxlAddresses + XxlJobConstant.JOB_GROUP_PAGE_LIST;
        Map<String, Object> params = new HashMap<>();
        params.put("appname", appName);
        params.put("title", title);

        HttpResponse response = null;
        try {
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            Integer id = JSON.parseObject(response.body()).getJSONArray("data").getJSONObject(0).getInteger("id");
            if (response.isOk() && Objects.nonNull(id)) {
                return BasicResultVO.success(id);
            }
        } catch (Exception e) {
            log.error("CronTaskService#getGroupId fail,e:{},param:{},response:{}", com.google.common.base.Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(params), JSON.toJSONString(response.body()));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(response.body()));
    }

    @Override
    public BasicResultVO createGroup(XxlJobGroup xxlJobGroup) {
        Map<String, Object> params = JSON.parseObject(JSON.toJSONString(xxlJobGroup), Map.class);
        String path = xxlAddresses + XxlJobConstant.JOB_GROUP_INSERT_URL;

        HttpResponse response;
        ReturnT returnT = null;

        try {
            response = HttpRequest.post(path).form(params).cookie(getCookie()).execute();
            returnT = JSON.parseObject(response.body(), ReturnT.class);
            if (response.isOk() && ReturnT.SUCCESS_CODE == returnT.getCode()) {
                return BasicResultVO.success();
            }
        } catch (Exception e) {
            log.error("CronTaskService#createGroup fail,e:{},param:{},response:{}", com.google.common.base.Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(params), JSON.toJSONString(returnT));
        }
        return BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR, JSON.toJSONString(returnT));
    }


}
