package com.oauth2.demo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public")
class PublicController {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP", "message" to "Public endpoint accessible to all")
    }

    @GetMapping("/info")
    fun info(): Map<String, Any> {
        return mapOf(
            "application" to "Auth Demo",
            "version" to "1.0.0",
            "description" to "OAuth2 + Keycloak Authentication Demo with Spring MVC",
            "authentication" to "OAuth2 with Keycloak",
            "stack" to "Spring Boot + Spring MVC"
        )
    }
}
