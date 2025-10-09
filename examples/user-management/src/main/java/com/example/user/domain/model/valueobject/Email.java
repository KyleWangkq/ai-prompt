package com.example.user.domain.model.valueobject;

import com.example.user.shared.exception.BusinessException;
import lombok.Value;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 邮箱值对象
 * 
 * 确保邮箱格式的正确性和不可变性
 */
@Value
public class Email {
    
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);
    
    String value;
    
    public Email(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        
        String trimmedEmail = email.trim().toLowerCase();
        
        if (!isValidFormat(trimmedEmail)) {
            throw new BusinessException("邮箱格式不正确: " + email);
        }
        
        if (trimmedEmail.length() > 100) {
            throw new BusinessException("邮箱长度不能超过100个字符");
        }
        
        this.value = trimmedEmail;
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidFormat(String email) {
        return PATTERN.matcher(email).matches();
    }
    
    /**
     * 获取邮箱域名
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return atIndex >= 0 ? value.substring(atIndex + 1) : "";
    }
    
    /**
     * 获取邮箱用户名部分
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return atIndex >= 0 ? value.substring(0, atIndex) : value;
    }
    
    /**
     * 判断是否为企业邮箱
     */
    public boolean isCorporateEmail() {
        String domain = getDomain();
        return !isCommonEmailProvider(domain);
    }
    
    /**
     * 判断是否为常见邮箱提供商
     */
    private boolean isCommonEmailProvider(String domain) {
        String[] commonProviders = {
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com",
            "qq.com", "163.com", "126.com", "sina.com"
        };
        
        for (String provider : commonProviders) {
            if (domain.equals(provider)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 创建邮箱值对象的工厂方法
     */
    public static Email of(String email) {
        return new Email(email);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}