package com.bytz.cms.payment.application.assembler;

import com.bytz.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.interfaces.model.PaymentCreateRO;
import com.bytz.cms.payment.interfaces.model.PaymentVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 支付单聚合根的MapStruct转换器
 * 
 * <p>负责RO/VO与Domain对象之间的转换</p>
 * <p>术语来源：Glossary.md - 应用层术语</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentAssembler {
    
    /**
     * 将创建请求转换为创建命令
     * 
     * @param ro 创建支付单请求对象
     * @return 创建支付单命令
     */
    CreatePaymentCommand toCreateCommand(PaymentCreateRO ro);
    
    /**
     * 将支付单聚合根转换为响应对象
     * 
     * @param aggregate 支付单聚合根
     * @return 支付单响应对象
     */
    @Mapping(target = "pendingAmount", expression = "java(aggregate.getPendingAmount())")
    PaymentVO toVO(PaymentAggregate aggregate);
    
    /**
     * 批量转换支付单聚合根为响应对象
     * 
     * @param aggregates 支付单聚合根列表
     * @return 支付单响应对象列表
     */
    List<PaymentVO> toVOs(List<PaymentAggregate> aggregates);
}
