# GitHub Copilot 提示词库

本目录包含用于 DDD（领域驱动设计）架构设计和代码生成的 GitHub Copilot 提示词集合。

## 📁 目录结构

```
.github/prompts/
├── README.md                    # 本文件：提示词库总览和使用指南
├── ddd/                         # DDD 架构设计提示词（推荐使用）
│   ├── README.md               # DDD 提示词使用指南
│   ├── master-ddd.prompt.md    # 主控制器：协调所有 DDD 设计流程
│   ├── glossary.prompt.md      # 全局词汇表生成器
│   ├── context.prompt.md       # 限界上下文设计器
│   ├── domain.prompt.md        # 领域层设计器
│   ├── application.prompt.md   # 应用层设计器
│   ├── infrastructure.prompt.md # 基础设施层设计器
│   └── integration.prompt.md   # 上下文集成设计器
├── code-generation/            # 代码生成提示词
│   ├── README.md               # 代码生成使用指南
│   ├── ddd-to-java.prompt.md   # DDD 文档转 Java 代码
│   └── ddd-to-yaml.prompt.md   # DDD 文档转 YAML
└── requirements/               # 需求分析提示词
    ├── README.md               # 需求分析使用指南
    ├── business-analysis.prompt.md  # 业务架构设计
    └── use-case-modeling.prompt.md  # 用例建模
```

## 🎯 快速开始

### 1. DDD 架构设计（推荐流程）

如果您需要基于需求文档生成完整的 DDD 设计文档，请使用 **ddd/** 目录下的提示词：

1. **阅读主控制器文档**: [`ddd/master-ddd.prompt.md`](ddd/master-ddd.prompt.md)
2. **按照执行顺序使用各个子提示词**:
   - `glossary.prompt.md` → 建立术语体系
   - `context.prompt.md` → 设计上下文边界
   - `domain.prompt.md` → 设计领域模型
   - `application.prompt.md` → 设计应用服务
   - `infrastructure.prompt.md` → 设计基础设施
   - `integration.prompt.md` → 设计上下文集成

**特点**:
- ✅ 完整的 DDD 分层架构设计
- ✅ 严格遵循需求文档
- ✅ 跨文档一致性保证
- ✅ 术语标准化管理
- ✅ 提供详细的验证机制

### 2. 代码生成

将 DDD 设计文档转换为代码骨架，请使用 **code-generation/** 目录：

- **Java/Spring Boot**: [`code-generation/ddd-to-java.prompt.md`](code-generation/ddd-to-java.prompt.md)
- **YAML 中间格式**: [`code-generation/ddd-to-yaml.prompt.md`](code-generation/ddd-to-yaml.prompt.md)

### 3. 需求分析

需要进行业务分析或用例建模，请使用 **requirements/** 目录：

- **业务架构设计**: [`requirements/business-analysis.prompt.md`](requirements/business-analysis.prompt.md)
- **用例建模**: [`requirements/use-case-modeling.prompt.md`](requirements/use-case-modeling.prompt.md)

## 📚 详细文档

每个子目录都包含 README.md 文件，提供该类别提示词的详细使用说明和示例。

## 🔄 版本说明

- **当前版本**: v2.0
- **最后更新**: 2025-10-09
- **重大变更**: 
  - 重新组织目录结构，按功能分类
  - 统一文件命名规范
  - 删除重复和过时的提示词
  - 添加详细的使用文档

## 📖 相关文档

- [DDD 设计文档 Prompt 设计说明书](../../DDD设计文档Prompt设计说明书.md)
- [支付模块需求设计](../../支付模块需求设计.md)
- [DDD 设计文档验证报告](../../docs/DDD设计文档验证报告.md)

## 🤝 贡献指南

如需添加或修改提示词，请遵循以下原则：

1. **文件命名**: 使用小写字母和连字符，如 `use-case-modeling.prompt.md`
2. **分类清晰**: 将提示词放在合适的目录下
3. **文档完整**: 每个提示词应包含角色设定、使用说明和示例
4. **更新索引**: 修改后更新相应的 README.md

## ⚠️ 注意事项

- 旧版本的提示词文件（如 `ddd.prompt.md`, `ddd-cn.prompt.md` 等）已被重新整理到新的目录结构中
- 如果您之前使用的是根目录下的提示词文件，请迁移到新的分类目录
- 建议使用 `ddd/master-ddd.prompt.md` 作为 DDD 设计的入口点
