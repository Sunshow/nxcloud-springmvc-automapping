package nxcloud.ext.springmvc.automapping.sample

import org.springframework.stereotype.Component

interface UserContract {
    fun getUserBody(): User
}

@Component
class UserContractImpl : UserContract {
    override fun getUserBody(): User {
        return User(
            name = "Hello"
        )
    }
}

data class User(
    val name: String,
)
