package com.bytz.modules.cms.payment.application.assembler;

import com.bytz.modules.cms.payment.application.command.CancelPaymentCommand;
import com.bytz.modules.cms.payment.application.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.application.command.ExecutePaymentCommand;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentEntity;
import com.bytz.modules.cms.payment.interfaces.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付单转换器
 * Payment Assembler
 * <p>
 * 使用MapStruct进行支付单相关对象之间的转换
 * RO → Command → Domain Aggregate/PaymentEntity → VO
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentAssembler {

    /**
     * PaymentCreateRO转换为CreatePaymentCommand
     *
     * @param ro 创建支付单请求对象
     * @return 创建支付单命令
     */
    CreatePaymentCommand toCreateCommand(PaymentCreateRO ro);

    /**
     * PaymentAggregate转换为PaymentVO
     *
     * @param aggregate 支付单聚合根
     * @return 支付单响应对象
     */
    @Mapping(target = "pendingAmount", expression = "java(aggregate.getPendingAmount())")
    PaymentVO toVO(PaymentAggregate aggregate);

    /**
     * PaymentAggregate列表转换为PaymentVO列表
     *
     * @param aggregates 支付单聚合根列表
     * @return 支付单响应对象列表
     */
    List<PaymentVO> toVOs(List<PaymentAggregate> aggregates);

    /**
     * PaymentEntity转换为PaymentVO（用于CQRS查询）
     *
     * @param entity 支付单数据库实体
     * @return 支付单响应对象
     */
    @Mapping(target = "pendingAmount", expression = "java(calculatePendingAmount(entity))")
    PaymentVO entityToVO(PaymentEntity entity);

    /**
     * PaymentEntity列表转换为PaymentVO列表（用于CQRS查询）
     *
     * @param entities 支付单数据库实体列表
     * @return 支付单响应对象列表
     */
    List<PaymentVO> entitiesToVOs(List<PaymentEntity> entities);

    /**
     * PaymentTransaction转换为PaymentTransactionVO
     *
     * @param transaction 支付流水对象
     * @return 支付流水响应对象
     */
    PaymentTransactionVO toTransactionVO(PaymentTransaction transaction);

    /**
     * PaymentTransaction列表转换为PaymentTransactionVO列表
     *
     * @param transactions 支付流水对象列表
     * @return 支付流水响应对象列表
     */
    List<PaymentTransactionVO> toTransactionVOs(List<PaymentTransaction> transactions);

    /**
     * 计算待支付金额（用于查询场景）
     *
     * @param entity 支付单实体
     * @return 待支付金额
     */
    default BigDecimal calculatePendingAmount(PaymentEntity entity) {
        if (entity == null || entity.getPaymentAmount() == null || entity.getPaidAmount() == null) {
            return BigDecimal.ZERO;
        }
        return entity.getPaymentAmount().subtract(entity.getPaidAmount());
    }

    /**
     * BatchPaymentExecuteRO转换为ExecutePaymentCommand
     * RO中已经包含paymentId（数据库主键），直接映射即可
     *
     * @param ro         批量支付执行请求对象
     * @param resellerId 经销商ID
     * @return 执行支付命令
     */
    default ExecutePaymentCommand toBatchPaymentCommand(BatchPaymentExecuteRO ro, String resellerId) {
        if (ro == null) {
            return null;
        }

        List<ExecutePaymentCommand.PaymentItem> items = ro.getPaymentItems().stream()
                .map(item -> ExecutePaymentCommand.PaymentItem.builder()
                        .paymentId(item.getPaymentId())  // 直接使用paymentId
                        .amount(item.getAmount())
                        .build())
                .collect(Collectors.toList());

        return ExecutePaymentCommand.builder()
                .paymentItems(items)
                .paymentChannel(ro.getPaymentChannel())
                .operatorId(resellerId)
                .operatorName(resellerId)
                .build();
    }


    /**
     * CancelPaymentRO转换为CancelPaymentCommand
     *
     * @param ro 取消支付单请求对象
     * @return 取消支付单命令
     */
    default CancelPaymentCommand toCancelPaymentCommand(CancelPaymentRO ro) {
        if (ro == null) {
            return null;
        }
        return CancelPaymentCommand.builder()
                .paymentId(ro.getPaymentId())
                .reason(ro.getReason())
                .build();
    }
}