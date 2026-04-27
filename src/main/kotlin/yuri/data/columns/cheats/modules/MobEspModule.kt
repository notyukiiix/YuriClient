package yuri.data.columns.cheats.modules

import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ambient.Bat
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.entity.monster.Giant
import net.minecraft.world.entity.player.Player
import yuri.YuriData
import yuri.util.DungeonUtils

object MobEspModule {
    private const val TITLE = "Mob ESP"
    private const val OPTION_STARRED_ESP = 0
    private const val OPTION_HIDDEN_MOBS = 1
    private const val OPTION_SHOW_FELS = 2
    private const val OPTION_SHOW_SHADOW_ASSASSINS = 3
    private const val OPTION_SHOW_STEALTHY = 4

    private val watcherMobs: Set<String> = setOf(
        "Angry Archaeologist",
        "Frozen Adventurer",
        "Shadow Assassin",
        "Lost Adventurer",
        "Withermancer"
    )

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private val labels: Array<String> = arrayOf(
        "Starred ESP",
        "Hidden Mobs",
        "Show Fels",
        "Show Shadow Assassins",
        "Show Stealthy"
    )

    private val toggles: BooleanArray = booleanArrayOf(
        true,
        true,
        true,
        true,
        true
    )
    private var glowColorRgb: Int = 0xFFFF00
    private val starMobs: MutableSet<Int> = hashSetOf()
    private val checkedMarkers: MutableSet<Int> = hashSetOf()
    private var lastLevelIdentity: String = ""

    @JvmStatic
    fun optionCount(): Int = labels.size

    @JvmStatic
    fun optionLabel(index: Int): String = labels[index]

    @JvmStatic
    fun isEnabled(index: Int): Boolean = toggles[index]

    @JvmStatic
    fun setEnabled(index: Int, enabled: Boolean) {
        toggles[index] = enabled
    }

    @JvmStatic
    fun toggle(index: Int) {
        toggles[index] = !toggles[index]
    }

    @JvmStatic
    fun glowColor(): Int = 0xFF000000.toInt() or (glowColorRgb and 0xFFFFFF)

    @JvmStatic
    fun glowColorHex(): String = String.format("#%06X", glowColorRgb and 0xFFFFFF)

