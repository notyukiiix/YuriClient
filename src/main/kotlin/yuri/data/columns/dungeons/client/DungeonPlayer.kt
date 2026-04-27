package yuri.data.columns.dungeons.client

import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import yuri.data.columns.dungeons.client.enums.DungeonClass
import yuri.data.columns.dungeons.map.handlers.DungeonScanner
import yuri.data.columns.dungeons.map.utils.MapUtils

data class DungeonPlayer(
    var name: String,
    var clazz: DungeonClass,
    var clazzLvl: Int,
    var skin: ResourceLocation = DefaultPlayerSkin.getDefaultTexture(),
    var isDead: Boolean = false,
) {
    var entity: AbstractClientPlayer? = null
        set(value) {
            field = value
            skin = DefaultPlayerSkin.getDefaultTexture()
        }

    var mapX = 0f
    var mapZ = 0f
    var yaw = 0f
    var icon = ""

    fun getRealPos(): Vec3 = Vec3(
        (mapX - MapUtils.startCorner.first) / MapUtils.coordMultiplier + DungeonScanner.startX - 15,
        (entity?.y ?: 0.0),
        (mapZ - MapUtils.startCorner.second) / MapUtils.coordMultiplier + DungeonScanner.startZ - 15
    )

    val clearedRooms: Pair<MutableSet<String>, MutableSet<String>> = mutableSetOf<String>() to mutableSetOf()
    val deaths: MutableList<String> = mutableListOf()
    var secretsBeforeRun: Long = 0

    companion object {
        fun get(name: String): DungeonPlayer? =
            YuriDungeonListener.dungeonTeammates.find { it.name == name } ?: YuriDungeonListener.thePlayer
    }
}
