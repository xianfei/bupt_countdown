package cn.edu.bupt.countdown.bean

import kotlinx.serialization.Serializable

@Serializable
data class EventOldBean(
    var date: String,
    var context: String = "",
    var type: Int = 0,
    var picture_path: String = "",
    var isFavourite: Boolean = false
)