    @JvmStatic
    fun trySetGlowColor(value: String?): Boolean {
        if (value == null) {
            return false
        }
        var normalized = value.trim()
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1)
        }
        if (normalized.length != 6) {
            return false
        }
        for (ch in normalized) {
            if (Character.digit(ch, 16) < 0) {
                return false
            }
        }
        glowColorRgb = normalized.toInt(16) and 0xFFFFFF
        return true
    }

    @JvmStatic
    fun shouldStarredEsp(): Boolean = module.enabled && toggles[OPTION_STARRED_ESP]

    @JvmStatic
    fun shouldHiddenMobs(): Boolean = module.enabled && toggles[OPTION_HIDDEN_MOBS]

    @JvmStatic
    fun shouldShowHiddenFels(): Boolean = shouldHiddenMobs() && toggles[OPTION_SHOW_FELS]

    @JvmStatic
    fun shouldShowHiddenShadowAssassins(): Boolean = shouldHiddenMobs() && toggles[OPTION_SHOW_SHADOW_ASSASSINS]

    @JvmStatic
    fun shouldShowHiddenStealthy(): Boolean = shouldHiddenMobs() && toggles[OPTION_SHOW_STEALTHY]

    @JvmStatic
    fun shouldRevealHiddenMob(entity: Entity): Boolean {
        if (!shouldHiddenMobs() || !DungeonUtils.inDungeons() || !entity.isInvisible) {
            return false
        }
        if (!shouldShowHiddenFels() && !shouldShowHiddenShadowAssassins() && !shouldShowHiddenStealthy()) {
            return false
        }
        val name = displayName(entity) ?: return false
        if (name.isEmpty()) {
            return false
        }
        val isFel = shouldShowHiddenFels() && entity is EnderMan && name == "Dinnerbone"
        val isShadowAssassin = shouldShowHiddenShadowAssassins() &&
            entity is AbstractClientPlayer &&
            name.contains("Shadow Assassin", ignoreCase = true)
        val isWatcherMob = shouldShowHiddenStealthy() &&
            entity is AbstractClientPlayer &&
            watcherMobs.any { it.equals(name, ignoreCase = true) }
        val isGiant = shouldShowHiddenStealthy() && entity is Giant && !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty
        return isFel || isShadowAssassin || isWatcherMob || isGiant
    }

    @JvmStatic
    fun shouldStarredMobEsp(entity: Entity): Boolean {
        if (!shouldStarredEsp() || !DungeonUtils.inDungeons()) {
            return false
        }
        refreshForWorldChange()
        if (entity.id in starMobs) {
            return true
        }
        // Extra fallback checks so obvious dungeon targets are still highlighted even without marker packets.
        return when (entity) {
            is Bat -> !entity.isInvisible && !entity.isPassenger
            is EnderMan -> entity.name.string == "Dinnerbone"
            is Player -> {
                val n = entity.name.string
                n.contains("Shadow Assassin", ignoreCase = true) ||
                    n.equals("Lost Adventurer", ignoreCase = true) ||
                    n.equals("Diamond Guy", ignoreCase = true) ||
                    n.equals("King Midas", ignoreCase = true) ||
                    isStarredName(n) ||
                    displayName(entity)?.let { isStarredName(it) } == true
            }
            else -> {
                if (isStarredName(entity.name.string)) {
                    return true
                }
                val name = displayName(entity) ?: return false
                isStarredName(name)
            }
        }
    }

    @JvmStatic
    fun onEntityDataUpdated(entityId: Int) {
        if (!shouldStarredEsp() || !DungeonUtils.inDungeons()) {
            return
        }
        refreshForWorldChange()
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return
        val entity = level.getEntity(entityId) ?: return
        if (entity is ArmorStand) {
            val markerName = entity.customName?.string ?: return
            // Hypixel starred mob markers: star symbol in the line (often with HP / heart suffix).
            if (isStarredName(markerName)) {
                checkStarMob(entity, markerName)
            }
            return
        }
        if (entity is Player) {
            val n = entity.name.string
            if (n.equals("Shadow Assassin", ignoreCase = true) ||
                n.equals("Lost Adventurer", ignoreCase = true) ||
                n.equals("Diamond Guy", ignoreCase = true) ||
                n.equals("King Midas", ignoreCase = true)
            ) {
                starMobs.add(entity.id)
            }
        }
    }

    private fun checkStarMob(armorStand: ArmorStand, markerName: String) {
        if (!checkedMarkers.add(armorStand.id)) {
            return
        }
        val level = armorStand.level()
        val offset = if (markerName.uppercase().contains("WITHERMANCER")) 3 else 1
        val directId = armorStand.id - offset
        val direct = level.getEntity(directId)
        if (direct != null && direct !is ArmorStand) {
            starMobs.add(direct.id)
            return
        }
        val nearby = level.getEntities(armorStand, armorStand.boundingBox.move(0.0, -1.0, 0.0)) { it !is ArmorStand }
        val candidate = nearby.firstOrNull {
            when (it) {
                is Player -> !it.isInvisible && it != Minecraft.getInstance().player
                is LivingEntity -> true
                else -> true
            }
        }
        if (candidate != null) {
            starMobs.add(candidate.id)
        }
    }

    private fun refreshForWorldChange() {
        val level = Minecraft.getInstance().level
        val id = level?.dimension()?.location()?.toString() ?: "none"
        if (id != lastLevelIdentity) {
            lastLevelIdentity = id
            starMobs.clear()
            checkedMarkers.clear()
        }
    }

    private fun displayName(entity: Entity): String? =
        entity.customName?.string?.trim()?.takeIf { it.isNotEmpty() }

    private fun isStarredName(name: String): Boolean {
        if (name.isEmpty()) {
            return false
        }
        for (ch in name) {
            when (ch) {
                '✯', '✭', '★', '☆', '⭐', '✦', '✧', '✪', '❋', '※' -> return true
            }
        }
        return false
    }
}
