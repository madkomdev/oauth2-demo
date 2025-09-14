package com.oauth2.demo.domain

import jakarta.persistence.*

@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    val name: RoleType,
    
    val description: String? = null,
    
    @ManyToMany(mappedBy = "roles")
    val users: MutableSet<User> = mutableSetOf()
) {
    constructor() : this(name = RoleType.USER)
}

enum class RoleType(val displayName: String) {
    ADMIN("Administrator"),
    MANAGER("Manager"),
    USER("Regular User"),
    GUEST("Guest User")
}
