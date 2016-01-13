package ru.euphoriadev.vk.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.service.LongPollService;

/**
 * Created by Igor on 16.11.15.
 * Базовый класс, который позволяет поддерживать глобальное состояние приложения
 */
public class AppLoader extends Application {
    public static final String TAG = "AppLoader";
    public static final String APP_DIR = "Euphoria";

    /** Default font */
    public static final String SYSTEM_FONT = "Default";

    /** Fonts **/
    public static final String ROBOTO_LIGHT = "Roboto-Light.ttf";
    public static final String ROBOTO_REGULAR = "Roboto-Regular.ttf";
    public static final String DROID_REGULAR = "DroidSans.ttf";
    public static final String DROID_BOLD = "DroidSans-Bold.ttf";

    /** Preferences key **/
    public static final String KEY_IS_NIGHT_THEME = "is_night_theme";
    public static final String KEY_COLOUR_THEME = "colour_theme";
    public static final String KEY_COLOR_IN_MESSAGES = "color_in_messages";
    public static final String KEY_COLOR_OUT_MESSAGES = "color_out_messages";
    public static final String KEY_SHOW_DIVIDER = "show_divider";
    public static final String KEY_USE_TWO_PROFILE = "use_two_profile";
    public static final String KEY_FORCED_LOCALE = "forced_locale";
    public static final String KEY_FONT = "font";
    public static final String KEY_ONLINE_STATUS = "online_status";
    public static final String KEY_HIDE_TYPING = "hide_typing";
    public static final String KEY_ENABLE_NOTIFY = "enable_notify";
    public static final String KEY_ENABLE_NOTIFY_VIBRATE = "enable_notify_vibrate";
    public static final String KEY_ENABLE_NOTIFY_LED = "enable_notify_led";
    public static final String KEY_RFM = "resend_failed_msg";
    public static final String KEY_ENCRYPT_MESSAGES = "encrypt_messages";
    public static final String KEY_WRITE_LOG = "write_log";
    public static final String KEY_MAKING_DRAWER_HEADER = "making_drawer_header";


    /** Cached important preferences */
    public boolean isDarkTheme;
    public boolean writeLog;
    public String themeName;
    public String forcedLocale;
    public String fontName;
    public String makingDrawerHeader;


    public static volatile Context appContext;
    private SharedPreferences sPrefs;
    private ExecutorService mExecutor;
    private Handler mHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        Log.i(TAG, "onCreate");

        sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mExecutor = Executors.newSingleThreadExecutor();
        mHandler = new Handler(appContext.getMainLooper());

