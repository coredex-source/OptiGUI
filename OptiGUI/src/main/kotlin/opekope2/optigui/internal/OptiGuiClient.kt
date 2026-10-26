package opekope2.optigui.internal

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import opekope2.optigui.internal.interaction.InteractionHandler
import opekope2.optigui.internal.selector.registerLoadSelectors
import opekope2.optigui.internal.selector.registerSelectors
import opekope2.optigui.registry.ContainerDefaultGuiTextureRegistry
import opekope2.optigui.util.MOD_ID

@Mod(MOD_ID, dist = [Dist.CLIENT])
internal class OptiGuiClient(modEventBus: IEventBus) {
    init {
        initialize(modEventBus)
        registerSelectors()
        registerLoadSelectors()
        ContainerDefaultGuiTextureRegistry.initialize()
        TextureReplacer.initialize()
        FilterLoader.initialize(modEventBus)
        InteractionHandler.initialize()
    }
}
