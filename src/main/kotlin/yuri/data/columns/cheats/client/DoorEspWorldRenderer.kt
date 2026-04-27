package yuri.data.columns.cheats.client

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import yuri.data.columns.cheats.DungeonDoorTracker
import yuri.data.columns.cheats.modules.DoorEspModule

object DoorEspWorldRenderer {
    @JvmStatic
    fun render(context: WorldRenderContext) {
        if (!DoorEspModule.isActive()) {
            return
        }
        val matrices = context.matrices() ?: return
        val mc = Minecraft.getInstance()
        if (mc.level == null) {
            return
        }
        val camPos = mc.gameRenderer.mainCamera.position
        val buffer = context.consumers().getBuffer(RenderType.lines())
        val openable = DoorEspModule.shouldUseOpenableHighlight()
        val argb = if (openable) DoorEspModule.openableArgb() else DoorEspModule.outlineArgb()
        val r = ((argb shr 16) and 0xFF) / 255.0f
        val g = ((argb shr 8) and 0xFF) / 255.0f
        val b = (argb and 0xFF) / 255.0f
        val a = ((argb shr 24) and 0xFF) / 255.0f

        matrices.pushPose()
        matrices.translate(-camPos.x.toFloat(), -camPos.y.toFloat(), -camPos.z.toFloat())

        DungeonDoorTracker.forEachDoorPacked { packed ->
            val pos = BlockPos.of(packed)
            val box = AABB(pos)
            ShapeRenderer.renderLineBox(matrices.last(), buffer, box, r, g, b, a)
        }

        matrices.popPose()
    }
}
