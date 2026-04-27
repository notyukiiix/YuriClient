package yuri

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import yuri.client.ChatCopyHelper
import yuri.client.CustomScoreboardHudLayer
import yuri.client.ImageHudHudLayer
import yuri.client.ShortCommandHelper
import yuri.data.columns.cheats.DungeonDoorTracker
import yuri.data.columns.cheats.modules.DoorEspModule
import yuri.data.columns.cheats.modules.MobEspModule
import yuri.data.columns.dungeons.DungeonDoorChatState
import yuri.data.columns.dungeons.map.handlers.ScoreCalculation
import yuri.data.columns.dungeons.modules.DungeonMapModule
import yuri.data.columns.dungeons.modules.KeyHighlightModule
import yuri.data.columns.dungeons.client.YuriDungeonListener
import yuri.data.columns.dungeons.waypoints.DungeonWaypoints
import yuri.data.columns.visual.modules.YuriHudMetricsModule
import yuri.data.columns.visual.modules.YuriVisualBundleModule
import yuri.data.columns.general.modules.ChatModule
import yuri.data.columns.dev.modules.OpsecModule
import yuri.data.columns.visual.modules.FullbrightModule
import yuri.data.columns.visual.modules.ImageHudModule
import yuri.data.columns.general.modules.SillySpeakModule

class YuriClientMod : ClientModInitializer {
    override fun onInitializeClient() {
        GlobalCosmetics.init()
        YuriConfig.load()
        ImageHudHudLayer.register()
        CustomScoreboardHudLayer.register()
        MobEspModule.registerEvents()
        DungeonDoorChatState.registerEvents()
        DungeonDoorTracker.registerEvents()
        DoorEspModule.registerEvents()
        KeyHighlightModule.registerEvents()
        DungeonMapModule.registerEvents()
        YuriDungeonListener.registerEvents()
        DungeonWaypoints.registerEvents()
        ScoreCalculation.registerEvents()
        YuriHudMetricsModule.registerEvents()
        YuriVisualBundleModule.registerEvents()

        ClientLifecycleEvents.CLIENT_STARTED.register {
            ImageHudModule.reloadTexture()
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            GlobalCosmetics.refreshResolvedNames(client)
            FullbrightModule.onClientTick(client)
            while (OPEN_GUI_KEY.consumeClick()) {
                openGui(client)
            }

            if (openGuiQueued) {
                openGuiQueued = false
                openGui(client)
            }

            OpsecModule.applyLocalNametag(client)

            if (ChatModule.shouldClickToCopy()) {
                val rightDown =
                    GLFW.glfwGetMouseButton(client.window.handle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS
                if (rightDown && !chatCopyRightMouseWasDown) {
                    ChatCopyHelper.tryCopyUnderCursor(client)
                }
                chatCopyRightMouseWasDown = rightDown
            } else {
                chatCopyRightMouseWasDown = false
            }
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("yuri")
                    .then(
                        ClientCommandManager.literal("hud").executes { ctx ->
                            val client = ctx.source.client
                            client.setScreen(YuriHudEditScreen(client.screen))
                            1
                        }
                    )
                    .executes {
                        queueOpenGui()
                        1
                    }
            )
        }

        ClientSendMessageEvents.ALLOW_COMMAND.register { command ->
            val trimmed = command?.trim().orEmpty()
            val body = normalizedOutgoingCommandBody(command)
            if (ChatModule.shouldShortCommands()) {
                val rewritten = ShortCommandHelper.rewriteShortCommand(trimmed)
                if (rewritten != null && !rewritten.equals(trimmed, ignoreCase = true)) {
                    val mc = Minecraft.getInstance()
                    val normalized = rewritten.removePrefix("/")
                    mc.execute {
                        mc.player?.connection?.sendCommand(normalized)
                    }
                    return@register false
                }
            }
            if (isYuriHudCommand(body)) {
                openYuriHudEditor()
                return@register false
            }
            if (!isYuriCommand(body)) {
                return@register true
            }

            queueOpenGui()
            false
        }

        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            val body = normalizedOutgoingCommandBody(message)
            if (isYuriHudCommand(body)) {
                openYuriHudEditor()
                return@register false
            }
            true
        }

        ClientSendMessageEvents.MODIFY_CHAT.register { message ->
            SillySpeakModule.transformOutgoingMessage(message)
        }
    }

    companion object {
        private var openGuiKey: InputConstants.Key =
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_SHIFT)

        private val OPEN_GUI_KEY: KeyMapping = KeyBindingHelper.registerKeyBinding(
            KeyMapping("key.yuri.open_gui", openGuiKey.type, openGuiKey.value, KeyMapping.Category.MISC)
        )

        private var openGuiQueued: Boolean = false

        private var chatCopyRightMouseWasDown: Boolean = false

        private val commandWhitespaceRegex = Regex("\\s+")

        private fun normalizedOutgoingCommandBody(command: String?): String {
            if (command.isNullOrBlank()) {
                return ""
            }
            return command.trim()
                .removePrefix("/")
                .replace(commandWhitespaceRegex, " ")
                .lowercase()
        }

        private fun isYuriHudCommand(normalizedBodyLowercase: String): Boolean {
            return normalizedBodyLowercase == "yuri hud" ||
                normalizedBodyLowercase.startsWith("yuri hud ")
        }

        private fun isYuriCommand(normalizedBodyLowercase: String): Boolean {
            return normalizedBodyLowercase == "yuri" || normalizedBodyLowercase.startsWith("yuri ")
        }

        private fun openYuriHudEditor() {
            val mc = Minecraft.getInstance()
            mc.setScreen(YuriHudEditScreen(mc.screen))
        }

        private fun queueOpenGui() {
            openGuiQueued = true
        }

        @JvmStatic
        fun setOpenGuiKey(key: InputConstants.Key?) {
            if (key == null) {
                return
            }

            openGuiKey = key
            OPEN_GUI_KEY.setKey(key)
            KeyMapping.resetMapping()
        }

        @JvmStatic
        fun setOpenGuiKeyCode(keyCode: Int) {
            setOpenGuiKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode))
        }

        @JvmStatic
        fun getOpenGuiKey(): InputConstants.Key = openGuiKey

        @JvmStatic
        fun getOpenGuiKeyType(): String = openGuiKey.type.name

        @JvmStatic
        fun getOpenGuiKeyCode(): Int = openGuiKey.value

        @JvmStatic
        fun getOpenGuiKeyName(): String = OPEN_GUI_KEY.translatedKeyMessage.string

        private fun openGui(client: Minecraft?) {
            if (client != null) {
                client.setScreen(YuriScreen())
            }
        }

    }
}
