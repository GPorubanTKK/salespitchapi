package com.rld.salespitchapi.websockets

import com.rld.salespitchapi.SalespitchapiApplication.Companion.logger
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

class MessageHandler : WebSocketHandler {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("Connection established with {}", session.remoteAddress)
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val json = (message as TextMessage).payload
        session.sendMessage(TextMessage(json))
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        throw Exception("Transport error in session ${session.id}", exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.debug("Connection closed with {} code {}", session.remoteAddress, closeStatus.code)
    }

    override fun supportsPartialMessages() = false
}