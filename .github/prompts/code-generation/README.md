# 代码生成提示词

本目录包含将 DDD 设计文档转换为可编译代码的提示词集合。

## 🎯 功能概述

这些提示词用于将已完成的 DDD 架构设计文档转换为特定技术栈的代码骨架，支持快速启动项目开发。

## 📋 提示词清单

### 1. DDD 文档转 Java 代码

**文件**: [ddd-to-java.prompt.md](ddd-to-java.prompt.md)

**技术栈**:
- Java 11+
- Spring Boot 2.7+
- MyBatis-Plus
- Maven

**输入**: DDD 设计文档（领域层、应用层、基础设施层）

**输出**:
- 完整的 Maven 项目结构
- 领域层代码（聚合根、实体、值对象、Repository 接口）
- 应用层代码（应用服务、DTO、转换器）
- 基础设施层代码（Repository 实现、DO、Mapper）
- 配置文件和 pom.xml

**特点**:
- ✅ 完整的包结构和命名规范
- ✅ 符合 DDD 分层架构
- ✅ 可直接编译运行
- ✅ 包含必要的依赖和配置

### 2. DDD 文档转 YAML

**文件**: [ddd-to-yaml.prompt.md](ddd-to-yaml.prompt.md)

**用途**: 将 DDD 设计文档转换为结构化的 YAML 格式，作为代码生成的中间格式

**输入**: DDD 设计文档

**输出**: 结构化的 YAML 配置文件，包含：
- 上下文定义
- 聚合结构
- 实体和值对象
- 应用服务
- Repository 接口

**特点**:
- ✅ 结构化表示
- ✅ 易于解析和转换
- ✅ 支持多种代码生成工具
- ✅ 可作为配置驱动开发的基础

## 🚀 使用流程

### 完整流程

```bash
# 1. 使用 DDD 提示词生成设计文档
参考 ../ddd/ 目录下的提示词

# 2. 将设计文档转换为 YAML（可选）
使用 ddd-to-yaml.prompt.md

# 3. 生成 Java 代码骨架
使用 ddd-to-java.prompt.md

# 4. 补充业务逻辑实现
在生成的代码骨架基础上添加具体实现
```

### 直接生成代码

```bash
# 如果已有完整的 DDD 设计文档
直接使用 ddd-to-java.prompt.md 生成代码
```

## 📊 生成的项目结构示例

```
project-root/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/project/
│   │   │       ├── domain/                 # 领域层
│   │   │       │   ├── model/              # 聚合、实体、值对象
│   │   │       │   ├── repository/         # Repository 接口
│   │   │       │   └── event/              # 领域事件
│   │   │       ├── application/            # 应用层
│   │   │       │   ├── service/            # 应用服务
│   │   │       │   └── dto/                # DTO
│   │   │       └── infrastructure/         # 基础设施层
│   │   │           ├── persistence/        # Repository 实现
│   │   │           ├── mapper/             # MyBatis Mapper
│   │   │           └── config/             # 配置类
│   │   └── resources/
│   │       ├── application.yml
│   │       └── mapper/                     # MyBatis XML
│   └── test/
│       └── java/
└── README.md
```

## ⚠️ 注意事项

1. **设计文档质量**: 生成代码的质量取决于输入的 DDD 设计文档质量
2. **业务逻辑**: 生成的是代码骨架，不包含具体业务逻辑实现
3. **技术栈适配**: 如需其他技术栈，可以修改提示词中的技术约束
4. **代码审查**: 生成的代码需要人工审查和调整

## 🔄 扩展开发

如需支持其他技术栈（如 .NET、Python 等），可以参考现有提示词的结构创建新的代码生成提示词：

1. 定义目标技术栈和框架
2. 设计包/模块结构
3. 定义代码模板和命名规范
4. 提供配置文件模板
5. 添加依赖管理配置

## 📖 相关文档

- [DDD 架构设计提示词](../ddd/README.md)
- [DDD 设计文档 Prompt 设计说明书](../../../DDD设计文档Prompt设计说明书.md)

## 🔄 版本历史

- **v2.0** (2025-10-09): 重新整理代码生成提示词，统一命名规范
- **v1.0**: 初始版本
