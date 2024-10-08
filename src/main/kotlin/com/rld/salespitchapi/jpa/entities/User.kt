package com.rld.salespitchapi.jpa.entities

import com.google.gson.annotations.Expose
import jakarta.persistence.*
import java.io.File

@Entity
@Table(name="users")
open class User {
    @Expose @Id open var email: String = ""
    open var password: String? = null
    @Expose open var firstName: String? = null
    @Expose open var lastName: String? = null
    @Expose open var phoneNumber: String? = null
    @OneToMany(cascade = [CascadeType.ALL])
    open var matches: MutableList<Match> = mutableListOf()
    fun photoPath(base: String): File = File("$base/$email/photo/profile.jpg")
    fun videoPath(base: String): File = File("$base/$email/video/profile.mp4")
    companion object {
        fun of(
            firstName: String,
            lastName: String,
            email: String,
            password: String,
            phoneNumber: String
        ): User = User().apply {
            this.firstName = firstName
            this.lastName = lastName
            this.email = email
            this.password = password
            this.phoneNumber = phoneNumber
        }
    }
}