package com.rld.salespitchapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider

@Configuration
@PropertySource("classpath:aws.properties")
internal class AwsCredentialsHelper {
    @Autowired lateinit var env: Environment

    /**
     * Find and validate AWS SES Credentials
     *
     * @author Gedeon Poruban
     * @return validated AWS credentials for accesssing SES
     * */
    fun getCredentials(): AwsCredentials = StaticCredentialsProvider
        .create(
            AwsBasicCredentials.create(
                env.getProperty("aws.accessKey"),
                env.getProperty("aws.secretKey")
            )
        )
        .resolveCredentials()

    fun getAgent(): String = env.getProperty("ses.emailAgent", "gedeon@porubannursery.com")
}