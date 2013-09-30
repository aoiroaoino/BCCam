package com.applicative.goldrush.bccam

import android.os.Bundle
import android.preference.PreferenceFragment
import android.app.Activity
import android.preference.ListPreference
import android.preference.EditTextPreference
import android.content.SharedPreferences

class SettingActivity extends Activity {
  
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFlagment()).commit()
  }
  
  class SettingsFlagment extends PreferenceFragment
    with SharedPreferences.OnSharedPreferenceChangeListener
  {
    override def onCreate(savedInstanceState: Bundle): Unit = {
      super.onCreate(savedInstanceState)
      addPreferencesFromResource(R.xml.activity_setting)
      
      // 設定画面開いた際の値でSummaryを更新する。
      setPreferenceSummary
    }
    
    override def onResume(): Unit = {
      super.onResume()
      getPreferenceScreen.getSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
    
    override def onPause(): Unit = {
      super.onPause()
      getPreferenceScreen.getSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
    
    // Preferenceの値が変更された際に呼ばれる
    def onSharedPreferenceChanged(sharedPreference: SharedPreferences, key: String): Unit = {
      setPreferenceSummary()
    }
    
    private def setPreferenceSummary(): Unit = {
      val userNamePref = getPreferenceScreen.findPreference("user_name").asInstanceOf[EditTextPreference]
      userNamePref.setSummary(userNamePref.getText)
      
      val destinationPref = getPreferenceScreen.findPreference("destination").asInstanceOf[ListPreference]
      destinationPref.setSummary(destinationPref.getValue)
    }
  }
}