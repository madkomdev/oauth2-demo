package com.oauth2.demo.controller

import com.oauth2.demo.repository.RoleRepository
import com.oauth2.demo.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val userService: UserService,
    private val roleRepository: RoleRepository
) {

    @GetMapping("/system/info")
    @PreAuthorize("hasRole('ADMIN')")
    fun getSystemInfo(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        val users = userService.getAllUsers()
        val totalUsers = users.size
        val activeUsers = users.count { it.enabled }
        val roleDistribution = users.flatMap { it.roles }
            .groupBy { it.name }
            .mapValues { it.value.size }

        return mapOf(
            "totalUsers" to totalUsers,
            "activeUsers" to activeUsers,
            "inactiveUsers" to (totalUsers - activeUsers),
            "roleDistribution" to roleDistribution,
            "systemAdmin" to jwt.getClaimAsString("preferred_username"),
            "timestamp" to LocalDateTime.now()
        )
    }

    @PostMapping("/users/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    fun enableUser(@PathVariable userId: String, @AuthenticationPrincipal jwt: Jwt): Map<String, String> {
        // Note: In a real implementation, you'd update the user's enabled status
        return mapOf(
            "message" to "User $userId has been enabled",
            "adminUser" to jwt.getClaimAsString("preferred_username")
        )
    }

    @PostMapping("/users/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    fun disableUser(@PathVariable userId: String, @AuthenticationPrincipal jwt: Jwt): Map<String, String> {
        // Note: In a real implementation, you'd update the user's enabled status
        return mapOf(
            "message" to "User $userId has been disabled",
            "adminUser" to jwt.getClaimAsString("preferred_username")
        )
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllRoles(): List<Map<String, Any?>> {
        return roleRepository.findAll().map { role ->
            mapOf(
                "id" to role.id,
                "name" to role.name,
                "description" to role.description,
                "userCount" to role.users.size
            )
        }
    }

    @GetMapping("/audit/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    fun getActiveSessions(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        // In a real implementation, you'd track active sessions
        return mapOf(
            "message" to "Active sessions endpoint - would show all active user sessions",
            "adminUser" to jwt.getClaimAsString("preferred_username"),
            "timestamp" to LocalDateTime.now()
        )
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminDashboard(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        val users = userService.getAllUsers()
        val totalUsers = users.size
        val roleDistribution = users.flatMap { it.roles }
            .groupBy { it.name }
            .mapValues { it.value.size }

        return mapOf(
            "welcome" to "Admin Dashboard - ${jwt.getClaimAsString("preferred_username")}",
            "totalUsers" to totalUsers,
            "roleDistribution" to roleDistribution,
            "accessibleEndpoints" to listOf(
                "/api/admin/*",
                "/api/manager/*",
                "/api/user/*"
            )
        )
    }
}
