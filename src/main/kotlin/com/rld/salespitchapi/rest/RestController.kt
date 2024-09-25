package com.rld.salespitchapi.rest

import com.google.gson.GsonBuilder
import com.rld.salespitchapi.jpa.entities.Match
import com.rld.salespitchapi.jpa.entities.MatchGsonWrapper
import com.rld.salespitchapi.jpa.entities.User
import com.rld.salespitchapi.jpa.repositories.MatchRepository
import com.rld.salespitchapi.jpa.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

@RestController
@RequestMapping("/app/api")
class UserController {
    val pfpPath = "${System.getProperty("user.home")}\\Desktop\\salespitchdata"

    @Autowired lateinit var users: UserRepository
    @Autowired lateinit var matches: MatchRepository

    private val hasher = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

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
    ): ResponseEntity<LinkedMultiValueMap<String, Any>> {
        val user = users.getUserByEmail(email)
        if(user == null || !hasher.matches(password, user.password!!))
            throw Exception("User does not exist or password is wrong. email: $email password: $password")
        return packUser(user)
    }

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
        @RequestPart profilePicture: MultipartFile
    ) {
        val checkUser = users.getUserByEmail(email)
        require(checkUser == null)
        with(File("$pfpPath\\$email\\profilepicture")) {
            if(!exists()) mkdirs()
            with(File("$pfpPath\\$email\\profilepicture\\picture.jpg")) {
                writeBytes(profilePicture.bytes)
            }
        }
        val user = User().apply {
            this.firstname = firstname.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase() }
            this.lastname = lastname.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase() }
            this.password = hasher.encode(password)
            this.email = email
            this.phoneNumber = phone
            this.profilePicturePath = "$pfpPath/$email/profilepicture/picture.jpg"
        }
        users.save(user)
    }

    /**
     * Gets the next user from the database.  Current system is an in-progress proxy for the system.
     *
     * @author Gedeon Poruban
     * @since 0.0.1
     * */

    @PostMapping("/getnext")
    @Deprecated("Work in progress method.")
    fun getNextUser(@RequestParam index: Int): ResponseEntity<LinkedMultiValueMap<String, Any>> {
        val user = users.getUserByIndex(index)
        return packUser(user)
    }

    @PostMapping("/match")
    fun match(
        @RequestParam user: String,
        @RequestParam match: String
    ) {
        val wasMatchedBack = matches.getMatchByUser1AndUser2(match, user)
        if(wasMatchedBack != null) {
            matches.save(wasMatchedBack.apply {
                accepted = true
            })
        } else {
            matches.save(Match().apply {
                user1 = users.getUserByEmail(user)
                user2 = users.getUserByEmail(match)
            })
        }
    }

    @PostMapping("/matches")
    fun getMatches(@RequestParam user: String): String {
        val acceptedMatches = matches.getSuccessfulMatchesByUser(user)
        val jsonObj = if(acceptedMatches != null) MatchGsonWrapper(acceptedMatches) else MatchGsonWrapper(listOf())
        return GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
            .toJson(jsonObj, MatchGsonWrapper::class.java)
    }

    @PostMapping("/password/send")
    fun setPasswordReset(
        @RequestParam email: String
    ): ResponseEntity<Any> {
        return if(users.getUserByEmail(email) == null)
            ResponseEntity.internalServerError().build()
        else ResponseEntity.ok().build()
    }

    @PostMapping("/password/verify")
    fun verifyResetCode(
        @RequestParam code: String
    ) {
        TODO("Implement password reset system")
    }

    @PostMapping("/password/reset")
    fun attemptResetPassword(
        @RequestParam email: String,
        @RequestParam code: String,
        @RequestParam password: String
    ) {
        val user = users.getUserByEmail(email)
        require(isValid(code) && user != null)
        user.password = hasher.encode(password)
        users.save(user)
    }

    private fun isValid(code: String): Boolean = TODO("Implement password reset system")

    fun packUser(user: User?): ResponseEntity<LinkedMultiValueMap<String, Any>> {
        if(user == null)
            throw Exception("User does not exist")
        val gson = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        val pictureBytes = with(File("$pfpPath/${user.email}/profilepicture/picture.jpg")) {
            require(exists()) { "Profile picture cannot be accessed." }
            readBytes()
        }
        return ResponseEntity
            .status(200)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(LinkedMultiValueMap<String, Any>().apply {
                add("user", HttpEntity(gson.toJson(user, User::class.java), HttpHeaders()))
                add("picture", HttpEntity(InputStreamResource(pictureBytes.inputStream()), HttpHeaders()))
            })
    }
}