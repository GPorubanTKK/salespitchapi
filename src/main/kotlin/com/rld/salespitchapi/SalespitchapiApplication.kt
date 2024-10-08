package com.rld.salespitchapi

import com.rld.salespitchapi.SalespitchapiApplication.Companion.logger
import com.rld.salespitchapi.services.PasswordResetService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@SpringBootApplication
class SalespitchapiApplication {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SalespitchapiApplication::class.java)
	}
}

fun main(args: Array<String>) {
	runApplication<SalespitchapiApplication>(*args)
}

@RestControllerAdvice
class ErrorHandler {
	@ExceptionHandler
	fun handle(request: HttpServletRequest, e: Exception): ResponseEntity<Unit> {
		logger.error("$e at ${request.servletPath}")
		return when(e) {
			is PasswordResetService.QuotaException -> ResponseEntity.status(503).build()
			else -> ResponseEntity.internalServerError().build()
		}
	}
}