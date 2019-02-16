package sam.gymnotes;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.util.Log;

/**
 * Created by Sam on 09/09/2017.
 */

public class myBackupAgent extends BackupAgentHelper {

    DBHandler DB;
    String prefs;

    static final String DB_BACKUP_KEY = "GymNotes_DB";
    static final String PREFS_BACKUP_KEY = "GymNotes_Prefs";

    public myBackupAgent(){

    }

    @Override
    public void onCreate() {
        super.onCreate();

        DB = new DBHandler(this);

        prefs = getString(R.string.shared_preferences);

        DB.open();
        String DBPath = DB.getPath();
        String DBName = DB.getDBName();
        DB.close();

        this.getDatabasePath(DB.getDBName()).getAbsolutePath();

        Log.i("MY BACKUP AGENT ", "PATH ='" + DBPath + "'");



        FileBackupHelper fbuh = new FileBackupHelper(this, "../databases/" + DBName);
        addHelper(DB_BACKUP_KEY, fbuh);

        SharedPreferencesBackupHelper spbh = new SharedPreferencesBackupHelper(this, prefs);
        addHelper(PREFS_BACKUP_KEY, spbh);
    }

}
