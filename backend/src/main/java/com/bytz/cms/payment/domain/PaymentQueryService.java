package com.bytz.cms.payment.domain;

import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.cms.payment.domain.enums.PaymentStatus;
import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.domain.enums.RelatedBusinessType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付查询与统计（筛选支付单、查询支付单/流水/进度等）
 * 
 * <p>领域服务类型：查询服务</p>
 * <p>需求来源：需求文档4.3节支付筛选与批量管理、4.7节支付结果查询</p>
 * <p>相关用例：UC-PM-002、UC-PM-005</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Service
public class PaymentQueryService {
    
    @Resource
    private IPaymentRepository paymentRepository;
    
    /**
     * 支持状态/类型/时间/金额/渠道等筛选（UC-PM-002）
     * 
     * <p>筛选条件：支付单状态、支付类型、订单范围、金额范围、时间范围、经销商ID、关联业务类型</p>
     * <p>用例来源：UC-PM-002步骤1-4</p>
     * 
     * @param resellerId 经销商ID（必填）
     * @param paymentStatus 支付状态（可选）
     * @param paymentType 支付类型（可选）
     * @param relatedBusinessType 关联业务类型（可选）
     * @param startTime 创建开始时间（可选）
     * @param endTime 创建结束时间（可选）
     * @return 符合条件的支付单列表
     */
    public List<PaymentAggregate> filterPayments(
            String resellerId,
            PaymentStatus paymentStatus,
            PaymentType paymentType,
            RelatedBusinessType relatedBusinessType,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        // TODO: 实现支付单筛选逻辑
        // 调用repository的filter方法，支持多条件组合查询
        return paymentRepository.filter(resellerId, paymentStatus, paymentType, startTime, endTime);
    }
    
    /**
     * 查询支付单详情与流水历史（UC-PM-005）
     * 
     * <p>查询内容：支付单基础信息、支付进度、退款进度、支付流水历史、退款流水历史、关联订单信息</p>
     * <p>用例来源：UC-PM-005步骤1-3</p>
     * 
     * @param paymentId 支付单号
     * @return 包含流水的支付单聚合根
     */
    public PaymentAggregate queryPaymentDetail(String paymentId) {
        // TODO: 实现支付单详情查询
        // 调用repository的loadWithTransactions方法，加载完整聚合
        return paymentRepository.loadWithTransactions(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("支付单不存在：" + paymentId));
    }
}
