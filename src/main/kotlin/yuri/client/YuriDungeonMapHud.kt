package yuri.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import yuri.data.columns.dungeons.map.DungeonInfo
import yuri.data.columns.dungeons.map.compat.DungeonMapConfig
import yuri.data.columns.dungeons.map.compat.DungeonMapLocation
import yuri.data.columns.dungeons.map.core.Door
import yuri.data.columns.dungeons.map.core.DoorType
import yuri.data.columns.dungeons.map.core.Room
import yuri.data.columns.dungeons.map.core.RoomState
import yuri.data.columns.dungeons.map.core.RoomType
import yuri.data.columns.dungeons.map.core.Tile
import yuri.data.columns.dungeons.map.core.Unknown
import yuri.data.columns.dungeons.map.handlers.DungeonPathFinder
import yuri.data.columns.dungeons.map.handlers.HotbarMapColorParser
import yuri.data.columns.dungeons.map.handlers.ScoreCalculation
import yuri.data.columns.dungeons.map.utils.MapUtils
import yuri.data.columns.dungeons.client.YuriDungeonListener
import yuri.data.columns.dungeons.modules.DungeonMapModule
import net.minecraft.network.chat.Component
import yuri.util.DungeonUtils
import java.awt.Color
import kotlin.math.roundToInt

/**
 * In-game dungeon map HUD (grid, connectors, checkmarks, teammate dots, optional score strip).
 * Ported from the NoammAddons dungeon map renderer, trimmed to Fabric [GuiGraphics] only (no external Render2D).
 */
object YuriDungeonMapHud {
    @JvmField
    var hudX: Int = 8

    @JvmField
    var hudY: Int = 8

    private const val MAP_W = 128

    fun render(graphics: GuiGraphics) {
        if (!DungeonMapModule.module.enabled || !DungeonUtils.inDungeons()) {
            return
        }
        if (DungeonInfo.mapData == null || !MapUtils.calibrated) {
            return
        }
        if (DungeonMapConfig.mapHideInBoss && DungeonMapLocation.inBoss) {
            return
        }
        val mc = Minecraft.getInstance()
        val font = mc.font
        val mapH = if (DungeonMapConfig.mapExtraInfo) 140 else 128

        val pose = graphics.pose()
        pose.pushMatrix()
        pose.translate(hudX.toFloat(), hudY.toFloat())

        drawBackground(graphics, mapH)
        pose.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat())
        renderRooms(graphics)
        renderRoomNames(graphics, font)
        pose.translate(-MapUtils.startCorner.first.toFloat(), -MapUtils.startCorner.second.toFloat())
        renderPlayerDots(graphics)
        if (DungeonMapConfig.mapExtraInfo) {
            renderExtraInfo(graphics, font, mapH)
        }

