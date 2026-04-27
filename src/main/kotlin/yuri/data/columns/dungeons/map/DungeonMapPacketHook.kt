package yuri.data.columns.dungeons.map

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import yuri.util.DungeonUtils

object DungeonMapPacketHook {
    @JvmStatic
    fun onMapItemData(mc: Minecraft, packet: ClientboundMapItemDataPacket) {
        if (!DungeonUtils.inDungeons()) return
        val level = mc.level ?: return
        val data = level.getMapData(packet.mapId()) ?: return
        DungeonInfo.mapData = data
    }
}
