package com.rld.salespitchapi.services

import com.google.gson.JsonObject
import com.rld.salespitchapi.websocket_util.MessageHandler
import com.rld.salespitchapi.websocket_util.SystemMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@Service
@EnableWebSocket
internal class MessagingService : WebSocketConfigurer {
    private lateinit var userService: UserService
    @Autowired fun setupUserService(@Lazy userService: UserService) { this.userService = userService }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(messageHandler(), "/messages")
    }


    @Bean(name = ["messageHandler"]) fun messageHandler() = MessageHandler { id, password ->
        try {
            userService.authenticateUser(id, password)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun notifyUsers(text: String, recipients: List<String>) {
        val wsClient = messageHandler()
        for(recipient in recipients) {
            wsClient.sendMessageTo(recipient, SystemMessage(
                "",
                JsonObject().apply {
                    addProperty("payload", text)
                }
            ))
        }
    }

    fun userIsAuthed(email: String): Boolean =
        messageHandler().authenticatedSessions.containsKey(email)


    fun disconnectUser(email: String) {
        with(messageHandler()) {
            connectedSessions[authenticatedSessions[email]]!!.close(CloseStatus.NORMAL)
        }
    }
}