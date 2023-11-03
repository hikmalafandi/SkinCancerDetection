package com.submission.humic.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference private constructor(private val dataStore: DataStore<Preferences>){

    fun getUser(): Flow<DataUser> {
        return dataStore.data.map { preferences ->
            DataUser(
                preferences[NAME_KEY] ?: "",
                preferences[GENDER_KEY] ?: "",
                preferences[PHONE_KEY] ?: "",
                preferences[EMAIL_KEY] ?: "",
                preferences[PASSWORD_KEY] ?: "",
                preferences[STATUS_KEY] ?: false
            )
        }
    }


    suspend fun saveUser(dataUser: DataUser) {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = dataUser.name
            preferences[GENDER_KEY] = dataUser.gender
            preferences[PHONE_KEY] = dataUser.phone
            preferences[EMAIL_KEY] = dataUser.email
            preferences[PASSWORD_KEY] = dataUser.password
            preferences[STATUS_KEY] = dataUser.isLogin
        }
    }


    suspend fun savePatient(patient: DataPatient) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = patient.id
            preferences[NAME_KEY] = patient.name
            preferences[GENDER_KEY] = patient.gender
            preferences[PHONE_KEY] = patient.phone
            preferences[EMAIL_KEY] = patient.email
        }
    }

    fun getPatient(): Flow<DataPatient> {
        return dataStore.data.map { preferences ->
            DataPatient(
                (preferences[USER_ID_KEY] ?: null)!!,
                preferences[NAME_KEY] ?: "",
                preferences[GENDER_KEY] ?: "",
                preferences[PHONE_KEY] ?: "",
                preferences[EMAIL_KEY] ?: "",
            )
        }
    }

    suspend fun login() {
        dataStore.edit { preferences ->
            preferences[STATUS_KEY] = true
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[STATUS_KEY] = false
        }
    }

    suspend fun saveUserId(userId: Int) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    fun getUserId(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val NAME_KEY = stringPreferencesKey("name")
        private val GENDER_KEY = stringPreferencesKey("gender")
        private val PHONE_KEY = stringPreferencesKey("phone")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val STATUS_KEY = booleanPreferencesKey("status")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_ID_KEY = intPreferencesKey("user_id")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE= instance
                instance
            }
        }
    }
}