package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RefundStatus;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import com.bytz.modules.cms.payment.domain.valueobject.CompletedPaymentTransactionValueObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 支付单聚合根
 * Payment Aggregate Root
 * 
 * 管理支付单的完整生命周期，包括创建、支付执行、退款处理、状态管理
 * 这是DDD中的聚合根，封装所有支付相关的业务逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAggregate {
    
    /**
     * 数据库主键ID（使用雪花算法生成）
     */
    private String id;
    
    /**
     * 支付单号（聚合根业务编码）
     */
    private String code;
    
    /**
     * 关联订单号
     */
    private String orderId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 已退款金额
     */
    private BigDecimal refundedAmount;
    
    /**
     * 实际收款金额（已支付金额 - 已退款金额）
     */
    private BigDecimal actualAmount;
    
    /**
     * 币种（默认CNY）
     */
    private String currency;
    
    /**
     * 支付类型
     */
    private PaymentType paymentType;
    
    /**
     * 支付状态
     */
    private PaymentStatus paymentStatus;
    
    /**
     * 退款状态
     */
    private RefundStatus refundStatus;
    
    /**
     * 业务描述
     */
    private String businessDesc;
    
    /**
     * 支付截止时间
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 关联业务ID
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 业务到期日
     */
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务标签（JSON格式）
     */
    private String businessTags;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 创建人姓名
     */
    private String createByName;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 更新人姓名
     */
    private String updateByName;
    
    /**
     * 运行期支付流水（可修改的流水，状态为PROCESSING）
     * 业务规则：同时只能有一条支付明细为运行期状态
     * Running transaction that can be modified (only one at a time)
     */
    private PaymentTransaction runningTransaction;

    /**
     * 已完成支付流水列表（不可修改的值对象，状态为SUCCESS或FAILED）
     * Completed transactions that are immutable value objects
     */
    @Builder.Default
    private List<CompletedPaymentTransactionValueObject> completedTransactions = new ArrayList<>();

    /**
     * 获取所有流水（运行期 + 已完成）
     * 用于向后兼容，返回所有流水的统一视图
     *
     * @return 按创建时间倒序排列的所有流水列表
     * @deprecated 建议使用 getRunningTransaction() 和 getCompletedTransactions()
     */
    public List<PaymentTransaction> getTransactions() {
        List<PaymentTransaction> allTransactions = new ArrayList<>();
        if (runningTransaction != null) {
            allTransactions.add(runningTransaction);
        }
        // 将已完成的值对象转换为PaymentTransaction以保持向后兼容
        for (CompletedPaymentTransactionValueObject completed : completedTransactions) {
            allTransactions.add(convertToTransaction(completed));
        }
        allTransactions.sort(Comparator.reverseOrder());
        return allTransactions;
    }

    /**
     * 将已完成流水值对象转换为PaymentTransaction（仅用于向后兼容）
     */
    private PaymentTransaction convertToTransaction(CompletedPaymentTransactionValueObject vo) {
        return PaymentTransaction.builder()
                .id(vo.getId())
                .code(vo.getCode())
                .paymentId(vo.getPaymentId())
                .transactionType(vo.getTransactionType())
                .transactionStatus(vo.getTransactionStatus())
                .transactionAmount(vo.getTransactionAmount())
                .paymentChannel(vo.getPaymentChannel())
                .channelTransactionNumber(vo.getChannelTransactionNumber())
                .channelPaymentRecordId(vo.getChannelPaymentRecordId())
                .paymentWay(vo.getPaymentWay())
                .originalTransactionId(vo.getOriginalTransactionId())
                .businessOrderId(vo.getBusinessOrderId())
                .createTime(vo.getCreateTime())
                .completeDateTime(vo.getCompleteDateTime())
                .expirationTime(vo.getExpirationTime())
                .businessRemark(vo.getBusinessRemark())
                .createBy(vo.getCreateBy())
                .createByName(vo.getCreateByName())
                .updateBy(vo.getUpdateBy())
                .updateByName(vo.getUpdateByName())
                .updateTime(vo.getUpdateTime())
                .build();
    }

    /**
     * 创建新的支付单
     * 
     * @param orderId 订单号
     * @param resellerId 经销商ID
     * @param paymentAmount 支付金额
     * @param currency 币种
     * @param paymentType 支付类型
     * @param businessDesc 业务描述
     * @param paymentDeadline 支付截止时间
     * @param relatedBusinessId 关联业务ID
     * @param relatedBusinessType 关联业务类型
     * @param businessExpireDate 业务到期日
     * @return 新创建的支付单聚合根
     * TODO: 实现支付单创建逻辑，包括参数验证、唯一性校验、金额验证等
     */
    public static PaymentAggregate create(
            String orderId,
            String resellerId,
            BigDecimal paymentAmount,
            String currency,
            PaymentType paymentType,
            String businessDesc,
            LocalDateTime paymentDeadline,
            String relatedBusinessId,
            RelatedBusinessType relatedBusinessType,
            LocalDateTime businessExpireDate) {
        
        // TODO: 实现创建逻辑
        // 1. 参数验证
        // 2. 生成唯一支付单号
        // 3. 初始化金额字段
        // 4. 设置初始状态为未支付
        // 5. 如果是信用还款类型，验证订单号与信用记录的绑定关系
        
        return PaymentAggregate.builder()
                .orderId(orderId)
                .resellerId(resellerId)
                .paymentAmount(paymentAmount)
                .paidAmount(BigDecimal.ZERO)
                .refundedAmount(BigDecimal.ZERO)
                .actualAmount(BigDecimal.ZERO)
                .currency(currency != null ? currency : "CNY")
                .paymentType(paymentType)
                .paymentStatus(PaymentStatus.UNPAID)
                .refundStatus(RefundStatus.NO_REFUND)
                .businessDesc(businessDesc)
                .paymentDeadline(paymentDeadline)
                .relatedBusinessId(relatedBusinessId)
                .relatedBusinessType(relatedBusinessType)
                .businessExpireDate(businessExpireDate)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .runningTransaction(null)
                .completedTransactions(new ArrayList<>())
                .build();
    }
    
    /**
     * 执行支付操作
     * 
     * @param paymentChannel 支付渠道
     * @param amount 本次支付金额
     * @param channelTransactionNumber 渠道交易号
     * @return 创建的支付流水
     * TODO: 实现支付执行逻辑，包括状态验证、金额验证、流水创建等
     */
    public PaymentTransaction executePayment(
            PaymentChannel paymentChannel,
            BigDecimal amount,
            String channelTransactionNumber,
            String channelPaymentRecordId) {
        
        // TODO: 实现支付执行逻辑
        // 1. 验证支付单状态是否允许支付
        // 2. 验证支付金额不超过待支付金额
        // 3. 创建支付流水记录
        // 4. 更新支付单状态为"支付中"
        // 5. 返回支付流水
        
        // 业务规则：同时只能有一条支付明细为运行期
        if (this.runningTransaction != null) {
            throw new IllegalStateException("已存在运行期的支付流水，同时只能有一条支付明细为运行期状态");
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
                .paymentId(this.id)
                .transactionType(TransactionType.PAYMENT)
                .transactionStatus(TransactionStatus.PROCESSING)
                .transactionAmount(amount)
                .paymentChannel(paymentChannel)
                .channelTransactionNumber(channelTransactionNumber)
                .channelPaymentRecordId(channelPaymentRecordId)
                .build();

        this.runningTransaction = transaction;
        this.paymentStatus = PaymentStatus.PAYING;

        return transaction;
    }
    
    /**
     * 处理支付回调，更新支付结果
     * 
     * @param transactionCode 流水号
     * @param success 是否成功
     * @param completeTime 完成时间
     * TODO: 实现支付回调处理逻辑，包括流水状态更新、支付单金额和状态更新等
     */
    public void handlePaymentCallback(String transactionCode, boolean success, LocalDateTime completeTime) {
        // TODO: 实现回调处理逻辑
        // 1. 查找对应的支付流水
        // 2. 更新流水状态
        // 3. 如果成功，累加已支付金额
        // 4. 更新支付单状态（部分支付/已支付）
        // 5. 重新计算实际收款金额
        // 6. 将完成的流水从运行期列表移到已完成列表

        PaymentTransaction transaction = findTransactionByCode(transactionCode);
        if (transaction == null) {
            throw new IllegalArgumentException("未找到对应的支付流水");
        }
        
        if (success) {
            transaction.markAsSuccess(completeTime);
            this.paidAmount = this.paidAmount.add(transaction.getTransactionAmount());
            this.actualAmount = this.paidAmount.subtract(this.refundedAmount);
            
            // 判断是否全额支付
            if (this.paidAmount.compareTo(this.paymentAmount) == 0) {
                this.paymentStatus = PaymentStatus.PAID;
            } else {
                this.paymentStatus = PaymentStatus.PARTIAL_PAID;
            }
        } else {
            transaction.markAsFailed("支付失败");
            this.paymentStatus = PaymentStatus.FAILED;
        }
        
        // 将完成的流水移到已完成列表（转换为不可变值对象）
        moveTransactionToCompleted(transaction);

        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 将完成的流水从运行期移到已完成列表
     *
     * @param transaction 完成的流水
     */
    private void moveTransactionToCompleted(PaymentTransaction transaction) {
        // 清空运行期流水
        this.runningTransaction = null;

        // 转换为不可变值对象并添加到已完成列表
        CompletedPaymentTransactionValueObject completedTransaction = CompletedPaymentTransactionValueObject.builder()
                .id(transaction.getId())
                .code(transaction.getCode())
                .paymentId(transaction.getPaymentId())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getTransactionStatus())
                .transactionAmount(transaction.getTransactionAmount())
                .paymentChannel(transaction.getPaymentChannel())
                .channelTransactionNumber(transaction.getChannelTransactionNumber())
                .channelPaymentRecordId(transaction.getChannelPaymentRecordId())
                .paymentWay(transaction.getPaymentWay())
                .originalTransactionId(transaction.getOriginalTransactionId())
                .businessOrderId(transaction.getBusinessOrderId())
                .createTime(transaction.getCreateTime())
                .completeDateTime(transaction.getCompleteDateTime())
                .expirationTime(transaction.getExpirationTime())
                .businessRemark(transaction.getBusinessRemark())
                .createBy(transaction.getCreateBy())
                .createByName(transaction.getCreateByName())
                .updateBy(transaction.getUpdateBy())
                .updateByName(transaction.getUpdateByName())
                .updateTime(transaction.getUpdateTime())
                .build();

        this.completedTransactions.add(completedTransaction);
    }

    /**
     * 执行退款操作
     * 
     * @param refundAmount 退款金额
     * @param originalTransactionId 原支付流水ID（必须提供）
     * @param businessOrderId 业务单号（退款单号）
     * @param refundReason 退款原因
     * @return 创建的退款流水
     */
    public PaymentTransaction executeRefund(
            BigDecimal refundAmount,
            String originalTransactionId,
            String businessOrderId,
            String refundReason) {
        
        // 1. 验证必填参数
        if (originalTransactionId == null) {
            throw new IllegalArgumentException("原支付流水ID不能为空");
        }
        
        // 2. 验证支付单状态是否允许退款
        if (!canRefund()) {
            throw new IllegalArgumentException("当前支付单状态不允许退款");
        }
        
        // 3. 验证退款金额不超过可退款金额（已支付金额 - 已退款金额）
        BigDecimal refundableAmount = this.paidAmount.subtract(this.refundedAmount);
        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException("退款金额不能超过可退款金额");
        }
        
        // 4. 查找并验证原支付流水（需要在已完成列表中查找）
        CompletedPaymentTransactionValueObject originalTransaction = findCompletedTransactionById(originalTransactionId);
        if (originalTransaction == null) {
            throw new IllegalArgumentException("未找到原支付流水: " + originalTransactionId);
        }
        
        if (!originalTransaction.isPaymentTransaction()) {
            throw new IllegalArgumentException("指定的流水不是支付流水");
        }
        
        if (!originalTransaction.isSuccess()) {
            throw new IllegalArgumentException("指定的支付流水未成功，无法退款");
        }
        
        // 业务规则：同时只能有一条支付明细为运行期
        if (this.runningTransaction != null) {
            throw new IllegalStateException("已存在运行期的支付流水，同时只能有一条支付明细为运行期状态");
        }

        // 5. 创建退款流水记录
        PaymentTransaction refundTransaction = PaymentTransaction.builder()
                .paymentId(this.id)
                .transactionType(TransactionType.REFUND)
                .transactionStatus(TransactionStatus.PROCESSING)
                .transactionAmount(refundAmount)
                .paymentChannel(originalTransaction.getPaymentChannel())
                .originalTransactionId(originalTransactionId)
                .businessOrderId(businessOrderId)
                .businessRemark(refundReason)
                .createTime(LocalDateTime.now())
                .build();

        // 6. 更新退款状态
        this.runningTransaction = refundTransaction;
        this.refundStatus = RefundStatus.REFUNDING;
        this.updateTime = LocalDateTime.now();
        
        return refundTransaction;
    }
    
    /**
     * 处理退款回调，更新退款结果
     * 
     * @param transactionCode 退款流水号
     * @param success 是否成功
     * @param completeTime 完成时间
     * TODO: 实现退款回调处理逻辑，包括流水状态更新、退款金额累加、退款状态更新等
     */
    public void handleRefundCallback(String transactionCode, boolean success, LocalDateTime completeTime) {
        // TODO: 实现退款回调处理逻辑
        // 1. 查找对应的退款流水
        // 2. 更新流水状态
        // 3. 如果成功，累加已退款金额
        // 4. 重新计算实际收款金额
        // 5. 更新退款状态（部分退款/全额退款）
        // 6. 将完成的流水从运行期列表移到已完成列表

        PaymentTransaction transaction = findTransactionByCode(transactionCode);
        if (transaction == null) {
            throw new IllegalArgumentException("未找到对应的退款流水");
        }
        
        if (success) {
            transaction.markAsSuccess(completeTime);
            this.refundedAmount = this.refundedAmount.add(transaction.getTransactionAmount());
            this.actualAmount = this.paidAmount.subtract(this.refundedAmount);
            
            // 判断是否全额退款
            if (this.refundedAmount.compareTo(this.paidAmount) == 0) {
                this.refundStatus = RefundStatus.FULL_REFUNDED;
            } else {
                this.refundStatus = RefundStatus.PARTIAL_REFUNDED;
            }
        } else {
            transaction.markAsFailed("退款失败");
            this.refundStatus = RefundStatus.REFUND_FAILED;
        }
        
        // 将完成的流水移到已完成列表
        moveTransactionToCompleted(transaction);

        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 计算待支付金额
     * 
     * @return 待支付金额
     */
    public BigDecimal getPendingAmount() {
        return this.paymentAmount.subtract(this.paidAmount);
    }
    
    /**
     * 判断是否允许支付
     * 
     * @return true如果允许支付，否则false
     * TODO: 实现支付允许条件的完整验证逻辑
     */
    public boolean canPay() {
        // TODO: 完善支付允许条件
        // 1. 状态必须为未支付或部分支付
        // 2. 待支付金额必须大于0
        // 3. 支付单未被冻结
        return (PaymentStatus.UNPAID.equals(this.paymentStatus) 
                || PaymentStatus.PARTIAL_PAID.equals(this.paymentStatus))
                && getPendingAmount().compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 判断是否允许退款
     * 
     * @return true如果允许退款，否则false
     * TODO: 实现退款允许条件的完整验证逻辑
     */
    public boolean canRefund() {
        // TODO: 完善退款允许条件
        // 1. 必须有已支付金额
        // 2. 已退款金额小于已支付金额
        // 3. 支付单未被冻结
        return this.paidAmount.compareTo(BigDecimal.ZERO) > 0
                && this.refundedAmount.compareTo(this.paidAmount) < 0;
    }
    
    /**
     * 验证退款前置条件
     * 
     * @param refundAmount 退款金额
     * @throws IllegalArgumentException 如果不满足退款条件
     */
    public void validateRefundable(BigDecimal refundAmount) {
        if (!canRefund()) {
            throw new IllegalArgumentException("当前支付单状态不允许退款");
        }
        
        BigDecimal refundableAmount = this.paidAmount.subtract(this.refundedAmount);
        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException(
                    String.format("退款金额 %s 超过可退款金额 %s", refundAmount, refundableAmount));
        }
        
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
    }
    
    /**
     * 判断是否为信用还款类型
     * 
     * @return true如果是信用还款，否则false
     */
    public boolean isCreditRepayment() {
        return PaymentType.CREDIT_REPAYMENT.equals(this.paymentType);
    }
    
    /**
     * 根据主键ID查找运行期支付流水
     *
     * @param transactionId 流水ID
     * @return 支付流水，如果未找到返回null
     */
    private PaymentTransaction findTransactionById(String transactionId) {
        if (this.runningTransaction != null
                && this.runningTransaction.getId() != null
                && this.runningTransaction.getId().equals(transactionId)) {
            return this.runningTransaction;
        }
        return null;
    }

    /**
     * 根据主键ID查找已完成的支付流水
     *
     * @param transactionId 流水ID
     * @return 已完成的支付流水值对象，如果未找到返回null
     */
    private CompletedPaymentTransactionValueObject findCompletedTransactionById(String transactionId) {
        return this.completedTransactions.stream()
                .filter(t -> t.getId() != null && t.getId().equals(transactionId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据业务编码查找运行期支付流水
     *
     * @param transactionCode 流水业务编码
     * @return 支付流水，如果未找到返回null
     */
    private PaymentTransaction findTransactionByCode(String transactionCode) {
        if (this.runningTransaction != null
                && this.runningTransaction.getCode() != null
                && this.runningTransaction.getCode().equals(transactionCode)) {
            return this.runningTransaction;
        }
        return null;
    }

    /**
     * 获取最新的处理中的支付流水
     * 用于批量支付流程中回写渠道信息
     *
     * @return 最新的处理中的支付流水，如果未找到返回null
     */
    public PaymentTransaction getLatestProcessingTransaction() {
        if (this.runningTransaction != null
                && TransactionType.PAYMENT.equals(this.runningTransaction.getTransactionType())
                && TransactionStatus.PROCESSING.equals(this.runningTransaction.getTransactionStatus())) {
            return this.runningTransaction;
        }
        return null;
    }
    
    /**
     * 停止支付单
     * 
     * @param reason 停止原因
     * TODO: 实现停止支付单的逻辑
     */
    public void stop(String reason) {
        // TODO: 实现停止逻辑
        // 1. 验证当前状态是否允许停止
        // 2. 更新状态为已停止
        // 3. 记录停止原因
        this.paymentStatus = PaymentStatus.STOPPED;
        this.businessDesc = (this.businessDesc != null ? this.businessDesc + "; " : "") + "停止原因: " + reason;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 冻结支付单
     * 
     * @param reason 冻结原因
     * TODO: 实现冻结支付单的逻辑
     */
    public void freeze(String reason) {
        // TODO: 实现冻结逻辑
        // 1. 验证当前状态是否允许冻结
        // 2. 更新状态为已冻结
        // 3. 记录冻结原因
        this.paymentStatus = PaymentStatus.FROZEN;
        this.businessDesc = (this.businessDesc != null ? this.businessDesc + "; " : "") + "冻结原因: " + reason;
        this.updateTime = LocalDateTime.now();
    }

    // 新增：当设置聚合根ID时，确保同步所有子聚合的 paymentId
    public void setId(String id) {
        this.id = id;
        if (this.runningTransaction != null) {
            this.runningTransaction.setPaymentId(id);
        }
    }
}