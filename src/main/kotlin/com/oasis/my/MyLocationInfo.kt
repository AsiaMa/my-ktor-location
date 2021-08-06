package com.oasis.my

import kotlin.reflect.KClass

public data class MyLocationInfo internal constructor(
    val klass: KClass<*>,
    val parent: MyLocationInfo?,
    val parentParameter: MyLocationPropertyInfo?,
    val path: String,
    val pathParameters: List<MyLocationPropertyInfo>,
    val queryParameters: List<MyLocationPropertyInfo>
)

public abstract class MyLocationPropertyInfo internal constructor(
    public val name: String,
    public val isOptional: Boolean
) {
    public final override fun hashCode(): Int = name.hashCode()
    public final override fun equals(other: Any?): Boolean = other is MyLocationPropertyInfo &&
            name == other.name &&
            isOptional == other.isOptional

    public final override fun toString(): String = "Property(name = $name, optional = $isOptional)"
}
