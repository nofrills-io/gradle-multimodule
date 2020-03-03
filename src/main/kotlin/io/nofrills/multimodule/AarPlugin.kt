package io.nofrills.multimodule

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project

class AarPlugin : AndroidPlugin() {
    override val androidPluginId: String = PLUGIN_ID_ANDROID_LIBRARY

    override fun getComponentNameForVariant(variant: BaseVariant): String {
        return variant.name
    }

    override fun getDefaultPublishVariant(project: Project): BaseVariant? {
        val libraryExtension = project.extensions.getByType(LibraryExtension::class.java)
        return libraryExtension.libraryVariants.find { it.name == libraryExtension.defaultPublishConfig }
    }

    override fun getPublicationNameForVariant(variant: BaseVariant): String {
        return "${variant.name}Aar"
    }
}
