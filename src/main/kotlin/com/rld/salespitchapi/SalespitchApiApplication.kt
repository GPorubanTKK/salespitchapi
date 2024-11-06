package com.rld.salespitchapi

import com.rld.salespitchapi.SalespitchApiApplication.Companion.logger
import com.rld.salespitchapi.services.PasswordResetService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.async.AsyncRequestNotUsableException

@SpringBootApplication
class SalespitchApiApplication {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(SalespitchApiApplication::class.java)
	}
}

fun main(args: Array<String>) {
	runApplication<SalespitchApiApplication>(*args)
}

@RestControllerAdvice
class ErrorHandler {
	@ExceptionHandler(value = [AsyncRequestNotUsableException::class])
	fun handleNoAsyncRequest(request: HttpServletRequest, e: Exception) = ResponseEntity.internalServerError().build<Unit>()

	@ExceptionHandler
	fun handle(request: HttpServletRequest, e: Exception): ResponseEntity<Unit> {
		logger.error("$e at ${request.servletPath}")
		return when(e) {
			is PasswordResetService.QuotaException -> ResponseEntity.status(503).build()
			else -> ResponseEntity.internalServerError().build()
		}
	}
}