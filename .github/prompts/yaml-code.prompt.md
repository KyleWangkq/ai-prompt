# YAML → Java 代码生成 Prompt

你是一名资深的 Java 架构师和 DDD 实践专家，请根据下面给定的 YAML 文档，生成完整的 Java 项目代码骨架。

## 技术栈要求
- Spring Boot 2.7
- MyBatis-Plus
- 数据库：MySQL 或 PostgreSQL
- Lombok 简化实体类
- 遵循 DDD 分层与以下包结构：

com.example.project
├─ interfaces
│  └─ model
├─ application
│  └─ service
├─ event
├─ domain
│  ├─ entity
│  ├─ repository
│  └─ service
├─ infrastructure
│  └─ mapper
│    └─ xml
│  
└─ config

## 输出要求
1. 首先生成文件树（包含所有文件路径）。
2. 然后输出每个 Java 文件的完整代码骨架（包含 package、import、注解、类签名、Lombok 注解、必要的 TODO）。
3. 对于 Mapper XML（如果 generateMapperXml=true），生成示例 SQL。
4. Application Service 与 Controller 层之间通过 RO→Entity→VO 转换，不直接暴露领域实体。
5. 所有自定义方法在接口中列出签名，在实现类中写 TODO。

## 输入 YAML
[在这里粘贴由 ddd-to-yaml.md 生成的 YAML]

## 输出
完整的文件树和对应的 Java 源码骨架。
