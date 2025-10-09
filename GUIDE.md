# DDD GitHub Copilot Workspace 完整指南

欢迎使用DDD (Domain Driven Design) GitHub Copilot工作空间！这是一个专为领域驱动设计开发的完整解决方案。

## 🌟 特性概览

- **AI驱动的代码生成**: 智能DDD代码生成和优化
- **完整的DDD架构**: 五层架构支持（interfaces, application, domain, infrastructure, shared）
- **自动化工作流**: GitHub Actions集成，自动PR生成
- **质量保证**: 代码验证、测试生成和重构建议
- **快速启动**: 一键项目脚手架生成

## 🚀 快速开始

### 1. 创建新的DDD项目

使用项目生成器创建标准DDD项目结构：

```bash
./scripts/create-ddd-project.sh -p com.example.ecommerce -n ecommerce-service
```

### 2. 激活GitHub Copilot Agent

在VS Code中打开命令面板，选择"GitHub Copilot: Enable Agent"，然后使用以下命令：

- `@workspace /ddd-generate` - 生成DDD代码
- `@workspace /ddd-validate` - 验证代码架构
- `@workspace /ddd-test` - 生成测试代码
- `@workspace /ddd-refactor` - 重构建议

### 3. 设置自动化工作流

项目会自动包含GitHub Actions工作流，支持：
- 自动代码生成
- 质量检查
- 测试执行
- PR自动创建

## 🏗️ 项目结构

```
your-project/
├── src/main/java/com/company/project/
│   ├── interfaces/          # 接口层 - REST API, GraphQL等
│   │   ├── controller/      # 控制器
│   │   └── model/          # DTO, VO等
│   ├── application/         # 应用层 - 业务流程编排
│   │   ├── service/        # 应用服务
│   │   └── command/        # 命令对象
│   ├── domain/             # 领域层 - 核心业务逻辑
│   │   ├── model/          # 聚合根、实体、值对象
│   │   ├── repository/     # 仓储接口
│   │   └── service/        # 领域服务
│   ├── infrastructure/     # 基础设施层 - 技术实现
│   │   ├── repository/     # 仓储实现
│   │   ├── mapper/         # 数据映射
│   │   └── config/         # 配置类
│   └── shared/             # 共享组件
│       ├── exception/      # 异常定义
│       └── util/           # 工具类
├── .github/
│   ├── copilot/            # Copilot配置
│   ├── workflows/          # GitHub Actions
│   └── prompts/            # 提示模板
└── scripts/                # 工具脚本
```

## 🤖 GitHub Copilot 命令

### 核心生成命令

#### `/ddd-generate`
生成完整的DDD代码结构
```
@workspace /ddd-generate 生成用户管理聚合，包含用户注册、登录和密码重置功能
```

#### `/ddd-validate`
验证现有代码的DDD架构合规性
```
@workspace /ddd-validate 检查当前代码是否符合DDD最佳实践
```

#### `/ddd-test`
生成对应的测试代码
```
@workspace /ddd-test 为UserAggregate生成单元测试和集成测试
```

#### `/ddd-refactor`
提供重构建议
```
@workspace /ddd-refactor 分析代码并提供DDD重构建议
```

### 文档和分析命令

#### `/ddd-document`
生成DDD文档
```
@workspace /ddd-document 生成领域模型文档和API文档
```

#### `/ddd-analyze`
分析领域复杂度
```
@workspace /ddd-analyze 分析当前领域模型的复杂度和依赖关系
```

#### `/ddd-migrate`
数据库迁移支持
```
@workspace /ddd-migrate 生成数据库迁移脚本
```

### 自动化命令

#### `/ddd-pr`
自动生成Pull Request
```
@workspace /ddd-pr 为当前更改创建DDD代码审查PR
```

#### `/ddd-deploy`
部署准备
```
@workspace /ddd-deploy 准备部署配置和脚本
```

## 📝 使用示例

### 示例1: 创建电商订单聚合

```
@workspace /ddd-generate 

请生成一个电商订单管理聚合，包含以下功能：
- 创建订单
- 取消订单  
- 添加订单项
- 计算订单总金额
- 订单状态管理

要求：
- 使用Spring Boot 2.7+
- 集成MyBatis-Plus
- 包含值对象：Money, OrderStatus
- 包含业务规则验证
```

