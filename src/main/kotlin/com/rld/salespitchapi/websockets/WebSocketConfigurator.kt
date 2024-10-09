package com.rld.salespitchapi.websockets

import com.rld.salespitchapi.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfigurator : WebSocketConfigurer {
    @Autowired private lateinit var userService: UserService

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(messageHandler(), "/messages")
    }

    @Bean fun messageHandler(): WebSocketHandler = MessageHandler { id, password ->
        try {
            userService.authenticateUser(id, password)
            true
        } catch (ignored: Exception) { false }
    }
}