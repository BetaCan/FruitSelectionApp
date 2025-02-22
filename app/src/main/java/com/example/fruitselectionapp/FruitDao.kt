package com.example.fruitselectionapp

import androidx.room.*

@Dao
interface FruitDao {
    @Query("SELECT * FROM fruits")
    suspend fun getAllFruits(): List<Fruit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFruit(fruit: Fruit)

    @Query("DELETE FROM fruits WHERE name = :name")
    suspend fun deleteFruitByName(name: String)
}