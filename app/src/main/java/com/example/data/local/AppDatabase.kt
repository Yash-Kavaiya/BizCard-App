package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.model.RelationshipType

class Converters {
    @TypeConverter
    fun fromStatus(status: LeadStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): LeadStatus = try {
        LeadStatus.valueOf(value)
    } catch (e: Exception) {
        LeadStatus.NEW
    }

    @TypeConverter
    fun fromSource(source: LeadSource): String = source.name

    @TypeConverter
    fun toSource(value: String): LeadSource = try {
        LeadSource.valueOf(value)
    } catch (e: Exception) {
        LeadSource.SCAN
    }

    @TypeConverter
    fun fromRelationship(rel: RelationshipType): String = rel.name

    @TypeConverter
    fun toRelationship(value: String): RelationshipType = try {
        RelationshipType.valueOf(value)
    } catch (e: Exception) {
        RelationshipType.PROSPECT
    }
}

@Database(entities = [LeadEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun leadDao(): LeadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bizcard_lead_database"
                )
                // Upgrades must ship a Migration so users never lose saved leads.
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
