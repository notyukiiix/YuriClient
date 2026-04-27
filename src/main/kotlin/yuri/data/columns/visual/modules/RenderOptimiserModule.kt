package yuri.data.columns.visual.modules

import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import yuri.YuriData
import yuri.util.DungeonUtils

object RenderOptimiserModule {
    private const val TITLE = "Render Optimiser"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private val labels: Array<String> = arrayOf(
        "Hide Falling Blocks",
        "Hide Lightning",
        "Hide Experience Orbs",
        "Hide Death Animation",
        "Hide Armor Stands",
        "Hide Explosion Particles",
        "Hide Archer Passive",
        "Hide Soul Weaver",
        "Hide Tentacle Head",
        "Hide Fire Overlay",
        "Hide Actionbar",
        "Hide Effects"
    )

    private val toggles: BooleanArray = booleanArrayOf(
        true,
        true,
        true,
        true,
        false,
        false,
        true,
        true,
        true,
        true,
        false,
        false
    )

    const val IDX_HIDE_FALLING_BLOCKS: Int = 0
    const val IDX_HIDE_LIGHTNING: Int = 1
    const val IDX_HIDE_EXPERIENCE_ORBS: Int = 2
    const val IDX_HIDE_DEATH_ANIMATION: Int = 3
    const val IDX_HIDE_ARMOR_STANDS: Int = 4
    const val IDX_HIDE_EXPLOSION_PARTICLES: Int = 5
    const val IDX_HIDE_ARCHER_PASSIVE: Int = 6
    const val IDX_HIDE_SOUL_WEAVER: Int = 7
    const val IDX_HIDE_TENTACLE_HEAD: Int = 8
    const val IDX_HIDE_FIRE_OVERLAY: Int = 9
    const val IDX_HIDE_ACTIONBAR_HUD: Int = 10
    const val IDX_HIDE_EFFECTS_HUD: Int = 11

    const val TENTACLE_TEXTURE: String =
        "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1NzI3NzI0OSwKICAicHJvZmlsZUlkIiA6ICIxODA1Y2E2MmM0ZDI0M2NiOWQxYmY4YmM5N2E1YjgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSdWxsZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdkODM2NzQ5MjZiODk3MTRlNmI1YTU1NDcwNTAxYzA0YjA2NmRkODdiZjZjMzM1Y2RkYzZlNjBhMWExYTVmNSIKICAgIH0KICB9Cn0="

    const val SOUL_WEAVER_TEXTURE: String =
        "eyJ0aW1lc3RhbXAiOjE1NTk1ODAzNjI1NTMsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZjI0ZWQ2ODc1MzA0ZmE0YTFmMGM3ODViMmNiNmE2YTcyNTYzZTlmM2UyNGVhNTVlMTgxNzg0NTIxMTlhYTY2In19fQ=="

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
    fun isModuleActive(): Boolean = module.enabled

    @JvmStatic
    fun shouldDisableFireOverlay(): Boolean = isModuleActive() && isEnabled(IDX_HIDE_FIRE_OVERLAY)

    @JvmStatic
    fun shouldHideActionBarHud(): Boolean = isModuleActive() && isEnabled(IDX_HIDE_ACTIONBAR_HUD)

    @JvmStatic
    fun shouldHideEffectsHud(): Boolean = isModuleActive() && isEnabled(IDX_HIDE_EFFECTS_HUD)

    @JvmStatic
    fun shouldBlockEntitySpawn(type: EntityType<*>): Boolean {
        if (!isModuleActive()) {
            return false
        }
        if (isEnabled(IDX_HIDE_FALLING_BLOCKS) && type == EntityType.FALLING_BLOCK) {
            return true
        }
        if (isEnabled(IDX_HIDE_LIGHTNING) && type == EntityType.LIGHTNING_BOLT) {
            return true
        }
        if (isEnabled(IDX_HIDE_EXPERIENCE_ORBS) && type == EntityType.EXPERIENCE_ORB) {
            return true
        }
        return false
    }

    @JvmStatic
    fun shouldHideDeathAnimationRender(entity: Entity?): Boolean {
        if (!isModuleActive() || !isEnabled(IDX_HIDE_DEATH_ANIMATION) || entity == null) {
            return false
        }
        val living = entity as? LivingEntity ?: return false
        return living.deathTime > 0
    }

    @JvmStatic
    fun shouldHideDyingMobArmorStand(entity: Entity?): Boolean {
        if (!isModuleActive() || !isEnabled(IDX_HIDE_DEATH_ANIMATION) || !isEnabled(IDX_HIDE_ARMOR_STANDS) || entity == null) {
            return false
        }
        if (entity !is ArmorStand) {
            return false
        }
        val vehicle = entity.vehicle
        val living = vehicle as? LivingEntity ?: return false
        return living.deathTime > 0
    }

    @JvmStatic
    fun shouldCancelArcherBoneMealMetadata(entityId: Int, localPlayerId: Int, packed: List<SynchedEntityData.DataValue<*>>): Boolean {
        if (!isModuleActive() || !isEnabled(IDX_HIDE_ARCHER_PASSIVE) || !DungeonUtils.inDungeons()) {
            return false
        }
        if (entityId == localPlayerId) {
            return false
        }
        for (data in packed) {
            if (data.id != 8) {
                continue
            }
            val value = data.value
            if (value is ItemStack && !value.isEmpty && value.item == Items.BONE_MEAL) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun shouldCancelExplosionParticle(particle: ParticleOptions): Boolean {
        if (!isModuleActive() || !isEnabled(IDX_HIDE_EXPLOSION_PARTICLES)) {
            return false
        }
        val type = particle.type
        return type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER
    }

    @JvmStatic
    fun shouldDiscardEntityForEquipmentSlot(slot: EquipmentSlot, stack: ItemStack): Boolean {
        if (!isModuleActive() || !DungeonUtils.inDungeons() || stack.isEmpty) {
            return false
        }
        val encoded = playerHeadTextureValue(stack) ?: return false
        if (isEnabled(IDX_HIDE_SOUL_WEAVER) && slot == EquipmentSlot.HEAD && encoded == SOUL_WEAVER_TEXTURE) {
            return true
        }
        if (isEnabled(IDX_HIDE_TENTACLE_HEAD) && slot == EquipmentSlot.HEAD && encoded == TENTACLE_TEXTURE) {
            return true
        }
        return false
    }

    private fun playerHeadTextureValue(stack: ItemStack): String? {
        val profile = stack.get(DataComponents.PROFILE) ?: return null
        val textures = profile.partialProfile().properties.get("textures") ?: return null
        return textures.firstOrNull()?.value
    }
}
