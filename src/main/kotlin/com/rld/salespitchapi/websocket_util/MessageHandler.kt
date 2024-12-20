package com.rld.salespitchapi.websocket_util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.rld.salespitchapi.SalespitchApiApplication.Companion.logger
import com.rld.salespitchapi.removeValue
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.lang.IllegalStateException
import java.net.InetSocketAddress
import com.rld.salespitchapi.websocket_util.WebSocketMessage as WSMessage

class MessageHandler(private val onRequestAuth: (String, String) -> Boolean) : WebSocketHandler {
    val connectedSessions = mutableMapOf<InetSocketAddress, WebSocketSession>() //all sessions
    val authenticatedSessions = mutableMapOf<String, InetSocketAddress>() //sessions identified by the clients and checked by the db

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("Connection established with {}", session.remoteAddress)
        println("Connection established with ${session.remoteAddress}")
        connectedSessions[session.remoteAddress!!] = session
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val (type, to, from, payload) = WSMessage.from(message as TextMessage)
        if(type != MessageType.Identify) require(authenticatedSessions.containsValue(session.remoteAddress!!))
        when(type!!) {
            MessageType.Identify -> {
                val password = payload!!["password"].asString
                val isValid = try {
                    require(!authenticatedSessions.containsKey(from)) { "Session is already authenticated" } //session isn't already authenticated
                    require(onRequestAuth(from!!, password)) { "Session authKey is invalid" }
                    authenticatedSessions[from] = session.remoteAddress!!
                    println("Authenticated $from with address ${session.remoteAddress}")
                    true
                } catch (_: Exception) { false }
                session.sendMessage(IdentityMessage("", JsonObject().apply { addProperty("password", password); addProperty("valid", isValid) }))
            }
            MessageType.Message -> {
                println("Sending message ${payload!!["content"].asString} to $to")
                val dest = connectedSessions[authenticatedSessions[to]]!!
                dest.sendMessage(DirectMessage(from!!, to!!, JsonObject()))
            }
            MessageType.Match -> throw IllegalStateException("Server should not receive match packets")
            MessageType.System -> println("Received a system message from $from: $payload")
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.debug("Transport error in session ${session.id}", exception)
        println("Transport error in session ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.debug("Connection closed with {} code {}", session.remoteAddress, closeStatus.code)
        println("Connection closed with ${session.remoteAddress} code ${closeStatus.code}")
        connectedSessions.remove(session.remoteAddress!!)
        authenticatedSessions.removeValue(session.remoteAddress!!)
    }

    override fun supportsPartialMessages() = false

    fun sendMessageTo(recipient: String, message: WSMessage) =
        connectedSessions[authenticatedSessions[recipient]]?.sendMessage(message)

    private fun WebSocketSession.sendMessage(message: WSMessage) {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        sendMessage(TextMessage(gson.toJson(message, message::class.java)))
    }
}