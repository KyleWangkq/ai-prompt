# GitHub Copilot Agent 使用指南

## 快速开始

### 在 VS Code 中使用 Copilot Chat

1. **打开 Copilot Chat** (Ctrl+Shift+I 或 Cmd+Shift+I)

2. **使用 DDD 代码生成命令**:
```
@workspace /ddd-generate
项目根包：com.example.ecommerce
聚合：
  - Order（聚合根）
    字段：
      - id: Long (订单ID)
      - customerId: Long (客户ID)
      - status: String (订单状态)
      - totalAmount: BigDecimal (总金额)
    业务行为：
      - cancel(): 取消订单
      - pay(): 支付订单
      - ship(): 发货
业务约束：
  - 已支付订单不能取消
  - 总金额必须大于0
```

3. **创建 Pull Request**:
```
@workspace /pr-ddd
分支名：feature/ddd-order
基础分支：main
描述：添加订单领域的 DDD 架构
```

## 高级用法

### 配置参数
```
@workspace /ddd-generate
配置: { db: mysql, generateMapperXml: true, useSoftDelete: true }
项目根包：com.example.project
聚合：[聚合定义]
```

### 批量生成多个聚合
```
@workspace /ddd-generate
项目根包：com.example.ecommerce
聚合：
  - User（聚合根）
    字段：[用户字段定义]
    业务行为：[用户业务方法]
  - Product（聚合根）
    字段：[产品字段定义]
    业务行为：[产品业务方法]
  - Order（聚合根）
    字段：[订单字段定义]
    业务行为：[订单业务方法]
```

## GitHub Actions 自动化

### 手动触发工作流
1. 进入 GitHub 仓库
2. 点击 "Actions" 标签页
3. 选择 "DDD PR Generator" 工作流
4. 点击 "Run workflow"
5. 填入参数并运行

### API 触发 (适合 CI/CD 集成)
```bash
curl -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/your-org/your-repo/actions/workflows/ddd-pr-generator.yml/dispatches \
  -d '{
    "ref": "main",
    "inputs": {
      "domain_spec": "聚合：...",
      "base_package": "com.example.project",
      "branch_name": "feature/ddd-user"
    }
  }'
```

## 最佳实践

### 1. 分支命名规范
- `feature/ddd-[domain-name]` - 新增领域
- `refactor/ddd-[domain-name]` - 重构现有领域
- `fix/ddd-[domain-name]` - 修复领域问题

### 2. 提交信息规范
- `feat: Add DDD structure for User domain`
- `refactor: Improve Order aggregate design`
- `fix: Correct Product repository implementation`

### 3. PR 审查要点
- 检查 DDD 分层架构是否正确
- 验证业务逻辑封装是否合理
- 确认代码可编译且测试通过
- 评估性能和扩展性

## 故障排除

### 常见问题

**Q: 生成的代码编译失败**
A: 检查以下几点：
- 项目依赖是否正确 (Spring Boot, MyBatis-Plus, Lombok)
- 包名是否与实际项目结构匹配
- 导入语句是否正确

**Q: Copilot Chat 无法识别命令**
A: 确保以下配置正确：
- `.github/copilot/agent.yml` 文件存在
- 命令路径配置正确
- VS Code Copilot 扩展已更新

**Q: PR 创建失败**
A: 检查权限设置：
- GitHub Token 是否有仓库写权限
- 分支保护规则是否允许创建 PR
- 工作流权限是否正确配置

### Debug 模式
```
@workspace /ddd-generate --debug
# 显示详细生成过程
```

## 自定义扩展

### 添加新的代码模板
1. 修改 `.github/prompts/ddd-code.prompts.md`
2. 添加新的代码模板示例
3. 更新 agent 配置

### 集成其他工具
- **代码质量检查**: SonarQube, Checkstyle
- **API 文档生成**: Swagger, Spring REST Docs  
- **测试覆盖**: JaCoCo, Codecov

## 支持与反馈

如果遇到问题或有改进建议，请：
1. 查看项目 Issues
2. 创建新的 Issue 并使用 `copilot-agent` 标签
3. 提供详细的错误信息和使用场景