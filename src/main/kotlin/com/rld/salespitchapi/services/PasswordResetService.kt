package com.rld.salespitchapi.services

import com.rld.salespitchapi.jpa.entities.ResetRequest
import com.rld.salespitchapi.jpa.repositories.ResetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Service
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service class PasswordResetService {
    @Autowired private lateinit var userService: UserService
    @Autowired private lateinit var passwordResetRepository: ResetRepository

    private val hasher = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    fun sendResetMail(email: String) {
        val request = ResetRequest.of(
            user = userService.getUser(email),
            timeStamp = getTS(),
            period = 20,
            generateResetCode(6)
        )
        passwordResetRepository.save(request)
        sendMail(
            request.user!!.email,
            "Password reset code for Salespitch",
            "Your reset code is ${request.assocCode!!}. It is valid for ${request.grantedPeriod} minutes."
        )
    }

    fun validateResetCode(email: String, providedCode: String): Boolean {
        val user = userService.getUser(email)
        val request = passwordResetRepository.getResetRequestsByUser(user)
        val ret = request != null && //a request was made
            request.assocCode!! == providedCode && //the user provided a valid reset code
            request.initTimestamp!!.toDateTime() //the user presented it within the valid time period
                .plusMinutes(request.grantedPeriod.toLong())
                .isAfter(LocalDateTime.now())
        require(ret)
        return true
    }

    fun resetPassword(email: String, newPassword: String) {
        val user = userService.getUser(email)
        val request = passwordResetRepository.getResetRequestsByUser(user)
        passwordResetRepository.delete(request!!)
        user.password = hasher.encode(newPassword)
        userService.saveUpdatedUser(user)
    }

    @Autowired private lateinit var credentialsHelper: AwsCredentialsHelper

    private fun getSESClient() = SesClient
        .builder()
        .credentialsProvider { credentialsHelper.getCredentials() }
        .region(Region.US_EAST_2)
        .build()

    private fun sendMail(email: String, subject: String, text: String) {
        val client = getSESClient()
        client.checkQuota() //make sure we aren't sending too many emails
        client.sendEmail { builder ->
            builder.message { msg ->
                msg.body { body -> body.text { content -> content.data(text) } }
                msg.subject { it.data(subject) }
            }
            builder.destination { dest -> dest.toAddresses(listOf(email)) }
            builder.source(credentialsHelper.getAgent())
        }
    }

    private fun SesClient.checkQuota() {
        if(sendQuota.sentLast24Hours() + 1 >= sendQuota.max24HourSend()) throw QuotaException()
    }

    class QuotaException: Exception()

    private fun String.toDateTime(): LocalDateTime =
        LocalDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(this))

    private fun getTS(): String = LocalDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)
    private fun generateResetCode(len: Int): String {
        val chars = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        return buildString { for(i in 0..< len) append(chars.random()) }
    }
}