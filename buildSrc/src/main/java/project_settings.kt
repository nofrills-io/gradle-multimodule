import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency

object Ver {
    object AndroidBuildTools : VersionConstraintAction(preferred = "3.6.1", required = "[3.5,4.0)")
    object Dokka : VersionConstraintAction(preferred = "0.10.1", required = "[0.10,1.0)")
    object Kotlin : VersionConstraintAction(preferred = "1.3.70", required = "[1.3,2.0)")

    abstract class VersionConstraintAction(private val preferred: String, private val required: String) :
        Action<ExternalModuleDependency> {
        override fun execute(t: ExternalModuleDependency) {
            t.version {
                require(required)
                prefer(preferred)
            }
        }
    }
}
