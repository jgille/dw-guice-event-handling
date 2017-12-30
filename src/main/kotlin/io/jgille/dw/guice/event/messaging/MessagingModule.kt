package io.jgille.dw.guice.event.messaging

import com.google.inject.AbstractModule

class MessagingModule: AbstractModule() {
    override fun configure() {
        bind(DomainEventHandler::class.java).to(ReflectiveDomainEventHandler::class.java)
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler