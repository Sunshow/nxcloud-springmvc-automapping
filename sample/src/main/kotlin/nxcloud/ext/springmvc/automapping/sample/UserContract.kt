package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingBean
import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingContract
import org.springframework.stereotype.Component

// 按协议解析
@SampleSessionRequired
@AutoMappingContract(paths = ["/user"])
interface UseCaseContract {

    @AutoMappingContract(method = AutoMappingContract.Method.GET, beanType = UserService::class)
    fun info()

    /**
     * 测试注解方式验证 Session
     */
    @SampleSessionRequired
    @AutoMappingContract(beanType = UserService::class)
    fun rename()

    @AutoMappingContract(method = AutoMappingContract.Method.GET, beanType = UserService::class)
    fun submit()

    @AutoMappingContract(
        paths = ["/test1", "/test2"],
        method = AutoMappingContract.Method.GET,
        beanType = UserService::class,
        beanMethod = "info"
    )
    fun test()

    @AutoMappingContract(
        beanType = UserService::class,
        consumes = ["application/x-www-form-urlencoded"]
    )
    fun create()

    @SampleSessionRequired
    @AutoMappingContract(
        paths = ["/rename1"],
        beanType = UserService::class,
        beanMethod = "rename",
        consumes = ["application/x-www-form-urlencoded"],
    )
    fun testSetStringToInt()

    @AutoMappingContract(
        method = AutoMappingContract.Method.GET,
        paths = ["/profile/{name}"],
        beanType = UserService::class,
        beanMethod = "create",
        consumes = []
    )
    fun testPath()

    @AutoMappingContract(
        paths = ["/update/{name}"],
        beanType = UserService::class,
        beanMethod = "update",
    )
    fun testPathWithBody()

    @AutoMappingContract(
        method = AutoMappingContract.Method.GET,
        paths = ["/rename/{name}"],
        consumes = [],
        beanType = UserService::class,
        beanMethod = "rename",
    )
    fun testPathInObj()

    @AutoMappingContract(
        paths = ["/rename2/{name}"],
        beanType = UserService::class,
        beanMethod = "rename",
    )
    fun testPathInBody()

    @AutoMappingContract(
        consumes = [],
        beanType = UserService::class,
        beanMethod = "rename",
    )
    fun rename3()
}

@AutoMappingBean
interface UserService {

    fun info(): User

    fun rename(user: User): User

    fun submit(user: User)

    fun create(name: String, age: Int): User

    fun update(name: String, user: User): User
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
            name = "rename: ${user.name}",
            age = user.age,
        )
    }

    override fun create(name: String, age: Int): User {
        return User(
            name = name,
            age = age,
        )
    }

    override fun update(name: String, user: User): User {
        return User(
            name = "update: $name, ${user.name}",
            age = user.age,
        )
    }

    override fun submit(user: User) {
        println(user)
    }
}

class User(
    var name: String? = null,
    var age: Int = 0,
) {
    constructor() : this("empty")
}
