package com.github.adilsoncruz.pdfsnapshottesting

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport

public object SnapshotJsonSerializer {
    private val mapper =
        jacksonObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)

    public fun serialize(value: SnapshotReport): String {
        return mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(value)
    }

    public fun deserialize(json: String): SnapshotReport {
        return mapper.readValue(json)
    }
}
