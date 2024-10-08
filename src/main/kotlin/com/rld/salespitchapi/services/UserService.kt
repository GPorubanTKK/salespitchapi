package com.rld.salespitchapi.services

import com.google.gson.GsonBuilder
import com.rld.salespitchapi.jpa.entities.User
import com.rld.salespitchapi.jpa.repositories.UserRepository
import org.apache.commons.lang3.SystemUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap

@Service class UserService {
    @Autowired private lateinit var userRepository: UserRepository

    /**
     * The base filesystem path for locating data on disk
     * */
    val dataPath = when {
        SystemUtils.IS_OS_WINDOWS -> "${System.getProperty("user.home").replace('\\', '/')}/Desktop/salespitchdata"
        SystemUtils.IS_OS_LINUX -> "${System.getProperty("user.home")}/salespitchdata"
        SystemUtils.IS_OS_MAC -> "${System.getProperty("user.home")}/salespitchdata"
        else -> throw ExceptionInInitializerError("OS ${SystemUtils.OS_NAME} is unsupported.")
    }

    private val hasher = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    fun getUser(email: String): User =
        userRepository.getUserByEmail(email) ?: throw NoSuchElementException("User $email does not exist.")

    fun getUser(index: Int): LinkedMultiValueMap<String, Any> {
        val user = userRepository.getUserByIndex(index) ?: throw NoSuchElementException("User at index $index does not exist")
        return packUser(user)
    }

    fun saveUpdatedUser(user: User) = userRepository.save(user)

    fun authenticateUser(email: String, password: String): LinkedMultiValueMap<String, Any> {
        val user = getUser(email)
        require(hasher.matches(password, user.password))
        return packUser(user)
    }

    fun getVideo(email: String): ByteArray {
        val user = getUser(email)
        return with(user.videoPath(dataPath)) {
            require(exists())
            readBytes()
        }
    }

    fun matchWith(mathWithEmail: String) {

    }

    private fun packUser(user: User): LinkedMultiValueMap<String, Any> {
        val gson = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        val pictureBytes = with(user.photoPath(dataPath)) {
            require(exists()) { "Profile picture cannot be accessed." }
            readBytes()
        }
        return LinkedMultiValueMap<String, Any>().apply {
            add("user", HttpEntity(gson.toJson(user, User::class.java), HttpHeaders()))
            add("picture", HttpEntity(InputStreamResource(pictureBytes.inputStream()), HttpHeaders()))
        }
    }
}