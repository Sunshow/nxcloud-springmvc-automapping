# NXCloud SpringMVC AutoMapping - Agent Knowledge Base

## 项目概述
这是一个 Spring Boot 快速开发框架，通过 Contract 接口约定自动注册 Spring MVC 请求映射，省去手动定义 Controller 的过程。

## 技术栈
- Kotlin 1.9.0
- Spring Boot 3.3.4
- Gradle 8.2.1
- Java 17 (target)

## 项目结构
```
nxcloud-springmvc-automapping/
├── springmvc-automapping-base/     # 基础注解定义
├── springmvc-automapping/          # 核心实现
├── ext-spring-boot-starter-.../    # Spring Boot 自动配置
└── sample/                         # 示例项目
```

## 核心组件

### 注解 (springmvc-automapping-base)
- `@AutoMappingContract` - 标记接口方法为自动映射端点
- `@AutoMappingBean` - 标记 Bean 接口支持自动映射

### 核心类 (springmvc-automapping)
- `AutoMappingRequestParameterTypeBinding` - 请求参数类型绑定管理，维护方法与参数类型的映射缓存
- `AutoMappingContractRegistrar` - Contract 接口解析与注册
- `AutoMappingRequestHandlerRegistrar` - Bean 方法自动注册处理器
- `AutoMappingHandlerMethodArgumentResolver` - 参数解析器

### 参数解析器 (spi/impl)
- `JacksonAutoMappingRequestParameterResolver` - JSON Body 解析
- `QueryParameterAutoMappingRequestParameterResolver` - Query/Form 参数解析
- `PathVariableAutoMappingRequestParameterResolver` - 路径变量解析

## 关键实现细节

### 缓存结构 (AutoMappingRequestParameterTypeBinding)
使用 `ConcurrentHashMap` 保证线程安全：
- `originalBindingCache` - 原始方法参数类型映射
- `bindingCache` - 转换后的参数类型映射
- `declaringMethodCache` - 声明处方法缓存
- `pathVariableCache` - 路径变量缓存 (pattern -> 变量名集合)

### getBridgedMethod 多重回退策略
1. 优先：`RequestContextHolder.getRequestAttributes()` 获取 `HandlerMethod`
2. 回退：通过 `this$0` 反射获取 (兼容旧版本)
3. 最终：`parameter.method!!` (保底)

### 路径变量正则
```kotlin
"""\{([^:}]+)(?::[^}]*)?\}""".toRegex()
```
支持带约束的路径变量如 `{id:\d+}`

## Contract 使用示例
```kotlin
@AutoMappingContract(paths = ["/user"])
interface UserContract {
    @AutoMappingContract(
        method = AutoMappingContract.Method.GET,
        paths = ["/profile/{name}"],
        beanType = UserService::class,
        beanMethod = "create",
        consumes = []
    )
    fun testPath()
}
```

## 构建命令
```bash
# 构建子项目（避免根项目 Kotlin target 问题）
./gradlew :springmvc-automapping:build -x test
./gradlew :sample:build -x test

# 运行示例
./gradlew :sample:bootRun
```

## 已知限制
1. 多路径模式带路径变量时会抛异常（设计限制，防止缓存冲突）
2. 嵌套对象参数绑定不支持
3. 方法有多个原始类型参数时，非路径变量参数需通过 Query 传递

## 测试端点 (sample)
- GET `/user/profile/{name}?age=25` - 路径变量 + Query
- POST `/user/update/{name}` + JSON Body - 路径变量 + Body
- GET `/user/rename/{name}` - 路径变量注入对象
- POST `/user/rename2/{name}` + JSON Body - 路径变量注入 Body
