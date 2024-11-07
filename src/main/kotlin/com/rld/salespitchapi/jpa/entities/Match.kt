package com.rld.salespitchapi.jpa.entities

import com.google.gson.annotations.Expose
import jakarta.persistence.*

@Entity
@Table(name="matches")
open class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    open var id: Long? = null
    @Expose
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user1")
    open var user1: User? = null
    @Expose
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user2")
    open var user2: User? = null
    @Expose
    open var accepted: Boolean = false
    companion object {
        fun of(
            user1: User,
            user2: User,
            accepted: Boolean = false
        ): Match = Match().apply {
            this.user1 = user1
            this.user2 = user2
            this.accepted = accepted
        }
    }
}


data class MatchGsonWrapper(
    @Expose val matches: List<Match>
)
