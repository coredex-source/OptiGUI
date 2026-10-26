package opekope2.optigui.internal.selector

import net.neoforged.fml.ModList
import opekope2.optigui.internal.util.*
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import kotlin.jvm.optionals.getOrNull

private val versionOperators = listOf(">=", "<=", ">", "<", "=", "~", "^")

internal class ConditionalLoadSelector : (String) -> Boolean {
    override fun invoke(selectorValue: String) = selectorValue.toBooleanStrict()
}

internal class ModsLoadSelector : (String) -> Boolean {
    override fun invoke(selectorValue: String): Boolean {
        val modCheckResults = selectorValue.splitIgnoreEmpty(*delimiters)
            ?.assertNotEmpty()
            ?.mapNotNull(::parseVersion) {
                throw RuntimeException("Invalid mod predicates: ${joinNotFound(it)}")
            }
            ?.assertNotEmpty()
            ?.map { (modId, versionPredicate) ->
                ModList.get().getModContainerById(modId).getOrNull()?.modInfo?.version?.let(versionPredicate)
                    ?: false
            }
            ?: return false

        return modCheckResults.all { it }
    }

    private fun parseVersion(modWithVersion: String): Pair<String, (ArtifactVersion) -> Boolean>? {
        return try {
            val versionIndex = modWithVersion.indexOfAny(versionOperators)
            if (versionIndex < 0) {
                return modWithVersion to { true }
            }

            val modId = modWithVersion.substring(0 until versionIndex)
            val modVersion = modWithVersion.substring(versionIndex).ifEmpty { return modId to { true } }
            val versionPredicate = parseVersionPredicate(modVersion)

            modId to versionPredicate
        } catch (e: Exception) {
            null
        }
    }

    private fun parseVersionPredicate(predicate: String): (ArtifactVersion) -> Boolean {
        val operator = versionOperators.first(predicate::startsWith)
        val referenceString = predicate.substring(operator.length)
        require(referenceString.isNotEmpty()) { "Missing version after `$operator`" }
        val reference = DefaultArtifactVersion(referenceString)

        return when (operator) {
            ">=" -> { version -> version >= reference }
            "<=" -> { version -> version <= reference }
            ">" -> { version -> version > reference }
            "<" -> { version -> version < reference }
            "=" -> { version -> version.compareTo(reference) == 0 }
            "~" -> { version ->
                version >= reference && version.majorVersion == reference.majorVersion &&
                        version.minorVersion == reference.minorVersion
            }
            else -> { version -> version >= reference && version.majorVersion == reference.majorVersion }
        }
    }
}
