package yuri.data.columns.dungeons

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component
import yuri.util.DungeonUtils

/**
 * Tracks wither / blood keys from dungeon chat so Door ESP can tint "openable" doors
 * without a full dungeon map (mirrors legitcat [DoorEsp] chat state machine).
 */
object DungeonDoorChatState {
    private var witherKeys: Int = 0
    private var bloodKey: Boolean = false
    private var bloodOpened: Boolean = false
    private var eventsRegistered: Boolean = false

    @JvmStatic
    fun getWitherKeys(): Int = witherKeys

    @JvmStatic
    fun hasBloodKey(): Boolean = bloodKey

    @JvmStatic
    fun isBloodDoorOpened(): Boolean = bloodOpened

    /**
     * Openable highlight: have a wither key and/or blood key while blood door still exists.
     */
    @JvmStatic
    fun shouldHighlightOpenableRough(): Boolean =
        witherKeys > 0 || (bloodKey && !bloodOpened)

    @JvmStatic
    fun registerEvents() {
        if (eventsRegistered) {
            return
        }
        eventsRegistered = true
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            onGameMessage(message)
        }
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            reset()
        }
    }

    private fun reset() {
        witherKeys = 0
        bloodKey = false
        bloodOpened = false
    }

    private fun onGameMessage(message: Component) {
        if (!DungeonUtils.inDungeons()) {
            return
        }
        val text = stripFormatting(message.string).trim()
        val dm = DungeonMessages
        val witherClaim = dm.witherKeyClaimPattern.matcher(text).matches()
        val witherPickup = text == dm.witherKeyPickedUpString
        if (witherClaim || witherPickup) {
            witherKeys++
            return
        }
        if (dm.witherDoorOpenPattern.matcher(text).matches()) {
            if (witherKeys > 0) {
                witherKeys--
            }
            return
        }
        val bloodClaim = dm.bloodKeyClaimPattern.matcher(text).matches()
        val bloodPickup = text == dm.bloodKeyPickedUpString
        if (!bloodClaim && !bloodPickup) {
            if (text == dm.bloodDoorOpenMessage) {
                bloodKey = false
                bloodOpened = true
            }
            return
        }
        bloodKey = true
    }

    private fun stripFormatting(raw: String): String =
        raw.replace(Regex("(?i)§[0-9a-fk-or]"), "").trim()
}
