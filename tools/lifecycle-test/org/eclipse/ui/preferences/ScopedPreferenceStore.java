package org.eclipse.ui.preferences;

public class ScopedPreferenceStore {
  public String getString(String key) {
    return System.getProperty(key, "");
  }

  public boolean getBoolean(String key) {
    return Boolean.getBoolean(key);
  }
}
