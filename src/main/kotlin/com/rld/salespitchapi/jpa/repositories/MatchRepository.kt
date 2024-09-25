package com.rld.salespitchapi.jpa.repositories

import com.rld.salespitchapi.jpa.entities.Match
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface MatchRepository : CrudRepository<Match, Long> {
    @Query("SELECT match from Match match WHERE (match.user1.email = :email OR match.user2.email = :email) AND match.accepted = true")
    fun getSuccessfulMatchesByUser(@Param("email") email: String): List<Match>?

    @Query("SELECT match from Match match WHERE match.user1.email = :email1 AND match.user2.email = :email2")
    fun getMatchByUser1AndUser2(
        @Param("email1") user1: String,
        @Param("email2") user2: String
    ): Match?
}