        updatePreferences();
        createAppDir();

//        TypefaceManager.setDefaultFonts();
        Thread.setDefaultUncaughtExceptionHandler(FileLogger.DEFAULT_ERROR_HANDLER);
    }

    /** Methods for themes **/

    /**
     * Apply theme to activity, without {@link Activity#recreate()}
     * @param withLocale true if you want to change language
     */
    public void applyTheme(Activity activity, boolean withLocale, boolean drawingStatusBar) {
        updatePreferenceColor();
        ThemeManager.applyTheme(activity, drawingStatusBar);
//        int styleId = 0;
//        switch (themeName.toUpperCase()) {
//            case "DARK":      styleId = R.style.AppBaseTheme_Dark; break;
//            case "BLACK":     styleId = R.style.AppBaseTheme_Black ;break;
//            case "INDIGO":    styleId = isDarkTheme ? R.style.AppBaseTheme_Indigo : R.style.AppBaseThemeLight_Indigo; break;
//            case "RED":       styleId = isDarkTheme ? R.style.AppBaseTheme_Red    : R.style.AppBaseThemeLight_Red;    break;
//            case "ORANGE":    styleId = isDarkTheme ? R.style.AppBaseTheme_Orange : R.style.AppBaseThemeLight_Orange; break;
//            case "BLUE_GREY": styleId = isDarkTheme ? R.style.AppBaseTheme_Blue_Grey : R.style.AppBaseThemeLight_Blue_Grey; break;
//            case "PINK":      styleId = isDarkTheme ? R.style.AppBaseTheme_Pink : R.style.AppBaseThemeLight_Pink; break;
//            case "TEAL":      styleId = isDarkTheme ? R.style.AppBaseTheme_Teal : R.style.AppBaseThemeLight_Teal; break;
//            case "GREEN":      styleId = isDarkTheme ? R.style.AppBaseTheme_Green : R.style.AppBaseThemeLight_Green; break;
//            case "DEEP_ORANGE": styleId = isDarkTheme ? R.style.AppBaseTheme_Deep_Orange : R.style.AppBaseThemeLight_Deep_Orange; break;
//            case "BROWN":     styleId = isDarkTheme ? R.style.AppBaseTheme_Brown : R.style.AppBaseThemeLight_Brown; break;
//
//            default:
//                styleId = R.style.AppBaseTheme_Indigo;
//        }
//        activity.setTheme(styleId);
//
//        if (withLocale) {
//            if (!forcedLocale.equalsIgnoreCase(Locale.getDefault().getLanguage())) {
//                Locale locale = new Locale(forcedLocale);
//                Locale.setDefault(locale);
//                Configuration config = new Configuration();
//                config.locale = locale;
//                appContext.getResources().updateConfiguration(config,
//                        appContext.getResources().getDisplayMetrics());
//            }
//        }
//
//        if (drawingStatusBar)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = activity.getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }


    }

    /**
     * Apply theme to activity, without {@link Activity#recreate()}
     * @param activity
     */
    public void applyTheme(Activity activity) {
        applyTheme(activity, true, false);
    }

    /**
     * Получение Header-а шторки, основывайсь на текущей теме.
     * @param withSolidBackground учитывать настройки
     * @return
     */
    public Drawable getDrawerHeader(Context context, boolean withSolidBackground) {
        if (makingDrawerHeader.equals("blur_photo")) {
            return null;
        }
        if (withSolidBackground) {
            if (makingDrawerHeader.equals("solid_background")) {
                return new ColorDrawable(ThemeUtils.getThemeAttrColor(context, R.attr.colorPrimary));
            }
        }
        int drawableId;
        switch (themeName.toUpperCase()) {
            case "DARK":   drawableId = R.drawable.drawer_header_dark; break;
            case "BLACK":  drawableId = R.drawable.drawer_header_black;  break;
            case "INDIGO": drawableId = R.drawable.drawer_header; break;
            case "RED":    drawableId = R.drawable.drawer_header_black;  break;
            case "ORANGE": drawableId = R.drawable.drawer_header_orange2; break;
            case "BLUE_GREY": drawableId = R.drawable.drawer_header; break;
            case "PINK":  drawableId = R.drawable.drawer_header_pink; break;
            case "TEAL":  drawableId = R.drawable.drawer_header_black; break;
            case "DEEP_ORANGE": drawableId = R.drawable.drawer_header_orange2; break;
            case "BROWN": drawableId = R.drawable.drawer_header; break;
            case "GREEN": drawableId = R.drawable.drawer_header_green; break;

            default: drawableId = R.drawable.drawer_header; break;
        }
        return appContext.getResources().getDrawable(drawableId);
    }


    public static AppLoader getLoader(Context applicationContext) {
        return (AppLoader) applicationContext;
    }

    public static AppLoader getLoader() {
        return (AppLoader) appContext;
    }


    public SharedPreferences getPreferences() {
        return sPrefs;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public ExecutorService getExecutor() {
        return mExecutor;
    }

    public File getExternalFilesDir() {
        return Environment.getExternalStorageDirectory();
    }

    public File getAppDir() {
        final File file = new File(getExternalFilesDir().getAbsolutePath() + "/" + APP_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * Обновление настроек
     */
    public void updatePreferences() {
        isDarkTheme = sPrefs.getBoolean(KEY_IS_NIGHT_THEME, true);
        themeName = sPrefs.getString(KEY_COLOUR_THEME, "Red");
        forcedLocale = sPrefs.getString(KEY_FORCED_LOCALE, Locale.getDefault().getLanguage());
        fontName = sPrefs.getString(KEY_FONT, ROBOTO_REGULAR);
        writeLog = sPrefs.getBoolean(KEY_WRITE_LOG, true);
        makingDrawerHeader = sPrefs.getString(KEY_MAKING_DRAWER_HEADER, "Default");
    }

    public void updatePreferenceColor() {
        isDarkTheme = sPrefs.getBoolean(KEY_IS_NIGHT_THEME, true);
        themeName = sPrefs.getString(KEY_COLOUR_THEME, "Red");
    }

    /**
     * Создание папки приложения
     * @return true если папку создалась, false = если папка уже была ранее создана
     */
    private boolean createAppDir() {
        File file = new File(Environment.getExternalStorageDirectory(), APP_DIR);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }


}
