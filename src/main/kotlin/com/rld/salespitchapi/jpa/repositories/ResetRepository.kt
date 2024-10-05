package com.rld.salespitchapi.jpa.repositories

import com.rld.salespitchapi.jpa.entities.ResetRequest
import com.rld.salespitchapi.jpa.entities.User
import org.springframework.data.repository.CrudRepository

interface ResetRepository : CrudRepository<ResetRequest, Long> {
    fun getResetRequestsByUser(user: User): List<ResetRequest>?
}