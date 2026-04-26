package com.github.adilsoncruz.pdfsnapshottesting.kotest

public object CurrentKotestTest {
    private val threadLocal = ThreadLocal<String>()

    public fun set(uniqueId: String) {
        threadLocal.set(uniqueId)
    }

    public fun get(): String? {
        return threadLocal.get()
    }

    public fun clear() {
        threadLocal.remove()
    }
}
