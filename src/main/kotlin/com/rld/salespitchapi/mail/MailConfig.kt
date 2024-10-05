package com.rld.salespitchapi.mail

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration class MailConfig {
    @Bean fun javaMailSender(): JavaMailSender = JavaMailSenderImpl()
}