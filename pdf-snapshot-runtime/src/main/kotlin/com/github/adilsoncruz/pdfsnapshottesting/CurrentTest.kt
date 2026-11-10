package com.github.adilsoncruz.pdfsnapshottesting

public object CurrentTest {
    private val threadLocal = InheritableThreadLocal<String>()

    public fun set(uniqueId: String) {
        threadLocal.set(uniqueId)
    }

    public fun get(): String? = CurrentTestCoroutineContext.get() ?: threadLocal.get()

    public fun clear() {
        threadLocal.remove()
    }
}
