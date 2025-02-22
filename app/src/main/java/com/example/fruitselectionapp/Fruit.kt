package com.example.fruitselectionapp
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fruits")
data class Fruit(
    @PrimaryKey val name: String, // Fruit name
    val emoji: String // Emoji representing the fruit
)