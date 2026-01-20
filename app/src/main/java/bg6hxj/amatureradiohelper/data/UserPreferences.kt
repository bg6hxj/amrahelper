package bg6hxj.amatureradiohelper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * 用户偏好设置管理类
 * 使用 DataStore 存储用户信息
 */
class UserPreferences(private val context: Context) {
    
    companion object {
        private val NICKNAME_KEY = stringPreferencesKey("nickname")
        private val CALLSIGN_KEY = stringPreferencesKey("callsign")
        private val AVATAR_URI_KEY = stringPreferencesKey("avatar_uri")
        private val LAST_SELECTED_LEVEL_KEY = stringPreferencesKey("last_selected_level")
    }
    
    /**
     * 获取昵称
     */
    val nickname: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NICKNAME_KEY] ?: "未设置"
    }
    
    /**
     * 获取呼号
     */
    val callsign: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CALLSIGN_KEY] ?: "未设置"
    }
    
    /**
     * 获取头像 URI
     */
    val avatarUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AVATAR_URI_KEY]
    }
    
    /**
     * 保存昵称
     */
    suspend fun saveNickname(nickname: String) {
        context.dataStore.edit { preferences ->
            preferences[NICKNAME_KEY] = nickname
        }
    }
    
    /**
     * 保存呼号
     */
    suspend fun saveCallsign(callsign: String) {
        context.dataStore.edit { preferences ->
            preferences[CALLSIGN_KEY] = callsign
        }
    }
    
    /**
     * 保存头像 URI
     */
    suspend fun saveAvatarUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[AVATAR_URI_KEY] = uri
        }
    }
    
    /**
     * 获取最后选择的题库等级
     */
    val lastSelectedLevel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_SELECTED_LEVEL_KEY] ?: "A"
    }

    /**
     * 保存最后选择的题库等级
     */
    suspend fun saveLastSelectedLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SELECTED_LEVEL_KEY] = level
        }
    }
    
    /**
     * 清除所有数据
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
