package yuri.client

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.world.phys.AABB
import yuri.data.columns.dungeons.map.DungeonInfo
import yuri.data.columns.dungeons.map.compat.DungeonMapConfig
import yuri.data.columns.dungeons.map.compat.DungeonMapLocation
import yuri.data.columns.dungeons.map.core.Door
import yuri.data.columns.dungeons.map.core.DoorType
import yuri.data.columns.dungeons.map.core.RoomState
import yuri.data.columns.dungeons.map.handlers.DungeonPathFinder
import yuri.data.columns.dungeons.client.YuriDungeonListener
import yuri.data.columns.dungeons.modules.DungeonMapModule
import yuri.util.DungeonUtils

/**
 * World-space wither door highlights (Noamm dungeon map feature), using line boxes only (lighter than filled quads).
 */
object YuriWitherDoorWorldRenderer {
    @JvmStatic
    fun render(context: WorldRenderContext) {
        if (!DungeonMapModule.module.enabled || !DungeonUtils.inDungeons()) return
        if (!DungeonMapConfig.boxWitherDoors || DungeonMapLocation.inBoss) return
        val mc = Minecraft.getInstance()
        if (mc.level == null) return
        val matrices = context.matrices() ?: return
        val buffer = context.consumers().getBuffer(RenderType.lines())
        val argb = if (YuriDungeonListener.doorKeys > 0) {
            DungeonMapConfig.witherDoorKeyArgb
        } else {
            DungeonMapConfig.witherDoorNoKeyArgb
        }
        val r = ((argb shr 16) and 0xFF) / 255.0f
        val g = ((argb shr 8) and 0xFF) / 255.0f
        val b = (argb and 0xFF) / 255.0f
        val a = ((argb shr 24) and 0xFF) / 255.0f

        val cam = mc.gameRenderer.mainCamera.position
        matrices.pushPose()
        matrices.translate(-cam.x.toFloat(), -cam.y.toFloat(), -cam.z.toFloat())

        val hideUndiscovered = !DungeonMapConfig.dungeonMapCheater || YuriDungeonListener.dungeonStarted

        for (tile in DungeonInfo.dungeonList) {
            if (tile !is Door) continue
            if (tile.type == DoorType.ENTRANCE || tile.type == DoorType.NORMAL) continue
            if (tile.opened) continue
            if (hideUndiscovered && tile.state == RoomState.UNDISCOVERED && !DungeonPathFinder.isFairy(tile)) continue

            val cx = tile.x + 0.5
            val cy = 69.0
            val cz = tile.z + 0.5
            val box = AABB(cx - 1.5, cy - 2.0, cz - 1.5, cx + 1.5, cy + 2.0, cz + 1.5)
            ShapeRenderer.renderLineBox(matrices.last(), buffer, box, r, g, b, a)
        }

        matrices.popPose()
    }
}
