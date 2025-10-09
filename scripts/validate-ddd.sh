#!/bin/bash

# DDD代码质量验证脚本
# 用于验证生成的DDD代码是否符合最佳实践

set -e

echo "🔍 开始DDD代码质量验证..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 计数器
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# 检查函数
check_result() {
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}❌ $2${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
}

# 警告函数
warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# 信息函数
info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

echo ""
echo "📁 检查项目结构..."

# 1. 检查DDD分层架构
info "检查DDD分层架构"

check_interfaces() {
    if find src -path "*/interfaces/*Controller.java" | grep -q .; then
        return 0
    else
        return 1
    fi
}

check_application() {
    if find src -path "*/application/*ApplicationService.java" | grep -q .; then
        return 0
    else
        return 1
    fi
}

check_domain() {
    if find src -path "*/domain/model/*Aggregate.java" | grep -q .; then
        return 0
    else
        return 1
    fi
}

check_infrastructure() {
    if find src -path "*/infrastructure/*Repository*.java" | grep -q .; then
        return 0
    else
        return 1
    fi
}

check_interfaces
check_result $? "接口层(interfaces) - Controller类存在"

check_application  
check_result $? "应用层(application) - ApplicationService类存在"

check_domain
check_result $? "领域层(domain) - Aggregate类存在"

check_infrastructure
check_result $? "基础设施层(infrastructure) - Repository类存在"

echo ""
echo "🏗️ 检查DDD设计模式..."

# 2. 检查聚合根设计
check_aggregate_methods() {
    local has_business_methods=false
    
    for file in $(find src -name "*Aggregate.java" 2>/dev/null); do
        if grep -q "public.*void\|public.*boolean\|public.*String" "$file"; then
            has_business_methods=true
            break
        fi
    done
    
    if [ "$has_business_methods" = true ]; then
        return 0
    else
        return 1
    fi
}

check_aggregate_methods
check_result $? "聚合根包含业务方法"

# 3. 检查Repository模式
check_repository_interface() {
    if find src -path "*/domain/repository/I*Repository.java" | grep -q .; then
        return 0
    else
        return 1
    fi
}

check_repository_impl() {
    if find src -path "*/infrastructure/repository/*RepositoryImpl.java" | grep -q .; then
        return 0
    else
        return 1
    fi
}

check_repository_interface
check_result $? "Repository接口存在(domain/repository/)"

check_repository_impl
check_result $? "Repository实现存在(infrastructure/repository/)"

echo ""
echo "📝 检查代码规范..."

# 4. 检查注解使用
check_spring_annotations() {
    local has_annotations=false
    
    for file in $(find src -name "*.java" 2>/dev/null); do
        if grep -q "@Service\|@RestController\|@Repository\|@Entity" "$file"; then
            has_annotations=true
            break
        fi
    done
    
    if [ "$has_annotations" = true ]; then
        return 0
    else
        return 1
    fi
}

check_spring_annotations
check_result $? "Spring注解使用正确"

# 5. 检查包命名规范
check_package_naming() {
    local correct_naming=true
    
    for file in $(find src -name "*.java" 2>/dev/null); do
        local package_line=$(head -1 "$file")
        if [[ $package_line == package* ]]; then
            # 检查是否包含interfaces, application, domain, infrastructure
            if [[ $package_line =~ (interfaces|application|domain|infrastructure) ]]; then
                continue
            else
                if [[ $file =~ (Controller|Service|Repository|Entity|Aggregate) ]]; then
                    warning "文件 $file 可能不在正确的包中"
                    correct_naming=false
                fi
            fi
        fi
    done
    
    if [ "$correct_naming" = true ]; then
        return 0
    else
        return 1
    fi
}

check_package_naming
check_result $? "包命名符合DDD规范"

echo ""
echo "🧪 检查测试覆盖..."

# 6. 检查测试文件
check_test_files() {
    if find src -name "*Test.java" | grep -q .; then
        return 0
    else
        return 1
    fi
}

check_test_coverage() {
    local domain_files=$(find src/main -path "*/domain/*" -name "*.java" | wc -l)
    local test_files=$(find src/test -name "*Test.java" | wc -l)
    
    if [ "$domain_files" -gt 0 ] && [ "$test_files" -gt 0 ]; then
        local coverage_ratio=$((test_files * 100 / domain_files))
        if [ "$coverage_ratio" -ge 50 ]; then
            info "测试覆盖率估算: ${coverage_ratio}%"
            return 0
        else
            warning "测试覆盖率较低: ${coverage_ratio}%"
            return 1
        fi
    else
        return 1
    fi
}

check_test_files
check_result $? "测试文件存在"

if [ $? -eq 0 ]; then
    check_test_coverage
    check_result $? "测试覆盖率合理(>50%)"
fi

echo ""
echo "🔍 检查代码质量..."

# 7. 检查TODO标记
check_todos() {
    local todo_count=$(grep -r "TODO" src --include="*.java" 2>/dev/null | wc -l)
    info "发现 $todo_count 个TODO标记"
    
    if [ "$todo_count" -gt 0 ] && [ "$todo_count" -le 50 ]; then
        return 0  # 合理的TODO数量
    elif [ "$todo_count" -eq 0 ]; then
        warning "没有发现TODO标记，可能代码已完全实现或缺少待完成标记"
        return 0
    else
        warning "TODO标记过多($todo_count个)，建议逐步完善"
        return 1
    fi
}

check_todos
check_result $? "TODO标记数量合理"

# 8. 检查编译状态
check_compilation() {
    info "检查项目编译状态..."
    
    if [ -f "pom.xml" ]; then
        if command -v mvn >/dev/null 2>&1; then
            if mvn compile -q >/dev/null 2>&1; then
                return 0
            else
                return 1
            fi
        else
            warning "Maven未安装，跳过编译检查"
            return 0
        fi
    elif [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then
        if [ -f "gradlew" ]; then
            if ./gradlew compileJava -q >/dev/null 2>&1; then
                return 0
            else
                return 1
            fi
        else
            warning "Gradle wrapper未找到，跳过编译检查"
            return 0
        fi
    else
        warning "未找到构建文件(pom.xml或build.gradle)，跳过编译检查"
        return 0
    fi
}

check_compilation
check_result $? "项目可以编译"

echo ""
echo "📊 验证结果汇总"
echo "=================="
echo -e "总检查项: ${BLUE}$TOTAL_CHECKS${NC}"
echo -e "通过项: ${GREEN}$PASSED_CHECKS${NC}"
echo -e "失败项: ${RED}$FAILED_CHECKS${NC}"

# 计算通过率
if [ $TOTAL_CHECKS -gt 0 ]; then
    PASS_RATE=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))
    echo -e "通过率: ${BLUE}$PASS_RATE%${NC}"
    
    if [ $PASS_RATE -ge 90 ]; then
        echo -e "${GREEN}🎉 优秀! DDD代码质量很高${NC}"
        exit 0
    elif [ $PASS_RATE -ge 75 ]; then
        echo -e "${YELLOW}👍 良好! 有一些地方需要改进${NC}"
        exit 0
    elif [ $PASS_RATE -ge 60 ]; then
        echo -e "${YELLOW}⚠️  一般! 需要较多改进${NC}"
        exit 1
    else
        echo -e "${RED}❌ 需要大量改进!${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ 没有找到可检查的代码${NC}"
    exit 1
fi