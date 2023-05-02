package net.info420.trouveurarticle.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Classe qui gère les préférences de l'utilisateurs
 */
public class AppSettings {
    /**
     * Déclaration de l'ID de la permission pour accéder au menu d'option
     */
    public static final int SETTINGS_PERMISSION = 1;
    /**
     * Déclaration de la clé de la préférence de rafraichisement du service de suivi
     */
    private static final String PREF_REFRESH_TIME = "refresh_time";
    /**
     * Valeur par défaut du temps de rafraichisement du service de suivi
     */
    public static final int DefaultRefreshTime = 120;
    /**
     * Déclaration de la clé de préférence du rafraichissement du service de suivi lorsque l'appareil utilise des données cellulaires
     */
    private static final String PREF_REFRESH_TIME_CELL_DATA = "refresh_time_cell_data";
    /**
     * Valeur par défaut du temps de rafraichissement du service de suivi lorsque l'appareil utilise des données cellulaire
     */
    public static final int DefaultRefreshTimeCellData = 180;
    /**
     * Déclaration de la clé de préférence du rafraichissement du service de suivi lorsque l'appareil est branché
     */
    private static final String PREF_REFRESH_TIME_CELL_PLUGGING_IN = "refresh_time_cell_plugged_in";
    /**
     * Valeur par défaut du temps de rafraichissement du service de suivi lorsque l'appareil est branché
     */
    public static final int DefaultRefreshTimeCellPluggedIn = 60;
    /**
     * Déclaration de la clé de préférence pour désactiver le rafraichissement du système de suivi lorsque l'appareil utilise des données cellulaires
     */
    private static final String PREF_DISABLE_REFRESH_CELL_DATA = "disable_refresh_cell_data";
    /**
     * Valeur par défaut pour désactiver le rafraichissement du système de suivi lorsque l'appareil utilise des données cellulaires
     */
    public static final boolean DefaultDisableRefreshCellData = true;
    /**
     * Déclaration de la clé de préférence pour désactiver le rafraichissement du système de suivi lorsque la batterie est faible
     */
    private static final String PREF_DISABLE_REFRESH_BATTERY_LOW = "disable_refresh_battery_low";
    /**
     * Valeur par défaut pour désactiver le rafraichissement du système de suivi lorsque la batterie est faible
     */
    public static final boolean DefaultDisableRefreshBatteryLow = true;
    /**
     * Déclaration de la clé de préférence pour activer le remplacement automatique du nom de produit
     */
    private static final String PREF_AUTOMATICALLY_REPLACE_PRODUCT_NAME = "automatically_replace_product_name";
    /**
     * Valeur par défaut pour activer le remplacement automatique du nom de produit
     */
    public static final boolean DefaultAutomaticallyReplaceProductName = true;
    /**
     * Déclaration de la clé de préférence pour activer le rafraichissement automatique des données
     */
    private static final String PREF_AUTOMATICALLY_REFRESH_DATA = "automatically_refresh_data";
    /**
     * Valeur par défaut pour activer le rafraichissement automatique des données
     */
    public static final boolean DefaultAutomaticallyRefreshData = true;
    /**
     * Déclaration de la clé de préférence pour le nombre de fois que l'utilisateur refuse de donner la permission pour accéder au menu d'options
     */
    private static final String PREF_OPTION_DENIED_AMOUNT = "time_dangerous_permission_denied";

    /**
     * Préférences de l'application
     */
    private SharedPreferences sharedPreferences;

    /**
     * Constructeur par défaut de la classe. Obtient les préférences de l'application
     * @param context Contexte de l'application
     */
    public AppSettings(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Méthode qui obtient le temps de rafraichissement du service de suivi
     * @return int, Temps de rafraichissement du service de suivi
     */
    public int getRefreshTime() {
        return sharedPreferences.getInt(PREF_REFRESH_TIME, DefaultRefreshTime);
    }

    /**
     * Méthode qui défini le temps de rafraichissement du service de suivi
     * @param time Temps de rafraichissement du service de suivi
     */
    public void setRefreshTime(int time) {
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME, time).apply();
    }

    /**
     * Méthode qui obtient le temps de rafraichissement du service de suivi lorsque l'appareil utilise des données cellulaire
     * @return int, Temps de rafraichissement lorsque l'appareil utilise des données cellulaire
     */
    public int getRefreshTimeCellData() {
        return sharedPreferences.getInt(PREF_REFRESH_TIME_CELL_DATA, DefaultRefreshTimeCellData);
    }

