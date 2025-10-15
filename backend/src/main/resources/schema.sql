-- Payment Module Database Schema
-- 支付模块数据库表结构

-- 支付单表
CREATE TABLE IF NOT EXISTS `cms_payment` (
  `id` VARCHAR(32) NOT NULL COMMENT '支付单号，主键',
  `order_id` VARCHAR(32) NULL COMMENT '关联订单号',
  `reseller_id` VARCHAR(32) NOT NULL COMMENT '经销商ID',
  `payment_amount` DECIMAL(20,6) NOT NULL COMMENT '支付金额',
  `paid_amount` DECIMAL(20,6) NOT NULL DEFAULT 0 COMMENT '已支付金额',
  `refunded_amount` DECIMAL(20,6) NOT NULL DEFAULT 0 COMMENT '已退款金额',
  `actual_amount` DECIMAL(20,6) NOT NULL DEFAULT 0 COMMENT '实际收款金额',
  `currency` VARCHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `payment_type` VARCHAR(20) NOT NULL COMMENT '支付类型',
  `payment_status` VARCHAR(20) NOT NULL COMMENT '支付状态',
  `refund_status` VARCHAR(20) NOT NULL COMMENT '退款状态',
  `business_desc` VARCHAR(500) NULL COMMENT '业务描述',
  `payment_deadline` DATETIME NULL COMMENT '支付截止时间',
  `priority_level` INT NULL DEFAULT 2 COMMENT '优先级',
  `related_business_id` VARCHAR(32) NULL COMMENT '关联业务ID',
  `related_business_type` VARCHAR(20) NULL COMMENT '关联业务类型',
  `business_expire_date` DATETIME NULL COMMENT '业务到期日',
  `business_tags` JSON NULL COMMENT '业务标签',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(32) NULL COMMENT '创建人',
  `create_by_name` VARCHAR(32) NULL COMMENT '创建人姓名',
  `update_by` VARCHAR(32) NULL COMMENT '更新人',
  `update_by_name` VARCHAR(32) NULL COMMENT '更新人姓名',
  `del_flag` INT NOT NULL DEFAULT 0 COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_reseller_id` (`reseller_id`),
  INDEX `idx_payment_status` (`payment_status`),
  INDEX `idx_payment_type` (`payment_type`),
  INDEX `idx_related_business` (`related_business_id`, `related_business_type`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单表';

-- 支付流水表
CREATE TABLE IF NOT EXISTS `cms_payment_transaction` (
  `id` VARCHAR(32) NOT NULL COMMENT '流水号，主键',
  `payment_id` VARCHAR(32) NOT NULL COMMENT '关联支付单号',
  `transaction_type` VARCHAR(20) NOT NULL COMMENT '流水类型',
  `transaction_status` VARCHAR(20) NOT NULL COMMENT '流水状态',
  `transaction_amount` DECIMAL(20,6) NOT NULL COMMENT '交易金额',
  `payment_channel` VARCHAR(50) NOT NULL COMMENT '支付渠道',
  `channel_transaction_number` VARCHAR(64) NULL COMMENT '渠道交易号',
  `payment_way` VARCHAR(20) NULL COMMENT '支付方式',
  `original_transaction_id` VARCHAR(32) NULL COMMENT '原交易流水号',
  `business_order_id` VARCHAR(32) NULL COMMENT '业务单号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `complete_datetime` DATETIME NULL COMMENT '交易完成时间',
  `expiration_time` DATETIME NULL COMMENT '支付过期时间',
  `remark` VARCHAR(500) NULL COMMENT '业务备注',
  PRIMARY KEY (`id`),
  INDEX `idx_payment_id` (`payment_id`),
  INDEX `idx_channel_transaction` (`channel_transaction_number`),
  INDEX `idx_transaction_type` (`transaction_type`),
  INDEX `idx_transaction_status` (`transaction_status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';
