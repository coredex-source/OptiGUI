package opekope2.optigui.interaction

import net.minecraft.client.gui.screens.Screen
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Callback fired before an interaction begins.
 */
fun interface IBeforeInteractionBeginCallback {
    /**
     * Called right before an interaction begins. This is the last place when calling [Interaction.prepare] is valid.
     *
     * @param screen The screen, which triggered the interaction and is about to be opened
     */
    fun onBeforeBegin(screen: Screen)

    companion object {
        private val callbacks = CopyOnWriteArrayList<IBeforeInteractionBeginCallback>()

        /**
         * Event fired before an interaction begins.
         */
        @JvmStatic
        fun register(callback: IBeforeInteractionBeginCallback) {
            callbacks += callback
        }

        internal fun fire(screen: Screen) {
            callbacks.forEach { callback ->
                callback.onBeforeBegin(screen)
            }
        }
    }
}
