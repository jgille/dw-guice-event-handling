package io.jgille.dw.guice.event.core

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner

abstract class DomainEvent

@Schema("order_placed_v1")
data class OrderPlacedEvent(val orderId: String, val amount: Long) : DomainEvent()

@Schema("order_shipped_v1")
data class OrderShippedEvent(val orderId: String) : DomainEvent()

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Schema(val value: String)

class DomainEventSchemas private constructor(private val mapping: Map<String, Class<out DomainEvent>>) {

    fun classFor(schema: String): Class<out DomainEvent>? {
        return mapping[schema]
    }

    companion object {
        @JvmStatic
        fun fromEventsOnClassPath(): DomainEventSchemas {
            val domainEventTypes = Reflections("io.jgille", SubTypesScanner()).getSubTypesOf(DomainEvent::class.java)

            val mapping = domainEventTypes.filter {
                it.getAnnotation(Schema::class.java) != null
            }
                    .map { it.getAnnotation(Schema::class.java).value to it }
                    .toMap()
            return DomainEventSchemas(mapping)
        }

    }

}
