package com.example.fruitselectionapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Fruit::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fruitDao(): FruitDao
}