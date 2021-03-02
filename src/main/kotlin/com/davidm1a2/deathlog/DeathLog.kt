package com.davidm1a2.deathlog

import net.minecraft.client.Minecraft
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Mod("deathlog")
class DeathLog {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    fun onPlayerDeathEvent(event: LivingDeathEvent) {
        if (event.entityLiving == Minecraft.getInstance().player) {
            val deathLogDir = Minecraft.getInstance().gameDir.resolve("DeathLog")
            if (!deathLogDir.exists()) {
                deathLogDir.mkdirs()
            }

            val deathLogName = LocalDateTime.now().format(DEATH_LOG_NAME_FORMATTER)
            val deathLog = deathLogDir.resolve("$deathLogName.txt")

            val player = event.entityLiving as PlayerEntity
            val source = event.source
            deathLog.printWriter().use {
                logDeath(it, player, source)
            }
        }
    }

    private fun logDeath(log: PrintWriter, player: PlayerEntity, source: DamageSource) {
        with(log) {
            val inventory = player.inventory

            println("${player.gameProfile.name} died! Message was: ${source.getDeathMessage(player).string}")
            println("XP level: ${player.experienceLevel}")
            println()

            println("Player Armor:")
            inventory.armorInventory.forEach { println(it.toLogString()) }
            println()

            println("Player Offhand Slot:")
            println(inventory.offHandInventory[0].toLogString())
            println()

            println("Player Inventory:")
            inventory.mainInventory
                .filter { !it.isEmpty }
                .forEach { println(it.toLogString()) }
            println()
        }
    }

    companion object {
        private val DEATH_LOG_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh-mm-ss")

        private fun ItemStack.toLogString(): String {
            if (isEmpty) {
                return "-> Empty"
            }

            return buildString {
                append("-> ${count}x")

                val itemName = item.name.string
                val nametagName = displayName.string
                if (itemName == nametagName) {
                    append(" $itemName")
                } else {
                    append(" $itemName named $nametagName")
                }

                if (tag != null) {
                    append(". Data: ${tag?.toString()}")
                }
            }
        }
    }
}