package com.aletropy.skya.island

import org.bukkit.NamespacedKey

object Islands
{
    val DEFAULT_PLAYER_KEY = NamespacedKey.minecraft("default_island")
    val CHERRY_PLAYER_KEY = NamespacedKey.minecraft("cherry_island")
    val SNOWY_PLAYER_KEY = NamespacedKey.minecraft("snowy_island")

    val NETHER_KEY = NamespacedKey.minecraft("nether_island")
    val JUNGLE_TEMPLE_KEY = NamespacedKey.minecraft("jungle_pyramid_island")

    val PLAYER_KEYS = listOf(
        DEFAULT_PLAYER_KEY,
        CHERRY_PLAYER_KEY,
        SNOWY_PLAYER_KEY
    )

    val KEYS = listOf(
        DEFAULT_PLAYER_KEY,
        CHERRY_PLAYER_KEY,
        SNOWY_PLAYER_KEY,
        NETHER_KEY,
        JUNGLE_TEMPLE_KEY
    )
}