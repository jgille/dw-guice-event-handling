package io.jgille.dw.guice.event.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import io.jgille.dw.guice.event.core.DomainEventSchemas

class MessageHandler @Inject constructor(private val domainEventSchemas: DomainEventSchemas,
                                         private val objectMapper: ObjectMapper,
                                         private val eventHandler: DomainEventHandler) {

    fun handle(message: Message) {
        val eventClass = domainEventSchemas.classFor(message.schema)
        val domainEvent= eventClass?.let {
            objectMapper.readValue(message.payload, eventClass)
        } ?: throw IllegalArgumentException("Unknown schema: ${message.schema}")
        eventHandler.handle(domainEvent)
    }

}

class Message(val schema: String, val payload: ByteArray)