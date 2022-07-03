package com.danilkinkin.buckwheat.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Draw(
    @ColumnInfo(name = "value")
    val value: Double,

    @ColumnInfo(name = "date")
    val date: Date,
) {
    @PrimaryKey(autoGenerate = true) var uid: Int = 0
}
