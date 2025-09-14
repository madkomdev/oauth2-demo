package com.oauth2.demo.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class SessionService {

    data class SessionInfo(
        val userId: String,
        val username: String,
        val email: String,
        val roles: List<String>,
        val issuedAt: LocalDateTime,
        val expiresAt: LocalDateTime,
        val issuer: String
    )

    fun getCurrentSession(): SessionInfo? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            val jwt = authentication.token
            return createSessionInfo(jwt)
        }
        return null
    }

    fun getCurrentSession(jwt: Jwt): SessionInfo {
        return createSessionInfo(jwt)
    }

    fun isSessionValid(): Boolean {
        val session = getCurrentSession()
        return session != null && session.expiresAt.isAfter(LocalDateTime.now())
    }

    fun isSessionValid(jwt: Jwt): Boolean {
        val session = createSessionInfo(jwt)
        return session.expiresAt.isAfter(LocalDateTime.now())
    }

    fun getRemainingSessionTime(): Long? {
        val session = getCurrentSession() ?: return null
        val now = LocalDateTime.now()
        return if (session.expiresAt.isAfter(now)) {
            Duration.between(now, session.expiresAt).seconds
        } else {
            0L
        }
    }

    fun getRemainingSessionTime(jwt: Jwt): Long {
        val session = createSessionInfo(jwt)
        val now = LocalDateTime.now()
        return if (session.expiresAt.isAfter(now)) {
            Duration.between(now, session.expiresAt).seconds
        } else {
            0L
        }
    }

    private fun createSessionInfo(jwt: Jwt): SessionInfo {
        return SessionInfo(
            userId = jwt.subject,
            username = jwt.getClaimAsString("preferred_username"),
            email = jwt.getClaimAsString("email"),
            roles = extractRoles(jwt),
            issuedAt = toLocalDateTime(jwt.issuedAt),
            expiresAt = toLocalDateTime(jwt.expiresAt),
            issuer = jwt.issuer.toString()
        )
    }

    private fun extractRoles(jwt: Jwt): List<String> {
        val roles = mutableListOf<String>()
        
        // Extract realm roles
        val realmAccess = jwt.getClaimAsMap("realm_access")
        val realmRoles = realmAccess?.get("roles") as? List<*>
        realmRoles?.forEach { role ->
            roles.add(role.toString())
        }
        
        return roles
    }

    private fun toLocalDateTime(instant: Instant?): LocalDateTime {
        return instant?.atZone(ZoneId.systemDefault())?.toLocalDateTime() ?: LocalDateTime.now()
    }
}
