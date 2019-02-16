package sam.gymnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHandler {

    // I wrote this when I first learnt SQL so don't judge

    private static final int DB_VERSION = 2;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase DB;

    private static final String DB_NAME = "GYM_NOTES_DATABASE";
    private final Context DB_CONTEXT;

    private static final String KEY_ID = "_id";
    private static final String KEY_COLUMN_ARG = KEY_ID + " INTEGER PRIMARY KEY";
    private static final int COL_ID = 0;

    private static final String EXERCISE_TABLE = "Exercise_Table";
    private static final String WORKOUT_TABLE = "Workout_Table";
    private static final String SET_TABLE = "Set_Table";

    private static final String GROUP_TABLE = "Group_Table";
    private static final String GROUP_EXERCISE_TABLE = "Group_Exercises_Table";

    private static final String[] TABLES_IN_DB = {EXERCISE_TABLE, WORKOUT_TABLE, SET_TABLE, GROUP_TABLE, GROUP_EXERCISE_TABLE};

    private static final String[][] COLUMNS_ARGS = {    { KEY_COLUMN_ARG, "exercise_name TEXT NOT NULL", "icon_path TEXT", "unit TEXT", "increment TEXT", "warmup_weight TEXT", "working_weight TEXT", "archived INTEGER NOT NULL DEFAULT 0" },
                                                        { KEY_COLUMN_ARG, "exercise_id INTEGER NOT NULL", "date TEXT NOT NULL", "note TEXT" },
                                                        { KEY_COLUMN_ARG, "exercise_id INTEGER NOT NULL", "workout_id INTEGER NOT NULL", "position INTEGER NOT NULL", "number INTEGER NOT NULL", "weight TEXT NOT NULL", "reps INTEGER NOT NULL", "note TEXT", "is_warmup INTEGER NOT NULL"},

                                                        { KEY_COLUMN_ARG, "group_name TEXT NOT NULL"  },
                                                        { KEY_COLUMN_ARG, "group_id INTEGER NOT NULL", "exercise_id INTEGER NOT NULL"}
                                                    };



    public DBHandler(Context context){
        DB_CONTEXT = context;
        DBHelper = new DatabaseHelper(context, DB_NAME, DB_VERSION);
    }

    public String getDBName(){
        return DB_NAME;
    }

    public String getPath(){
        return DB.getPath();
    }

    public void open(){
        close();
        DB = DBHelper.getWritableDatabase();

    }
    public void close(){
        if (DB != null && DB.isOpen()){
            DB.close();
        }
    }

    public long insertRow(String table, ContentValues cv){
        return DB.insert(table, null, cv);
    }

    public void deleteRow(String table, long ID){
        DB.delete(table, (KEY_ID + "=" + ID), null);
    }

    public void deleteAllRows(String table){
        DB.delete(table, null, null);
    }

    public void deleteAllRows(String table, String where){
        DB.delete(table, where, null);
    }

    public Cursor getRow(String table, long id, String[] columns){
        return getAllRows(table, columns, KEY_ID + " = " + id);
    }

    public Cursor getAllRows(String table, String[] columns){
        return DB.query(false, table, columns, null, null, null, null, null, null);
    }

    public Cursor getAllRows(String table, String[] columns, String where){
        return DB.query(false, table, columns, where, null, null, null, null, null);
    }

    public void editRow(String table, ContentValues cv, long ID){
        DB.update(table, cv, (KEY_ID + "=" + ID), null);
    }

    public void execSQL(String sql){
        DB.execSQL(sql);
    }

    public Cursor rawQuery(String sql, String[] args){
        return DB.rawQuery(sql, args);
    }

    public Cursor rawQuery(String sql){
        return DB.rawQuery(sql, new String[] {});
    }




    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context, String DB_NAME, int version) {
            super(context, DB_NAME, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {

            // Creates Tables
            for (int t = 0; t < TABLES_IN_DB.length; t++) {
                String command = "CREATE TABLE " + TABLES_IN_DB[t] + "( ";

                for (int c = 0; c < COLUMNS_ARGS[t].length; c++) {
                    command = command + COLUMNS_ARGS[t][c] + ", ";
                }

                command = command.substring(0, command.length() - 2) + ");";

                _db.execSQL(command);
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {

            if (oldVersion <= 1){

                // Create extra 2 tables
                for (int t = 3; t <= 4; t++) {
                    String command = "CREATE TABLE " + TABLES_IN_DB[t] + "( ";

                    for (int c = 0; c < COLUMNS_ARGS[t].length; c++) {
                        command = command + COLUMNS_ARGS[t][c] + ", ";
                    }

                    command = command.substring(0, command.length() - 2) + ");";

                    _db.execSQL(command);
                }

                // Add extra column to exercise table
                String c = "ALTER TABLE " + EXERCISE_TABLE + " ADD COLUMN archived INTEGER NOT NULL DEFAULT 0";
                _db.execSQL(c);



                Log.i("DB Manager", "Database updated from version 1 to 2");

            }


        }
    }

}

