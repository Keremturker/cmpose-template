@file:OptIn(KoinInternalApi::class)

package com.kturker.multiplatform.di

import androidx.lifecycle.SavedStateHandle
import com.kturker.multiplatform.core.domain.BuildEnvironment
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.annotation.Property
import org.koin.core.module.Module
import org.koin.core.module.flatten
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Checks the Koin graph that initKoin starts (its Android variant): for every definition in
 * appModules() created from a class, each parameter of that class's public constructors must
 * be defined by a module in that list.
 *
 * Koin's own Module.verify() counts a parameter marked @Provided as defined, and every
 * dependency that crosses Gradle modules is @Provided here (KOIN_CONFIG_CHECK asks for it),
 * so it cannot tell that a feature's data or domain module is missing from appModules().
 * This check treats @Provided parameters like any other.
 *
 * Not checked: the parameters of provider functions (`@Single fun provideX(dep: Dep): X`),
 * whose returned type's constructors are read instead, and qualifiers (@Named). A type that
 * reaches the graph some other way (like SavedStateHandle, which navigation supplies, or
 * BuildEnvironment, which initKoin registers itself), or
 * that a third-party class returned by a provider function asks for in its constructor, goes
 * into extraTypes.
 */
class KoinGraphTest {

    @Test
    fun everyConstructorDependencyInAppModulesIsDefined() {
        val missing = missingDependencies(
            modules = appModules(),
            extraTypes = setOf(SavedStateHandle::class, BuildEnvironment::class)
        )

        assertTrue(missing.isEmpty(), "Not defined in appModules():\n" + missing.joinToString("\n"))
    }
}

private fun missingDependencies(modules: List<Module>, extraTypes: Set<KClass<*>>): List<String> {
    val definitions = flatten(modules).flatMap { it.mappings.values }.map { it.beanDefinition }.distinct()
    val defined = definitions.flatMap { listOf(it.primaryType) + it.secondaryTypes }.toSet() + extraTypes

    return definitions.flatMap { definition ->
        definition.primaryType.constructors
            .filter { it.visibility == KVisibility.PUBLIC }
            .flatMap { it.parameters }
            .filter { it.isResolvedFromTheGraph() }
            .mapNotNull { it.dependencyType() }
            .filterNot { it in defined }
            .map { "${definition.primaryType.qualifiedName} needs ${it.qualifiedName}" }
    }.distinct()
}

// Koin fills default-valued and nullable parameters without a definition, and takes
// @InjectedParam and @Property values from parametersOf() and properties.
private fun KParameter.isResolvedFromTheGraph(): Boolean =
    !isOptional && !type.isMarkedNullable && annotations.none { it is InjectedParam || it is Property }

// A Lazy<T> or List<T> parameter needs a definition of T.
private fun KParameter.dependencyType(): KClass<*>? {
    val classifier = type.classifier as? KClass<*> ?: return null
    return if (classifier == Lazy::class || classifier == List::class) {
        type.arguments.firstOrNull()?.type?.classifier as? KClass<*>
    } else {
        classifier
    }
}
