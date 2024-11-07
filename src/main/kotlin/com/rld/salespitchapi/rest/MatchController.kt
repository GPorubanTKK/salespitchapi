package com.rld.salespitchapi.rest

import com.rld.salespitchapi.JsonResponse
import com.rld.salespitchapi.baseMapping
import com.rld.salespitchapi.gson
import com.rld.salespitchapi.services.MatchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$baseMapping/matches")
class MatchController {
    @Autowired private lateinit var matchService: MatchService

    @PostMapping("/matchwith")
    fun matchUsers(
        @RequestParam from: String,
        @RequestParam to: String
    ) = matchService.matchWith(from, to)

    @PostMapping("/deletematch")
    fun removeMatch(
        @RequestParam from: String,
        @RequestParam to: String
    ) = matchService.removeMatch(from, to)

    @PostMapping("/getmatches")
    fun getMatches(@RequestParam email: String): JsonResponse =
        ResponseEntity.ok(gson.toJson(matchService.getMatchesForUser(email)))
}