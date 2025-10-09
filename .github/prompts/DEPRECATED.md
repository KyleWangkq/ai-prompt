# 已废弃的提示词文件

本目录下的以下文件已被重新整理和分类到新的目录结构中，建议使用新的分类结构。

## 📋 文件迁移对照表

### 已迁移到新结构的文件

| 旧文件名 | 新位置 | 说明 |
|---------|--------|------|
| `code-ddd.prompt.md` | `code-generation/ddd-to-java.prompt.md` | DDD 文档转 Java 代码 |
| `ddd-yaml.prompt.md` | `code-generation/ddd-to-yaml.prompt.md` | DDD 文档转 YAML |
| `request.prompt.md` | `requirements/business-analysis.prompt.md` | 业务架构设计 |
| `design.prompt.md` | `requirements/use-case-modeling.prompt.md` | 用例建模 |

### 待处理的重复文件

以下文件与 `ddd/` 目录下的结构化提示词功能重复，建议使用 `ddd/` 目录下的版本：

| 文件名 | 推荐替代 | 说明 |
|--------|---------|------|
| `ddd.prompt.md` | `ddd/master-ddd.prompt.md` | 旧版本的 DDD 设计提示词，功能较为基础 |
| `ddd-cn.prompt.md` | `ddd/` 目录下的各个子提示词 | 中文版本的 DDD 代码生成提示词 |
| `ddd-prompts.md` | `code-generation/` 目录 | 英文版本的 DDD 代码生成提示词 |
| `ddd-code.prompts.md` | `code-generation/ddd-to-java.prompt.md` | DDD 代码生成助手 |
| `yaml-code.prompt.md` | `code-generation/` 目录 | YAML 转代码 |

## ⚠️ 重要提示

1. **推荐使用新结构**: 新的目录结构更加清晰，分类更加合理
2. **功能更强大**: `ddd/` 目录下的提示词体系更加完整，包含：
   - 更强的一致性验证机制
   - 完整的文档依赖管理
   - 详细的质量保证检查
   - 术语标准化管理
3. **向后兼容**: 旧文件暂时保留，但建议尽快迁移到新结构

## 🔄 迁移指南

### 如果您正在使用旧版本提示词

**场景1：使用 `ddd.prompt.md` 进行 DDD 设计**
```
旧方式：使用单个 ddd.prompt.md 文件
新方式：使用 ddd/master-ddd.prompt.md 协调完整流程

优势：
- 更清晰的分层设计
- 强制的依赖检查
- 完整的术语管理
- 跨文档一致性保证
```

**场景2：使用 `code-ddd.prompt.md` 或 `ddd-code.prompts.md` 生成代码**
```
旧方式：使用根目录下的代码生成文件
新方式：使用 code-generation/ddd-to-java.prompt.md

优势：
- 统一的命名规范
- 更清晰的分类
- 易于维护和扩展
```

**场景3：使用 `request.prompt.md` 或 `design.prompt.md` 进行需求分析**
```
旧方式：使用根目录下的需求分析文件
新方式：使用 requirements/ 目录下的文件

优势：
- 按功能分类
- 更完整的使用说明
- 与 DDD 设计流程更好衔接
```

## 📅 废弃时间表

- **当前阶段** (v2.0): 旧文件保留，添加废弃警告
- **下一版本** (v2.1): 将旧文件移动到 `deprecated/` 子目录
- **未来版本** (v3.0): 完全删除旧文件

## 🆘 需要帮助？

如果您在迁移过程中遇到问题，请：

1. 查看新目录下的 README.md 文件了解详细使用说明
2. 参考 `ddd/master-ddd.prompt.md` 了解完整的 DDD 设计流程
3. 查看示例文档了解实际用法

## 📖 相关文档

- [新的提示词库总览](README.md)
- [DDD 架构设计提示词](ddd/README.md)
- [代码生成提示词](code-generation/README.md)
- [需求分析提示词](requirements/README.md)
