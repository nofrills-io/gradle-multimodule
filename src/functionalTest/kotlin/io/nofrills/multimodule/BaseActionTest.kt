package io.nofrills.multimodule

import org.junit.After
import java.io.File

@Suppress("FunctionName")
abstract class BaseActionTest {
    companion object {
        internal val androidTypes = setOf(aar, apk)
        internal const val baseAndroidConfig = "android { compileSdkVersion(28) }"
    }

//    @After
//    fun tearDown() {
//        File(FunctionalTestPath).deleteRecursively()
//    }
}
