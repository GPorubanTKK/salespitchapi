package com.rld.salespitchapi.jpa.entities

import jakarta.persistence.*

@Entity
@Table(name = "password_resets")
open class ResetRequest {
    @Id @GeneratedValue open var resetId: Long? = null
    @OneToOne open var user: User? = null
    open var initTimestamp: String? = null
    open var grantedPeriod: Int = 0
    open var assocCode: String? = null
    companion object {
        fun of(user: User, timeStamp: String, period: Int, resetCode: String) = ResetRequest().apply {
            this.user = user
            this.initTimestamp = timeStamp
            this.grantedPeriod = period
            this.assocCode = resetCode
        }
    }
}