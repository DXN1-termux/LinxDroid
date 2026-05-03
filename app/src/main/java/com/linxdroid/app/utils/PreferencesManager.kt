package com.linxdroid.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "linxdroid_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_INSTALLED_DISTRO = stringPreferencesKey("installed_distro")
        val KEY_VNC_PORT         = intPreferencesKey("vnc_port")
        val KEY_VNC_DISPLAY      = intPreferencesKey("vnc_display")
        val KEY_CUSTOM_ARGS      = stringPreferencesKey("custom_proot_args")
        val KEY_FIRST_LAUNCH     = stringPreferencesKey("first_launch_done")
    }

    val installedDistro: Flow<String?> = context.dataStore.data.map { it[KEY_INSTALLED_DISTRO] }
    val vncPort: Flow<Int>             = context.dataStore.data.map { it[KEY_VNC_PORT] ?: 5900 }
    val vncDisplay: Flow<Int>          = context.dataStore.data.map { it[KEY_VNC_DISPLAY] ?: 0 }
    val customArgs: Flow<String>       = context.dataStore.data.map { it[KEY_CUSTOM_ARGS] ?: "" }
    val isFirstLaunch: Flow<Boolean>   = context.dataStore.data.map { it[KEY_FIRST_LAUNCH] == null }

    suspend fun setInstalledDistro(id: String?) {
        context.dataStore.edit { prefs ->
            if (id == null) prefs.remove(KEY_INSTALLED_DISTRO)
            else prefs[KEY_INSTALLED_DISTRO] = id
        }
    }

    suspend fun setVncPort(port: Int) {
        context.dataStore.edit { it[KEY_VNC_PORT] = port }
    }

    suspend fun setVncDisplay(display: Int) {
        context.dataStore.edit { it[KEY_VNC_DISPLAY] = display }
    }

    suspend fun setCustomArgs(args: String) {
        context.dataStore.edit { it[KEY_CUSTOM_ARGS] = args }
    }

    suspend fun markFirstLaunchDone() {
        context.dataStore.edit { it[KEY_FIRST_LAUNCH] = "done" }
    }
}
