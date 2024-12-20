package com.rld.salespitchapi.services

import com.rld.salespitchapi.jpa.entities.Match
import com.rld.salespitchapi.jpa.entities.MatchGsonWrapper
import com.rld.salespitchapi.jpa.entities.MatchInfoWrapper
import com.rld.salespitchapi.jpa.repositories.MatchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service class MatchService {
    @Autowired private lateinit var matchRepository: MatchRepository
    @Autowired private lateinit var userService: UserService
    @Autowired private lateinit var notificationService: MessagingService

    fun getMatchesForUser(email: String): MatchGsonWrapper {
        require(userService.isAuthed(email))
        val raw = matchRepository.getMatchesByUser(email)
        return MatchGsonWrapper(raw.map { (u1, u2, accepted) ->
            MatchInfoWrapper(userService.getUser(u1!!.email), userService.getUser(u2!!.email), accepted)
        })
    }

    fun matchWith(requestor: String, target: String) {
        require(userService.isAuthed(requestor) && userService.exists(target))
        val matchBack = getMatch(target, requestor)
        if(matchBack != null) {
            matchBack.accepted = true
            matchRepository.save(matchBack)
            notificationService.notifyUsers("System Test", listOf(requestor, target))
        } else matchRepository.save(Match.of(
            userService.getUser(requestor),
            userService.getUser(target)
        ))
        notificationService.notifyUsers("System Test", listOf(target))
    }

    fun removeMatch(requestor: String, target: String) {
        require(userService.isAuthed(requestor))
        val match = matchRepository.getMatchOfUser1AndUser2(requestor, target)
        if(match != null) matchRepository.delete(match)
    }

    private fun getMatch(requestor: String, target: String): Match? = matchRepository.getMatchByUser1AndUser2(requestor, target)
}