# 全局词汇表 (Global Glossary)

## 文档信息
| 项目 | 内容 |
|------|------|
| **文档名称** | 全局词汇表 |
| **项目名称** | 企业间特种设备定制交易系统 - 支付模块 |
| **文档版本** | v1.0 |
| **创建日期** | 2025年9月26日 |
| **最后更新** | 2025年9月26日 |
| **维护责任** | 架构团队 |

## 词汇表说明

本词汇表定义了支付模块及相关业务领域的标准术语,所有团队成员和相关文档必须遵循此词汇表中的术语定义,确保跨团队沟通的一致性和准确性。

---

## 核心业务术语 (Core Business Terms)

### 1. Payment Order (支付单)
- **英文术语**: Payment Order
- **中文术语**: 支付单
- **定义**: 系统内部生成的支付请求记录,由支付模块管理和生成唯一支付单号,包含完整的支付信息和状态
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Payment Order ID, Transaction, Order
- **示例**: "支付单 PO202509260001 已创建,等待用户支付"
- **变更历史**: v1.0 - 初始定义

### 2. Payment Channel (支付渠道)
- **英文术语**: Payment Channel
- **中文术语**: 支付渠道
- **定义**: 第三方支付服务商(如银联、支付宝企业版)或企业内部支付管理模块,为支付执行提供具体的技术实现
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Channel Provider, Payment Method, Channel Configuration
- **示例**: "银联B2B支付渠道", "企业内部账户渠道"
- **变更历史**: v1.0 - 初始定义

### 3. Transaction (交易)
- **英文术语**: Transaction
- **中文术语**: 交易
- **定义**: 支付渠道执行的具体支付操作,包含渠道返回的唯一交易编号和交易状态
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Transaction ID, Payment Transaction, Refund Transaction
- **示例**: "交易 TX202509260001 已成功完成"
- **变更历史**: v1.0 - 初始定义

### 4. Batch Payment (合并支付)
- **英文术语**: Batch Payment
- **中文术语**: 合并支付
- **定义**: 多个支付单选择同一个支付渠道进行合并处理的支付方式,提高支付效率并降低手续费
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Payment Order, Payment Channel, Bulk Operation
- **示例**: "客户选择3个支付单进行银联合并支付"
- **变更历史**: v1.0 - 初始定义

### 5. Partial Payment (部分支付)
- **英文术语**: Partial Payment
- **中文术语**: 部分支付
- **定义**: 支付单可以选择一个渠道支付部分金额,支持多次部分支付直到完成全额支付
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Payment Order, Paid Amount, Pending Amount
- **示例**: "支付单总额100万,本次部分支付30万"
- **变更历史**: v1.0 - 初始定义

### 6. Refund (退款)
- **英文术语**: Refund
- **中文术语**: 退款
- **定义**: 将已支付金额返还给付款方的操作,由订单系统审批后由支付模块执行
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Refund Transaction, Refund Amount, Original Transaction
- **示例**: "支付单PO001执行部分退款5万元"
- **变更历史**: v1.0 - 初始定义

### 7. Reconciliation (对账)
- **英文术语**: Reconciliation
- **中文术语**: 对账
- **定义**: 与支付渠道核对交易数据的过程,确保系统内交易记录与渠道记录一致
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Settlement, Transaction Verification, Financial Audit
- **示例**: "每日定时与银联进行交易对账"
- **变更历史**: v1.0 - 初始定义

### 8. Payment Callback (支付回调)
- **英文术语**: Payment Callback
- **中文术语**: 支付回调
- **定义**: 支付渠道在支付完成后异步通知系统的机制,用于更新支付状态
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Callback Processing, Notification, Webhook
- **示例**: "银联回调通知支付成功,系统更新支付单状态"
- **变更历史**: v1.0 - 初始定义

### 9. Payment Type (支付类型)
- **英文术语**: Payment Type
- **中文术语**: 支付类型
- **定义**: 区分支付单业务性质的分类,包括预付款、尾款、其他费用等
- **使用上下文**: Payment Context (支付上下文), Order Context (订单上下文)
- **相关术语**: Advance Payment, Final Payment, Order Lifecycle
- **示例**: "预付款类型的支付单", "尾款类型的支付单"
- **变更历史**: v1.0 - 初始定义

### 10. Company User (企业用户)
- **英文术语**: Company User
- **中文术语**: 企业用户
- **定义**: 在系统中注册的企业实体,作为B2B交易的参与主体,具有支付资格和权限
- **使用上下文**: User Context (用户上下文), Payment Context (支付上下文)
- **相关术语**: Enterprise Account, Business Entity, Corporate Customer
- **示例**: "企业用户ABC公司发起支付请求"
- **变更历史**: v1.0 - 初始定义