### 示例2: 验证代码架构

```
@workspace /ddd-validate

请检查我的OrderAggregate实现是否符合DDD最佳实践：
- 聚合边界是否合理
- 业务不变性是否得到保护
- 仓储模式是否正确实现
- 领域事件是否合理使用
```

### 示例3: 生成测试代码

```
@workspace /ddd-test

为OrderAggregate生成完整的测试套件：
- 单元测试（聚合根、实体、值对象）
- 集成测试（仓储、应用服务）
- 端到端测试（API层）
- 测试数据构建器
```

## 🔧 配置说明

### Agent配置 (`.github/copilot/agent.yml`)

```yaml
name: ddd-copilot-agent
version: 2.0.0
description: DDD架构的GitHub Copilot智能助手

commands:
  - name: ddd-generate
    description: 生成DDD架构代码
    prompt_file: .github/prompts/ddd-generator.prompt.md
  
  # ... 其他命令配置
```

### 工作流配置 (`.github/workflows/ddd-pr-generator.yml`)

自动化流程包括：
- 代码生成
- 质量检查
- 测试执行
- 文档生成
- PR创建

## 🛠️ 工具脚本

### 项目生成器
```bash
./scripts/create-ddd-project.sh -p com.example.project -n my-service
```

### 代码验证器
```bash
./scripts/validate-ddd.sh
```

## 📚 DDD最佳实践

### 1. 聚合设计原则
- 聚合要小而内聚
- 通过ID引用其他聚合
- 在事务边界内修改单个聚合
- 使用领域事件进行聚合间通信

### 2. 仓储模式
- 面向聚合根设计仓储接口
- 在领域层定义接口，基础设施层实现
- 支持查询对象模式

### 3. 应用服务
- 编排业务流程
- 管理事务边界
- 处理领域事件
- 不包含业务逻辑

### 4. 领域模型
- 富领域模型
- 封装业务不变性
- 表达业务概念
- 避免贫血模型

## 🔍 代码质量检查

工作空间包含自动化质量检查：

### 架构合规性
- ✅ 分层架构检查
- ✅ 依赖方向验证
- ✅ 包命名约定
- ✅ 注解使用规范

### 代码质量
- ✅ 编译检查
- ✅ 测试覆盖率
- ✅ 代码复杂度
- ✅ 最佳实践检查

### DDD模式
- ✅ 聚合边界
- ✅ 仓储模式
- ✅ 领域服务
- ✅ 值对象设计

## 📊 质量报告

验证脚本会生成详细的质量报告：

```
🏗️ DDD架构验证报告
===================

📁 包结构检查: ✅ 通过 (100%)
📦 Spring注解检查: ✅ 通过 (95%)
🔧 编译状态检查: ✅ 通过 (100%)
🧪 测试覆盖率: ⚠️  需要改进 (65%)
📋 TODO分析: ✅ 优秀 (2个待办事项)

总体质量评分: 🟢 90% (优秀)

详细建议：
- 增加集成测试覆盖率
- 完善领域事件测试
```

## 🆘 故障排除

### 常见问题

1. **Agent命令不工作**
   - 确保VS Code中GitHub Copilot扩展已启用
   - 检查`.github/copilot/agent.yml`配置
   - 重启VS Code

2. **生成的代码不符合预期**
   - 检查prompt模板配置
   - 提供更详细的需求描述
   - 使用示例中的格式

3. **GitHub Actions失败**
   - 检查Java版本配置
   - 验证Maven/Gradle配置
   - 查看工作流日志

### 获取帮助

- 📖 查看示例项目: `examples/user-management/`
- 🔧 运行验证脚本: `./scripts/validate-ddd.sh`
- 💬 使用GitHub Discussions讨论问题

## 🚀 升级指南

保持工作空间最新：

1. 更新Agent配置
2. 同步Prompt模板
3. 升级GitHub Actions工作流
4. 更新依赖版本

---

**开始您的DDD开发之旅！** 

使用 `@workspace /ddd-generate` 命令生成第一个聚合，体验智能化的领域驱动设计开发流程。

---

*生成时间: $(date "+%Y-%m-%d %H:%M:%S")*  
*工作空间版本: 2.0.0*