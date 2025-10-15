package com.bytz.cms.payment.domain.model;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * 关联业务信息值对象
 * 支持多种业务场景的关联需求
 */
@Value
public class RelatedBusinessInfo {
    
    /**
     * 关联业务ID（如信用记录ID、提货单ID等）
     */
    String relatedBusinessId;
    
    /**
     * 关联业务类型
     */
    RelatedBusinessType relatedBusinessType;
    
    /**
     * 业务到期日（如信用还款到期日、提货到期日等）
     */
    LocalDateTime businessExpireDate;
    
    /**
     * 创建关联业务信息
     */
    public static RelatedBusinessInfo of(String relatedBusinessId, 
                                        RelatedBusinessType relatedBusinessType,
                                        LocalDateTime businessExpireDate) {
        return new RelatedBusinessInfo(relatedBusinessId, relatedBusinessType, businessExpireDate);
    }
    
    /**
     * 验证关联业务信息
     */
    public void validate() {
        if (relatedBusinessId != null && relatedBusinessType == null) {
            throw new IllegalArgumentException("关联业务ID存在时，必须指定关联业务类型");
        }
    }
}
