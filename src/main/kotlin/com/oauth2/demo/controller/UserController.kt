package com.oauth2.demo.controller

import com.oauth2.demo.service.SessionService
import com.oauth2.demo.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val sessionService: SessionService
) {

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    fun getUserProfile(@AuthenticationPrincipal jwt: Jwt): Map<String, Any?> {
        val user = userService.getCurrentUser(jwt)
        return mapOf(
            "keycloakId" to jwt.subject,
            "username" to jwt.getClaimAsString("preferred_username"),
            "email" to jwt.getClaimAsString("email"),
            "firstName" to jwt.getClaimAsString("given_name"),
            "lastName" to jwt.getClaimAsString("family_name"),
            "roles" to (user?.roles?.map { it.name } ?: emptyList()),
            "localUser" to user
        )
    }

    @GetMapping("/session")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    fun getSessionInfo(@AuthenticationPrincipal jwt: Jwt): SessionService.SessionInfo {
        return sessionService.getCurrentSession(jwt)
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    fun getUserDashboard(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        val session = sessionService.getCurrentSession(jwt)
        val remainingTime = sessionService.getRemainingSessionTime(jwt)
        
        return mapOf(
            "welcome" to "Welcome to your dashboard, ${jwt.getClaimAsString("preferred_username")}!",
            "sessionInfo" to session,
            "sessionRemainingSeconds" to remainingTime,
            "accessibleEndpoints" to listOf(
                "/api/user/profile",
                "/api/user/session",
                "/api/user/dashboard"
            )
        )
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    fun syncUserProfile(@AuthenticationPrincipal jwt: Jwt): Map<String, Any?> {
        val user = userService.syncUserFromJwt(jwt)
        return mapOf(
            "message" to "User profile synchronized",
            "user" to user
        )
    }
}
