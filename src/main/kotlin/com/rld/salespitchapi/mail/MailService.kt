package com.rld.salespitchapi.mail

import com.rld.salespitchapi.jpa.entities.ResetRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service class MailService {
    @Autowired lateinit var sender: JavaMailSender

    fun sendResetMail(to: ResetRequest) {
        val msg = sender.createMimeMessage()
        val helper = MimeMessageHelper(msg, true)
        helper.setTo(to.user!!.email)
        helper.setSubject("Password reset code for Salespitch")
        helper.setText("Your reset code is ${to.assocCode!!}. It is valid for ${to.grantedPeriod} minutes.")
        sender.send(msg)
    }

    fun sendMail(email: String, text: String) {
        val msg = sender.createMimeMessage()
        val helper = MimeMessageHelper(msg, true)
        helper.setTo(email)
        helper.setSubject("Test")
        helper.setText(text)
        sender.send(msg)
    }
}