package com.oauth2.demo.controller

import com.oauth2.demo.domain.RoleType
import com.oauth2.demo.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/manager")
class ManagerController(
    private val userService: UserService
) {

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun getAllUsers(): List<Map<String, Any?>> {
        return userService.getAllUsers().map { user ->
            mapOf(
                "id" to user.keycloakId,
                "username" to user.username,
                "email" to user.email,
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "enabled" to user.enabled,
                "roles" to user.roles.map { it.name },
                "createdAt" to user.createdAt
            )
        }
    }

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun assignRole(
        @PathVariable userId: String,
        @RequestBody roleRequest: RoleAssignmentRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): Map<String, String> {
        userService.assignRole(userId, roleRequest.role)
        return mapOf(
            "message" to "Role ${roleRequest.role} assigned to user $userId",
            "assignedBy" to jwt.getClaimAsString("preferred_username")
        )
    }

    @DeleteMapping("/users/{userId}/roles")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun removeRole(
        @PathVariable userId: String,
        @RequestBody roleRequest: RoleAssignmentRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): Map<String, String> {
        userService.removeRole(userId, roleRequest.role)
        return mapOf(
            "message" to "Role ${roleRequest.role} removed from user $userId",
            "removedBy" to jwt.getClaimAsString("preferred_username")
        )
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun getManagerReports(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        val totalUsers = userService.getAllUsers().size
        val usersByRole = userService.getAllUsers()
            .flatMap { it.roles }
            .groupBy { it.name }
            .mapValues { it.value.size }

        return mapOf(
            "totalUsers" to totalUsers,
            "usersByRole" to usersByRole,
            "generatedBy" to jwt.getClaimAsString("preferred_username"),
            "timestamp" to LocalDateTime.now()
        )
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun getManagerDashboard(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        val totalUsers = userService.getAllUsers().size
        
        return mapOf(
            "welcome" to "Manager Dashboard - ${jwt.getClaimAsString("preferred_username")}",
            "totalUsers" to totalUsers,
            "accessibleEndpoints" to listOf(
                "/api/manager/users",
                "/api/manager/reports",
                "/api/manager/dashboard",
                "/api/user/*"
            )
        )
    }

    data class RoleAssignmentRequest(val role: RoleType)
}
