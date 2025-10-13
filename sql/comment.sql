-- =====================================================
-- PostgreSQL 数据库注释脚本
-- 用于为现有支付模块表结构添加注释
-- 适用于 PostgreSQL 数据库
-- =====================================================

-- =====================================================
-- 1. cms_payment 表注释
-- =====================================================

-- 表注释
COMMENT ON TABLE cms_payment IS '支付单表 - 存储支付单的基本信息和状态';

-- 字段注释
COMMENT ON COLUMN cms_payment.id IS '支付单号 - 主键，唯一标识一个支付单';
COMMENT ON COLUMN cms_payment.order_id IS '关联订单号 - 关联的订单ID';
COMMENT ON COLUMN cms_payment.reseller_id IS '经销商ID - 支付方的经销商标识';
COMMENT ON COLUMN cms_payment.payment_amount IS '支付金额 - 应支付总金额，单位：元，精度6位小数';
COMMENT ON COLUMN cms_payment.paid_amount IS '已支付金额 - 实际已支付金额，单位：元，精度6位小数';
COMMENT ON COLUMN cms_payment.refunded_amount IS '已退款金额 - 已退款总金额，单位：元，精度6位小数';
COMMENT ON COLUMN cms_payment.actual_amount IS '实际收款金额 - 计算字段：已支付金额 - 已退款金额';
COMMENT ON COLUMN cms_payment.currency IS '币种 - 货币代码，如：CNY（人民币）、USD（美元）';
COMMENT ON COLUMN cms_payment.payment_type IS '支付类型 - 支付单类型：ADVANCE_PAYMENT（预付款）、FINAL_PAYMENT（尾款）、OTHER_FEE（其他费用）、CREDIT_REPAYMENT（信用还款）';
COMMENT ON COLUMN cms_payment.payment_status IS '支付状态 - 支付单状态：UNPAID（未支付）、PAYING（支付中）、PARTIAL_PAID（部分支付）、PAID（已支付）、FAILED（失败）、STOPPED（已停止）、FROZEN（已冻结）';
COMMENT ON COLUMN cms_payment.refund_status IS '退款状态 - 退款状态：NO_REFUND（无退款）、REFUNDING（退款中）、PARTIAL_REFUNDED（部分退款）、FULL_REFUNDED（全额退款）、REFUND_FAILED（退款失败）';
COMMENT ON COLUMN cms_payment.business_desc IS '业务描述 - 支付单的业务说明和备注信息';
COMMENT ON COLUMN cms_payment.payment_deadline IS '支付截止时间 - 支付单的最后支付期限';
COMMENT ON COLUMN cms_payment.priority_level IS '优先级 - 支付单优先级：1-高、2-中、3-低';
COMMENT ON COLUMN cms_payment.related_business_id IS '关联业务ID - 关联的业务单据ID（如信用记录、配送单等）';
COMMENT ON COLUMN cms_payment.related_business_type IS '关联业务类型 - 关联业务类型：CREDIT_RECORD（信用记录）、DELIVERY_ORDER（配送单）、ADDITIONAL_SERVICE（增值服务）';
COMMENT ON COLUMN cms_payment.business_expire_date IS '业务到期日 - 关联业务的到期时间';
COMMENT ON COLUMN cms_payment.business_tags IS '业务标签 - JSON格式，存储业务相关的标签信息';
COMMENT ON COLUMN cms_payment.create_time IS '创建时间 - 记录创建时间';
COMMENT ON COLUMN cms_payment.update_time IS '更新时间 - 记录最后更新时间';
COMMENT ON COLUMN cms_payment.create_by IS '创建人ID - 创建该记录的用户ID';
COMMENT ON COLUMN cms_payment.create_by_name IS '创建人姓名 - 创建该记录的用户姓名';
COMMENT ON COLUMN cms_payment.update_by IS '更新人ID - 最后更新该记录的用户ID';
COMMENT ON COLUMN cms_payment.update_by_name IS '更新人姓名 - 最后更新该记录的用户姓名';
COMMENT ON COLUMN cms_payment.del_flag IS '删除标识 - 逻辑删除标记：0-正常、1-已删除';
COMMENT ON COLUMN cms_payment.version IS '乐观锁版本号 - 用于并发控制的版本号';

-- =====================================================
-- 2. cms_payment_transaction 表注释
-- =====================================================

