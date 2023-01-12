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
@RequestMapping(value = "/api/user/getUserUseCase", method = RequestMethod.POST)
@ResponseBody
GetUserUseCase.Output getUserUseCase(@RequestBody GetUserUseCase.Input input);
```