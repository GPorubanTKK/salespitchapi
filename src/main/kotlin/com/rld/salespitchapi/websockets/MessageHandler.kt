package com.rld.salespitchapi.websockets

import com.rld.salespitchapi.SalespitchapiApplication.Companion.logger
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import com.rld.salespitchapi.websockets.WebSocketMessage.Companion.from
import java.net.InetSocketAddress

class MessageHandler(private val onRequestAuth: (String, String) -> Boolean) : WebSocketHandler {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("Connection established with {}", session.remoteAddress)
        println("Connection established with ${session.remoteAddress}")
        connectedSessions[session.remoteAddress!!] = session
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val (type, msg) = from(message as TextMessage)
        if(type != MessageType.Identify) require(authenticatedSessions.containsValue(session.remoteAddress!!))
        when(type) {
            MessageType.Identify -> {
                msg as IdentityMessage
                require(!authenticatedSessions.containsKey(msg.from) && onRequestAuth(msg.from, msg.payload)) //session isn't already authenticated
                authenticatedSessions[msg.from] = session.remoteAddress!!
            }
            MessageType.Message -> {
                msg as DirectMessage
                println("Sending message ${msg.payload} to ${msg.to}")
                val dest = connectedSessions[authenticatedSessions[msg.to]]!!
                dest.sendMessage("${msg.from}: ${msg.payload}")
            }
            MessageType.Match -> TODO("Implement websocket handling of matches")
            MessageType.System -> println("Received a system message from ${msg.from}: ${msg.payload}")
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        throw Exception("Transport error in session ${session.id}", exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.debug("Connection closed with {} code {}", session.remoteAddress, closeStatus.code)
        println("Connection closed with ${session.remoteAddress} code ${closeStatus.code}")
        connectedSessions.remove(session.remoteAddress!!)
        val userId = authenticatedSessions.entries.first { (_, v) -> v == session.remoteAddress!! }.key
        authenticatedSessions.remove(userId)
    }

    override fun supportsPartialMessages() = false

    companion object { //TODO: fix potential concurrency issues with shared mutable state
        val connectedSessions = mutableMapOf<InetSocketAddress, WebSocketSession>() //all sessions
        val authenticatedSessions = mutableMapOf<String, InetSocketAddress>() //sessions identified by the clients
    }

    private fun WebSocketSession.sendMessage(str: String) = sendMessage(TextMessage(str))
}