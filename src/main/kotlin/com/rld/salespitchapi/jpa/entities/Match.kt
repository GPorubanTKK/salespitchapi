package com.rld.salespitchapi.jpa.entities

import com.google.gson.annotations.Expose
import jakarta.persistence.*

@Entity
@Table(name="Matches")
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
    open var accepted: Boolean = false
}

data class MatchGson(
    @Expose val user1: User?,
    @Expose val user2: User?
)


data class MatchGsonWrapper(
    @Expose val matches: List<MatchGson>
)
