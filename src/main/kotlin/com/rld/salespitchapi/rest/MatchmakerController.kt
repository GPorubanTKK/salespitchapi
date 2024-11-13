package com.rld.salespitchapi.rest

import com.rld.salespitchapi.MediaResource
import com.rld.salespitchapi.MultipartResponse
import com.rld.salespitchapi.baseMapping
import com.rld.salespitchapi.contentType
import com.rld.salespitchapi.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("$baseMapping/matchmake")
class MatchmakerController {
    @Autowired private lateinit var userService: UserService

    @PostMapping("/getnextuser", produces = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun getNextUser(@RequestParam email: String, @RequestParam index: Int): MultipartResponse {
        require(userService.isAuthed(email))
        return ResponseEntity.ok(userService.getUser(index))
    }

    @GetMapping("/getuservideo/{requester}/{target}")
    fun getUserVideo(
        @PathVariable requester: String,
        @PathVariable target: String
    ): MediaResource {
        require(userService.isAuthed(requester)) { "Requester $requester is not authed" }
        val video = userService.getVideo(target)
        return ResponseEntity.ok()
            .contentType("video/mp4")
            .contentLength(video.size.toLong())
            .body(video)
    }
}