### 11. Payment Order ID (支付单号)
- **英文术语**: Payment Order ID
- **中文术语**: 支付单号
- **定义**: 由支付模块管理和生成的唯一流水号,作为支付单的唯一标识符
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Payment Order, Unique Identifier, System ID
- **示例**: "PO202509260001", "PAY_20250926_123456"
- **变更历史**: v1.0 - 初始定义

### 12. Transaction ID (交易号)
- **英文术语**: Transaction ID
- **中文术语**: 交易号
- **定义**: 支付渠道返回的唯一交易编号,用于在支付渠道中标识具体的支付操作
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Payment Channel, External System ID, Channel Response
- **示例**: "2025092612345678901234", "TXN_UNIONPAY_001"
- **变更历史**: v1.0 - 初始定义

### 13. Payment Transaction (支付流水)
- **英文术语**: Payment Transaction
- **中文术语**: 支付流水
- **定义**: 记录支付单每次支付操作的详细流水记录,包含支付金额、渠道、状态等信息
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Payment Order, Transaction Record, Payment History
- **示例**: "支付流水显示用户通过银联支付了30万元"
- **变更历史**: v1.0 - 初始定义

### 14. Refund Transaction (退款流水)
- **英文术语**: Refund Transaction
- **中文术语**: 退款流水
- **定义**: 记录支付单每次退款操作的详细流水记录,包含退款金额、原因、状态等信息
- **使用上下文**: Payment Context (支付上下文)
- **相关术语**: Payment Order, Refund Record, Financial Audit
- **示例**: "退款流水显示向用户退还了5万元"
- **变更历史**: v1.0 - 初始定义

### 15. Money (金额)
- **英文术语**: Money
- **中文术语**: 金额
- **定义**: 包含数值和货币类型的金额值对象,确保金额计算的准确性和货币一致性
- **使用上下文**: Payment Context (支付上下文), Finance Context (财务上下文)
- **相关术语**: Currency, Amount, Financial Value
- **示例**: "100000.00 CNY", "50000.00 USD"
- **变更历史**: v1.0 - 初始定义

---

## 术语分类索引

### 按业务领域分类
- **支付管理**: Payment Order, Payment Channel, Transaction, Batch Payment, Partial Payment, Payment Order ID, Transaction ID
- **流水记录**: Payment Transaction, Refund Transaction, Transaction Record
- **退款管理**: Refund, Refund Transaction
- **系统集成**: Payment Callback, Reconciliation
- **业务分类**: Payment Type, Company User
- **基础数据**: Money, Currency, Financial Value

### 按技术层次分类
- **领域层**: Payment Order, Payment Channel, Transaction, Refund, Money
- **应用层**: Batch Payment, Partial Payment, Payment Callback, Payment Transaction, Refund Transaction
- **基础设施层**: Reconciliation, Channel Configuration, Payment Order ID, Transaction ID

### 按使用频率分类
- **高频术语**: Payment Order, Payment Channel, Transaction, Company User, Money, Payment Order ID
- **中频术语**: Batch Payment, Partial Payment, Refund, Payment Type, Payment Transaction, Transaction ID
- **低频术语**: Reconciliation, Payment Callback, Refund Transaction

---

## 术语使用规范

### 命名约定
1. **英文术语**: 使用 Pascal Case (如 PaymentOrder)
2. **中文术语**: 使用标准中文表述
3. **代码实现**: 遵循各编程语言的命名规范
4. **数据库字段**: 使用 snake_case (如 payment_order_id)
5. **API接口**: 使用 camelCase (如 paymentOrderId)

### 使用原则
1. **一致性原则**: 同一概念在所有文档和代码中使用相同术语
2. **准确性原则**: 术语定义必须准确反映业务含义
3. **简洁性原则**: 优先使用简洁明了的术语
4. **标准化原则**: 遵循行业标准和最佳实践
5. **可扩展原则**: 术语定义支持业务发展和系统扩展

---

## 维护说明

### 更新流程
1. **提出变更**: 任何人都可以提出术语变更需求
2. **评审讨论**: 架构团队组织相关人员进行评审
3. **影响分析**: 分析术语变更对现有文档和代码的影响
4. **更新执行**: 批准后更新词汇表和相关文档
5. **通知发布**: 向所有相关团队发布变更通知

### 版本管理
- **主版本**: 重大术语体系调整时递增
- **次版本**: 新增重要术语或修改现有术语定义时递增
- **修订版本**: 修正错误或完善描述时递增

### 质量保证
- **定期审查**: 每季度审查术语使用情况
- **一致性检查**: 定期检查文档和代码中的术语使用一致性
- **培训推广**: 新团队成员必须学习和遵循词汇表规范

---

## 附录

### 参考资料
- 支付模块需求设计文档 v1.0
- Payment Context 设计文档 v1.0
- DDD分层架构设计规范

### 相关标准
- ISO 20022: 金融业务消息标准
- PCI DSS: 支付卡行业数据安全标准
- 企业软件架构设计最佳实践