-- 表注释
COMMENT ON TABLE cms_payment_transaction IS '支付交易流水表 - 记录支付单的所有交易流水明细';

-- 字段注释
COMMENT ON COLUMN cms_payment_transaction.id IS '交易流水号 - 主键，唯一标识一笔交易流水';
COMMENT ON COLUMN cms_payment_transaction.payment_id IS '支付单号 - 关联的支付单ID，外键关联cms_payment表';
COMMENT ON COLUMN cms_payment_transaction.third_party_trans_id IS '第三方交易号 - 支付渠道返回的交易流水号';
COMMENT ON COLUMN cms_payment_transaction.payment_channel IS '支付渠道 - 支付渠道类型：ALIPAY（支付宝）、WECHAT（微信）、BANK_CARD（银行卡）、ENTERPRISE_ACCOUNT（企业账户）';
COMMENT ON COLUMN cms_payment_transaction.transaction_type IS '交易类型 - 交易类型：PAYMENT（支付）、REFUND（退款）、TRANSFER（转账）';
COMMENT ON COLUMN cms_payment_transaction.transaction_amount IS '交易金额 - 交易金额，单位：元，精度6位小数，支付为正数，退款为负数';
COMMENT ON COLUMN cms_payment_transaction.transaction_status IS '交易状态 - 交易状态：PENDING（待处理）、PROCESSING（处理中）、SUCCESS（成功）、FAILED（失败）、TIMEOUT（超时）、CANCELLED（已取消）';
COMMENT ON COLUMN cms_payment_transaction.failure_reason IS '失败原因 - 交易失败时的具体原因描述';
COMMENT ON COLUMN cms_payment_transaction.request_time IS '请求时间 - 向支付渠道发起请求的时间';
COMMENT ON COLUMN cms_payment_transaction.response_time IS '响应时间 - 支付渠道返回响应的时间';
COMMENT ON COLUMN cms_payment_transaction.request_data IS '请求数据 - JSON格式，存储发送给支付渠道的请求报文';
COMMENT ON COLUMN cms_payment_transaction.response_data IS '响应数据 - JSON格式，存储支付渠道返回的响应报文';
COMMENT ON COLUMN cms_payment_transaction.notification_data IS '通知数据 - JSON格式，存储支付渠道的异步通知数据';
COMMENT ON COLUMN cms_payment_transaction.retry_count IS '重试次数 - 交易失败后的重试次数，最大10次';
COMMENT ON COLUMN cms_payment_transaction.next_retry_time IS '下次重试时间 - 下次自动重试的预定时间';
COMMENT ON COLUMN cms_payment_transaction.remarks IS '备注 - 交易相关的备注信息';
COMMENT ON COLUMN cms_payment_transaction.create_time IS '创建时间 - 记录创建时间';
COMMENT ON COLUMN cms_payment_transaction.update_time IS '更新时间 - 记录最后更新时间';
COMMENT ON COLUMN cms_payment_transaction.create_by IS '创建人ID - 创建该记录的用户ID';
COMMENT ON COLUMN cms_payment_transaction.create_by_name IS '创建人姓名 - 创建该记录的用户姓名';
COMMENT ON COLUMN cms_payment_transaction.update_by IS '更新人ID - 最后更新该记录的用户ID';
COMMENT ON COLUMN cms_payment_transaction.update_by_name IS '更新人姓名 - 最后更新该记录的用户姓名';
COMMENT ON COLUMN cms_payment_transaction.del_flag IS '删除标识 - 逻辑删除标记：0-正常、1-已删除';
COMMENT ON COLUMN cms_payment_transaction.version IS '乐观锁版本号 - 用于并发控制的版本号';

-- =====================================================
-- 索引注释说明
-- =====================================================
-- PostgreSQL 不支持为索引直接添加注释
-- 以下是索引的说明信息，仅供参考

