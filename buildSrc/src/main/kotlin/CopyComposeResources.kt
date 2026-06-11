import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Workaround for CMP 1.10.3 + com.android.kotlin.multiplatform.library.
 *
 * The compose plugin registers :composeApp's prepared commonMain compose
 * resources on the consuming app via the legacy android sourceSets asset API,
 * and that registration silently drops the converted XML value resources (the
 * .cvr files that back stringResource / pluralStringResource) — only raw
 * resources such as fonts come through. The app then crashes at the first
 * stringResource with MissingResourceException.
 *
 * Touching the variant `sources.assets` API at all disables that legacy
 * registration, so this task takes over completely: it copies the entire
 * prepared compose-resources tree (fonts AND value resources) under the package
 * path the generated Res accessors expect, and is wired into a variant via
 * addGeneratedSourceDirectory so every consumer (merge, lint, package) picks up
 * the dependency. Revisit when the compose plugin fixes value-resource packaging
 * for the AGP KMP library plugin.
 */
abstract class CopyComposeResources @Inject constructor(
    private val fs: FileSystemOperations,
) : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val preparedResources: DirectoryProperty

    @get:Input
    abstract val resourcePackage: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copy() {
        val target = outputDir.dir("composeResources/${resourcePackage.get()}")
        fs.sync {
            from(preparedResources)
            into(target)
        }
    }
}
