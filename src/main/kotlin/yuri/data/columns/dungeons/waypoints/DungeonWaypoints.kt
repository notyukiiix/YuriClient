package yuri.data.columns.dungeons.waypoints

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.BlockPos
import yuri.data.columns.dungeons.map.core.RoomState
import yuri.data.columns.dungeons.map.utils.ScanUtils
import yuri.data.columns.dungeons.client.YuriDungeonEvents
import yuri.data.columns.dungeons.client.enums.SecretType
import yuri.data.columns.dungeons.modules.DungeonWaypointsModule
import java.awt.Color
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Dungeon waypoint / secret overlay state. World rendering is a placeholder until a line renderer is wired.
 */
object DungeonWaypoints {
    data class DungeonWaypoint(
        val pos: BlockPos,
        val color: Color,
        val filled: Boolean,
        val outline: Boolean,
        val phase: Boolean,
    )

    private data class SecretWaypoint(val pos: BlockPos, val type: SecretType, val color: Color)

    private val gson = Gson()
    private val configPath = FabricLoader.getInstance().configDir.resolve("yuri/dungeon_waypoints.json")

    private val secretPositions by lazy { ScanUtils.roomList.associate { it.name to it.secretCoords } }

    val waypoints = mutableMapOf<String, List<DungeonWaypoint>>()
    val currentRoomWaypoints = CopyOnWriteArrayList<DungeonWaypoint>()
    private val currentSecrets = CopyOnWriteArrayList<SecretWaypoint>()

    private var registered = false

    @JvmStatic
    fun registerEvents() {
        if (registered) return
        registered = true

        loadConfig()

        YuriDungeonEvents.registerRoomEnter { event ->
            if (!DungeonWaypointsModule.isActive()) return@registerRoomEnter
            currentRoomWaypoints.clear()
            currentSecrets.clear()

            val room = event.room
            val roomName = room.name
            val roomRotation = 360 - (room.rotation ?: return@registerRoomEnter)
            val roomCorner = room.corner ?: return@registerRoomEnter

            waypoints[roomName]?.map { wp ->
                wp.copy(pos = ScanUtils.getRealCoord(wp.pos, roomCorner, roomRotation))
            }?.let { currentRoomWaypoints.addAll(it) }

            if (room.mainRoom.state == RoomState.GREEN) return@registerRoomEnter
            val coords = secretPositions[roomName] ?: return@registerRoomEnter

            fun addSecrets(list: List<BlockPos>, type: SecretType, color: Color) {
                list.forEach { p ->
                    currentSecrets.add(SecretWaypoint(ScanUtils.getRealCoord(p, roomCorner, roomRotation), type, color))
                }
            }
            addSecrets(coords.redstoneKey, SecretType.REDSTONE_KEY, Color.RED)
            addSecrets(coords.wither, SecretType.WITHER_ESSANCE, Color.BLACK)
            addSecrets(coords.bat, SecretType.BAT, Color.GREEN)
            addSecrets(coords.item, SecretType.ITEM, Color.MAGENTA)
            addSecrets(coords.chest, SecretType.CHEST, Color.MAGENTA)
        }

        YuriDungeonEvents.registerSecretFound { event ->
            if (!DungeonWaypointsModule.isActive() || currentSecrets.isEmpty()) return@registerSecretFound
            if (event.type == SecretType.LEVER) return@registerSecretFound
            val target = currentSecrets.find { it.pos == event.pos }
            if (target != null) currentSecrets.remove(target)
        }

        WorldRenderEvents.END_MAIN.register(::renderWaypointsPlaceholder)

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            currentSecrets.clear()
            currentRoomWaypoints.clear()
        }
    }

    private fun renderWaypointsPlaceholder(@Suppress("UNUSED_PARAMETER") context: WorldRenderContext) {
        if (!DungeonWaypointsModule.isActive()) return
        // Placeholder: draw waypoint / secret boxes in world space.
    }

    private fun loadConfig() {
        runCatching {
            if (!Files.exists(configPath)) return
            val type = object : TypeToken<MutableMap<String, List<DungeonWaypoint>>>() {}.type
            val loaded = gson.fromJson<MutableMap<String, List<DungeonWaypoint>>>(Files.newBufferedReader(configPath), type)
            if (loaded != null) waypoints.putAll(loaded)
        }
    }

    @JvmStatic
    fun saveConfig() {
        runCatching {
            Files.createDirectories(configPath.parent)
            Files.newBufferedWriter(configPath).use { w -> gson.toJson(waypoints, w) }
        }
    }
}
