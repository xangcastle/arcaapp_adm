package com.twine.arca_adm.general;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Jose Williams Garcia on 13/5/2017.
 */


public class SessionManager{
    int PRIVATE_MODE = 0;
    Context _context;
    SharedPreferences.Editor editor;
    SharedPreferences pref;

    public SessionManager(Context paramContext)
    {
        this._context = paramContext;
        this.pref = this._context.getSharedPreferences("AndroidGreeve", this.PRIVATE_MODE);
        this.editor = this.pref.edit();
    }

    public void saveSharedValue(String Key, String Value)
    {
        this.editor.putString(Key, Value);
        this.editor.commit();
    }
    public String getSharedValue(String Key)
    {
        try {
            return this.pref.getString(Key, null);
        }
        catch (NumberFormatException ex){
            return null;
        }
    }
}
