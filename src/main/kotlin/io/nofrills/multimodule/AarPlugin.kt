package io.nofrills.multimodule

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project

class AarPlugin : AndroidPlugin() {
    override val androidPluginId: String = PLUGIN_ID_ANDROID_LIBRARY

    override fun getBaseVariants(project: Project): Collection<BaseVariant> {
        val libraryExtension = project.extensions.getByType(LibraryExtension::class.java)
        return libraryExtension.libraryVariants
    }

    override fun getComponentNameForVariant(variant: BaseVariant): String {
        return variant.name
    }

    override fun getPublicationNameForVariant(variant: BaseVariant): String {
        return "${variant.name}Aar"
    }
}
