package nxcloud.ext.springmvc.automapping.sample

import org.springframework.stereotype.Component

interface UserContract {

    fun info(): User

    fun rename(user: User): User

    fun submit(user: User)
}

@Component
class UserContractImpl : UserContract {
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

    override fun submit(user: User) {
        println(user)
    }
}

data class User(
    val name: String,
)
