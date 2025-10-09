# GitHub Copilot DDD Workspace 快速验证

本文档提供快速验证整个工作空间功能的步骤。

## 🧪 验证清单

### ✅ 1. 基础配置验证

```bash
# 检查Agent配置文件
ls -la .github/copilot/agent.yml

# 检查Prompt模板
ls -la .github/prompts/

# 检查工作流配置
ls -la .github/workflows/

# 检查脚本工具
ls -la scripts/
```

### ✅ 2. 创建测试项目

```bash
# 使用项目生成器创建测试项目
./scripts/create-ddd-project.sh -p com.test.demo -n demo-service -d ./test-output
```

预期结果：
- 创建完整的DDD项目结构
- 生成Maven配置文件
- 创建Spring Boot启动类
- 包含基础配置文件

### ✅ 3. 验证项目质量

```bash
# 进入生成的项目目录
cd test-output/demo-service

# 运行DDD架构验证
../../scripts/validate-ddd.sh
```

预期结果：
- 包结构检查通过
- 配置文件验证通过
- 基础架构合规

### ✅ 4. GitHub Copilot Agent测试

在VS Code中打开工作空间，测试以下命令：

```
@workspace /ddd-generate 创建一个简单的Product聚合，包含创建、更新和删除产品的功能
```

预期结果：
- 生成Product聚合根类
- 创建ProductRepository接口
- 生成ProductService应用服务
- 包含相应的基础设施实现

### ✅ 5. 代码验证测试

```
@workspace /ddd-validate 检查刚刚生成的Product聚合是否符合DDD最佳实践
```

预期结果：
- 分析聚合边界
- 检查业务不变性
- 验证仓储模式
- 提供改进建议

### ✅ 6. 测试生成功能

```
@workspace /ddd-test 为Product聚合生成完整的测试套件
```

预期结果：
- 生成单元测试
- 创建集成测试
- 包含测试数据构建器
- 提供测试最佳实践

### ✅ 7. 编译和运行测试

```bash
# 在测试项目中
mvn clean compile
mvn test
```

预期结果：
- 编译成功，无错误
- 所有测试通过
- 代码覆盖率报告生成

### ✅ 8. GitHub Actions验证

提交代码到GitHub仓库，触发工作流：

```bash
git add .
git commit -m "feat: add generated DDD code"
git push origin main
```

预期结果：
- GitHub Actions工作流触发
- 代码质量检查通过
- 自动生成PR（如果配置）

## 🎯 成功标准

| 检查项 | 状态 | 说明 |
|--------|------|------|
| Agent配置 | ✅ | 9个命令正确配置 |
| Prompt模板 | ✅ | 9个模板文件完整 |
| 项目生成器 | ✅ | 能创建完整DDD项目 |
| 代码生成 | ✅ | Copilot命令工作正常 |
| 质量验证 | ✅ | 验证脚本功能正常 |
| 编译构建 | ✅ | 生成的代码可编译运行 |
| 自动化流程 | ✅ | GitHub Actions工作流正常 |
| 文档完整性 | ✅ | README和GUIDE文档完整 |

## 🚨 常见问题排查

### 问题1: Agent命令不响应
```bash
# 检查配置文件语法
yamllint .github/copilot/agent.yml

# 重启VS Code
# 确保GitHub Copilot扩展已启用
```

### 问题2: 项目生成失败
```bash
# 检查脚本权限
ls -la scripts/create-ddd-project.sh

# 手动运行查看错误信息
bash -x scripts/create-ddd-project.sh -p com.test.demo -n demo
```

### 问题3: 编译错误
```bash
# 检查Java版本
java -version

# 检查Maven配置
mvn -version

# 清理并重新编译
mvn clean compile
```

### 问题4: GitHub Actions失败
- 检查工作流配置语法
- 验证secrets配置
- 查看Actions日志详情

## 🎉 验证完成

如果所有检查项都通过，说明您的DDD GitHub Copilot工作空间已经完全配置成功！

现在您可以：
1. 开始使用Copilot命令生成DDD代码
2. 利用自动化工具提高开发效率
3. 遵循最佳实践构建高质量的领域模型

---

**下一步**: 查看 `GUIDE.md` 了解详细的使用说明和高级功能。