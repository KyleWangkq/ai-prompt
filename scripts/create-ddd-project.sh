#!/bin/bash

# DDD项目快速启动脚本
# 用于快速创建新的DDD项目结构

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 信息函数
info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
}

# 显示帮助信息
show_help() {
    echo "DDD项目快速启动脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -p, --package    项目基础包名 (必需)"
    echo "  -n, --name       项目名称 (可选)"
    echo "  -d, --directory  输出目录 (默认: ./generated)"
    echo "  -h, --help       显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 -p com.example.ecommerce -n ecommerce-service"
    echo "  $0 --package com.company.order --name order-management --directory ./my-project"
    echo ""
}

# 参数解析
PACKAGE=""
PROJECT_NAME=""
OUTPUT_DIR="./generated"

while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--package)
            PACKAGE="$2"
            shift 2
            ;;
        -n|--name)
            PROJECT_NAME="$2"
            shift 2
            ;;
        -d|--directory)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# 验证必需参数
if [ -z "$PACKAGE" ]; then
    error "包名是必需的参数"
    show_help
    exit 1
fi

# 如果未指定项目名，从包名提取
if [ -z "$PROJECT_NAME" ]; then
    PROJECT_NAME=$(echo "$PACKAGE" | sed 's/.*\.//')
    info "使用默认项目名: $PROJECT_NAME"
fi

# 显示配置信息
echo "🚀 DDD项目生成器"
echo "=================="
echo "项目名称: $PROJECT_NAME"
echo "基础包名: $PACKAGE"
echo "输出目录: $OUTPUT_DIR"
echo ""

# 确认继续
read -p "继续创建项目? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    info "已取消"
    exit 0
fi

# 创建目录结构
info "创建项目目录结构..."

# 转换包名为路径
PACKAGE_PATH=$(echo "$PACKAGE" | tr '.' '/')

# 创建基础目录
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/test/java/$PACKAGE_PATH"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/resources"

# 创建DDD分层目录
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/interfaces/model"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/application"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/domain/repository"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/domain/entity"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/domain/model"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/infrastructure/mapper"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/infrastructure/repository"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/infrastructure/config"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/shared/exception"
mkdir -p "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/shared/model"

success "目录结构创建完成"

# 创建Maven pom.xml
info "创建Maven配置文件..."