        pose.popMatrix()
    }

    private fun drawBackground(graphics: GuiGraphics, mapH: Int) {
        val bg = DungeonMapConfig.colorRoom.value // reuse warm neutral; background was semi-white in Noamm
        val argb = (50 shl 24) or (bg.red shl 16) or (bg.green shl 8) or bg.blue
        graphics.fill(0, 0, MAP_W, mapH.toInt(), argb)
        val border = Color.WHITE.rgb and 0xFFFFFF or (0xC0 shl 24)
        graphics.fill(0, 0, MAP_W, 1, border)
        graphics.fill(0, mapH - 1, MAP_W, mapH, border)
        graphics.fill(0, 0, 1, mapH, border)
        graphics.fill(MAP_W - 1, 0, MAP_W, mapH, border)
    }

    private fun renderRooms(graphics: GuiGraphics) {
        val connectorSize = (HotbarMapColorParser.quarterRoom.takeUnless { it == -1 } ?: 4)
        val roomSize = MapUtils.mapRoomSize

        for (y in 0..10) {
            for (x in 0..10) {
                val tile = DungeonInfo.dungeonList[y * 11 + x]
                if (tile is Unknown) continue
                if (tile.state == RoomState.UNDISCOVERED && !DungeonMapConfig.dungeonMapCheater) continue
                if (tile is Door && doorDisplayState(y, x) == RoomState.UNDISCOVERED && !DungeonMapConfig.dungeonMapCheater) continue

                val color = tile.color

                val xOffset = (x shr 1) * (roomSize + connectorSize)
                val yOffset = (y shr 1) * (roomSize + connectorSize)
                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                when {
                    xEven && yEven -> if (tile is Room) {
                        fillRect(graphics, xOffset, yOffset, roomSize, roomSize, color)
                    }
                    !xEven && !yEven -> {
                        fillRect(graphics, xOffset, yOffset, roomSize + connectorSize, roomSize + connectorSize, color)
                    }
                    else -> drawConnector(graphics, xOffset, yOffset, connectorSize, tile is Door, !xEven, color)
                }

                if (tile is Room && tile.core == 0) {
                    drawCheckmarkGlyph(graphics, tile.state, xOffset + roomSize / 2 - 3, yOffset + roomSize / 2 - 4)
                }
            }
        }
    }

    private fun doorDisplayState(row: Int, column: Int): RoomState {
        val rooms = DungeonPathFinder.getConnectingDoorRooms(row, column)
        if (rooms.size != 2) return RoomState.UNDISCOVERED
        if (rooms.any { it.state == RoomState.UNDISCOVERED }) return RoomState.UNDISCOVERED
        return RoomState.UNOPENED
    }

    private fun fillRect(graphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int, c: Color) {
        graphics.fill(x, y, x + w, y + h, c.rgb and 0xFFFFFF or (0xFF shl 24))
    }

    private fun drawConnector(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        doorWidth: Int,
        doorway: Boolean,
        vertical: Boolean,
        color: Color,
    ) {
        val doorwayOffset = if (MapUtils.mapRoomSize == 16) 5 else 6
        val roomSize = MapUtils.mapRoomSize
        val width = if (doorway) 6 else roomSize
        var x1 = if (vertical) x + roomSize else x
        var y1 = if (vertical) y else y + roomSize
        if (doorway) {
            if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
        }
        val rw = if (vertical) doorWidth else width
        val rh = if (vertical) width else doorWidth
        fillRect(graphics, x1, y1, rw, rh, color)
    }

    private fun drawCheckmarkGlyph(graphics: GuiGraphics, state: RoomState, x: Int, y: Int) {
        val (ch, col) = when (state) {
            RoomState.GREEN -> "✔" to 0xFF55FF55.toInt()
            RoomState.CLEARED -> "✔" to 0xFFFFFFFF.toInt()
            RoomState.FAILED -> "✖" to 0xFFFF5555.toInt()
            RoomState.UNOPENED -> if (!DungeonMapConfig.dungeonMapCheater) return else "?" to 0xFFAAAAAA.toInt()
            else -> return
        }
        val mc = Minecraft.getInstance()
        graphics.drawString(mc.font, ch, x, y, col, false)
    }

    private fun renderRoomNames(graphics: GuiGraphics, font: net.minecraft.client.gui.Font) {
        val roomSize = MapUtils.mapRoomSize.toFloat()
        val gapSize = HotbarMapColorParser.quarterRoom.toFloat()
        val halfRoom = HotbarMapColorParser.halfRoom.toFloat()
        val fullCell = roomSize + gapSize

        for ((name, unq) in DungeonInfo.uniqueRooms) {
            if (name == "Unknown") continue
            val room = unq.mainRoom
            if (room.data.type == RoomType.ENTRANCE) continue
            if (!DungeonMapConfig.dungeonMapCheater &&
                (room.state == RoomState.UNDISCOVERED || room.state == RoomState.UNOPENED)
            ) {
                continue
            }
            val checkPos = unq.getCheckmarkPosition()
            val cX = ((checkPos.first / 2f) * fullCell + halfRoom).roundToInt()
            val cY = ((checkPos.second / 2f) * fullCell + halfRoom).roundToInt()
            val color = when (room.state) {
                RoomState.GREEN -> 0xFF55FF55.toInt()
                RoomState.CLEARED -> 0xFFFFFFFF.toInt()
                RoomState.FAILED -> 0xFFFF5555.toInt()
                else -> 0xFFAAAAAA.toInt()
            }
            val label = unq.cacheSplitName.joinToString(" ")
            val scale = 0.65f
            val p = graphics.pose()
            p.pushMatrix()
            p.translate(cX.toFloat(), cY.toFloat())
            p.scale(scale)
            val w = font.width(label)
            graphics.drawString(font, label, -w / 2, -4, color, false)
            p.popMatrix()
        }
    }

    private fun renderPlayerDots(graphics: GuiGraphics) {
        if (DungeonMapLocation.inBoss) return
        val list = YuriDungeonListener.dungeonTeammatesNoSelf + listOfNotNull(YuriDungeonListener.thePlayer)
        for (p in list) {
            if (p.isDead) continue
            val entity = p.entity
            val (mx, mz) = if (entity != null && entity.isAlive) {
                MapUtils.coordsToMap(entity.position())
            } else {
                p.mapX to p.mapZ
            }
            val cx = mx.roundToInt()
            val cy = mz.roundToInt()
            val border = (p.clazz.color.rgb and 0xFFFFFF) or (0xFF shl 24)
            graphics.fill(cx - 3, cy - 3, cx + 3, cy + 3, border)
            graphics.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFF222222.toInt())
        }
    }

    private fun renderExtraInfo(graphics: GuiGraphics, font: net.minecraft.client.gui.Font, mapH: Int) {
        if (!DungeonMapConfig.dungeonMapCheater && !YuriDungeonListener.dungeonStarted) return
        val secrets = "${ScoreCalculation.foundSecrets}/${DungeonInfo.secretCount.coerceAtLeast(0)}"
        val line1 = "Secrets $secrets   Crypts ${ScoreCalculation.cryptsCount}/6"
        val line2 = "Score ${ScoreCalculation.score}   Deaths ${ScoreCalculation.deathCount}   M:${if (ScoreCalculation.mimicKilled) "Y" else "n"} P:${if (ScoreCalculation.princeKilled) "Y" else "n"}"
        val yBase = 128
        graphics.drawCenteredString(font, Component.literal(line1), MAP_W / 2, yBase - 2, 0xFFFFFF)
        graphics.drawCenteredString(font, Component.literal(line2), MAP_W / 2, yBase + 8, 0xFFFFAA)
    }
}
