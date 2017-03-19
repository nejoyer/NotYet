package com.outlook.notyetapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.outlook.notyetapp.R;

// Preference manager intended to be provide by DI so that preferences can easily
// be stored and retrieved.
// Also, easily mocked for unit testing.
public class SharedPreferencesManager {

    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public static final String DBDATE_LAST_UPDATED_TO_KEY = "dbdatelastupdatedto";
    public static final String QUERY_DATE_KEY = "querydate";
    public static final String SHOW_ALL_KEY = "showall";
    public final static String SHOULD_SHOW_HERO_TOOL_TIP_KEY = "should_show_hero_tool_tip";
    public final static String HIDE_HEADER_KEY = "hideheader";
    private final static String EULA_AGREED_KEY = "eula";

    public SharedPreferencesManager(Context context) {
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mContext = context;
    }

    @Nullable
    public Long getLastDBDateUpdatedTo() {
        return this.mSharedPreferences.getLong(DBDATE_LAST_UPDATED_TO_KEY, 0);
    }

    public void setLastDBDateUpdatedTo(long dbDateUpdatedTo){
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putLong(DBDATE_LAST_UPDATED_TO_KEY, dbDateUpdatedTo);
        editor.apply();
    }

    @Nullable
    public Long getQueryDate(){
        return this.mSharedPreferences.getLong(QUERY_DATE_KEY, 0);
    }

    public void setQueryDate(long queryDate){
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putLong(QUERY_DATE_KEY, queryDate);
        editor.apply();
    }

    @Nullable
    public Boolean getShowAll(){
        return this.mSharedPreferences.getBoolean(SHOW_ALL_KEY, false);
    }

    public void setShowAll(boolean showAll){
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putBoolean(SHOW_ALL_KEY, showAll);
        editor.apply();
    }

    @Nullable
    public Boolean getShouldShowHeroToolTip(){
        return this.mSharedPreferences.getBoolean(SHOULD_SHOW_HERO_TOOL_TIP_KEY, true);
    }

    public void setShouldShowHeroToolTip(boolean shouldShowHeroToolTip){
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putBoolean(SHOULD_SHOW_HERO_TOOL_TIP_KEY, shouldShowHeroToolTip);
        editor.apply();
    }

    @Nullable
    public Boolean getHideHeader(){
        return this.mSharedPreferences.getBoolean(HIDE_HEADER_KEY, false);
    }

    public void setHideHeader(boolean hideHeader){
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putBoolean(HIDE_HEADER_KEY, hideHeader);
        editor.apply();
    }

    @Nullable
    public String getEULAAgreed(){
        return this.mSharedPreferences.getString(EULA_AGREED_KEY, mContext.getString(R.string.date_last_agreed));
    }

    public void setEULAAgreed(String eulaAgreed){
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putString(EULA_AGREED_KEY, eulaAgreed);
        editor.apply();
    }

    @Nullable
    public String getOffset(){
        //Uses a string from xml file rather than const in this file because of how it binds to preferences
        return this.mSharedPreferences.getString(this.mContext.getString(R.string.pref_day_change_key), "0");
    }

    //Used in testing
    @Nullable
    public void clearOffset(){
        //Uses a string from xml file rather than const in this file because of how it binds to preferences
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.remove(this.mContext.getString(R.string.pref_day_change_key));
        editor.apply();
    }

    public void clearPreferences(){
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
