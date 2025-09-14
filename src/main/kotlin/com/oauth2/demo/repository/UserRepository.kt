package com.oauth2.demo.repository

import com.example.auth_demo.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByUsername(username: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.keycloakId = ?1")
    fun findByKeycloakIdWithRoles(keycloakId: String): Optional<User>
}
