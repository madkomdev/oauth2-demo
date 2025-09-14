package com.oauth2.demo.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "app_users")
data class User(
    @Id
    val keycloakId: String,
    
    @Column(unique = true, nullable = false)
    val username: String,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    val firstName: String? = null,
    val lastName: String? = null,
    val enabled: Boolean = true,
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: MutableSet<Role> = mutableSetOf(),
    
    @CreationTimestamp
    val createdAt: LocalDateTime? = null,
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime? = null
) {
    constructor() : this("", "", "")
}
