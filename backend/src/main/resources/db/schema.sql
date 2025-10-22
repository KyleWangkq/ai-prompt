-- 支付模块数据库初始化脚本
-- Payment Module Database Initialization Script

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cms_payment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cms_payment;

-- 支付单表（Payment Table）
CREATE TABLE IF NOT EXISTS cms_payment
(
    id                      BIGINT AUTO_INCREMENT NOT NULL COMMENT '主键ID，自增',
    code                    VARCHAR(32) NOT NULL COMMENT '支付单号，业务编码',
    order_id                VARCHAR(32) NOT NULL COMMENT '关联订单号',
    reseller_id             VARCHAR(32) NOT NULL COMMENT '经销商ID',
    payment_amount          DECIMAL(20, 6) NOT NULL COMMENT '支付金额',
    paid_amount             DECIMAL(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '已支付金额',
    refunded_amount         DECIMAL(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '已退款金额',
    actual_amount           DECIMAL(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '实际收款金额',
    currency                VARCHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    payment_type            VARCHAR(20) NOT NULL COMMENT '支付类型（ADVANCE_PAYMENT/FINAL_PAYMENT/OTHER_PAYMENT/CREDIT_REPAYMENT）',
    payment_status          VARCHAR(20) NOT NULL COMMENT '支付状态（UNPAID/PAYING/PARTIAL_PAID/PAID/FAILED/STOPPED/FROZEN）',
    refund_status           VARCHAR(20) NOT NULL DEFAULT 'NO_REFUND' COMMENT '退款状态（NO_REFUND/REFUNDING/PARTIAL_REFUNDED/FULL_REFUNDED/REFUND_FAILED）',
    business_desc           VARCHAR(500) NULL COMMENT '业务描述',
    payment_deadline        DATETIME NULL COMMENT '支付截止时间',
    priority_level          INT NULL COMMENT '优先级（1-高，2-中，3-低）',
    related_business_id     VARCHAR(32) NULL COMMENT '关联业务ID',
    related_business_type   VARCHAR(20) NULL COMMENT '关联业务类型（CREDIT_RECORD/DELIVERY_ORDER/ORDER）',
    business_expire_date    DATETIME NULL COMMENT '业务到期日',
    business_tags           JSON NULL COMMENT '业务标签',
    del_flag                INT NULL DEFAULT 0 COMMENT '删除状态（0-正常，1-删除）',
    create_by               VARCHAR(32) NULL COMMENT '创建人',
    create_by_name          VARCHAR(32) NULL COMMENT '创建人姓名',
    create_time             DATETIME NULL COMMENT '创建时间',
    update_by               VARCHAR(32) NULL COMMENT '更新人',
    update_by_name          VARCHAR(32) NULL COMMENT '更新人姓名',
    update_time             DATETIME NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) COMMENT '支付单表' ROW_FORMAT = DYNAMIC;

-- 支付单表索引
CREATE INDEX idx_order_id ON cms_payment (order_id);
CREATE INDEX idx_reseller_id ON cms_payment (reseller_id);
CREATE INDEX idx_payment_status ON cms_payment (payment_status);
CREATE INDEX idx_payment_type ON cms_payment (payment_type);
CREATE INDEX idx_related_business ON cms_payment (related_business_type, related_business_id);
CREATE INDEX idx_create_time ON cms_payment (create_time);

-- 支付流水表（Payment Transaction Table）
CREATE TABLE IF NOT EXISTS cms_payment_transaction
(
    id                          BIGINT AUTO_INCREMENT NOT NULL COMMENT '主键ID，自增',
    code                        VARCHAR(32) NOT NULL COMMENT '流水号，业务编码',
    payment_id                  BIGINT NOT NULL COMMENT '支付单ID，外键关联',
    transaction_type            VARCHAR(20) NOT NULL COMMENT '流水类型（PAYMENT/REFUND）',
    transaction_status          VARCHAR(20) NOT NULL COMMENT '流水状态（PROCESSING/SUCCESS/FAILED）',
    transaction_amount          DECIMAL(20, 6) NOT NULL COMMENT '交易金额',
    payment_channel             VARCHAR(50) NOT NULL COMMENT '支付渠道（ONLINE_PAYMENT/WALLET_PAYMENT/WIRE_TRANSFER/CREDIT_ACCOUNT）',
    channel_transaction_number  VARCHAR(64) NULL COMMENT '渠道交易号',
    payment_way                 VARCHAR(20) NULL COMMENT '支付方式',
    original_transaction_id     BIGINT NULL COMMENT '原流水ID（退款时使用）',
    business_order_id           VARCHAR(32) NULL COMMENT '业务单号（如退款单号）',
    create_time                 DATETIME NOT NULL COMMENT '创建时间',
    complete_date_time          DATETIME NULL COMMENT '完成时间',
    expiration_time             DATETIME NULL COMMENT '过期时间',
    business_remark             VARCHAR(500) NULL COMMENT '业务备注',
    del_flag                    INT NULL DEFAULT 0 COMMENT '删除状态（0-正常，1-删除）',
    create_by                   VARCHAR(32) NULL COMMENT '创建人',
    create_by_name              VARCHAR(32) NULL COMMENT '创建人姓名',
    update_by                   VARCHAR(32) NULL COMMENT '更新人',
    update_by_name              VARCHAR(32) NULL COMMENT '更新人姓名',
    update_time                 DATETIME NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) COMMENT '支付流水表' ROW_FORMAT = DYNAMIC;

-- 支付流水表索引
CREATE INDEX idx_payment_id ON cms_payment_transaction (payment_id);
CREATE INDEX idx_transaction_type ON cms_payment_transaction (transaction_type);
CREATE INDEX idx_transaction_status ON cms_payment_transaction (transaction_status);
CREATE INDEX idx_channel_transaction_number ON cms_payment_transaction (channel_transaction_number);
CREATE INDEX idx_original_transaction_id ON cms_payment_transaction (original_transaction_id);
CREATE INDEX idx_create_time ON cms_payment_transaction (create_time);
