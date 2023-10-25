package com.example.littlelemon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MenuNetwork(
    @SerialName("menu")
    val menu: List<MenuItemNetwork>
)

@Serializable
data class MenuItemNetwork(
    val id: Int,
    val title: String,
    val price: Double
) {
    fun toMenuItemRoom() = MenuItemRoom(
        id = id,
        title = title,
        price = price
    )
}
