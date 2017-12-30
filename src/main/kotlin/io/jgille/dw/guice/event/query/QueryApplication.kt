package io.jgille.dw.guice.event.query

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.multibindings.Multibinder
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Environment
import io.jgille.dw.guice.event.core.DomainEventSchemas
import io.jgille.dw.guice.event.core.OrderPayedEvent
import io.jgille.dw.guice.event.core.OrderPlacedEvent
import io.jgille.dw.guice.event.core.OrderShippedEvent
import io.jgille.dw.guice.event.messaging.*
import javax.ws.rs.POST
import javax.ws.rs.Path

class QueryApplication : Application<Configuration>() {
    override fun run(configuration: Configuration, environment: Environment) {
        val injector = Guice.createInjector(CoreModule(environment))
        environment.jersey().register(injector.getInstance(TriggerMessageHandlingResource::class.java))
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            QueryApplication().run(*args)
        }
    }
}

class CoreModule(private val environment: Environment) : AbstractModule() {
    override fun configure() {
        environment.objectMapper.apply {
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(KotlinModule())
        }

        bind(ObjectMapper::class.java).toInstance(environment.objectMapper)
        bind(DomainEventSchemas::class.java).toInstance(DomainEventSchemas.fromEventsOnClassPath())
        bind(DomainEventHandler::class.java).to(ReflectiveDomainEventHandler::class.java)
        bind(TriggerMessageHandlingResource::class.java)

        val binder = Multibinder.newSetBinder(binder(), Any::class.java, EventControllers::class.java)
        binder.addBinding().to(OrderController::class.java)
        binder.addBinding().to(PaymentController::class.java)
    }

}

class OrderController {

    @EventHandler
    fun onOrderPlaced(event: OrderPlacedEvent) {
        println("Order ${event.orderId} placed")
    }

    @EventHandler
    fun onOrderShipped(event: OrderShippedEvent) {
        println("Order ${event.orderId} shipped")
    }

}

class PaymentController {

    @EventHandler
    fun onOrderPayed(event: OrderPayedEvent) {
        println("Order ${event.orderId} payed")
    }

}

@Path("/")
class TriggerMessageHandlingResource @Inject constructor(private val messageHandler: MessageHandler,
                                                         private val objectMapper: ObjectMapper) {

    @POST
    @Path("/orders/placed")
    fun orderPlaced(request: OrderPlacedRequest) {
        messageHandler.handle(Message("order_placed_v1", objectMapper.writeValueAsBytes(request)))
    }

    @POST
    @Path("/orders/shipped")
    fun orderShipped(request: OrderShippedRequest) {
        messageHandler.handle(Message("order_shipped_v1", objectMapper.writeValueAsBytes(request)))
    }

    @POST
    @Path("/orders/payed")
    fun orderPlaced(request: OrderPayedRequest) {
        messageHandler.handle(Message("order_payed_v1", objectMapper.writeValueAsBytes(request)))
    }

    class OrderPlacedRequest {
        lateinit var orderId: String
        var amount: Long = 0
    }

    class OrderShippedRequest {
        lateinit var orderId: String
    }

    class OrderPayedRequest {
        lateinit var orderId: String
    }
}