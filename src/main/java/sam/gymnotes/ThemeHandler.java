package sam.gymnotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Sam on 15/09/2016.
 */
public class ThemeHandler {

    private static final String log = "ThemeHandler";

    private Context context;
    private SharedPreferences sharedPreferences;

    public final static String[] themes = {"blue", "red", "teal", "navy", "bronco", "pink", "green", "max"};
    public static final String defaultTheme = "teal";


    public ThemeHandler(Context c){
        context = c;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
    }

    public boolean gotPro(){
        return sharedPreferences.getBoolean(context.getString(R.string.shared_preferences_pro), false);
    }

    public String getThemeName(){
        if (!sharedPreferences.getBoolean(context.getString(R.string.shared_preferences_pro), false)){
            Log.v(log, "Not pro version, returning default theme " + defaultTheme);
            return defaultTheme;
        }

        String name = sharedPreferences.getString(context.getString(R.string.color_theme), "none");
        if (name.equals("none")) {
            Log.v(log, "No theme found in prefernces, returning default theme " + defaultTheme);
            return defaultTheme;
        }
        Log.v(log, "Setting theme to " + name);

        return name;
    }

    public void setTheme(String themeName){
        Log.v(log, "Setting theme to " + themeName);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.color_theme), themeName);
        editor.commit();
    }

    public int getActivityTheme(){
        return context.getResources().getIdentifier("ActivityTheme." + getThemeName(), "style", context.getPackageName());
    }

    public int getDialogTheme(){
        return context.getResources().getIdentifier("DialogTheme." + getThemeName(), "style", context.getPackageName());
    }

}
