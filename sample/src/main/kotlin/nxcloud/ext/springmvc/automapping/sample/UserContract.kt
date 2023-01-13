package nxcloud.ext.springmvc.automapping.sample

import org.springframework.stereotype.Component

interface UserContract {
    fun getUserBody(user: User): User
}

@Component
class UserContractImpl : UserContract {
    override fun getUserBody(user: User): User {
        return user.copy(
            name = "Hello, ${user.name}"
        )
    }
}

data class User(
    val name: String,
)
