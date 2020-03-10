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
