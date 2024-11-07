package com.rld.salespitchapi.rest

import com.rld.salespitchapi.baseMapping
import com.rld.salespitchapi.services.PasswordResetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$baseMapping/password")
class ResetController {
    @Autowired private lateinit var resetService: PasswordResetService

    @PostMapping("/requestreset")
    fun passwordResetRequest(@RequestParam email: String) =
        resetService.sendResetMail(email)

    @PostMapping("/verify")
    fun verifyRequestCode(
        @RequestParam email: String,
        @RequestParam code: String
    ) = require(resetService.validateResetCode(email, code))

    @PostMapping("/doreset")
    fun doPasswordReset(
        @RequestParam email: String,
        @RequestParam newPassword: String,
        @RequestParam code: String
    ) = resetService.resetPassword(email, newPassword, code)
}