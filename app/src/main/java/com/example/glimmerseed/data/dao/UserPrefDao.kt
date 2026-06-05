package com.example.glimmerseed.data.dao

import androidx.room.*
import com.example.glimmerseed.data.entity.UserPref
import kotlinx.coroutines.flow.Flow

/**
 * 用户设置数据访问对象
 */
@Dao
interface UserPrefDao {
    @Query("SELECT * FROM user_prefs")
    fun getAllPrefs(): Flow<List<UserPref>>

    @Query("SELECT * FROM user_prefs WHERE `key` = :key")
    suspend fun getPref(key: String): UserPref?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPref(pref: UserPref)

    @Update
    suspend fun updatePref(pref: UserPref)

    @Delete
    suspend fun deletePref(pref: UserPref)
}
