package com.oauth2.demo.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val authorities = extractAuthorities(jwt)
        return JwtAuthenticationToken(jwt, authorities)
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()
        
        // Extract realm roles from Keycloak JWT
        val realmAccess = jwt.getClaimAsMap("realm_access")
        val realmRoles = realmAccess?.get("roles") as? List<*>
        realmRoles?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_${role.toString().uppercase()}"))
        }
        
        // Extract resource access roles (client-specific roles)
        val resourceAccess = jwt.getClaimAsMap("resource_access")
        resourceAccess?.forEach { (clientId, clientRoles) ->
            val roles = (clientRoles as? Map<*, *>)?.get("roles") as? List<*>
            roles?.forEach { role ->
                authorities.add(SimpleGrantedAuthority("ROLE_${role.toString().uppercase()}"))
            }
        }
        
        // Extract groups (if configured in Keycloak)
        val groups = jwt.getClaimAsStringList("groups")
        groups?.forEach { group ->
            authorities.add(SimpleGrantedAuthority("GROUP_${group.uppercase()}"))
        }
        
        return authorities
    }
}
