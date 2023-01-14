# nxcloud-springmvc-automapping

自动将指定注解的类方法注册到 SpringMVC RequestMapping 用于 REST 访问

## 设计目标

- 通过指定注解自动扫描用于注册的类和需要注册的方法
- 支持自定义注册路径，也可以使用默认路径例如：/api/类名/方法名
- 通过扩展支持自定义实现注册规则
- 保持对 SpringMVC 兼容，例如支持用传统方式对生成的返回值做封装，支持自定义拦截器等
- 支持 Context 扩展以实现例如从 Session 读取 userId 等属性填充到入参等功能

## 示例

以 CLEAN 架构实现的 UseCase 为例，每个 UseCase 有各自的 Input 和 Output，
则可以实现自动注册路径并将 Input 作为参数，Output 作为返回值。

```java
package nxcloud.demo.domain.usecase.user;

public class GetUserUseCase {

    public Output execute(Input input) {
        return new Output();
    }

    static class Input {
        private String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    static class Output {
        private String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
```

可自动为此 UseCase 注册如下 RequestMapping：

```java
@RequestMapping(value = "/api/user/getUser", method = RequestMethod.POST)
@ResponseBody
GetUserUseCase.Output getUser(@RequestBody GetUserUseCase.Input input);
```

示例代码使用 Kotlin 编写，Java 可自行转换

### 1. 定义一个注解用于标记需要自动映射的用例 (也可以基于父类映射, 但实际情况不应该是所有用例都映射)

```kotlin
/**
 * 默认用于标识自动映射的注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AutoMappingUseCase
```

### 2. 在启动类上添加注解 @EnableAutoMapping

```kotlin
@NXEnableSpringMvcAutoMapping(
    basePackages = ["nxcloud.sample.api.domain"],
    autoMappingAnnotations = [AutoMappingUseCase::class]
)
@SpringBootApplication
class NXCloudSampleApiApplication

fun main(args: Array<String>) {
    runApplication<NXCloudSampleApiApplication>(*args)
}
```

### 3. 实现自定义映射规则

```kotlin
@Configuration
class CommonAutoMappingConfig {

    @Bean
    protected fun domainUseCaseAutoMappingRequestResolver(): AutoMappingRequestResolver {
        return object : AutoMappingRequestResolver {

            private var methodParameterMapping: MutableMap<Method, Class<*>> = mutableMapOf()

            private val options = RequestMappingInfo.BuilderConfiguration()
                .apply {
                    patternParser = PathPatternParser()
                }

            override fun resolveMapping(bean: Any, beanName: String): List<RequestResolvedInfo> {
                if (!AbstractUseCase::class.java.isAssignableFrom(bean::class.java)) {
                    return emptyList()
                }
                val method = bean.javaClass.methods.first {
                    it.name == "execute" && it.parameterCount == 1
                }

                val module = bean.javaClass.packageName
                    .substringAfter("nxcloud.sample.api.domain.")
                    .substringBefore(".")

                // 需要把抽象类的抽象参数映射为当前 UseCase 的实际参数
                methodParameterMapping[method] =
                    Class.forName("${AopUtils.getTargetClass(bean).canonicalName}\$InputData")

                return listOf(
                    RequestResolvedInfo(
                        RequestMappingInfo
                            .paths("/api/${module}/${beanName.substringBeforeLast("UseCase")}")
                            .consumes(MediaType.APPLICATION_JSON_VALUE)
                            .methods(RequestMethod.POST)
                            .options(options)
                            .build(),
                        bean,
                        method
                    ),
                )
            }

            override fun resolveParameterClass(parameter: MethodParameter): Class<*> {
                return methodParameterMapping[parameter.method!!]!!
            }

            override fun isSupportedMapping(bean: Any, beanName: String): Boolean {
                return AbstractUseCase::class.java.isAssignableFrom(bean::class.java)
            }

            override fun isSupportedParameterClass(parameter: MethodParameter): Boolean {
                return parameter.method
                    ?.let {
                        methodParameterMapping.containsKey(it)
                    }
                    ?: false
            }
        }
    }
}
```