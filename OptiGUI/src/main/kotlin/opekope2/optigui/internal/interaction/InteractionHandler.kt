package opekope2.optigui.internal.interaction

import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.*
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.neoforged.neoforge.client.event.ScreenEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import opekope2.optigui.interaction.IBeforeInteractionBeginCallback
import opekope2.optigui.interaction.Interaction
import opekope2.optigui.internal.TextureReplacer
import opekope2.optigui.registry.ContainerDefaultGuiTextureRegistry
import opekope2.optigui.util.identifier
import opekope2.optigui.util.interactionData
import opekope2.optigui.util.invalidateCachedReplacement

internal object InteractionHandler : IBeforeInteractionBeginCallback {
    fun initialize() {
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock::class.java, ::interactWithBlock)
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract::class.java, ::interactWithEntity)
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickItem::class.java, ::interactWithItem)
        IBeforeInteractionBeginCallback.register(this)
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Pre::class.java, ::beforeExtract)
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post::class.java, ::afterExtract)
    }

    private fun interactWithBlock(event: PlayerInteractEvent.RightClickBlock) {
        val world = event.level
        if (!world.isClientSide) return

        val player = event.entity
        val hand = event.hand
        val hitResult = event.hitVec
        val container = world.getBlockState(hitResult.blockPos).block.identifier
        val blockEntity = world.getBlockEntity(hitResult.blockPos)

        if (blockEntity != null) {
            Interaction.prepare(container, player, world, hand, hitResult, null, blockEntity)
            return
        }

        if (container in ContainerDefaultGuiTextureRegistry) {
            Interaction.prepare(container, player, world, hand, hitResult, null)
        }
    }

    private fun interactWithEntity(event: PlayerInteractEvent.EntityInteract) {
        val world = event.level
        if (!world.isClientSide) return

        val entity = event.target
        val hitResult = EntityHitResult(entity, entity.position().add(event.location))
        val container = entity.identifier
        Interaction.prepare(container, event.entity, world, event.hand, hitResult, null, entity)
    }

    private fun interactWithItem(event: PlayerInteractEvent.RightClickItem) {
        val world = event.level
        if (!world.isClientSide) return

        val player = event.entity
        val stack = player.getItemInHand(event.hand)
        if (stack.`is`(Items.WRITABLE_BOOK) || stack.`is`(Items.WRITTEN_BOOK)) {
            Interaction.prepare(stack.item.identifier, player, world, InteractionHand.MAIN_HAND, null, BookExtraProperties(0, 0))
            // BookExtraProperties will be updated later
        }
    }

    override fun onBeforeBegin(screen: Screen) {
        when (screen) {
            is BookEditScreen -> tryUpdateBookProperties(screen.currentPage + 1, screen.numPages)
            is BookViewScreen -> tryUpdateBookProperties(screen.currentPage + 1, screen.numPages)
        }
    }

    private fun beforeExtract(event: ScreenEvent.Render.Pre) {
        TextureReplacer.isReplacingTextures = true
    }

    private fun afterExtract(event: ScreenEvent.Render.Post) {
        TextureReplacer.isReplacingTextures = false
    }

    @JvmStatic
    fun interact(player: Player, world: Level, currentScreen: Screen) {
        val container = when (currentScreen) {
            is InventoryScreen -> Identifier.withDefaultNamespace("player")
            is CreativeModeInventoryScreen -> Identifier.withDefaultNamespace("player")
            is HangingSignEditScreen -> world.getBlockState(currentScreen.sign.blockPos).block.identifier
            else -> return
        }

        Interaction.prepare(container, player, world, InteractionHand.MAIN_HAND, null, null)
    }

    @JvmStatic
    fun tryUpdateBookProperties(currentPage: Int, pageCount: Int) {
        (interactionData?.extra as? BookExtraProperties)?.let {
            it.currentPage = currentPage
            it.pageCount = pageCount
            invalidateCachedReplacement()
        }
    }
}
