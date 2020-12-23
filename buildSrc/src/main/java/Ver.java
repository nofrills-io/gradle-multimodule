import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;

public class Ver {
    public static final VersionConstraintAction androidBuildTools = new VersionConstraintAction("4.1.1", "[4.1.0,4.2)");
    public static final VersionConstraintAction dokka = new VersionConstraintAction("1.4.10.2", "[1.4.10,1.5)");
    public static final VersionConstraintAction kotlin = new VersionConstraintAction("1.4.20", "[1.4.10,1.5)");

    public static class VersionConstraintAction implements Action<ExternalModuleDependency> {
        private final String preferred;
        private final String required;

        public VersionConstraintAction(String preferred, String required) {
            this.preferred = preferred;
            this.required = required;
        }

        @Override
        public void execute(ExternalModuleDependency externalModuleDependency) {
            externalModuleDependency.version(mutableVersionConstraint -> {
                mutableVersionConstraint.require(required);
                mutableVersionConstraint.prefer(preferred);
            });
        }
    }
}
