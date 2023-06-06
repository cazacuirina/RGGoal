package irina.dam.rggoal.Updates;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.util.Log;

import java.io.File;

public class LocalFilesBackup extends BackupAgentHelper {
    private static final String PREFS_BACKUP_KEY = "prefs_backup";
    private static final String DB_BACKUP_KEY = "db_backup";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper prefsHelper = new SharedPreferencesBackupHelper(this, "preferinte.xml");
        addHelper(PREFS_BACKUP_KEY, prefsHelper);

        FileBackupHelper fileDbHelper = new FileBackupHelper(this, getDatabasePath("goals.db").getAbsolutePath());
        addHelper(DB_BACKUP_KEY, fileDbHelper);
    }

    @Override
    public File getFilesDir() {
        File path = getExternalFilesDir(null);
        return (path != null) ? path : super.getFilesDir();
    }
}
