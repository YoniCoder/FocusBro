package com.yonas.focusbro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Tag::class], version = 1, exportSchema = false)
abstract class TagDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: TagDatabase? = null

        fun getInstance(context: Context): TagDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TagDatabase::class.java,
                    "tags_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}