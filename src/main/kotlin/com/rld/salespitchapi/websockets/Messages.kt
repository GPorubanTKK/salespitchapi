package com.rld.salespitchapi.websockets

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.rld.salespitchapi.websockets.MessageType.*
import org.springframework.web.socket.TextMessage

abstract class WebSocketMessage<V>(
    @Expose val type: MessageType? = null,
    @Expose val to: String?,
    @Expose val from: String,
    @Expose val payload: V
) {
    companion object {
        fun from(msg: TextMessage): Pair<MessageType, WebSocketMessage<*>> {
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
            return type to message
        }
    }
}

class IdentityMessage(from: String, payload: String): WebSocketMessage<String>(Identify, null, from, payload)
class SystemMessage(from: String, payload: String): WebSocketMessage<String>(System, null, from, payload)
class MatchMessage(from: String, to: String, payload: String): WebSocketMessage<String>(Match, to, from, payload)
class DirectMessage(from: String, to: String, payload: String): WebSocketMessage<String>(Message, to, from, payload)

enum class MessageType {
    Identify,
    Message,
    Match,
    System
}