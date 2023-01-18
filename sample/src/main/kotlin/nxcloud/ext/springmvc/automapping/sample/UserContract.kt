package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingBean
import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingContract
import org.springframework.stereotype.Component

// 按协议解析
@AutoMappingContract(paths = ["/user"])
interface UseCaseContract {

    @AutoMappingContract(method = AutoMappingContract.Method.GET, beanType = UserService::class)
    fun info()

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

data class User(
    val name: String,
    val age: Int = 0,
)
