package com.pichler.digitaleshirn.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: Category?): String? = category?.name

    @TypeConverter
    fun toCategory(value: String?): Category? = value?.let(Category::valueOf)
}
