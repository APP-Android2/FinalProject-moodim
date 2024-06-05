package kr.co.lion.modigm.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import kr.co.lion.modigm.model.UserData

class PreferenceUtil(context: Context) {

    private val prefs: SharedPreferences
    private val gson = Gson()

    init {
        // 마스터 키 생성 또는 가져오기
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // EncryptedSharedPreferences 생성
        prefs = EncryptedSharedPreferences.create(
            "encrypted_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue) ?: defValue
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    // 유저 정보를 SharedPreferences에 저장
    fun setUser(key: String, user: UserData) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(key, userJson).apply()
    }

    // SharedPreferences에서 유저 정보를 가져옴
    fun getUser(key: String): UserData? {
        val userJson = prefs.getString(key, null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserData::class.java)
        } else {
            null
        }
    }

    fun clearUser(key: String) {
        prefs.edit().remove(key).apply()
    }
}
