package com.oauth2.demo.service

import com.oauth2.demo.domain.RoleType
import com.oauth2.demo.domain.User
import com.oauth2.demo.repository.RoleRepository
import com.oauth2.demo.repository.UserRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {

    fun syncUserFromJwt(jwt: Jwt): User {
        val keycloakId = jwt.subject
        val username = jwt.getClaimAsString("preferred_username")
        val email = jwt.getClaimAsString("email")
        val firstName = jwt.getClaimAsString("given_name")
        val lastName = jwt.getClaimAsString("family_name")

        var user = userRepository.findById(keycloakId).orElse(null)
        
        if (user == null) {
            user = User(
                keycloakId = keycloakId,
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName
            )
            // Assign default USER role
            val userRole = roleRepository.findByName(RoleType.USER).orElseThrow()
            user.roles.add(userRole)
        } else {
            // Update existing user info
            user = user.copy(
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName
            )
        }

        return userRepository.save(user)
    }

    fun syncUserFromOidcUser(oidcUser: OidcUser): User {
        val keycloakId = oidcUser.subject
        val username = oidcUser.preferredUsername
        val email = oidcUser.email
        val firstName = oidcUser.givenName
        val lastName = oidcUser.familyName

        var user = userRepository.findById(keycloakId).orElse(null)
        
        if (user == null) {
            user = User(
                keycloakId = keycloakId,
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName
            )
            // Assign default USER role
            val userRole = roleRepository.findByName(RoleType.USER).orElseThrow()
            user.roles.add(userRole)
        } else {
            // Update existing user info
            user = user.copy(
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName
            )
        }

        return userRepository.save(user)
    }

    fun getCurrentUser(jwt: Jwt): User? {
        return userRepository.findById(jwt.subject).orElse(null)
    }

    fun assignRole(userId: String, roleType: RoleType) {
        val user = userRepository.findById(userId).orElseThrow()
        val role = roleRepository.findByName(roleType).orElseThrow()
        user.roles.add(role)
        userRepository.save(user)
    }

    fun removeRole(userId: String, roleType: RoleType) {
        val user = userRepository.findById(userId).orElseThrow()
        val role = roleRepository.findByName(roleType).orElseThrow()
        user.roles.remove(role)
        userRepository.save(user)
    }

    fun getAllUsers(): List<User> = userRepository.findAll()

    fun getUserById(id: String): User? = userRepository.findById(id).orElse(null)
}
