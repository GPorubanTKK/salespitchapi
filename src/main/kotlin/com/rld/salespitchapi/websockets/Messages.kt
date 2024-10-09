package com.rld.salespitchapi.websockets

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.rld.salespitchapi.websockets.MessageType.*
import org.springframework.web.socket.TextMessage

abstract class WebSocketMessage(
    @Expose val type: MessageType? = null,
    @Expose val to: String?,
    @Expose val from: String?,
    @Expose val payload: JsonObject? = null
) {
    operator fun component1(): MessageType? = type
    operator fun component2(): String? = to
    operator fun component3(): String? = from
    operator fun component4(): JsonObject? = payload

    companion object {
        fun from(msg: TextMessage): WebSocketMessage {
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            val type = MessageType.valueOf(gson.fromJson(msg.payload, JsonObject::class.java)["type"].asString)
            val message = gson.fromJson(
                msg.payload,
                when(type) {
                    Identify -> IdentityMessage::class.java
                    Message -> DirectMessage::class.java
                    Match -> MatchMessage::class.java
                    System -> SystemMessage::class.java
                }
            )
            return message
        }
    }
}

class IdentityMessage(from: String, payload: JsonObject): WebSocketMessage(Identify, null, from, payload)
class SystemMessage(to: String, payload: JsonObject): WebSocketMessage(System, to, null, payload)
class MatchMessage(from: String, to: String, payload: JsonObject): WebSocketMessage(Match, to, from, payload)
class DirectMessage(from: String, to: String, payload: JsonObject): WebSocketMessage(Message, to, from, payload)

enum class MessageType {
    Identify,
    Message,
    Match,
    System
}