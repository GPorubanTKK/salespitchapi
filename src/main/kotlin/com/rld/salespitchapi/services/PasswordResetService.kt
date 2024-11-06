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

//TODO: Impl code to clean-up unused requests

@Service class PasswordResetService {
    @Autowired private lateinit var userService: UserService
    @Autowired private lateinit var passwordResetRepository: ResetRepository
    @Autowired private lateinit var credentialsHelper: AwsCredentialsHelper

    private val hasher = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    private val timeoutPeriod = 20

    fun sendResetMail(email: String) {
        val user = userService.getUser(email)
        val prevRequest = passwordResetRepository.getResetRequestsByUser(user)
        require((prevRequest != null && !tsIsGood(prevRequest, 20)) || prevRequest == null)
        val newRequest = ResetRequest.of(
            user = user,
            timeStamp = getTS(),
            period = timeoutPeriod,
            resetCode = generateResetCode(6),
        )
        sendMail(email, "Password reset request for Salespitch", """
            Hello, you are receiving this because you have requested a password reset for your Salespitch account.
            Your reset code is: ${newRequest.assocCode}.  It will expire in $timeoutPeriod minutes.
        """.trimIndent())
        passwordResetRepository.save(newRequest)
    }

    fun validateResetCode(email: String, providedCode: String): Boolean {
        val user = userService.getUser(email)
        val request = passwordResetRepository.getResetRequestsByUser(user)
        val ret = request != null && //a request was made
            request.assocCode!! == providedCode && //the user provided a valid reset code
            tsIsGood(request, timeoutPeriod)
        require(ret)
        return true
    }

    fun resetPassword(email: String, newPassword: String, resetCode: String) {
        require(validateResetCode(email, resetCode))
        val user = userService.getUser(email)
        user.password = hasher.encode(newPassword)
        userService.saveUpdatedUser(user)
    }

    private fun sendMail(email: String, subject: String, text: String) {
        val client = SesClient
            .builder()
            .credentialsProvider { credentialsHelper.getCredentials() }
            .region(Region.US_EAST_2)
            .build()
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
        if(sendQuota.sentLast24Hours() + 2 >= sendQuota.max24HourSend()) throw QuotaException()
    }

    class QuotaException: Exception()

    private fun String.toDateTime(): LocalDateTime =
        LocalDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(this))

    private fun getTS(): String =
        LocalDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)

    private fun generateResetCode(len: Int): String {
        val chars = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        return buildString { for(i in 0..< len) append(chars.random()) }
    }

    private fun LocalDateTime.isWithinTimeZone(start: LocalDateTime, length: Int): Boolean =
        start.plusMinutes(length.toLong()).isAfter(this)

    private fun tsIsGood(request: ResetRequest, length: Int): Boolean =
        LocalDateTime.now().isWithinTimeZone(request.initTimestamp!!.toDateTime(), length)
}