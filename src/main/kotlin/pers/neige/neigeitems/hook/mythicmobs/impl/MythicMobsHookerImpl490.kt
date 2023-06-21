package pers.neige.neigeitems.hook.mythicmobs.impl

import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent
import io.lumine.xikage.mythicmobs.items.ItemManager
import io.lumine.xikage.mythicmobs.mobs.MobManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import pers.neige.neigeitems.hook.mythicmobs.MythicMobsHooker
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.submit
import kotlin.math.roundToInt

/**
 * 4.9.0版本MM挂钩
 *
 * @constructor 启用4.9.0版本MM挂钩
 */
class MythicMobsHookerImpl490 : MythicMobsHooker() {
    override val version = "490"

    override val spawnEventClass = MythicMobSpawnEvent::class.java

    // 4.9.0 -> int
    // 4.11.0 -> double
    // 放弃思考，开反
    private val spawnMobLevelMethod = spawnEventClass.getDeclaredMethod("getMobLevel")

    override val deathEventClass = MythicMobDeathEvent::class.java

    private val deathMobLevelMethod = deathEventClass.getDeclaredMethod("getMobLevel")

    override val reloadEventClass = MythicReloadedEvent::class.java

    private val itemManager: ItemManager = MythicMobs.inst().itemManager

    private val mobManager: MobManager = MythicMobs.inst().mobManager

    override val mobInfos = HashMap<String, ConfigurationSection>()

    private val apiHelper = MythicMobs.inst().apiHelper

    override val spawnListener = registerBukkitListener(MythicMobSpawnEvent::class.java, EventPriority.HIGH) {
        submit(async = true) {
            if (it.entity is LivingEntity) {
                val mobLevel = spawnMobLevelMethod.invoke(it).let { level ->
                    if (level is Double) {
                        level.roundToInt()
                    } else {
                        level as Int
                    }
                }
                spawnEvent(
                    it.mobType.internalName,
                    it.entity as LivingEntity,
                    mobLevel
                )
            }
        }
    }

    override val deathListener = registerBukkitListener(MythicMobDeathEvent::class.java) {
        submit(async = true) {
            if (it.entity is LivingEntity) {
                val mobLevel = deathMobLevelMethod.invoke(it).let { level ->
                    if (level is Double) {
                        level.roundToInt()
                    } else {
                        level as Int
                    }
                }
                deathEvent(
                    it.killer,
                    it.entity as LivingEntity,
                    it.mobType.internalName,
                    mobLevel
                )
            }
        }
    }

    override val reloadListener = registerBukkitListener(MythicReloadedEvent::class.java) {
        loadMobInfos()
    }

    init {
        loadMobInfos()
    }

    override fun getItemStack(id: String): ItemStack? {
        return itemManager.getItemStack(id)
    }

    // 这个版本并不需要同步获取物品
    override fun getItemStackSync(id: String): ItemStack? {
        return itemManager.getItemStack(id)
    }

    override fun castSkill(entity: Entity, skill: String, trigger: Entity?) {
        apiHelper.castSkill(entity, skill, trigger, entity.location, null, null, 1.0F)
    }

    override fun getItemIds(): List<String> {
        return itemManager.itemNames.toList()
    }

    override fun isMythicMob(entity: Entity): Boolean {
        return apiHelper.isMythicMob(entity)
    }

    override fun getMythicId(entity: Entity): String? {
        return if (apiHelper.isMythicMob(entity))
            return apiHelper.getMythicMobInstance(entity).type.internalName
        else
            null
    }
}