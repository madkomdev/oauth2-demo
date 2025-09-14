package com.oauth2.demo.config

import com.oauth2.demo.domain.Role
import com.oauth2.demo.domain.RoleType
import com.oauth2.demo.repository.RoleRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val roleRepository: RoleRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        println("Initializing default roles...")
        
        // Initialize roles if they don't exist
        RoleType.values().forEach { roleType ->
            if (roleRepository.findByName(roleType).isEmpty) {
                val role = Role(
                    name = roleType,
                    description = "Default ${roleType.displayName} role"
                )
                roleRepository.save(role)
                println("Created role: ${roleType.displayName}")
            }
        }
        
        println("Role initialization completed.")
    }
}
