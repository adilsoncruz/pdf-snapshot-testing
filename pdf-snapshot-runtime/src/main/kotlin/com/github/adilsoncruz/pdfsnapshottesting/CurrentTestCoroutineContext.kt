package com.github.adilsoncruz.pdfsnapshottesting

import kotlinx.coroutines.asContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine context helper to propagate [CurrentTest] unique ID across coroutine thread switches.
 */
public object CurrentTestCoroutineContext {
    private val coroutineThreadLocal = InheritableThreadLocal<String>()

    public fun asContextElement(uniqueId: String): CoroutineContext =
        coroutineThreadLocal.asContextElement(value = uniqueId)

    public fun get(): String? = coroutineThreadLocal.get()
}
