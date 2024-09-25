package com.rld.salespitchapi.jpa.repositories

import com.rld.salespitchapi.jpa.entities.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface UserRepository : CrudRepository<User, String> {
    @Query(value = "SELECT user from User user WHERE user.email = :email")
    fun getUserByEmail(@Param("email") email: String): User?

    @Query(value = "SELECT * FROM users LIMIT 1 OFFSET :i", nativeQuery = true)
    fun getUserByIndex(@Param("i") index: Int): User?
}