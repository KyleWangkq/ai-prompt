# DDD GitHub Copilot Workspace 🚀

> **专业级领域驱动设计开发工作空间**  
> 集成AI智能代码生成、自动化工作流和质量保证的完整解决方案

[![GitHub Copilot](https://img.shields.io/badge/GitHub%20Copilot-Enabled-blue)](https://github.com/features/copilot)
[![DDD Architecture](https://img.shields.io/badge/DDD-Architecture-green)](https://martinfowler.com/bliki/DomainDrivenDesign.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7+-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange)](https://openjdk.org/)

## ✨ 核心特性

🤖 **AI驱动的DDD代码生成**
- 智能聚合根、实体和值对象生成
- 自动仓储模式实现
- 领域服务和应用服务生成

🏗️ **完整的DDD架构支持**
- 五层架构自动搭建
- 架构合规性实时验证
- 最佳实践自动应用

🔄 **全自动化工作流**
- GitHub Actions集成
- 自动PR生成和代码审查
- 持续集成和质量检查

🧪 **智能测试生成**
- 单元测试自动生成
- 集成测试脚手架
- 测试数据构建器

## 🚀 30秒快速开始

### 1. 创建新项目
```bash
# 一键生成DDD项目结构
./scripts/create-ddd-project.sh -p com.company.ecommerce -n ecommerce-service
```

### 2. 激活AI助手
在VS Code中使用Copilot命令：
```
@workspace /ddd-generate 创建订单管理聚合，包含创建订单、取消订单、添加订单项功能
```

### 3. 验证代码质量
```bash
# 运行DDD架构验证
./scripts/validate-ddd.sh
```

## 🎯 智能命令系统

| 命令 | 功能 | 示例 |
|------|------|------|
| `@workspace /ddd-generate` | 生成DDD代码 | 生成用户管理聚合 |
| `@workspace /ddd-validate` | 架构验证 | 检查DDD合规性 |
| `@workspace /ddd-test` | 测试生成 | 生成完整测试套件 |
| `@workspace /ddd-refactor` | 重构建议 | AI驱动的优化建议 |
| `@workspace /ddd-document` | 文档生成 | 自动生成API文档 |
| `@workspace /ddd-analyze` | 复杂度分析 | 领域模型分析 |
| `@workspace /ddd-migrate` | 数据库迁移 | 生成迁移脚本 |
| `@workspace /ddd-pr` | 自动PR | 创建代码审查PR |
| `@workspace /ddd-deploy` | 部署准备 | 生成部署配置 |

## 📁 项目结构

```
workspace/
├── 📂 .github/
│   ├── 🤖 copilot/agent.yml           # Copilot Agent配置
│   ├── 📝 prompts/                    # AI提示模板库
│   └── ⚡ workflows/                  # 自动化工作流
├── 📂 examples/user-management/       # 完整DDD示例
├── 📂 scripts/                       # 开发工具脚本
├── 📂 docs/                          # DDD设计文档
├── 📖 README.md                      # 项目介绍
├── 📚 GUIDE.md                       # 详细使用指南
└── ✅ VERIFICATION.md                # 功能验证清单
```

## 🎨 支持的架构模式

### 🏛️ DDD五层架构
```
interfaces/     # 接口层 - REST API, GraphQL
application/    # 应用层 - 业务流程编排
domain/         # 领域层 - 核心业务逻辑
infrastructure/ # 基础设施层 - 数据访问
shared/         # 共享组件 - 通用工具
```

### 🧩 核心DDD概念
- ✅ 聚合根和实体
- ✅ 值对象和领域服务  
- ✅ 仓储模式
- ✅ 领域事件
- ✅ 应用服务
- ✅ 六边形架构

## 🛠️ 技术栈

- **Framework**: Spring Boot 2.7+, Spring Data JPA
- **ORM**: MyBatis-Plus 3.x
- **Language**: Java 17+
- **Build**: Maven/Gradle
- **Database**: MySQL, PostgreSQL, H2(测试)
- **Testing**: JUnit 5, Mockito, TestContainers
- **Tools**: Lombok, MapStruct

## 📊 质量保证

### 🔍 自动化检查
- ✅ **架构合规性**: DDD分层检查
- ✅ **代码质量**: 编译检查、复杂度分析
- ✅ **测试覆盖率**: 单元测试、集成测试
- ✅ **最佳实践**: Spring注解、包命名规范

### 📈 质量报告
```
🏗️ DDD架构验证报告
===================
📁 包结构检查: ✅ 通过 (100%)
📦 Spring注解检查: ✅ 通过 (95%)
🔧 编译状态检查: ✅ 通过 (100%)
🧪 测试覆盖率: ⚠️ 需要改进 (65%)

总体质量评分: 🟢 90% (优秀)
```

## 📚 学习资源

### 📖 文档指南
- **[完整使用指南](GUIDE.md)** - 详细功能说明和最佳实践
- **[快速验证](VERIFICATION.md)** - 功能验证清单
- **[示例项目](examples/user-management/)** - 完整DDD实现参考

### 🎯 使用示例
```
# 生成电商订单聚合
@workspace /ddd-generate 创建电商订单管理聚合，包含订单创建、支付、发货、退款功能

# 验证代码架构
@workspace /ddd-validate 检查OrderAggregate是否符合DDD最佳实践

# 生成测试代码
@workspace /ddd-test 为OrderAggregate生成完整测试套件，包含单元测试和集成测试
```

## 🚨 快速验证

运行以下命令验证工作空间配置：

```bash
# 1. 创建测试项目
./scripts/create-ddd-project.sh -p com.test.demo -n demo-service -d ./test

# 2. 验证项目质量
cd test/demo-service && ../../scripts/validate-ddd.sh

# 3. 编译测试
mvn clean compile test
```

## 🤝 贡献指南

我们欢迎您的贡献！请查看以下指南：

1. **Fork** 本仓库
2. **创建特性分支**: `git checkout -b feature/amazing-feature`
3. **提交更改**: `git commit -m 'Add amazing feature'`
4. **推送分支**: `git push origin feature/amazing-feature`
5. **创建Pull Request**

## 📄 许可证

本项目基于 MIT 许可证开源 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [GitHub Copilot](https://github.com/features/copilot) - AI代码生成
- [Spring Boot](https://spring.io/projects/spring-boot) - 应用框架  
- [MyBatis-Plus](https://baomidou.com/) - 数据访问层
- [DDD Community](https://github.com/ddd-by-examples) - DDD最佳实践

---

<div align="center">

**🎉 开始您的DDD开发之旅！**

使用 `@workspace /ddd-generate` 命令生成第一个聚合  
体验智能化的领域驱动设计开发流程

[![开始使用](https://img.shields.io/badge/开始使用-立即体验-success?style=for-the-badge)](GUIDE.md)

</div>

## ✨ 特性亮点

### 🏗️ 完整的DDD架构生成
- **五层架构**: interfaces、application、domain、infrastructure、shared
- **富领域模型**: 聚合根、实体、值对象、领域服务
- **Repository模式**: 接口与实现分离，基于MyBatis-Plus
- **事件驱动**: 完整的领域事件支持

### 🚀 智能化代码生成
- **一键生成**: 从DDD设计文档到完整项目代码
- **自动测试**: 生成完整的测试套件(单元测试、集成测试、API测试)
- **代码验证**: 自动检查架构合规性和代码质量
- **文档同步**: 自动更新API文档和架构文档

### 🔄 完整的开发工作流
- **PR自动化**: 自动创建包含详细描述的Pull Request
- **质量检查**: 编译验证、测试运行、代码规范检查
- **持续集成**: GitHub Actions自动化流程

## 🎯 快速开始

### 1. 在VS Code中使用

```bash
# 在Copilot Chat中执行
@workspace /ddd-generate
项目根包：com.example.ecommerce
聚合：
  - Order（聚合根）
    字段：
      - id: Long (订单ID)
      - customerId: Long (客户ID)
      - status: OrderStatus (订单状态)
      - totalAmount: BigDecimal (总金额)
    业务行为：
      - cancel(): 取消订单
      - pay(): 支付订单
      - ship(): 发货
业务约束：
  - 已支付订单不能取消
  - 总金额必须大于0
```

### 2. 通过GitHub Actions

1. 进入仓库的 **Actions** 标签页
2. 选择 **"DDD Code Generator & PR Creator"** 工作流
3. 点击 **"Run workflow"**
4. 填入参数：
   - **Domain Specification**: 领域规范描述
   - **Base Package**: 基础包名(如: com.example.project)
   - **Branch Name**: 分支名(如: feature/ddd-order)
   - **Generate Tests**: 是否生成测试(默认: true)
   - **Validate Code**: 是否验证代码(默认: true)

## 🛠️ 可用命令

| 命令 | 描述 | 用途 |
|------|------|------|
| `/ddd-generate` | 🏗️ 生成完整DDD代码结构 | 基于规范生成项目代码 |
| `/pr-ddd` | 📝 创建DDD代码PR | 自动化PR创建流程 |
| `/ddd-design` | 🎨 DDD设计助手 | 架构设计指导 |
| `/ddd-context` | 🗺️ 上下文映射 | 生成边界上下文集成 |
| `/ddd-glossary` | 📚 词汇表管理 | 管理领域术语 |
| `/ddd-validate` | ✅ 代码验证 | 验证DDD最佳实践 |
| `/ddd-refactor` | ♻️ 代码重构 | 重构为DDD模式 |
| `/ddd-test` | 🧪 测试生成 | 生成测试套件 |
| `/yaml-code` | 🔄 YAML转代码 | YAML规范转Java代码 |

## 📁 项目结构

```
.
├── .github/                        # GitHub配置
│   ├── copilot/
│   │   └── agent.yml               # Copilot Agent配置
│   ├── prompts/                    # Prompt模板库
│   │   ├── ddd-code.prompts.md     # DDD代码生成规范
│   │   ├── ddd-validation.prompt.md # 代码验证规范
│   │   ├── ddd-test-generator.prompt.md # 测试生成规范
│   │   ├── ddd-refactor.prompt.md  # 重构指导
│   │   └── pr-ddd-workflow.md      # PR工作流规范
│   └── workflows/
│       └── ddd-pr-generator.yml    # 自动化工作流
├── docs/                           # 设计文档
│   ├── contexts/payment/           # 支付上下文示例
│   └── 支付模块领域模型设计总结.md    # DDD设计总结
├── examples/                       # 代码示例
│   └── user-management/            # 用户管理DDD示例
└── README.md                       # 本文件
```

## 📚 文档指南

### DDD设计文档
- [支付模块DDD设计](./docs/contexts/payment/domain/Payment.md) - 完整的DDD设计示例
- [支付模块用例模型](./docs/支付模块用例模型.md) - 用例分析和建模
- [DDD设计验证报告](./docs/DDD设计文档验证报告.md) - 设计质量验证

### Prompt模板
- [DDD代码生成规范](./.github/prompts/ddd-code.prompts.md) - 详细的代码生成规则
- [测试生成指南](./.github/prompts/ddd-test-generator.prompt.md) - 完整的测试策略
- [代码验证标准](./.github/prompts/ddd-validation.prompt.md) - 质量检查规范

### 示例项目
- [用户管理系统](./examples/user-management/) - 完整的DDD实现示例

## 🎨 支持的架构模式

### 分层架构
```
┌─────────────────┐
│   interfaces    │ ← REST API、DTO转换
├─────────────────┤
│   application   │ ← 用例编排、事务控制
├─────────────────┤
│     domain      │ ← 业务逻辑、领域规则
├─────────────────┤
│ infrastructure  │ ← 数据持久化、外部集成
├─────────────────┤
│     shared      │ ← 共享组件、异常定义
└─────────────────┘
```

### 核心模式
- **聚合模式**: 聚合根控制业务不变性
- **Repository模式**: 面向领域的数据访问
- **领域服务**: 跨聚合的业务逻辑
- **领域事件**: 解耦的业务流程
- **值对象**: 保证数据完整性

## 🔧 技术栈

### 核心框架
- **Spring Boot** 2.7+ - 应用框架
- **MyBatis-Plus** 3.x - ORM框架  
- **Lombok** - 代码简化
- **Maven/Gradle** - 构建工具

### 测试框架
- **JUnit 5** - 单元测试
- **Mockito** - Mock框架
- **Spring Boot Test** - 集成测试
- **TestContainers** - 容器化测试

### 开发工具
- **GitHub Copilot** - AI编程助手
- **GitHub Actions** - CI/CD
- **VS Code** - 开发环境

## 🧪 测试策略

### 测试金字塔
```
      /\     E2E Tests (5%)
     /  \    API测试、端到端场景
    /____\   Integration Tests (25%)
   /      \  Repository测试、数据库集成
  /________\ Unit Tests (70%)
 /          \ 领域逻辑、业务规则测试
```

### 测试覆盖率目标
- **领域层**: 95%+ (业务逻辑核心)
- **应用层**: 90%+ (用例编排)
- **基础设施层**: 85%+ (数据访问)
- **接口层**: 80%+ (API端点)

## 🚀 最佳实践

### 1. 设计阶段
- 先进行事件风暴，识别聚合边界
- 明确定义统一语言(Ubiquitous Language)
- 区分核心域、支撑域和通用域

### 2. 编码阶段
- 聚合内强一致性，聚合间最终一致性
- 使用领域事件解耦业务流程
- Repository接口面向领域设计

### 3. 测试阶段
- 重点测试领域逻辑和业务规则
- 使用Given-When-Then格式编写测试
- 为聚合根编写完整的行为测试

## 📈 工作流程

### 开发流程
1. **需求分析** → 领域建模 → 事件风暴
2. **设计阶段** → 聚合设计 → 边界定义
3. **代码生成** → 使用Copilot生成基础代码
4. **业务实现** → 完善TODO标记的业务逻辑
5. **测试验证** → 运行测试套件验证功能
6. **代码审查** → PR review和质量检查

### 质量保证
- ✅ **编译检查**: 确保代码可编译
- ✅ **架构验证**: 检查DDD分层架构
- ✅ **测试覆盖**: 运行完整测试套件
- ✅ **代码规范**: 检查命名和注解规范
- ✅ **文档同步**: 更新API文档和设计文档

## 🤝 贡献指南

### 添加新的Prompt模板
1. 在 `.github/prompts/` 目录下创建新的 `.prompt.md` 文件
2. 在 `.github/copilot/agent.yml` 中注册新的prompt
3. 添加对应的命令和描述

### 改进现有功能
1. Fork 本仓库
2. 创建特性分支: `git checkout -b feature/improve-xxx`
3. 提交更改: `git commit -m 'Add some feature'`
4. 推送分支: `git push origin feature/improve-xxx`
5. 创建 Pull Request

## 📞 支持与反馈

- **问题报告**: [GitHub Issues](../../issues)
- **功能建议**: [GitHub Discussions](../../discussions)
- **文档改进**: 直接提交PR

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

**⭐ 如果这个项目对您有帮助，请给我们一个星标！**

**🚀 开始您的DDD之旅，让GitHub Copilot成为您的架构助手！**