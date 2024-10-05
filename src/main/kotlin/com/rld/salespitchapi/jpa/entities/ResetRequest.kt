package com.rld.salespitchapi.jpa.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "password_resets")
open class ResetRequest {
    @Id @GeneratedValue open var resetId: Long? = null
    @OneToOne open var user: User? = null
    open var initTimestamp: String? = null
    open var grantedPeriod: Int = 0
    open var assocCode: String? = null
}