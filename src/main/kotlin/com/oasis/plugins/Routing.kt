package com.oasis.plugins

import com.oasis.my.MyLocation
import com.oasis.my.MyLocations
import com.oasis.my.myGet
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting() {
    install(MyLocations) {
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        myGet<CustomLocation> {
            call.respondText("Location: name=${it.name}, arg1=${it.arg1}, arg2=${it.arg2}")
        }
        // Register nested routes
        myGet<Type.Edit> {
            call.respondText("Inside $it")
        }
        myGet<Type.List> {
            call.respondText("Inside $it")
        }
    }
}

@MyLocation("/location/{name}")
class CustomLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")

@MyLocation("/type/{name}")
data class Type(val name: String) {
    @MyLocation("/edit")
    data class Edit(val type: Type)

    @MyLocation("/list/{page}")
    data class List(val type: Type, val page: Int)
}
