package io.nofrills.multimodule

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project

class ApkPlugin : AndroidPlugin() {
    override val androidPluginId: String = PLUGIN_ID_ANDROID_APP

    override fun getComponentNameForVariant(variant: BaseVariant): String {
        return "${variant.name}_apk"
    }

    override fun getDefaultPublishVariant(project: Project): BaseVariant? {
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        return appExtension.applicationVariants.find { it.name == appExtension.defaultPublishConfig }
    }

    override fun getPublicationNameForVariant(variant: BaseVariant): String {
        return "${variant.name}Apk"
    }
}
