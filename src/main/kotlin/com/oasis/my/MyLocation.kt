package com.oasis.my

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
public annotation class MyLocation(val path: String)

public val Application.myLocations: MyLocations
    get() = feature(MyLocations)

public val PipelineContext<Unit, ApplicationCall>.myLocations: MyLocations
    get() = call.application.myLocations

public inline fun <reified T : Any> Route.myGet(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    return myLocation(T::class) {
        method(HttpMethod.Get) {
            myHandle(body)
        }
    }
}

public fun <T : Any> Route.myLocation(data: KClass<T>, body: Route.() -> Unit): Route {
    val entry = application.myLocations.createEntry(this, data)
    return entry.apply(body)
}

public inline fun <reified T : Any> Route.myHandle(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    return myHandle(T::class, body)
}

public fun <T : Any> Route.myHandle(
    dataClass: KClass<T>,
    body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    intercept(ApplicationCallPipeline.Features) {
        call.attributes.put(MyLocationInstanceKey, myLocations.resolve<T>(dataClass, call))
    }

    handle {
        @Suppress("UNCHECKED_CAST")
        val location = call.attributes[MyLocationInstanceKey] as T

        body(location)
    }
}

private val MyLocationInstanceKey = AttributeKey<Any>("LocationInstance")