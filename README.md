# nxcloud-springmvc-automapping

自动将指定注解的接口/类方法注册到 SpringMVC RequestMapping 用于 REST 访问

![image](https://img.shields.io/maven-central/v/net.sunshow.nxcloud/nxcloud-springmvc-automapping)

## 设计目标

- 通过指定注解自动扫描用于注册的类和需要注册的方法
- 支持通过接口自定义注册路径，也可以使用默认路径例如：/api/类名/方法名
- 通过扩展支持自定义实现注册规则
- 保持对 SpringMVC 兼容，例如支持用传统方式对生成的返回值 (ResponseBodyAdvice) 做封装，支持自定义拦截器等
- 支持 Context 扩展以实现例如从 Session 读取 userId 等属性填充到入参等功能

## 已知问题

- 不支持方法重载的选择，实际响应请求的 Bean 只能有且仅有一个和声明方法一致的处理方法
- 尚未支持 PathParam

## 使用

示例代码使用 Kotlin 编写，Java 可自行转换，完整的示例程序可查看 `sample`

### 示例服务 (实际响应请求处理的类)

```kotlin
interface UserService {

    fun info(): User

    fun rename(user: User): User

    fun submit(user: User)

    fun create(name: String, age: Int): User
}

@Component
class UserServiceImpl : UserService {
    override fun info(): User {
        return User(
            name = "info"
        )
    }

    override fun rename(user: User): User {
        return User(
            name = "rename: ${user.name}"
        )
    }

    override fun create(name: String, age: Int): User {
        return User(
            name = name,
            age = age,
        )
    }

    override fun submit(user: User) {
        println(user)
    }
}

data class User(
    val name: String,
    val age: Int = 0,
)
```

### 启用自动注册

添加依赖

```kotlin
// 自行替换相应的依赖方式
implementation("net.sunshow.nxcloud:nxcloud-ext-spring-boot-starter-springmvc-automapping:0.3.1")
```

SpringBootApplication 注解启动

```kotlin
@NXEnableSpringMvcAutoMapping
@SpringBootApplication
class AutoMappingSampleApp
```

### 协议声明

协议声明默认请求方式为 `POST`，默认接收的 `Content-Type` 为 `application/json`。

协议声明的方法为空方法即可，不需要任何参数和范围值，默认使用方法名作为映射路径，也可以在注解中自定义。

```kotlin
@SampleSessionRequired // 整个协议范围的自定义注解
@AutoMappingContract(paths = ["/user"]) // 指定整个协议前缀
interface UseCaseContract {

    @AutoMappingContract(method = AutoMappingContract.Method.GET, beanType = UserService::class)
    fun info()

    /**
     * 测试注解方式验证 Session
     */
    @SampleSessionRequired // 单个映射范围的自定义注解
    @AutoMappingContract(beanType = UserService::class)
    fun rename()

    @AutoMappingContract(method = AutoMappingContract.Method.GET, beanType = UserService::class)
    fun submit()

    @AutoMappingContract(
        paths = ["/test1", "/test2"], // 多个路径映射
        method = AutoMappingContract.Method.GET, // 映射成 GET 请求
        beanType = UserService::class,
        beanMethod = "info" // 指定处理方法名
    )
    fun test()

    @AutoMappingContract(
        beanType = UserService::class,
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun create()
}
```

### Bean 声明 (不推荐)

即直接在响应请求的 Bean 类型上加注解解析的场景，仅推荐用于测试场景，参见 `SampleAutoMappingBeanRequestResolver` 实现。

因这种方式如果要实现生产环境的路径映射不可避免的需要侵入业务代码，如确实有需要也可以通过自定义注解加上 `AutoMappingBeanRequestResolver`
扩展点封装实现自定义需求。

### 通过配置文件映射 (未实现)

可通过前文所述方式自行实现。

### 使用 ResponseBodyAdvice

```kotlin
@RestControllerAdvice
class SampleResponseBodyWrapperAdvice(
    private val autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding, // 用于扩展判断的核心调用方法
    private val objectMapper: ObjectMapper,
) : ResponseBodyAdvice<Any> {

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return returnType.method
            ?.let {
                // 判断是否是自动映射的方法
                // TODO 也可以加入其他自定义的例如包路径之类的判断条件
                autoMappingRequestParameterTypeBinding.isSupportedMethod(it)
            }
            ?: false
    }

}
```

### 使用拦截器检查协议声明处的自定义注解

```kotlin
@Component
class SampleSessionScopeInterceptor(
    private val autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding
) : AsyncHandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            println(
                autoMappingRequestParameterTypeBinding.getAnnotation(
                    handler.method,
                    SampleSessionRequired::class.java,
                    true,
                )
            )
        }
        return true
    }
}
```

### 自定义解析用于响应请求的方法名

实现 AutoMappingContractDataConverter 扩展点

```kotlin
/**
 * 将针对 UseCase 的自动映射处理方法名统一解析到 execute() 方法上
 */
@Bean
protected fun abstractUseCaseAutoMappingContractDataConverter(): AutoMappingContractDataConverter {
    return object : AutoMappingContractDataConverter {
        override fun convert(data: AutoMappingContractData): AutoMappingContractData {
            return data.copy(
                beanMethod = "execute"
            )
        }

        override fun isSupported(data: AutoMappingContractData): Boolean {
            return AbstractUseCase::class.java.isAssignableFrom(data.beanType)
        }
    }
}
```

### 将响应请求的方法的抽象入参类型解析为具体的实现类的入参类型

实现 AutoMappingRequestParameterTypeResolver 扩展点

```kotlin
/**
 * 将 UseCase 的抽象入参类型解析为具体的实现类的入参类型
 */
@Bean
protected fun abstractUseCaseAutoMappingRequestParameterTypeResolver(): AutoMappingRequestParameterTypeResolver {
    return object : AutoMappingRequestParameterTypeResolver {
        override fun isSupported(method: Method): Boolean {
            return AbstractUseCase::class.java.isAssignableFrom(method.declaringClass)
        }

        override fun resolveParameterType(method: Method): Array<Class<*>> {
            // 需要去掉 Spring 生成的 AOP 代理类名字后缀
            val useCaseClassName =
                StringUtils.substringBefore(method.declaringClass.canonicalName, ClassUtils.CGLIB_CLASS_SEPARATOR)
            // 固定只有一个内部类参数
            return arrayOf(Class.forName("${useCaseClassName}\$InputData"))
        }
    }
}
```

### 注入自定义属性到请求参数的解析

实现 AutoMappingRequestParameterInjector 扩展点

用途：

- 通过拦截器解析出用户 Session 信息例如 userId 保存到请求上下文中，最后在参数注入扩展点中注入到具体响应请求用例的参数中

```kotlin
    /**
 * 注入 Front memberId
 */
@Bean
protected fun frontMemberIdAutoMappingRequestParameterInjector(
    autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding,
): AutoMappingRequestParameterInjector {
    return object : AutoMappingRequestParameterInjector {
        override fun inject(
            parameterObj: Any,
            parameter: MethodParameter,
            resolvedParameterType: Class<*>,
            webRequest: NativeWebRequest
        ) {
            try {
                val field = parameterObj::class.java.getDeclaredField("memberId")
                field.isAccessible = true
                field.set(parameterObj, FrontRequestContextHolder.current().memberId)
            } catch (e: NoSuchFieldException) {
                // 忽略传递未知属性的情况
                logger.info { "$parameter 声明了验证 FrontSession, 但未要求 memberId" }
            }
        }

        override fun isSupported(
            parameterObj: Any,
            parameter: MethodParameter,
            resolvedParameterType: Class<*>,
            webRequest: NativeWebRequest
        ): Boolean {
            return parameter.method
                ?.let {
                    autoMappingRequestParameterTypeBinding.getAnnotation(
                        it,
                        FrontSessionRequired::class.java,
                        true
                    ) != null
                }
                ?: false
        }
    }
}
```

### 搭配 Shiro 权限验证注解使用

1. 实现 `AnnotationResolver` 从 `AutoMappingRequestParameterTypeBinding` 中获取注解
2. 继承 `AopAllianceAnnotationsAuthorizingMethodInterceptor` 自定义实现拦截器, 使用第 2 步实现的 `AnnotationResolver`
3. 模仿 `AuthorizationAttributeSourceAdvisor` 自定义实现一个 Advisor 以增强自动映射的实际处理类, 并将第 2 步的实现作为
   Advice 注入

注意：需要保证 `NXSpringMvcAutoMappingAutoConfiguration` 在 Shiro 自动配置之前执行