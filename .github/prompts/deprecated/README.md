# 已废弃的提示词

⚠️ **本目录下的文件已被废弃，请使用新的目录结构。**

## 📋 文件列表及替代方案

### 1. ddd.prompt.md
**状态**: 已废弃  
**推荐替代**: [`../ddd/master-ddd.prompt.md`](../ddd/master-ddd.prompt.md)  
**说明**: 旧版本的 DDD 设计提示词，功能较为基础。新版本提供了更完整的分层架构设计和一致性验证机制。

### 2. ddd-cn.prompt.md
**状态**: 已废弃  
**推荐替代**: [`../ddd/`](../ddd/) 目录下的各个子提示词  
**说明**: 中文版本的 DDD 代码生成提示词。新版本将 DDD 设计和代码生成分离，提供更清晰的职责划分。

### 3. ddd-prompts.md
**状态**: 已废弃  
**推荐替代**: [`../code-generation/`](../code-generation/) 目录  
**说明**: 英文版本的 DDD 代码生成提示词（基于 Codely 框架）。新版本提供了更完整的 Java/Spring Boot 代码生成方案。

### 4. ddd-code.prompts.md
**状态**: 已废弃  
**推荐替代**: [`../code-generation/ddd-to-java.prompt.md`](../code-generation/ddd-to-java.prompt.md)  
**说明**: DDD Spring Boot 代码生成助手。已整合到新的代码生成目录中。

### 5. yaml-code.prompt.md
**状态**: 已废弃  
**推荐替代**: [`../code-generation/`](../code-generation/) 目录下的相关提示词  
**说明**: YAML 转 Java 代码生成。功能已整合到新的代码生成流程中。

## 🔄 迁移指南

### 从旧版本迁移到新版本

**步骤1: 了解新的目录结构**
```
.github/prompts/
├── ddd/                    # DDD 架构设计（推荐）
├── code-generation/        # 代码生成
└── requirements/          # 需求分析
```

**步骤2: 选择合适的提示词**
- **DDD 架构设计**: 使用 `ddd/master-ddd.prompt.md` 作为入口点
- **代码生成**: 使用 `code-generation/ddd-to-java.prompt.md`
- **需求分析**: 使用 `requirements/` 目录下的提示词

**步骤3: 按照新的使用流程**
1. 需求分析 → 2. DDD 设计 → 3. 代码生成 → 4. 业务实现

## 📖 详细文档

- [新的提示词库总览](../README.md)
- [DDD 架构设计使用指南](../ddd/README.md)
- [代码生成使用指南](../code-generation/README.md)
- [需求分析使用指南](../requirements/README.md)

## ❓ 常见问题

**Q: 为什么要废弃这些文件？**  
A: 旧文件存在以下问题：
- 功能重复，难以维护
- 缺乏清晰的分类和组织
- 缺少完整的质量保证机制
- 文档依赖关系不清晰

**Q: 新版本有什么优势？**  
A: 新版本提供：
- 清晰的分类结构
- 完整的 DDD 分层架构支持
- 强大的一致性验证机制
- 详细的使用文档和示例
- 术语标准化管理

**Q: 旧文件什么时候会被删除？**  
A: 
- 当前阶段 (v2.0): 保留在 deprecated/ 目录
- 下一版本 (v2.1): 继续保留但添加更明显的废弃警告
- 未来版本 (v3.0): 可能完全删除

**Q: 我的项目还在使用旧版本，怎么办？**  
A: 建议尽快迁移到新版本，但如果暂时无法迁移：
- 旧文件仍然可用（在 deprecated/ 目录）
- 但不会再更新和维护
- 可能存在与新版本不兼容的问题

## 📞 获取帮助

如果您在迁移过程中遇到问题，请参考以下资源：

1. [DDD 设计文档 Prompt 设计说明书](../../../DDD设计文档Prompt设计说明书.md)
2. [支付模块需求设计示例](../../../支付模块需求设计.md)
3. [DDD 设计文档验证报告](../../../docs/DDD设计文档验证报告.md)

---

**最后更新**: 2025-10-09  
**版本**: v2.0
