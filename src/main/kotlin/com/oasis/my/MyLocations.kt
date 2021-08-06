package com.oasis.my

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

public open class MyLocations constructor(
    application: Application,
    routeService: MyLocationRouteService
) {

    public constructor(application: Application) : this(application, MyLocationAttributeRouteService())

    private val implementation: MyLocationsImpl = MyBackwardCompatibleImpl(application, routeService)

    public class Configuration {
        /**
         * Specifies an alternative routing service. Default is [LocationAttributeRouteService].
         */
        public var routeService: MyLocationRouteService? = null
    }

    public companion object Feature : ApplicationFeature<Application, Configuration, MyLocations> {
        override val key: AttributeKey<MyLocations> = AttributeKey("MyLocations")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): MyLocations {
            val configuration = Configuration().apply(configure)
            val myRouteService = configuration.routeService ?: MyLocationAttributeRouteService()
            return MyLocations(pipeline, myRouteService)
        }
    }

    private fun createEntry(parent: Route, info: MyLocationInfo): Route {
        val hierarchyEntry = info.parent?.let { createEntry(parent, it) } ?: parent
        return hierarchyEntry.createRouteFromPath(info.path)
    }

    public fun createEntry(parent: Route, locationClass: KClass<*>): Route {
        val info = implementation.getOrCreateInfo(locationClass)
        val pathRoute = createEntry(parent, info)


        return info.queryParameters.fold(pathRoute) { entry, query ->
            val selector = if (query.isOptional) {
                OptionalParameterRouteSelector(query.name)
            } else {
                ParameterRouteSelector(query.name)
            }
            entry.createChild(selector)
        }
    }

    public fun <T : Any> resolve(locationClass: KClass<*>, call: ApplicationCall): T {
        return resolve(locationClass, call.parameters)
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> resolve(locationClass: KClass<*>, parameters: Parameters): T {
        val info = implementation.getOrCreateInfo(locationClass)
        return implementation.instantiate(info, parameters) as T
    }
}

public interface MyLocationRouteService {
    /**
     * Retrieves routing information from a given [locationClass].
     * @return routing pattern, or null if a given class doesn't represent a route.
     */
    public fun findRoute(locationClass: KClass<*>): String?
}

public class MyLocationAttributeRouteService : MyLocationRouteService {
    private inline fun <reified T : Annotation> KAnnotatedElement.annotation(): T? {
        return annotations.singleOrNull { it.annotationClass == T::class } as T?
    }

    override fun findRoute(locationClass: KClass<*>): String? = locationClass.annotation<MyLocation>()?.path
}


internal class MyLocationPropertyInfoImpl(
    name: String,
    val kGetter: KProperty1.Getter<Any, Any?>,
    isOptional: Boolean
) : MyLocationPropertyInfo(name, isOptional)

public class MyLocationRoutingException(message: String) : Exception(message)