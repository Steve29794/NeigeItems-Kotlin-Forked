package pers.neige.neigeitems.hook.nms.impl

import net.minecraft.server.v1_12_R1.EntityItem
import net.minecraft.server.v1_12_R1.WorldServer
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Item
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import pers.neige.neigeitems.hook.nms.NMSHooker
import java.util.function.Consumer


class NMSHookerV1_12_R1Impl : NMSHooker() {
    override fun hasCustomModelData(itemMeta: ItemMeta?): Boolean { return false }

    override fun getCustomModelData(itemMeta: ItemMeta?): Int? { return null }

    override fun setCustomModelData(itemMeta: ItemMeta?, data: Int) {}

    override fun dropItem(
        world: World,
        location: Location,
        itemStack: ItemStack,
        function: Consumer<Item>
    ): Item {
        val craftWorld = world as CraftWorld
        val nmsWorld = craftWorld.handle as WorldServer

        val entity = EntityItem(
            nmsWorld,
            location.x,
            location.y,
            location.z,
            CraftItemStack.asNMSCopy(itemStack)
        )
        entity.pickupDelay = 10
        function.accept((entity.bukkitEntity as Item))
        nmsWorld.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM)
        return (entity.bukkitEntity as Item)
    }
}