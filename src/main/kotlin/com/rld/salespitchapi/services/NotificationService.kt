package com.rld.salespitchapi.services

import com.google.gson.JsonObject
import com.rld.salespitchapi.websocket_util.MatchMessage
import com.rld.salespitchapi.websocket_util.MessageHandler
import com.rld.salespitchapi.websocket_util.SystemMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@Service
@EnableWebSocket
class MessagingService : WebSocketConfigurer {
    @Autowired private lateinit var userService: UserService

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(messageHandler(), "/messages")
    }

    @Bean fun messageHandler() = MessageHandler { id, password ->
        try {
            userService.authenticateUser(id, password)
            true
        } catch (ignored: Exception) { false }
    }

    fun userIsAuthed(email: String): Boolean = messageHandler().authenticatedSessions.containsKey(email)

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
}