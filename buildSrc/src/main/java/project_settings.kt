import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency

object Ver {
    val androidBuildTools = VersionConstraintAction(preferred = "3.6.1", required = "[3.6,4.0)")
    val dokka = VersionConstraintAction(preferred = "0.10.1", required = "[0.10.1,1.0)")
    val kotlin = VersionConstraintAction(preferred = "1.3.70", required = "[1.3.20,2.0)")

    class VersionConstraintAction(private val preferred: String, private val required: String) :
        Action<ExternalModuleDependency> {
        override fun execute(t: ExternalModuleDependency) {
            t.version {
                require(required)
                prefer(preferred)
            }
        }
    }
}
