package com.rld.salespitchapi.rest

import com.rld.salespitchapi.MultipartResponse
import com.rld.salespitchapi.baseMapping
import com.rld.salespitchapi.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("$baseMapping/users")
class AccountController {
    @Autowired private lateinit var userService: UserService

    @PostMapping("/delete")
    fun deleteAccount(
        @RequestParam email: String,
        @RequestParam password: String
    ) {
        require(
            userService.isAuthed(email) &&
            run { try { userService.authenticateUser(email, password); true } catch(e: Exception) { false } }
        )
        userService.deleteUser(email)
    }

    @PostMapping("/auth")
    fun loginAccount(
        @RequestParam email: String,
        @RequestParam password: String
    ): MultipartResponse = ResponseEntity.ok(
        userService.authenticateUser(email, password)
    )

    @PostMapping("/create")
    fun createAccount(
        @RequestPart email: String,
        @RequestPart password: String,
        @RequestPart firstname: String,
        @RequestPart lastname: String,
        @RequestPart phone: String,
        @RequestPart picture: MultipartFile,
        @RequestPart video: MultipartFile
    ) = userService.createUser(
        email = email,
        password = password,
        firstname = firstname,
        lastname = lastname,
        phoneNumber = phone,
        pictureBytes = picture.bytes,
        videoBytes = video.bytes
    )
}