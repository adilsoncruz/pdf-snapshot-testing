package com.github.adilsoncruz.pdfsnapshottesting

import org.junit.jupiter.api.extension.ExtendWith
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@ExtendWith(SnapshotExtension::class)
public annotation class EnablePdfSnapshots
