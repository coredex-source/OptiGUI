@file: JvmName("ContainerTexturePath")

package opekope2.optigui.registry

import com.electronwill.nightconfig.core.UnmodifiableConfig
import net.minecraft.resources.Identifier
import net.neoforged.fml.ModList
import net.neoforged.neoforgespi.language.IModInfo

/**
 * Texture paths for Minecraft containers.
 *
 * To register your mod's containers' default GUI textures, add a mod property table named
 * `optigui:container_default_gui_textures` to your `neoforge.mods.toml`. They keys are the container IDs, and the
 * values are the default GUI textures:
 */
object ContainerDefaultGuiTextureRegistry {
    private const val CONTAINER_DEFAULT_TEXTURES_KEY = "optigui:container_default_gui_textures"

    private lateinit var containerTextures: MutableMap<Identifier, Identifier>

    internal fun initialize() {
        containerTextures = mutableMapOf()
        for (mod in ModList.get().mods) {
            try {
                loadModTextures(mod)
            } catch (e: Exception) {
                throw RuntimeException("Failed to load container default GUI textures from mod `${mod.modId}`", e)
            }
        }
    }

    private fun loadModTextures(mod: IModInfo) {
        val textures = when (val property = mod.modProperties[CONTAINER_DEFAULT_TEXTURES_KEY] ?: return) {
            is UnmodifiableConfig -> property.entrySet().associate { it.key to it.getValue<Any?>() }
            is Map<*, *> -> property
            else -> throw RuntimeException("`$CONTAINER_DEFAULT_TEXTURES_KEY` must be a table")
        }

        for ((key, value) in textures) {
            val valueString = value as String

            val keyId = Identifier.parse(key as String)
            val valueId = Identifier.parse(valueString)

            if (keyId in containerTextures) {
                throw RuntimeException("A default GUI texture for container `$key` has already been registered")
            }

            containerTextures[keyId] = valueId
        }
    }

    /**
     * Checks if the default GUI texture of a container is known.
     *
     * @param container The id of the entity or block entity
     */
    @JvmStatic
    operator fun contains(container: Identifier) = container in containerTextures

    /**
     * Gets the default GUI texture of the given container, or `null`, if it's unknown.
     *
     * @param container The id of the entity or block entity to get its GUI texture
     */
    @JvmStatic
    operator fun get(container: Identifier) = containerTextures[container]
}
