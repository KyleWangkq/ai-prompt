# GitHub Copilot Pull Request 工作流 - DDD 代码生成

## 使用场景
当需要为现有项目创建 DDD 代码结构的 Pull Request 时使用此 agent。

## 工作流程

### 步骤1: 分析需求
首先分析要生成的 DDD 结构：
```
/pr-ddd 
项目根包：com.example.project
聚合：
  - User（聚合根）
    字段：
      - id: Long (用户ID)
      - username: String (用户名)
      - email: String (邮箱)
      - status: String (状态)
    业务行为：
      - disable(): 禁用用户
      - enable(): 启用用户
      - changeEmail(newEmail): 修改邮箱
业务约束：
  - 用户名必须唯一
  - 邮箱格式必须正确
```

### 步骤2: 生成代码结构
基于 DDD 文档自动生成完整的项目文件结构和代码。

### 步骤3: 创建 Pull Request
自动创建包含以下内容的 PR：

#### PR 标题
`feat: Add DDD structure for [聚合名称] domain`

#### PR 描述模板
```markdown
## 🎯 功能描述
添加 [聚合名称] 领域的 DDD 架构代码

## 📁 文件结构
- **领域层**: 聚合根、实体、值对象、领域服务
- **基础设施层**: Repository 实现、Mapper、配置
- **应用层**: Application Service
- **接口层**: Controller、DTO

## 🔧 技术栈
- Spring Boot 2.7
- MyBatis-Plus 3.x
- Lombok
- DDD 分层架构

## ✅ 生成内容
- [x] 聚合根和实体类
- [x] Repository 接口和实现
- [x] MyBatis-Plus Mapper
- [x] Application Service
- [x] REST Controller
- [x] DTO (RO/VO)
- [x] 配置文件

## 📝 待完成 (TODO)
列出所有标记为 TODO 的功能点

## 🧪 测试建议
- 单元测试：领域逻辑测试
- 集成测试：Repository 测试
- API 测试：Controller 测试

## 🔍 Review 要点
- [ ] 包结构是否符合 DDD 规范
- [ ] 领域逻辑是否正确封装
- [ ] Repository 模式实现是否合理
- [ ] 代码是否可编译运行
```

## 🤖 Agent 命令

### 生成 DDD 代码
```
@workspace /ddd-generate 
项目根包：com.example.project
聚合：[聚合定义]
```

### 创建 PR
```
@workspace /pr-ddd
分支名：feature/ddd-[domain-name]
基础分支：main
标题：feat: Add DDD structure for [Domain] domain
```

## 自动化流程

### 1. 代码质量检查
- 编译检查
- 包结构验证
- 命名规范检查
- TODO 标记统计

### 2. 文档生成
- API 文档
- 领域模型图
- 数据库表结构

### 3. PR 模板应用
- 自动填充 PR 描述
- 添加相关标签
- 指定 Reviewer

## 使用示例

### 完整工作流
```bash
# 1. 在 Copilot Chat 中执行
@workspace /pr-ddd
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

# 2. Agent 自动执行
# - 生成完整代码结构
# - 创建新分支 feature/ddd-order
# - 提交代码到新分支
# - 创建 Pull Request
# - 应用 PR 模板
```

## 配置选项

### 高级配置
```yaml
pr_config:
  auto_assign_reviewers: true
  draft_mode: false
  delete_branch_after_merge: true
  squash_commits: true

code_config:
  db: mysql
  generateMapperXml: true
  useSoftDelete: true
  auditFields: true

quality_gates:
  compilation_check: true
  test_generation: true
  documentation: true
```

## 故障排除

### 常见问题
1. **编译失败**: 检查包依赖和语法
2. **命名冲突**: 检查类名和包名是否冲突
3. **PR 创建失败**: 检查分支权限和命名规范

### Debug 模式
```
@workspace /pr-ddd --debug
# 显示详细的生成过程和错误信息
```