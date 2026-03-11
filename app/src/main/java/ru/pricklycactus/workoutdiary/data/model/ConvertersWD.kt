package ru.pricklycactus.workoutdiary.data.model


import androidx.room.TypeConverter
import java.util.Date

class ConvertersWD {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}