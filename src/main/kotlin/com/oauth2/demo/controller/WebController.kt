package com.oauth2.demo.controller

import com.oauth2.demo.service.SessionService
import com.oauth2.demo.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class WebController(
    private val userService: UserService,
    private val sessionService: SessionService
) {

    @GetMapping("/")
    fun index(
        @AuthenticationPrincipal user: OidcUser?,
        @RequestParam(required = false) error: String?,
        model: Model
    ): String {
        model.addAttribute("isAuthenticated", user != null)
        model.addAttribute("user", user)
        model.addAttribute("error", error)
        
        if (user != null) {
            model.addAttribute("username", user.preferredUsername)
            model.addAttribute("email", user.email)
        }
        
        return "index"
    }

    @GetMapping("/dashboard")
    fun dashboard(
        @AuthenticationPrincipal user: OidcUser,
        @RegisteredOAuth2AuthorizedClient("keycloak") authorizedClient: OAuth2AuthorizedClient,
        model: Model
    ): String {
        // Sync user from OIDC user info
        val localUser = userService.syncUserFromOidcUser(user)
        
        // Get access token for API calls
        val accessToken = authorizedClient.accessToken.tokenValue
        
        // Extract user roles
        val roles = user.authorities.map { it.authority }
        
        model.addAttribute("user", user)
        model.addAttribute("localUser", localUser)
        model.addAttribute("username", user.preferredUsername)
        model.addAttribute("email", user.email)
        model.addAttribute("firstName", user.givenName)
        model.addAttribute("lastName", user.familyName)
        model.addAttribute("roles", roles)
        model.addAttribute("accessToken", accessToken)
        model.addAttribute("isAdmin", roles.contains("ROLE_ADMIN"))
        model.addAttribute("isManager", roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN"))
        
        return "dashboard"
    }

    @GetMapping("/profile")
    fun profile(
        @AuthenticationPrincipal user: OidcUser,
        @RegisteredOAuth2AuthorizedClient("keycloak") authorizedClient: OAuth2AuthorizedClient,
        model: Model
    ): String {
        val localUser = userService.syncUserFromOidcUser(user)
        val accessToken = authorizedClient.accessToken.tokenValue
        val refreshToken = authorizedClient.refreshToken?.tokenValue
        val roles = user.authorities.map { it.authority }
        
        model.addAttribute("user", user)
        model.addAttribute("localUser", localUser)
        model.addAttribute("username", user.preferredUsername)
        model.addAttribute("email", user.email)
        model.addAttribute("firstName", user.givenName)
        model.addAttribute("lastName", user.familyName)
        model.addAttribute("roles", roles)
        model.addAttribute("accessToken", accessToken)
        model.addAttribute("refreshToken", refreshToken)
        model.addAttribute("tokenExpiry", authorizedClient.accessToken.expiresAt)
        
        return "profile"
    }

    @GetMapping("/admin")
    fun admin(
        @AuthenticationPrincipal user: OidcUser,
        @RegisteredOAuth2AuthorizedClient("keycloak") authorizedClient: OAuth2AuthorizedClient,
        model: Model
    ): String {
        val roles = user.authorities.map { it.authority }
        
        // Check if user has admin role
        if (!roles.contains("ROLE_ADMIN")) {
            return "redirect:/dashboard?error=access_denied"
        }
        
        val allUsers = userService.getAllUsers()
        val accessToken = authorizedClient.accessToken.tokenValue
        
        model.addAttribute("user", user)
        model.addAttribute("username", user.preferredUsername)
        model.addAttribute("allUsers", allUsers)
        model.addAttribute("accessToken", accessToken)
        model.addAttribute("userCount", allUsers.size)
        model.addAttribute("roleDistribution", allUsers.flatMap { it.roles }.groupBy { it.name }.mapValues { it.value.size })
        
        return "admin"
    }
}
