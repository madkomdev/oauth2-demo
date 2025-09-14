package com.oauth2.demo.repository

import com.example.auth_demo.domain.Role
import com.example.auth_demo.domain.RoleType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByName(name: RoleType): Optional<Role>
}
