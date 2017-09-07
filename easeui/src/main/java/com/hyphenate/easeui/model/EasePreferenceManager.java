package com.hyphenate.easeui.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hyphenate.easeui.controller.EaseUI;

import java.util.Set;

import static com.hyphenate.easeui.R.string.set;

public class EasePreferenceManager {
    private SharedPreferences.Editor editor;
    private SharedPreferences mSharedPreferences;
    private static final String KEY_AT_GROUPS = "AT_GROUPS"; 
    
    @SuppressLint("CommitPrefEdits")
    private EasePreferenceManager(){
        mSharedPreferences = EaseUI.getInstance().getContext().getSharedPreferences("EM_SP_AT_MESSAGE", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }
    private static EasePreferenceManager instance;
    
    public synchronized static EasePreferenceManager getInstance(){
        if(instance == null){
            instance = new EasePreferenceManager();
        }
        return instance;
        
    }
    
    
    public void setAtMeGroups(Set<String> groups) {
        for (String str : groups) {
            Log.e("esmanager","atMeGroups:"+str);
        }

        editor.remove(KEY_AT_GROUPS);
        editor.putStringSet(KEY_AT_GROUPS, groups);
        editor.apply();
    }
    
    public Set<String> getAtMeGroups(){
        return mSharedPreferences.getStringSet(KEY_AT_GROUPS, null);
    }
    
}
