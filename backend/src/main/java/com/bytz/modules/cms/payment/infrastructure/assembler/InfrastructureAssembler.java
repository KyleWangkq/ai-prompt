package com.bytz.modules.cms.payment.infrastructure.assembler;

import com.bytz.modules.cms.payment.domain.enums.*;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentEntity;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

/**
 * 基础设施层转换器
 * Infrastructure Assembler
 * 
 * 使用MapStruct进行领域对象和数据库实体之间的转换
 * Domain Aggregate/Entity ↔ Database Entity
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InfrastructureAssembler {
    
    /**
     * PaymentAggregate转换为PaymentEntity（数据库实体）
     * 
     * @param aggregate 支付单聚合根
     * @return 支付单数据库实体
     */
    @Mapping(target = "paymentType", source = "paymentType", qualifiedByName = "paymentTypeToString")
    @Mapping(target = "paymentStatus", source = "paymentStatus", qualifiedByName = "paymentStatusToString")
    @Mapping(target = "refundStatus", source = "refundStatus", qualifiedByName = "refundStatusToString")
    @Mapping(target = "relatedBusinessType", source = "relatedBusinessType", qualifiedByName = "relatedBusinessTypeToString")
    @Mapping(target = "delFlag", constant = "0")
    PaymentEntity toPaymentEntity(PaymentAggregate aggregate);
    
    /**
     * PaymentEntity（数据库实体）转换为PaymentAggregate
     * 注意：此方法需要配合流水列表一起使用
     * 
     * @param entity 支付单数据库实体
     * @return 支付单聚合根（不包含流水）
     */
    @Mapping(target = "paymentAmount", source = "paymentAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "paidAmount", source = "paidAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "refundedAmount", source = "refundedAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "actualAmount", source = "actualAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "currency", source = "currency", defaultValue = "CNY")
    @Mapping(target = "paymentType", source = "paymentType", qualifiedByName = "stringToPaymentType")
    @Mapping(target = "paymentStatus", source = "paymentStatus", qualifiedByName = "stringToPaymentStatus")
    @Mapping(target = "refundStatus", source = "refundStatus", qualifiedByName = "stringToRefundStatus")
    @Mapping(target = "relatedBusinessType", source = "relatedBusinessType", qualifiedByName = "stringToRelatedBusinessType")
    @Mapping(target = "transactions", ignore = true)
    PaymentAggregate toPaymentAggregate(PaymentEntity entity);
    
    /**
     * PaymentTransaction（领域对象）转换为PaymentTransactionEntity（数据库实体）
     * 
     * @param domainTransaction 支付流水领域对象
     * @return 支付流水数据库实体
     */
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "transactionTypeToString")
    @Mapping(target = "transactionStatus", source = "transactionStatus", qualifiedByName = "transactionStatusToString")
    @Mapping(target = "paymentChannel", source = "paymentChannel", qualifiedByName = "paymentChannelToString")
    @Mapping(target = "delFlag", constant = "0")
    PaymentTransactionEntity toTransactionEntity(PaymentTransaction domainTransaction);
    
    /**
     * PaymentTransactionEntity（数据库实体）转换为PaymentTransaction（领域对象）
     * 
     * @param entity 支付流水数据库实体
     * @return 支付流水领域对象
     */
    @Mapping(target = "transactionAmount", source = "transactionAmount", qualifiedByName = "toBigDecimalOrZero")
    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "stringToTransactionType")
    @Mapping(target = "transactionStatus", source = "transactionStatus", qualifiedByName = "stringToTransactionStatus")
    @Mapping(target = "paymentChannel", source = "paymentChannel", qualifiedByName = "stringToPaymentChannel")
    PaymentTransaction toDomainTransaction(PaymentTransactionEntity entity);
    
    /**
     * PaymentTransactionEntity列表（数据库实体）转换为PaymentTransaction列表（领域对象）
     * 
     * @param entities 支付流水数据库实体列表
     * @return 支付流水领域对象列表
     */
    List<PaymentTransaction> toDomainTransactions(List<PaymentTransactionEntity> entities);
    
    // ========== 枚举转换方法 ==========
    
    @Named("paymentTypeToString")
    default String paymentTypeToString(PaymentType paymentType) {
        return paymentType != null ? paymentType.name() : null;
    }
    
    @Named("stringToPaymentType")
    default PaymentType stringToPaymentType(String paymentType) {
        return paymentType != null ? PaymentType.valueOf(paymentType) : null;
    }
    
    @Named("paymentStatusToString")
    default String paymentStatusToString(PaymentStatus paymentStatus) {
        return paymentStatus != null ? paymentStatus.name() : null;
    }
    
    @Named("stringToPaymentStatus")
    default PaymentStatus stringToPaymentStatus(String paymentStatus) {
        return paymentStatus != null ? PaymentStatus.valueOf(paymentStatus) : null;
    }
    
    @Named("refundStatusToString")
    default String refundStatusToString(RefundStatus refundStatus) {
        return refundStatus != null ? refundStatus.name() : null;
    }
    
    @Named("stringToRefundStatus")
    default RefundStatus stringToRefundStatus(String refundStatus) {
        return refundStatus != null ? RefundStatus.valueOf(refundStatus) : null;
    }
    
    @Named("relatedBusinessTypeToString")
    default String relatedBusinessTypeToString(RelatedBusinessType relatedBusinessType) {
        return relatedBusinessType != null ? relatedBusinessType.name() : null;
    }
    
    @Named("stringToRelatedBusinessType")
    default RelatedBusinessType stringToRelatedBusinessType(String relatedBusinessType) {
        return relatedBusinessType != null ? RelatedBusinessType.valueOf(relatedBusinessType) : null;
    }
    
    @Named("transactionTypeToString")
    default String transactionTypeToString(TransactionType transactionType) {
        return transactionType != null ? transactionType.name() : null;
    }
    
    @Named("stringToTransactionType")
    default TransactionType stringToTransactionType(String transactionType) {
        return transactionType != null ? TransactionType.valueOf(transactionType) : null;
    }
    
    @Named("transactionStatusToString")
    default String transactionStatusToString(TransactionStatus transactionStatus) {
        return transactionStatus != null ? transactionStatus.name() : null;
    }
    
    @Named("stringToTransactionStatus")
    default TransactionStatus stringToTransactionStatus(String transactionStatus) {
        return transactionStatus != null ? TransactionStatus.valueOf(transactionStatus) : null;
    }
    
    @Named("paymentChannelToString")
    default String paymentChannelToString(PaymentChannel paymentChannel) {
        return paymentChannel != null ? paymentChannel.name() : null;
    }
    
    @Named("stringToPaymentChannel")
    default PaymentChannel stringToPaymentChannel(String paymentChannel) {
        return paymentChannel != null ? PaymentChannel.valueOf(paymentChannel) : null;
    }
    
    @Named("toBigDecimalOrZero")
    default BigDecimal toBigDecimalOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
