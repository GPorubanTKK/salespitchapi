package com.rld.salespitchapi.websocket_util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.rld.salespitchapi.SalespitchApiApplication.Companion.logger
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.lang.IllegalStateException
import java.net.InetSocketAddress
import com.rld.salespitchapi.websocket_util.WebSocketMessage as WSMessage

class MessageHandler(private val onRequestAuth: (String, String) -> Boolean) : WebSocketHandler {
    init {
        println("Called ctor")
    }

    val connectedSessions = mutableMapOf<InetSocketAddress, WebSocketSession>() //all sessions
    val authenticatedSessions = mutableMapOf<String, InetSocketAddress>() //sessions identified by the clients

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
                require(!authenticatedSessions.containsKey(from) && onRequestAuth(from!!, payload!!["password"].asString)) //session isn't already authenticated
                authenticatedSessions[from!!] = session.remoteAddress!!
                println("Authenticated $from with address ${session.remoteAddress}")
                session.sendMessage(SystemMessage(from, JsonObject().apply { addProperty("content", "Welcome, $from!") }))
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
        val userId = authenticatedSessions.entries.first { (_, v) -> v == session.remoteAddress!! }.key
        authenticatedSessions.remove(userId)
    }

    override fun supportsPartialMessages() = false

    fun sendMessageTo(recipient: String, message: WSMessage) =
        connectedSessions[authenticatedSessions[recipient]]?.sendMessage(message)

    private fun WebSocketSession.sendMessage(message: WSMessage) {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        sendMessage(TextMessage(gson.toJson(message, message::class.java)))
    }
}