package com.aletropy.skya.blocks

import com.aletropy.skya.blocks.custom.SkyEssenceGenerator
import kotlin.reflect.KClass

object CustomBlocks
{
    const val SKY_ESSENCE_GENERATOR = "sky_essence_generator"

    val BLOCKS = mapOf(
        Pair(SKY_ESSENCE_GENERATOR, SkyEssenceGenerator::class)
    )

    val NAMES = run {
        var map = mutableMapOf<KClass<*>, String>()
        BLOCKS.forEach {
            map[it.value] = it.key
        }
        map.toMap()
    }
}