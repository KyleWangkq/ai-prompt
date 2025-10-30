# 支付模块领域模型文档（与当前实现对齐）

## 领域模型概览
- 聚合根（Aggregate Root）
  - PaymentAggregate：支付单的唯一聚合根，封装支付/退款的核心业务规则与状态。
- 聚合内领域对象
  - PaymentTransaction：支付流水/退款流水，记录每次交易过程与结果，由聚合根统一管理。
- 实体（Entity）
  - 当前版本无独立放置于 domain/entity 包的实体类。
- 值对象（Value Object）
  - 当前版本无独立值对象类。已完成流水通过 PaymentAggregate.completedTransactions 提供不可变视图（getTransactions 返回只读列表，含运行期流水与已完成流水）。
- 枚举（Enums）
  - PaymentChannel：支付渠道类型（ONLINE_PAYMENT、WALLET_PAYMENT、WIRE_TRANSFER、CREDIT_ACCOUNT）。
  - PaymentStatus：支付状态（UNPAID、PAYING、PARTIAL_PAID、PAID、FAILED、STOPPED、FROZEN、CANCELED）。
  - PaymentType：支付类型（ADVANCE_PAYMENT、FINAL_PAYMENT、OTHER_PAYMENT、CREDIT_REPAYMENT）。
  - RefundStatus：退款状态（NO_REFUND、REFUNDING、PARTIAL_REFUNDED、FULL_REFUNDED、REFUND_FAILED）。
  - RelatedBusinessType：关联业务类型（CREDIT_RECORD、DELIVERY_ORDER、ORDER）。
  - TransactionStatus：交易状态（PROCESSING、SUCCESS、FAILED）。
  - TransactionType：交易类型（PAYMENT、REFUND）。
- 命令（Command，domain/command）
  - StartPaymentCommand：渠道发起支付使用。
  - CreateRefundRequestCommand：创建退款申请参数模型。
  - QueryPaymentStatusCommand：查询支付状态。
  - QueryRefundStatusCommand：查询退款状态。
- 仓储接口（Repository，domain/repository）
  - IPaymentRepository：支付聚合根持久化与查询端口（save/findById/findByCode/findByOrderId/findByResellerId/findByRelatedBusinessId/findByIds/deleteById/deleteByCode）。
  - IPaymentChannelService：支付渠道服务端口（面向领域层的渠道能力抽象）。
- 领域服务（Domain Service）
  - PaymentDomainService：编排与跨聚合规则校验（支付/退款执行、回调处理、金额分配校验等）。

---

## 模型详细说明

### PaymentAggregate（聚合根）
- 描述
  - 代表一张支付单的完整生命周期，统一管理支付状态、退款状态、金额变更与交易流水，确保领域不变量：
    - 同时最多存在一条运行期流水（runningTransaction）。
    - 实际收款金额 actualAmount = paidAmount - refundedAmount。
    - 退款金额 ≤ 已支付金额 - 已退款金额。
- 关键属性
  - id：主键ID
  - code：支付单号
  - orderId：关联订单号
  - resellerId：经销商ID
  - paymentAmount：支付金额
  - paidAmount：已支付金额
  - refundedAmount：已退款金额
  - actualAmount：实际收款金额（派生）
  - currency：币种（默认 CNY）
  - paymentType：支付类型（PaymentType）
  - paymentStatus：支付状态（PaymentStatus）
  - refundStatus：退款状态（RefundStatus）
  - businessDesc：业务描述
  - paymentDeadline：支付截止时间
  - relatedBusinessId：关联业务ID
  - relatedBusinessType：关联业务类型（RelatedBusinessType）
  - businessExpireDate：业务到期日
  - businessTags：业务标签（JSON）
  - createTime/updateTime/createBy/createByName/updateBy/updateByName：审计字段
  - runningTransaction：运行期流水（仅 PROCESSING）
  - completedTransactions：已完成流水列表（SUCCESS/FAILED，不可变语义）
- 主要行为与规则
  - create(...)：静态工厂，初始化金额与初始状态（UNPAID/NO_REFUND），生成业务编码等。
  - executePayment(channel, amount, remark)：发起一次支付；生成运行期流水（PROCESSING），支付单状态置为 PAYING；需满足 canPay()。
  - handlePaymentCallback(transactionCode, success, completeTime)：处理支付回调；
    - 成功：累加 paidAmount，重算 actualAmount，状态在 PARTIAL_PAID/PAID 间转换。
    - 失败：置为 FAILED。
    - 完成后将运行期流水移入 completedTransactions。
  - executeRefund(refundAmount, originalTransactionId, businessOrderId, refundReason)：发起退款；校验金额与原成功支付流水，置 refundStatus=REFUNDING；运行期流水为退款 PROCESSING。
  - handleRefundCallback(transactionCode, success, completeTime)：处理退款回调；
    - 成功：累加 refundedAmount，重算 actualAmount，refundStatus 在 PARTIAL_REFUNDED/FULL_REFUNDED 间转换。
    - 失败：置为 REFUND_FAILED。
    - 完成后将运行期流水移入 completedTransactions。
  - getTransactions()：返回运行期 + 已完成流水的只读列表（按创建时间倒序）。
  - getPendingAmount()：计算待支付金额 = paymentAmount - paidAmount。
  - canPay()：允许在 UNPAID 或 PARTIAL_PAID 且无运行期流水且待支付金额>0 时支付。
  - canRefund()：已支付>0 且 已退款<已支付，且非 PAYING/UNPAID 且无运行期流水时允许退款。
  - validateRefundable(amount)：金额合法性校验（>0 且 ≤ 可退金额）。
  - findCompletedTransactionById(id)：在已完成流水中查找。
  - stop(reason)/freeze(reason)：将状态置为 STOPPED/FROZEN 并记录原因。
  - cancel(reason)：仅 UNPAID 且无任何流水、已支付=0 时可取消；置状态为 CANCELED。
  - canCancel()：是否可取消的判定。
  - setId(id)：同步子对象的 paymentId。
  - updateAggregateAfterPersistence()：持久化后如运行期流水已结束则转入完成列表。
