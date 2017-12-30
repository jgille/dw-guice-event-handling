package io.jgille.dw.guice.event.messaging

import com.google.inject.BindingAnnotation
import io.jgille.dw.guice.event.core.DomainEvent
import java.lang.reflect.Method
import javax.inject.Inject

interface DomainEventHandler {
    fun handle(event: DomainEvent)
}

class ReflectiveDomainEventHandler : DomainEventHandler {

    private val handlerFunctions: MutableMap<Class<out DomainEvent>, (DomainEvent) -> Unit> = mutableMapOf()

    @Suppress("ConvertSecondaryConstructorToPrimary", "unused")
    @Inject
    constructor(@EventControllers eventControllers: MutableSet<Any>) {
        eventControllers.forEach {registerEventController(it)}
    }

    override fun handle(event: DomainEvent) {
        val handlerFunction = handlerFunctions[event::class.java] ?:
                throw IllegalArgumentException("Unknown event class: ${event.javaClass}")
        handlerFunction(event)
    }

    private fun registerEventController(controller: Any) {
        controller.javaClass.methods.filter { isEventHandlerMethod(it) }.forEach {
            @Suppress("UNCHECKED_CAST")
            handlerFunctions[it.parameterTypes[0] as Class<out DomainEvent>] = {
                event -> it.invoke(controller, event)
            }
        }
    }

    private fun isEventHandlerMethod(method: Method): Boolean {
        return method.getAnnotation(EventHandler::class.java) != null
                && method.parameterCount == 1
                && DomainEvent::class.java.isAssignableFrom(method.parameterTypes[0])
    }

}

@BindingAnnotation
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventControllers