package com.rld.salespitchapi.services

import com.rld.salespitchapi.jpa.entities.Match
import com.rld.salespitchapi.jpa.repositories.MatchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service class MatchService {
    @Autowired private lateinit var matchRepository: MatchRepository
    @Autowired private lateinit var userService: UserService

    fun getMatches(email: String): List<Match> = matchRepository.getSuccessfulMatchesByUser(email)
    fun getMatch(requestor: String, target: String): Match? = matchRepository.getMatchByUser1AndUser2(requestor, target)

    fun matchWith(requestor: String, target: String) {
        val matchBack = getMatch(target, requestor)
        if(matchBack != null) {
            matchBack.accepted = true
            matchRepository.save(matchBack)
        } else matchRepository.save(Match.of(
            userService.getUser(requestor),
            userService.getUser(target)
        ))
    }
}