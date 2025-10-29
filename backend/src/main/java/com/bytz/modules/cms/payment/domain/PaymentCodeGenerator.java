package com.bytz.modules.cms.payment.domain;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 支付单号生成服务
 * 负责生成唯一的支付单号
 */
@Service
public class PaymentCodeGenerator {

    /**
     * 生成唯一的支付单号
     *
     * @return 唯一的支付单号
     */
    public String generatePaymentCode() {
        // 格式：PAY + 年月日 + 8位随机字符
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "PAY" + dateStr + randomStr;
    }

    /**
     * 生成唯一的支付流水号
     *
     * @return 唯一的支付流水号
     */
    public String generateTransactionCode() {
        // 格式：TXN + 年月日时分秒 + 6位随机字符
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "TXN" + dateTimeStr + randomStr;
    }
}