cat > "$OUTPUT_DIR/$PROJECT_NAME/pom.xml" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>$(echo "$PACKAGE" | cut -d'.' -f1-2)</groupId>
    <artifactId>$PROJECT_NAME</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>$PROJECT_NAME</name>
    <description>DDD架构的Spring Boot项目</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.14</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <mybatis-plus.version>3.5.3.1</mybatis-plus.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>\${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF

success "Maven配置文件创建完成"

# 创建application.yml
info "创建应用配置文件..."

cat > "$OUTPUT_DIR/$PROJECT_NAME/src/main/resources/application.yml" << EOF
spring:
  application:
    name: $PROJECT_NAME
  
  datasource:
    url: jdbc:mysql://localhost:3306/${PROJECT_NAME//-/_}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: \${DB_USERNAME:root}
    password: \${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    root: INFO
    $PACKAGE: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
EOF

success "应用配置文件创建完成"

# 创建主应用类
info "创建Spring Boot主应用类..."

MAIN_CLASS_NAME=$(echo "$PROJECT_NAME" | sed 's/-//g' | sed 's/\b\w/\U&/g')Application

cat > "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/${MAIN_CLASS_NAME}.java" << EOF
package $PACKAGE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot应用启动类
 * 
 * 基于DDD架构的$PROJECT_NAME服务
 */
@SpringBootApplication
public class ${MAIN_CLASS_NAME} {
    
    public static void main(String[] args) {
        SpringApplication.run(${MAIN_CLASS_NAME}.class, args);
    }
}
EOF

success "主应用类创建完成"

# 创建基础异常类
info "创建基础异常类..."

cat > "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/shared/exception/BusinessException.java" << EOF
package $PACKAGE.shared.exception;

/**
 * 业务异常类
 * 
 * 用于封装业务逻辑中的异常情况
 */
public class BusinessException extends RuntimeException {
    
    private String errorCode;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
EOF

success "基础异常类创建完成"

# 创建MyBatis-Plus配置
info "创建MyBatis-Plus配置..."

cat > "$OUTPUT_DIR/$PROJECT_NAME/src/main/java/$PACKAGE_PATH/infrastructure/config/MybatisPlusConfig.java" << EOF
package $PACKAGE.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 */
@Configuration
@MapperScan("$PACKAGE.infrastructure.mapper")
public class MybatisPlusConfig {
    
    /**
     * 分页插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
EOF

success "MyBatis-Plus配置创建完成"

# 创建项目README
info "创建项目README..."

cat > "$OUTPUT_DIR/$PROJECT_NAME/README.md" << EOF
# $PROJECT_NAME

基于DDD(领域驱动设计)架构的Spring Boot项目

## 项目结构

\`\`\`
src/main/java/$PACKAGE_PATH/
├── interfaces/              # 接口层
│   └── model/              # DTO对象
├── application/            # 应用层
├── domain/                 # 领域层
│   ├── repository/         # 仓储接口
│   ├── entity/            # 领域实体
│   └── model/             # 领域模型
├── infrastructure/         # 基础设施层
│   ├── mapper/            # MyBatis Mapper
│   ├── repository/        # 仓储实现
│   └── config/            # 配置类
└── shared/                # 共享组件
    ├── exception/         # 异常定义
    └── model/             # 共享模型
\`\`\`

## 快速开始

### 1. 环境要求
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
创建数据库: \`${PROJECT_NAME//-/_}\`

### 3. 运行项目
\`\`\`bash
mvn spring-boot:run
\`\`\`

### 4. 访问应用
- 应用地址: http://localhost:8080/api

## DDD设计原则

本项目遵循以下DDD设计原则:

1. **分层架构**: 清晰的分层边界和职责
2. **聚合设计**: 以聚合为单位管理业务不变性
3. **Repository模式**: 面向领域的数据访问
4. **领域服务**: 跨聚合的业务逻辑
5. **领域事件**: 解耦的业务流程

## 开发指南

### 添加新聚合
1. 在 \`domain/model/\` 下创建聚合根类
2. 在 \`domain/entity/\` 下创建实体类
3. 在 \`domain/repository/\` 下定义仓储接口
4. 在 \`infrastructure/repository/\` 下实现仓储
5. 在 \`application/\` 下创建应用服务
6. 在 \`interfaces/\` 下创建控制器

### 测试
\`\`\`bash
mvn test
\`\`\`

---

**生成于: $(date "+%Y-%m-%d %H:%M:%S")**
**生成工具: DDD项目生成器**
EOF

success "项目README创建完成"

# 创建.gitignore
info "创建.gitignore文件..."

cat > "$OUTPUT_DIR/$PROJECT_NAME/.gitignore" << EOF
# Compiled class file
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# virtual machine crash logs
hs_err_pid*

# IDE
.idea/
*.iml
*.ipr
*.iws
.vscode/

# Eclipse
.project
.classpath
.settings/
bin/

# NetBeans
nbproject/private/
nbbuild/
dist/
nbdist/
.nb-gradle/

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# Gradle
.gradle/
build/

# Spring Boot
application-local.yml
application-dev.yml
application-prod.yml

# Database
*.db
*.sqlite

# OS
.DS_Store
Thumbs.db
EOF

success ".gitignore文件创建完成"

echo ""
echo "🎉 项目创建完成!"
echo "=================="
echo "项目位置: $OUTPUT_DIR/$PROJECT_NAME"
echo ""
echo "下一步:"
echo "1. cd $OUTPUT_DIR/$PROJECT_NAME"
echo "2. 配置数据库连接 (src/main/resources/application.yml)"
echo "3. 使用GitHub Copilot生成DDD代码:"
echo "   @workspace /ddd-generate"
echo "4. mvn spring-boot:run"
echo ""
success "开始您的DDD开发之旅! 🚀"