package com.bytz.cms.payment.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域事件基类
 * Domain Event Base Class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 聚合根ID
     */
    private String aggregateId;
    
    /**
     * 事件类型
     */
    private String eventType;
}
