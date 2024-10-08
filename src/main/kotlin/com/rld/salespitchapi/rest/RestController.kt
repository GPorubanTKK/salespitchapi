package com.rld.salespitchapi.rest

import com.google.gson.GsonBuilder
import com.rld.salespitchapi.jpa.entities.*
import com.rld.salespitchapi.services.MatchService
import com.rld.salespitchapi.services.PasswordResetService
import com.rld.salespitchapi.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.io.File

@RestController
@RequestMapping("/app/api")
class SalesPitchController {

    @Autowired lateinit var userService: UserService
    @Autowired lateinit var resetService: PasswordResetService
    @Autowired lateinit var matchService: MatchService

    /**
     * Attempts to authenticate a user.  returns the user from the database as json and
     *
     * @author Gedeon Poruban
     * @since 0.0.1
     * */

    @PostMapping("/login")
    @ResponseBody
    fun attemptLogin(
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<LinkedMultiValueMap<String, Any>> = ResponseEntity.ok()
        .body(userService.authenticateUser(email, password))

    /**
     * Creates a new user account as saves info to the database
     *
     * @author Gedeon Poruban
     * @since 0.0.1
     * */

    @PostMapping("/signup")
    fun attemptMakeAccount(
        @RequestParam firstname: String,
        @RequestParam lastname: String,
        @RequestParam password: String,
        @RequestParam email: String,
        @RequestParam phone: String,
        @RequestPart profilePicture: MultipartFile,
        @RequestPart videoFile: MultipartFile
    ) {
        try { userService.getUser(email) } catch (e: NoSuchElementException) {
            val user = User.of(
                firstName = firstname.normalize(),
                lastName = lastname.normalize(),
                email = email,
                password = password,
                phoneNumber = phone
            )
            user.photoPath(userService.dataPath).saveData(profilePicture.bytes)
            user.videoPath(userService.dataPath).saveData(videoFile.bytes)
            userService.saveUpdatedUser(user)
        }
    }

    /**
     * Gets the next user from the database.  Current system is an in-progress proxy for the system.
     *
     * @author Gedeon Poruban
     * @since 0.0.1
     * */

    @PostMapping("/getnext")
    @Deprecated("Work in progress method.")
    fun getNextUser(@RequestParam index: Int): ResponseEntity<LinkedMultiValueMap<String, Any>> =
        ResponseEntity
            .ok()
            .body(userService.getUser(index))

    @PostMapping("/match")
    fun match(
        @RequestParam email: String,
        @RequestParam matchEmail: String
    ) = matchService.matchWith(email, matchEmail)

    @PostMapping("/matches")
    fun getMatches(@RequestParam email: String): String {
        val acceptedMatches = matchService.getMatches(email)
        val jsonObj = if(acceptedMatches.isNotEmpty())
            MatchGsonWrapper(acceptedMatches.map {
                MatchGson(userService.getUser(it.user1?.email!!), userService.getUser(it.user2?.email!!))
            })
        else MatchGsonWrapper(listOf())
        val json = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
            .toJson(jsonObj, MatchGsonWrapper::class.java)
        println("Created json $json")
        return json
    }

    @GetMapping("/{email}/video")
    @ResponseBody
    fun serveVideo(@PathVariable email: String): ResponseEntity<ByteArray> {
        val videoBytes = userService.getVideo(email)
        return ResponseEntity.ok()
            .header("Content-Type", "video/mp4")
            .header("Content-Length", videoBytes.size.toString())
            .body(videoBytes)
    }

    @PostMapping("/password/send")
    fun sendPasswordReset(@RequestParam email: String) = resetService.sendResetMail(email)

    @PostMapping("/password/validate")
    fun validatePasswordCode(
        @RequestParam email: String,
        @RequestParam code: String
    ) = resetService.validateResetCode(email, code)

    @PostMapping("/password/reset")
    fun attemptResetPassword(
        @RequestParam email: String,
        @RequestParam code: String,
        @RequestParam password: String
    ) {
        require(resetService.validateResetCode(email, code))
        resetService.resetPassword(email, password)
    }

    private fun String.normalize(): String = lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase() }
    private fun File.saveData(data: ByteArray) {
        if(this.isDirectory) return
        with(this.parentFile) {
            if(!exists()) mkdirs()
            writeBytes(data)
        }
    }
}