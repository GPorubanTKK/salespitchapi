package com.rld.salespitchapi.jpa.entities

import com.google.gson.annotations.Expose
import jakarta.persistence.*

@Entity
@Table(name="Users")
open class User {
    @Expose @Id open var email: String = ""
    open var password: String? = null
    @Expose open var firstname: String? = null
    @Expose open var lastname: String? = null
    @Expose open var phoneNumber: String? = null
    open var profilePicturePath: String? = null
    open var videoUri: String? = null
    @OneToMany(cascade = [CascadeType.ALL])
    open var matches: MutableList<Match> = mutableListOf()
}