- 状态流转（摘要）
  - PaymentStatus：UNPAID → PAYING → PARTIAL_PAID/PAID/FAILED；STOPPED/FROZEN/CANCELED 为终止/冻结态。
  - RefundStatus：NO_REFUND → REFUNDING → PARTIAL_REFUNDED/FULL_REFUNDED/REFUND_FAILED。

### PaymentTransaction（聚合内领域对象：支付/退款流水）
- 描述
  - 记录一次“支付或退款”的交易信息与状态变更；除交易状态与渠道号外不应被修改。
- 关键属性
  - id：主键ID
  - code：流水号
  - paymentId：所属支付单ID
  - transactionType：交易类型（PAYMENT/REFUND）
  - transactionStatus：交易状态（PROCESSING/SUCCESS/FAILED）
  - transactionAmount：交易金额
  - paymentChannel：支付渠道（PaymentChannel）
  - channelTransactionNumber：渠道交易号
  - channelPaymentRecordId：渠道支付记录ID
  - paymentWay：支付方式
  - originalTransactionId：原支付流水ID（用于退款）
  - businessOrderId：业务单号（如退款单号）
  - createTime/completeDateTime/expirationTime：时间信息
  - businessRemark：业务备注
  - createBy/createByName/updateBy/updateByName/updateTime：审计字段
- 行为
  - isPaymentTransaction()/isRefundTransaction()：类型判断。
  - isSuccess()/isFailed()/isProcessing()：状态判断。
  - markAsSuccess(completeTime)/markAsFailed(reason)：状态终结。
  - updateChannelTransactionNumber(no)：更新渠道交易号。
  - Comparable：按 createTime 排序；聚合根对外提供倒序视图（最新优先）。

---

## 领域服务（PaymentDomainService）
- 职责
  - 编排跨聚合或跨渠道的领域操作；进行支付/退款的前置校验、金额分配校验与回调处理；通过端口调用渠道服务与仓储。
- 核心方法（摘要）
  - executeSinglePayment(payment, channelService, amount, remark, resellerId)：单支付单支付执行。
  - executeUnifiedPayment(payments, allocatedAmounts, channelService, resellerId)：统一（批量/合并）支付。
  - validateCanPay(payment, amount)：支付前置校验。
  - validateAmountAllocation(payments, allocatedAmounts, total)：金额分配校验。
  - allocateAmounts(payments, amountMap, total)：金额分配（使用校验规则）。
  - validateRefund(payment, refundAmount)：退款前置校验。
  - executeRefund(paymentId, refundAmount, originalTransactionId, businessOrderId, refundReason)：选择退款流水并下发退款。
  - processPaymentCallback(paymentId, transactionCode, success, completeTime, channelMessage)：支付回调处理。
  - processRefundCallback(paymentId, transactionCode, success, completeTime, channelMessage)：退款回调处理。

---

## 仓储与端口
- IPaymentRepository（领域仓储接口）
  - save(...)、findById(...)、findByCode(...)、findByOrderId(...)、findByResellerId(...)、findByRelatedBusinessId(...)、findByIds(...)、deleteById(...)、deleteByCode(...)。
  - 说明：实现位于基础设施层；需同时持久化聚合根及其流水子对象，避免 N+1 访问。
- IPaymentChannelService（渠道服务端口）
  - 面向领域层抽象的渠道能力（发起支付、可用性校验、金额限制等；具体方法见实现与命令/响应模型）。

---

## 关键业务规则与边界
- 单一运行期流水：任一时刻仅允许一个 runningTransaction（PROCESSING）。
- 金额不变量：actualAmount = paidAmount - refundedAmount；不可为负。
- 支付许可：仅在 UNPAID 或 PARTIAL_PAID、无运行期流水、待支付金额>0 时允许支付。
- 退款许可：已支付>0、已退款<已支付、非 PAYING/UNPAID、无运行期流水。
- 退款金额：不得超过（已支付 - 已退款）。
- 取消：仅 UNPAID 且无任何流水、已支付=0 可取消；取消后不可逆。
- 视图一致性：getTransactions 返回运行期 + 已完成流水只读视图，按创建时间倒序。

---

## 与应用层/接口层协作说明（摘要）
- 应用层通过 PaymentDomainService 编排支付/退款用例，不嵌入业务规则；领域规则均收敛于 PaymentAggregate/PaymentTransaction 与 PaymentDomainService。
- DTO 与领域对象转换由 MapStruct Assembler（application/assembler）承担；接口层不暴露领域类型。

此文档将随领域模型演进而更新，若实现有调整，请同步修订本文件以保持一致性。