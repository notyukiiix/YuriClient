package yuri.data.columns.dungeons.client

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SkullBlock
import net.minecraft.world.level.block.entity.SkullBlockEntity
import yuri.data.columns.dungeons.map.compat.equalsOneOf

/**
 * SkyBlock dungeon helpers (floor detection strings, secret block checks).
 * Election / Paul bonus is not wired — [isPaul] is always false.
 */
object YuriDungeonUtils {
    val FLOOR_NAMES = listOf("ENTRANCE", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN")
    val floorEnterRegex = Regex("-+\\s.+ entered.+The Catacombs, Floor [IVX]+!\\s-+")
    const val WITHER_ESSENCE_ID = "e0f3e929-869e-3dca-9504-54c666ee6f23"
    const val REDSTONE_KEY_ID = "fed95410-aba1-39df-9b95-1d4f361eb66e"

    @JvmStatic
    fun isSecret(pos: BlockPos): Boolean {
        val mc = Minecraft.getInstance()
        val block = mc.level?.getBlockState(pos)?.block ?: Blocks.AIR
        return when {
            block is SkullBlock -> {
                val profile: ResolvableProfile? = (mc.level?.getBlockEntity(pos) as? SkullBlockEntity)?.ownerProfile
                val id = profile?.partialProfile()?.id?.toString().orEmpty()
                id.equalsOneOf(WITHER_ESSENCE_ID, REDSTONE_KEY_ID)
            }
            block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.LEVER -> true
            else -> false
        }
    }

    @JvmStatic
    fun isPaul(): Boolean = false

    val dungeonItemDrops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion",
        "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion",
        "Healing 8 Splash Potion", "Decoy", "Inflatable Jerry", "Spirit Leap",
        "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key",
        "Treasure Talisman", "Revive Stone", "Architect's First Draft",
        "Secret Dye", "Candycomb"
    )
}
