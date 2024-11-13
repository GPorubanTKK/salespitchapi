package com.rld.salespitchapi.services

import com.google.gson.GsonBuilder
import com.rld.salespitchapi.jpa.entities.User
import com.rld.salespitchapi.jpa.repositories.UserRepository
import com.rld.salespitchapi.normalize
import com.rld.salespitchapi.saveData
import org.apache.commons.lang3.SystemUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap

@Service class UserService {
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var messagingService: MessagingService

    private val hasher = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    /**
     * The base filesystem path for locating data on disk
     * */
    val dataPath = when {
        SystemUtils.IS_OS_WINDOWS -> "${System.getProperty("user.home").replace('\\', '/')}/Desktop/salespitchdata"
        SystemUtils.IS_OS_LINUX -> "${System.getProperty("user.home")}/salespitchdata"
        SystemUtils.IS_OS_MAC -> "${System.getProperty("user.home")}/salespitchdata"
        else -> throw ExceptionInInitializerError("OS ${SystemUtils.OS_NAME} is unsupported.")
    }


    fun getUser(email: String): User =
        userRepository.getUserByEmail(email) ?: throw NoSuchElementException("User $email does not exist.")

    fun getUser(index: Int): LinkedMultiValueMap<String, Any> {
        val user = userRepository.getUserByIndex(index) ?: throw NoSuchElementException("User at index $index does not exist")
        return packUser(user)
    }

    fun authenticateUser(email: String, password: String): LinkedMultiValueMap<String, Any> {
        val user = getUser(email)
        require(hasher.matches(password, user.password)) { "Passwords do not match. Sent hash ${hasher.encode(password)} does not equal ${user.password}" }
        return packUser(user)
    }

    fun isAuthed(email: String): Boolean = messagingService.userIsAuthed(email)

    fun getVideo(email: String): ByteArray {
        val user = getUser(email)
        return with(user.videoPath(dataPath)) {
            require(exists())
            readBytes()
        }
    }

    internal fun saveUpdatedUser(user: User) {
        require(exists(user.email))
        userRepository.save(user)
    }

    fun exists(email: String): Boolean = try { getUser(email); true } catch (_: NoSuchElementException) { false }

    fun createUser(
        email: String,
        password: String,
        firstname: String,
        lastname: String,
        phoneNumber: String,
        pictureBytes: ByteArray,
        videoBytes: ByteArray
    ) {
        try { getUser(email) } catch (_: java.util.NoSuchElementException) {
            val user = User.of(
                firstName = firstname.normalize(),
                lastName = lastname.normalize(),
                email = email,
                password = hasher.encode(password),
                phoneNumber = phoneNumber
            )
            user.photoPath(dataPath).saveData(pictureBytes)
            user.videoPath(dataPath).saveData(videoBytes)
            userRepository.save(user)
        }
    }

    fun deleteUser(email: String) {
        require(isAuthed(email))
        messagingService.disconnectUser(email)
        userRepository.delete(getUser(email))
    }

    private fun packUser(user: User): LinkedMultiValueMap<String, Any> {
        val gson = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        val pictureBytes = with(user.photoPath(dataPath)) {
            require(exists()) { "Profile picture cannot be accessed." }
            readBytes().also { println("Packing jpeg with ${it.size} bytes.") }
        }
        return LinkedMultiValueMap<String, Any>().apply {
            add("user", HttpEntity(gson.toJson(user, User::class.java), HttpHeaders()))
            add("picture", HttpEntity(ByteArrayResource(pictureBytes), HttpHeaders()))
        }
    }
}