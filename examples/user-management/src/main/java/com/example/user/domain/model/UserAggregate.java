package com.example.user.domain.model;

import com.example.user.domain.model.valueobject.Email;
import com.example.user.domain.model.valueobject.UserId;
import com.example.user.domain.model.valueobject.Username;
import com.example.user.shared.exception.BusinessException;
import com.example.user.shared.model.event.UserCreatedEvent;
import com.example.user.shared.model.event.UserDisabledEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户聚合根
 * 
 * 包含用户的核心业务逻辑和规则
 * 负责维护用户状态的一致性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAggregate {
    
    private UserId id;
    private Username username;
    private Email email;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 领域事件列表
    @Builder.Default
    private List<Object> domainEvents = new ArrayList<>();
    
    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE("激活"),
        DISABLED("禁用"),
        PENDING("待激活"),
        DELETED("已删除");
        
        private final String description;
        
        UserStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 创建新用户
     */
    public static UserAggregate create(Username username, Email email) {
        UserAggregate user = UserAggregate.builder()
            .username(username)
            .email(email)
            .status(UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
            
        // 发布用户创建事件
        user.addDomainEvent(new UserCreatedEvent(user.getId(), user.getUsername(), user.getEmail()));
        
        return user;
    }
    
    /**
     * 禁用用户
     */
    public void disable() {
        validateCanBeDisabled();
        
        this.status = UserStatus.DISABLED;
        this.updatedAt = LocalDateTime.now();
        
        // 发布用户禁用事件
        addDomainEvent(new UserDisabledEvent(this.id, this.username));
    }
    
    /**
     * 启用用户
     */
    public void enable() {
        validateCanBeEnabled();
        
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 修改邮箱
     */
    public void changeEmail(Email newEmail) {
        validateCanChangeEmail();
        validateEmailNotSame(newEmail);
        
        this.email = newEmail;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 修改用户名
     */
    public void changeUsername(Username newUsername) {
        validateCanChangeUsername();
        validateUsernameNotSame(newUsername);
        
        this.username = newUsername;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 删除用户（软删除）
     */
    public void delete() {
        validateCanBeDeleted();
        
        this.status = UserStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查用户是否激活
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
    
    /**
     * 检查用户是否被禁用
     */
    public boolean isDisabled() {
        return this.status == UserStatus.DISABLED;
    }
    
    /**
     * 检查用户是否已删除
     */
    public boolean isDeleted() {
        return this.status == UserStatus.DELETED;
    }
    
    /**
     * 获取用户显示名称
     */
    public String getDisplayName() {
        return this.username.getValue();
    }
    
    // ============ 私有验证方法 ============
    
    private void validateCanBeDisabled() {
        if (this.status == UserStatus.DISABLED) {
            throw new BusinessException("用户已处于禁用状态");
        }
        if (this.status == UserStatus.DELETED) {
            throw new BusinessException("已删除用户不能禁用");
        }
    }
    
    private void validateCanBeEnabled() {
        if (this.status == UserStatus.ACTIVE) {
            throw new BusinessException("用户已处于激活状态");
        }
        if (this.status == UserStatus.DELETED) {
            throw new BusinessException("已删除用户不能启用");
        }
    }
    
    private void validateCanChangeEmail() {
        if (this.status == UserStatus.DELETED) {
            throw new BusinessException("已删除用户不能修改邮箱");
        }
    }
    
    private void validateCanChangeUsername() {
        if (this.status == UserStatus.DELETED) {
            throw new BusinessException("已删除用户不能修改用户名");
        }
    }
    
    private void validateCanBeDeleted() {
        if (this.status == UserStatus.DELETED) {
            throw new BusinessException("用户已被删除");
        }
    }
    
    private void validateEmailNotSame(Email newEmail) {
        if (this.email.equals(newEmail)) {
            throw new BusinessException("新邮箱与当前邮箱相同");
        }
    }
    
    private void validateUsernameNotSame(Username newUsername) {
        if (this.username.equals(newUsername)) {
            throw new BusinessException("新用户名与当前用户名相同");
        }
    }
    
    // ============ 领域事件管理 ============
    
    private void addDomainEvent(Object event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
    }
    
    public List<Object> getDomainEvents() {
        return new ArrayList<>(this.domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ============ 转换方法 ============
    
    /**
     * 从实体转换为聚合根
     */
    public static UserAggregate fromEntity(com.example.user.domain.entity.UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return UserAggregate.builder()
            .id(new UserId(entity.getId()))
            .username(new Username(entity.getUsername()))
            .email(new Email(entity.getEmail()))
            .status(UserStatus.valueOf(entity.getStatus()))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    /**
     * 转换为实体
     */
    public com.example.user.domain.entity.UserEntity toEntity() {
        return com.example.user.domain.entity.UserEntity.builder()
            .id(this.id != null ? this.id.getValue() : null)
            .username(this.username.getValue())
            .email(this.email.getValue())
            .status(this.status.name())
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}