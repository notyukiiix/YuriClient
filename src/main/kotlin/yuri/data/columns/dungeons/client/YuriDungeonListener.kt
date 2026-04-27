package yuri.data.columns.dungeons.client

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.network.chat.Component
import yuri.data.columns.dungeons.client.enums.Blessing
import yuri.data.columns.dungeons.client.enums.DungeonClass
import yuri.data.columns.dungeons.client.enums.Puzzle
import yuri.data.columns.dungeons.map.DungeonInfo
import yuri.data.columns.dungeons.map.core.RoomState
import yuri.util.DungeonUtils

/**
 * Client-side dungeon tab list, chat hooks, and timer state (tab parsing every 10 ticks).
 */
object YuriDungeonListener {
    private val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+) .*?\\((\\w+)(?: (\\w+))*\\)$")
    private val puzzleCountRegex = Regex("§b§lPuzzles: §f\\((?<count>\\d)\\)")
    private val puzzleRegex = Regex(" (.+): \\[[✦✔✖].+")
    private val runEndRegex = Regex("^\\s*(Master Mode)? ?(?:The)? Catacombs - (Floor (.{1,3})|Entrance)$")

    var dungeonTeammates = mutableListOf<DungeonPlayer>()
    var dungeonTeammatesNoSelf: List<DungeonPlayer> = emptyList()
    var thePlayer: DungeonPlayer? = null

    var maxPuzzleCount = 0
    var puzzles = mutableListOf<Puzzle>()

    data class DualTime(val ticks: Long, val real: Long = System.currentTimeMillis())

    var dungeonStarted = false
    var dungeonStartTime: DualTime? = null
    var dungeonEnded = false

    var bloodOpenTime: DualTime? = null
    var watcherClearTime: DualTime? = null
    var watcherFinishSpawnTime: Long? = null
    var bossEntryTime: DualTime? = null
    var dungeonEndTime: Long? = null

    var lastDoorOpenner: DungeonPlayer? = null

    var currentTime = 0L
    var doorKeys = 0

    private var tickCounter = 0
    private var registered = false

    @JvmStatic
    fun registerEvents() {
        if (registered) return
        registered = true

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ -> resetWorld() }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!DungeonUtils.inDungeons()) return@register
            currentTime++
            tickCounter++
            if (tickCounter < 10) return@register
            tickCounter = 0
            refreshTabList(client)
        }

        ClientReceiveMessageEvents.GAME.register { message, _ ->
            if (!DungeonUtils.inDungeons()) return@register
            onGameMessage(message)
        }
    }

    private fun resetWorld() {
        dungeonStarted = false
        dungeonTeammates = mutableListOf()
        dungeonTeammatesNoSelf = emptyList()
        thePlayer = null
        maxPuzzleCount = 0
        puzzles.clear()
        dungeonStartTime = null
        dungeonEnded = false
        bloodOpenTime = null
        watcherClearTime = null
        watcherFinishSpawnTime = null
        bossEntryTime = null
        dungeonEndTime = null
        lastDoorOpenner = null
        currentTime = 0
        doorKeys = 0
        Blessing.reset()
    }

    private fun refreshTabList(client: Minecraft) {
        val conn = client.connection ?: return
        val selfName = client.gameProfile.name
        val infos: Collection<PlayerInfo> = conn.listedOnlinePlayers
        dungeonTeammates.clear()
        for (info in infos) {
            val tab = info.tabListDisplayName ?: Component.literal(info.profile.name)
            val text = tab.string
            updateDungeonTeammates(text.stripSection(), info)
        }
        thePlayer = dungeonTeammates.find { it.name == selfName }
        dungeonTeammatesNoSelf = dungeonTeammates.filter { it != thePlayer }
        dungeonTeammates.forEach { teammate ->
            if (teammate.entity == null) {
                teammate.entity = client.level?.players()?.find { it.name.string == teammate.name }
            }
        }
    }

    private fun onGameMessage(message: Component) {
        val text = message.string
        val unformatted = text.stripSection()
        when {
            unformatted.matches(runEndRegex) -> {
                dungeonEnded = true
                dungeonEndTime = currentTime
            }
            unformatted == "[BOSS] The Watcher: You have proven yourself. You may pass." -> {
                DungeonInfo.uniqueRooms["Blood"]?.mainRoom?.state = RoomState.GREEN
                watcherClearTime = DualTime(currentTime)
            }
            unformatted == "[BOSS] The Watcher: That will be enough for now." -> {
                DungeonInfo.uniqueRooms["Blood"]?.mainRoom?.state = RoomState.CLEARED
                watcherFinishSpawnTime = currentTime
            }
        }
    }

    private fun updateDungeonTeammates(tabName: String, second: PlayerInfo) {
        val match = tablistRegex.find(tabName) ?: return
        val g = match.groupValues
        if (g.size < 4) return
        val name = g[2]
        val clazz = g[3]
        val clazzLevel = g.getOrNull(4).orEmpty().ifEmpty { "0" }
        val skin = DefaultPlayerSkin.getDefaultTexture()

        dungeonTeammates.find { it.name == name }?.let { current ->
            current.clazz = if (clazz != "DEAD") DungeonClass.fromName(clazz) else current.clazz
            current.clazzLvl = clazzLevel.romanOrInt()
            current.skin = skin
            current.isDead = clazz == "DEAD"
        } ?: dungeonTeammates.add(
            DungeonPlayer(
                name,
                DungeonClass.fromName(clazz),
                clazzLevel.romanOrInt(),
                skin,
                clazz == "DEAD",
            )
        )

        updatePuzzleCount(tabName)
        updatePuzzles(tabName)
    }

    private fun updatePuzzleCount(tabName: String) {
        if (maxPuzzleCount != 0) return
        if (tabName.contains("Puzzles: ")) {
            maxPuzzleCount = puzzleCountRegex.find(tabName)?.groups?.get("count")?.value?.toIntOrNull() ?: maxPuzzleCount
            repeat(maxPuzzleCount) { puzzles.add(Puzzle.UNKNOWN) }
        }
    }

    private fun updatePuzzles(tabName: String) {
        val line = tabName.stripSection()
        val name = puzzleRegex.find(line)?.groupValues?.get(1) ?: return
        val newState = when {
            "✦" in line && "???" in line -> RoomState.UNOPENED
            "✦" in line -> RoomState.DISCOVERED
            "✖" in line -> RoomState.FAILED
            "✔" in line -> RoomState.GREEN
            else -> return
        }
        val detected = Puzzle.fromName(name) ?: return
        val puzzle = puzzles.find { it == detected && it != Puzzle.UNKNOWN } ?: run {
            if (detected != Puzzle.UNKNOWN) {
                puzzles.find { it == Puzzle.UNKNOWN }?.let {
                    puzzles.remove(it)
                    puzzles.add(detected)
                    return@run detected
                }
            }
            detected
        }
        if (puzzle != Puzzle.UNKNOWN && puzzle.state != newState) {
            puzzle.state = newState
        }
    }
}
