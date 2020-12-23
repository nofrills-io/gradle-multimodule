/*
 *    Copyright 2020 Mateusz Armatys
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.nofrills.multimodule

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project

class ApkPlugin : AndroidPlugin() {
    override val androidPluginId: String = PLUGIN_ID_ANDROID_APP

    override fun getBaseVariants(project: Project): Collection<BaseVariant> {
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        return appExtension.applicationVariants
    }

    override fun getComponentNameForVariant(variant: BaseVariant): String {
        return "${variant.name}_apk"
    }

    override fun getPublicationNameForVariant(variant: BaseVariant): String {
        return "${variant.name}Apk"
    }
}