-- cms_payment 表索引说明：
-- idx_cms_payment_order_id: 订单号索引，用于根据订单查询支付单
-- idx_cms_payment_reseller_id: 经销商ID索引，用于查询特定经销商的支付单
-- idx_cms_payment_status: 支付状态索引，用于按状态筛选支付单
-- idx_cms_payment_type: 支付类型索引，用于按类型查询支付单
-- idx_cms_payment_refund_status: 退款状态索引，用于退款状态查询
-- idx_payment_create_time: 创建时间索引，用于按时间范围查询
-- idx_payment_deadline: 支付截止时间索引，用于逾期支付查询
-- idx_payment_business_expire_date: 业务到期日索引，用于业务到期提醒
-- idx_payment_reseller_status: 经销商支付状态复合索引，用于查询特定经销商的特定状态支付单
-- idx_payment_order_type: 订单支付类型复合索引，用于订单维度的支付类型统计
-- idx_payment_status_time: 支付状态时间复合索引，用于按状态和时间范围的组合查询
-- idx_payment_related_business: 关联业务复合索引，用于关联业务查询
-- idx_payment_del_flag_status: 删除标识状态复合索引，用于排除已删除记录的状态查询

-- cms_payment_transaction 表索引说明：
-- idx_transaction_payment_id: 支付单号索引，用于查询特定支付单的所有交易流水
-- idx_transaction_channel_number: 渠道交易号索引，用于根据第三方交易号查询
-- idx_transaction_type: 交易类型索引，用于按交易类型筛选
-- idx_transaction_status: 交易状态索引，用于按状态查询交易
-- idx_transaction_channel: 支付渠道索引，用于按渠道统计和查询
-- idx_transaction_create_time: 创建时间索引，用于按时间范围查询交易
-- idx_transaction_complete_time: 完成时间索引，用于完成时间查询
-- idx_transaction_type_status: 交易类型状态复合索引，用于类型和状态的组合查询
-- idx_transaction_channel_time: 渠道时间复合索引，用于渠道维度的时间统计
-- idx_transaction_payment_type: 支付单交易类型复合索引，用于特定支付单的交易类型查询
-- idx_transaction_del_flag_status: 删除标识状态复合索引，用于排除已删除记录的状态查询

-- =====================================================
-- 约束注释说明
-- =====================================================

-- cms_payment 表约束说明：
-- chk_payment_amount: 确保支付金额大于0
-- chk_paid_amount: 确保已支付金额在0到应付金额之间
-- chk_refunded_amount: 确保已退款金额在0到已支付金额之间
-- chk_priority_level: 确保优先级为1、2或3

-- cms_payment_transaction 表约束说明：
-- chk_transaction_amount: 确保交易金额不为0（支付为正，退款为负）
-- chk_retry_count: 确保重试次数在0到10之间
-- fk_payment_transaction_payment: 外键约束，确保交易流水关联的支付单存在

-- =====================================================
-- Flowable 工作流集成说明
-- =====================================================

-- 本表结构遵循 Flowable 工作流引擎的最佳实践：
-- 1. 审计字段：create_by, create_by_name, update_by, update_by_name 用于流程审计追踪
-- 2. 逻辑删除：del_flag 字段支持 Flowable 历史数据保留要求
-- 3. 版本控制：version 字段支持并发流程实例的乐观锁控制
-- 4. 状态管理：payment_status 和 transaction_status 可与 Flowable 流程状态映射
-- 5. 时间戳：create_time 和 update_time 用于流程时间线追踪

-- 工作流变量映射建议：
-- - 流程启动时：payment_id 作为业务键（business key）
-- - 流程变量：payment_type, payment_status, payment_amount 等作为流程决策依据
-- - 任务分配：create_by 可用于任务创建人，update_by 用于任务处理人
-- - 历史追踪：通过 create_time 和 update_time 关联 Flowable 历史表

-- =====================================================
-- 脚本执行说明
-- =====================================================

-- 使用方法：
-- 1. 确保已连接到 PostgreSQL 数据库
-- 2. 确保目标表 cms_payment 和 cms_payment_transaction 已存在
-- 3. 执行本脚本：psql -U username -d database_name -f comment.sql
-- 4. 验证注释是否添加成功：
--    SELECT obj_description('cms_payment'::regclass, 'pg_class');
--    SELECT col_description('cms_payment'::regclass, 1);

-- 注意事项：
-- 1. 本脚本仅添加注释，不会修改表结构
-- 2. 如果表不存在，会报错但不影响其他注释的添加
-- 3. 重复执行本脚本会覆盖之前的注释
-- 4. PostgreSQL 的索引注释需要通过 COMMENT ON INDEX 语法，但本脚本仅提供说明

-- =====================================================
-- 结束
-- =====================================================
