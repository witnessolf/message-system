package com.web.dao;

import com.web.domain.SmsRecord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Author 17131
 * @Date 2024/1/9
 * @Description:
 */
public interface SmsRecordDao extends CrudRepository<SmsRecord, Long> {

    /**
     * 通过日期和手机号找到发送记录
     *
     * @param phone
     * @param sendDate
     * @return
     */
    List<SmsRecord> findByPhoneAndSendDate(Long phone, Integer sendDate);
}