    /**
     * Méthode qui défini le temps de rafraichissement du service de suiv lorsque l'appareil utilise des données cellulaire
     * @param time Temps de rafraichissement lorsque l'appareil utilise des données cellulaire
     */
    public void setRefreshTimeCellData(int time) {
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME_CELL_DATA, time).apply();
    }

    /**
     * Méthode qui obtient le temps de rafraichissement du service de suivi lorsque l'appareil est branché
     * @return
     */
    public int getRefreshTimeCellPluggedIn() {
        return sharedPreferences.getInt(PREF_REFRESH_TIME_CELL_PLUGGING_IN, DefaultRefreshTimeCellPluggedIn);
    }

    /**
     * Méthode qui défini le temps de rafraichissement du service de suivi lorsque l'appareil est branché
     * @param time Le temps de rafraichissement du service de suivi lorsque l'appareil est branché
     */
    public void setRefreshTimeCellPluggedIn(int time) {
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME_CELL_PLUGGING_IN, time).apply();
    }

    /**
     * Méthode qui obtient si le service de suivi doit être suspendu lorsque l'appareil utilise des données cellulaires
     * @return boolean, Si le service de suivi doit être suspendu lorsque l'appareil utilise des données cellularies
     */
    public boolean getDisableRefreshCellData() {
        return sharedPreferences.getBoolean(PREF_DISABLE_REFRESH_CELL_DATA, DefaultDisableRefreshCellData);
    }

    /**
     * Méthode qui défini si le service de suivi doit être suspendu lorsque l'appareil utilise des données cellulaire
     * @param disabled Si le service de suivi doit être suspendu lorsque l'appareil utilise des données cellulaire
     */
    public void setDisableRefreshCellData(boolean disabled) {
        sharedPreferences.edit().putBoolean(PREF_DISABLE_REFRESH_CELL_DATA, disabled).apply();
    }

    /**
     * Méthode qui obtient si le service de suivi doit être suspendu lorsque la batterie est faible
     * @return boolean, Si le service de suivi doit être suspendu lorsque la batterie est faible
     */
    public boolean getDisableRefreshBatteryLow() {
        return sharedPreferences.getBoolean(PREF_DISABLE_REFRESH_BATTERY_LOW, DefaultDisableRefreshBatteryLow);
    }

    /**
     * Méthode qui défini si le service de suivi est suspendu lorsque la batterie est faible
     * @param disabled Si le service de suivi est suspendu lorsque la batterie est faible
     */
    public void setDisableRefreshBatteryLow(boolean disabled) {
        sharedPreferences.edit().putBoolean(PREF_DISABLE_REFRESH_BATTERY_LOW, disabled).apply();
    }

    /**
     * Méthode qui obtient si le nom du produit doit être mis à jour automatiquement
     * @return boolean, Si le nom du produit doit être mis à jour automatiquement
     */
    public boolean getAutomaticallyReplaceProductName() {
        return sharedPreferences.getBoolean(PREF_AUTOMATICALLY_REPLACE_PRODUCT_NAME, DefaultAutomaticallyReplaceProductName);
    }

    /**
     * Méthode qui défini si le nom du produit doit être mis à jour automatiquement
     * @param enabled Si le nom du produit doit être mis à jour automatiquement
     */
    public void setAutomaticallyReplaceProductName(boolean enabled) {
        sharedPreferences.edit().putBoolean(PREF_AUTOMATICALLY_REPLACE_PRODUCT_NAME, enabled).apply();
    }

    /**
     * Méthode qui obtient si le rafraichissement automatique est activé
     * @return boolean, Si le rafraichissement automatique est activé
     */
    public boolean getAutomaticallyRefreshData() {
        return sharedPreferences.getBoolean(PREF_AUTOMATICALLY_REFRESH_DATA, DefaultAutomaticallyRefreshData);
    }

    /**
     * Méthode qui défini si le rafraichissement automatique est activé
     * @param enabled Si le rafraichiseement automatique est activé
     */
    public void setAutomaticallyRefreshData(boolean enabled) {
        sharedPreferences.edit().putBoolean(PREF_AUTOMATICALLY_REFRESH_DATA, enabled).apply();
    }

    /**
     * Méthode qui obtient le nombre de fois que l'utilisateur a refusé de donner la permission d'accès au menu d'options
     * @return int, Le nombre de fois que l'utilisateur a refusé de donner la permission d'accès au menu d'options
     */
    public int getPermissionDeniedAmount() {
        return sharedPreferences.getInt(PREF_OPTION_DENIED_AMOUNT, 0);
    }

    /**
     * Méthode qui incrémente le nombre de fois que l'utilisateur refuse de donner la permission d'accès au menu d'option
     */
    public void addPermissionDeniedAmount() {
        sharedPreferences.edit().putInt(PREF_OPTION_DENIED_AMOUNT, sharedPreferences.getInt(PREF_OPTION_DENIED_AMOUNT, 0) + 1).apply();
    }

    /**
     * Méthode qui retourne l'ID d'une nouvelle notification en autoincrémentant l'ID dans les préférences
     * @return int, l'ID de la notification
     */
    public int createNewNotification() {
        sharedPreferences.edit().putInt("NOTIFICATION_ID", sharedPreferences.getInt("NOTIFICATION_ID", 0) + 1);
        return sharedPreferences.getInt("NOTIFICATION_ID", 1);
    }

    /**
     * Méthode qui réinitialise les paramètres de l'application pour ceux par défaut
     */
    public void ResetSettings() {
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME, DefaultRefreshTime).apply();
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME_CELL_DATA, DefaultRefreshTimeCellData).apply();
        sharedPreferences.edit().putInt(PREF_REFRESH_TIME_CELL_PLUGGING_IN, DefaultRefreshTimeCellPluggedIn).apply();
        sharedPreferences.edit().putBoolean(PREF_DISABLE_REFRESH_CELL_DATA, DefaultDisableRefreshCellData).apply();
        sharedPreferences.edit().putBoolean(PREF_DISABLE_REFRESH_BATTERY_LOW, DefaultDisableRefreshBatteryLow).apply();
        sharedPreferences.edit().putBoolean(PREF_AUTOMATICALLY_REPLACE_PRODUCT_NAME, DefaultAutomaticallyReplaceProductName).apply();
        sharedPreferences.edit().putBoolean(PREF_AUTOMATICALLY_REFRESH_DATA, DefaultAutomaticallyRefreshData).apply();
    }
}
