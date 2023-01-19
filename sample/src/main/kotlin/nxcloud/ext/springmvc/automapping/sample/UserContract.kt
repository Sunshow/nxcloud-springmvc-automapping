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
}

@AutoMappingBean
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

class User(
    var name: String,
    var age: Int = 0,
) {
    constructor() : this("empty")
}
