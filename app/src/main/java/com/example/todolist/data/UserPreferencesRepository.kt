package com.example.todolist.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.todolist.model.AppLanguage
import com.example.todolist.model.BorderStyleMode
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.CornerStyleMode
import com.example.todolist.model.FirstDayOfWeekMode
import com.example.todolist.model.FontScaleMode
import com.example.todolist.model.LayoutDensityMode
import com.example.todolist.model.SubscriptionPlan
import com.example.todolist.model.UserPreferencesState
import com.example.todolist.ui.theme.AvailableThemes
import com.example.todolist.ui.theme.resolveThemeIdForMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "todo_user_preferences"
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val LOGGED_IN_USER_ID = intPreferencesKey("logged_in_user_id")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val FOLLOW_SYSTEM_THEME = booleanPreferencesKey("follow_system_theme")
        val SELECTED_THEME_ID = intPreferencesKey("selected_theme_id")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val COLORBLIND_MODE = stringPreferencesKey("colorblind_mode")
        val FONT_SCALE_MODE = stringPreferencesKey("font_scale_mode")
        val HIGH_CONTRAST_ENABLED = booleanPreferencesKey("high_contrast_enabled")
        val DYSLEXIA_FRIENDLY_ENABLED = booleanPreferencesKey("dyslexia_friendly_enabled")
        val LEFT_HANDED_MODE = booleanPreferencesKey("left_handed_mode")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val LARGE_TOUCH_TARGETS = booleanPreferencesKey("large_touch_targets")
        val ZEN_FOCUS_MODE = booleanPreferencesKey("zen_focus_mode")
        val BORDER_STYLE_MODE = stringPreferencesKey("border_style_mode")
        val CORNER_STYLE_MODE = stringPreferencesKey("corner_style_mode")
        val LAYOUT_DENSITY_MODE = stringPreferencesKey("layout_density_mode")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val PRIVACY_BLUR_MODE = booleanPreferencesKey("privacy_blur_mode")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val SUBSCRIPTION_PLAN = stringPreferencesKey("subscription_plan")
        val REWARDED_UNLOCK_UNTIL = longPreferencesKey("rewarded_unlock_until")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val LAST_DRIVE_SYNC_MILLIS = longPreferencesKey("last_drive_sync_millis")
        val DRIVE_AUTO_BACKUP_ENABLED = booleanPreferencesKey("drive_auto_backup_enabled")
        val DEFAULT_REMINDER_TIME = stringPreferencesKey("default_reminder_time")
    }

    companion object {
        const val NO_USER_LOGGED_IN = -1
        const val DEFAULT_DARK_MODE = false
        const val DEFAULT_FOLLOW_SYSTEM_THEME = false
        const val DEFAULT_THEME_ID = 0
        const val DEFAULT_NOTIFICATIONS = true
        val DEFAULT_LANGUAGE = AppLanguage.ES.code
        val DEFAULT_COLORBLIND_MODE = ColorblindMode.NONE.code
        val DEFAULT_FONT_SCALE = FontScaleMode.NORMAL.code
        const val DEFAULT_HIGH_CONTRAST = false
        const val DEFAULT_DYSLEXIA_FRIENDLY = false
        const val DEFAULT_LEFT_HANDED = false
        const val DEFAULT_REDUCE_MOTION = false
        const val DEFAULT_LARGE_TOUCH = false
        const val DEFAULT_ZEN_FOCUS = false
        val DEFAULT_BORDER_STYLE = BorderStyleMode.SUBTLE.code
        val DEFAULT_CORNER_STYLE = CornerStyleMode.ROUNDED.code
        val DEFAULT_LAYOUT_DENSITY = LayoutDensityMode.COMFORTABLE.code
        const val DEFAULT_HAPTIC_FEEDBACK = true
        const val DEFAULT_PRIVACY_BLUR = false
        const val DEFAULT_STREAK_DAYS = 1
        val DEFAULT_SUBSCRIPTION_PLAN = SubscriptionPlan.FREE.code
        const val DEFAULT_REWARDED_UNLOCK_UNTIL = 0L
        val DEFAULT_FIRST_DAY_OF_WEEK = FirstDayOfWeekMode.SYSTEM.code
        const val DEFAULT_LAST_DRIVE_SYNC_MILLIS = 0L
        const val DEFAULT_DRIVE_AUTO_BACKUP = true
        const val DEFAULT_REMINDER_TIME_VALUE = "09:00 AM"
    }

    val preferencesFlow: Flow<UserPreferencesState> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeId = preferences[PreferencesKeys.SELECTED_THEME_ID] ?: DEFAULT_THEME_ID
            val themePalette = AvailableThemes.find { it.id == themeId } ?: AvailableThemes[0]
            val isDark = preferences[PreferencesKeys.IS_DARK_MODE] ?: themePalette.isDark
            val syncedThemeId = resolveThemeIdForMode(themeId, isDark)

            UserPreferencesState(
                isLoaded = true,
                loggedInUserId = preferences[PreferencesKeys.LOGGED_IN_USER_ID] ?: NO_USER_LOGGED_IN,
                isDarkMode = isDark,
                followSystemTheme = preferences[PreferencesKeys.FOLLOW_SYSTEM_THEME]
                    ?: DEFAULT_FOLLOW_SYSTEM_THEME,
                themeId = syncedThemeId,
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED]
                    ?: DEFAULT_NOTIFICATIONS,
                languageCode = preferences[PreferencesKeys.APP_LANGUAGE] ?: DEFAULT_LANGUAGE,
                colorblindMode = ColorblindMode.fromCode(
                    preferences[PreferencesKeys.COLORBLIND_MODE] ?: DEFAULT_COLORBLIND_MODE
                ),
                fontScaleMode = FontScaleMode.fromCode(
                    preferences[PreferencesKeys.FONT_SCALE_MODE] ?: DEFAULT_FONT_SCALE
                ),
                highContrastEnabled = preferences[PreferencesKeys.HIGH_CONTRAST_ENABLED]
                    ?: DEFAULT_HIGH_CONTRAST,
                dyslexiaFriendlyEnabled = preferences[PreferencesKeys.DYSLEXIA_FRIENDLY_ENABLED]
                    ?: DEFAULT_DYSLEXIA_FRIENDLY,
                leftHandedMode = preferences[PreferencesKeys.LEFT_HANDED_MODE]
                    ?: DEFAULT_LEFT_HANDED,
                reduceMotion = preferences[PreferencesKeys.REDUCE_MOTION]
                    ?: DEFAULT_REDUCE_MOTION,
                largeTouchTargets = preferences[PreferencesKeys.LARGE_TOUCH_TARGETS]
                    ?: DEFAULT_LARGE_TOUCH,
                zenFocusMode = preferences[PreferencesKeys.ZEN_FOCUS_MODE]
                    ?: DEFAULT_ZEN_FOCUS,
                borderStyleMode = BorderStyleMode.fromCode(
                    preferences[PreferencesKeys.BORDER_STYLE_MODE] ?: DEFAULT_BORDER_STYLE
                ),
                cornerStyleMode = CornerStyleMode.fromCode(
                    preferences[PreferencesKeys.CORNER_STYLE_MODE] ?: DEFAULT_CORNER_STYLE
                ),
                layoutDensityMode = LayoutDensityMode.fromCode(
                    preferences[PreferencesKeys.LAYOUT_DENSITY_MODE] ?: DEFAULT_LAYOUT_DENSITY
                ),
                hapticFeedbackEnabled = preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED]
                    ?: DEFAULT_HAPTIC_FEEDBACK,
                privacyBlurMode = preferences[PreferencesKeys.PRIVACY_BLUR_MODE]
                    ?: DEFAULT_PRIVACY_BLUR,
                streakDays = preferences[PreferencesKeys.STREAK_DAYS] ?: DEFAULT_STREAK_DAYS,
                subscriptionPlan = SubscriptionPlan.fromCode(
                    preferences[PreferencesKeys.SUBSCRIPTION_PLAN] ?: DEFAULT_SUBSCRIPTION_PLAN
                ),
                rewardedUnlockUntilMillis = preferences[PreferencesKeys.REWARDED_UNLOCK_UNTIL]
                    ?: DEFAULT_REWARDED_UNLOCK_UNTIL,
                firstDayOfWeekMode = FirstDayOfWeekMode.fromCode(
                    preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] ?: DEFAULT_FIRST_DAY_OF_WEEK
                ),
                lastDriveSyncMillis = preferences[PreferencesKeys.LAST_DRIVE_SYNC_MILLIS]
                    ?: DEFAULT_LAST_DRIVE_SYNC_MILLIS,
                driveAutoBackupEnabled = preferences[PreferencesKeys.DRIVE_AUTO_BACKUP_ENABLED]
                    ?: DEFAULT_DRIVE_AUTO_BACKUP,
                defaultReminderTime = preferences[PreferencesKeys.DEFAULT_REMINDER_TIME]
                    ?: DEFAULT_REMINDER_TIME_VALUE
            )
        }

    suspend fun saveUserSession(userId: Int, userThemeId: Int) {
        val palette = AvailableThemes.find { it.id == userThemeId } ?: AvailableThemes[0]
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LOGGED_IN_USER_ID] = userId
            preferences[PreferencesKeys.SELECTED_THEME_ID] = palette.id
            preferences[PreferencesKeys.IS_DARK_MODE] = palette.isDark
        }
    }

    suspend fun clearUserSession() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LOGGED_IN_USER_ID] = NO_USER_LOGGED_IN
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            val currentThemeId = preferences[PreferencesKeys.SELECTED_THEME_ID] ?: DEFAULT_THEME_ID
            val targetThemeId = resolveThemeIdForMode(currentThemeId, isDark)
            preferences[PreferencesKeys.FOLLOW_SYSTEM_THEME] = false
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
            preferences[PreferencesKeys.SELECTED_THEME_ID] = targetThemeId
        }
    }

    suspend fun setFollowSystemTheme(followSystem: Boolean, currentSystemDark: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.FOLLOW_SYSTEM_THEME] = followSystem
            if (followSystem) {
                val currentThemeId = preferences[PreferencesKeys.SELECTED_THEME_ID] ?: DEFAULT_THEME_ID
                val targetThemeId = resolveThemeIdForMode(currentThemeId, currentSystemDark)
                preferences[PreferencesKeys.IS_DARK_MODE] = currentSystemDark
                preferences[PreferencesKeys.SELECTED_THEME_ID] = targetThemeId
            }
        }
    }

    suspend fun setThemePalette(themeId: Int) {
        val palette = AvailableThemes.find { it.id == themeId } ?: AvailableThemes[0]
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_THEME_ID] = palette.id
            preferences[PreferencesKeys.IS_DARK_MODE] = palette.isDark
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setLanguage(languageCode: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] = languageCode
        }
    }

    suspend fun setColorblindMode(mode: ColorblindMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.COLORBLIND_MODE] = mode.code
        }
    }

    suspend fun setFontScaleMode(mode: FontScaleMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SCALE_MODE] = mode.code
        }
    }

    suspend fun setHighContrastEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_CONTRAST_ENABLED] = enabled
        }
    }

    suspend fun setDyslexiaFriendlyEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.DYSLEXIA_FRIENDLY_ENABLED] = enabled
        }
    }

    suspend fun setLeftHandedMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LEFT_HANDED_MODE] = enabled
        }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.REDUCE_MOTION] = enabled
        }
    }

    suspend fun setLargeTouchTargets(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LARGE_TOUCH_TARGETS] = enabled
        }
    }

    suspend fun setZenFocusMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.ZEN_FOCUS_MODE] = enabled
        }
    }

    suspend fun setBorderStyleMode(mode: BorderStyleMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.BORDER_STYLE_MODE] = mode.code
        }
    }

    suspend fun setCornerStyleMode(mode: CornerStyleMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.CORNER_STYLE_MODE] = mode.code
        }
    }

    suspend fun setLayoutDensityMode(mode: LayoutDensityMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAYOUT_DENSITY_MODE] = mode.code
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun setPrivacyBlurMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.PRIVACY_BLUR_MODE] = enabled
        }
    }

    suspend fun incrementStreakDays() {
        context.userPreferencesDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.STREAK_DAYS] ?: DEFAULT_STREAK_DAYS
            preferences[PreferencesKeys.STREAK_DAYS] = current + 1
        }
    }

    suspend fun setSubscriptionPlan(plan: SubscriptionPlan) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBSCRIPTION_PLAN] = plan.code
        }
    }

    suspend fun grantRewardedProAccess24h(): Long {
        val unlockUntil = System.currentTimeMillis() + 86_400_000L
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.REWARDED_UNLOCK_UNTIL] = unlockUntil
        }
        return unlockUntil
    }

    suspend fun setFirstDayOfWeekMode(mode: FirstDayOfWeekMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] = mode.code
        }
    }

    suspend fun recordDriveSyncTimestamp(timestampMillis: Long = System.currentTimeMillis()) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_DRIVE_SYNC_MILLIS] = timestampMillis
        }
    }

    suspend fun setDriveAutoBackupEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.DRIVE_AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setDefaultReminderTime(timeFormatted: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REMINDER_TIME] = timeFormatted
        }
    }

    suspend fun resetToDefaultPreferences() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = DEFAULT_DARK_MODE
            preferences[PreferencesKeys.FOLLOW_SYSTEM_THEME] = DEFAULT_FOLLOW_SYSTEM_THEME
            preferences[PreferencesKeys.SELECTED_THEME_ID] = DEFAULT_THEME_ID
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = DEFAULT_NOTIFICATIONS
            preferences[PreferencesKeys.APP_LANGUAGE] = DEFAULT_LANGUAGE
            preferences[PreferencesKeys.COLORBLIND_MODE] = DEFAULT_COLORBLIND_MODE
            preferences[PreferencesKeys.FONT_SCALE_MODE] = DEFAULT_FONT_SCALE
            preferences[PreferencesKeys.HIGH_CONTRAST_ENABLED] = DEFAULT_HIGH_CONTRAST
            preferences[PreferencesKeys.DYSLEXIA_FRIENDLY_ENABLED] = DEFAULT_DYSLEXIA_FRIENDLY
            preferences[PreferencesKeys.LEFT_HANDED_MODE] = DEFAULT_LEFT_HANDED
            preferences[PreferencesKeys.REDUCE_MOTION] = DEFAULT_REDUCE_MOTION
            preferences[PreferencesKeys.LARGE_TOUCH_TARGETS] = DEFAULT_LARGE_TOUCH
            preferences[PreferencesKeys.ZEN_FOCUS_MODE] = DEFAULT_ZEN_FOCUS
            preferences[PreferencesKeys.BORDER_STYLE_MODE] = DEFAULT_BORDER_STYLE
            preferences[PreferencesKeys.CORNER_STYLE_MODE] = DEFAULT_CORNER_STYLE
            preferences[PreferencesKeys.LAYOUT_DENSITY_MODE] = DEFAULT_LAYOUT_DENSITY
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = DEFAULT_HAPTIC_FEEDBACK
            preferences[PreferencesKeys.PRIVACY_BLUR_MODE] = DEFAULT_PRIVACY_BLUR
            preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] = DEFAULT_FIRST_DAY_OF_WEEK
            preferences[PreferencesKeys.DEFAULT_REMINDER_TIME] = DEFAULT_REMINDER_TIME_VALUE
        }
    }
}
