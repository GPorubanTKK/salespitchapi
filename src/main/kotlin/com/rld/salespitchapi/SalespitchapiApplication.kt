package com.rld.salespitchapi

import com.rld.salespitchapi.SalespitchapiApplication.Companion.logger
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
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
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	fun handle(request: HttpServletRequest, e: Exception) {
		logger.error("$e at ${request.servletPath}")
	}
}