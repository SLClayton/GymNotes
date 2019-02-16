package sam.gymnotes.activities;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sam.gymnotes.DBHandler;
import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.array_adapters.ExerciseListAdapter;
import sam.gymnotes.dialogs.edit_exercise;
import sam.gymnotes.dialogs.edit_group;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Sam on 03/06/2017.
 */

public class GroupExerciseView extends AppCompatActivity {

    private String TABLE_GROUP_EXERCISES;
    private String TABLE_GROUP;
    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;

    private String prefs;
    private SharedPreferences sharedpreferences;

    private DBHandler DB;

    private ActionBar actionBar;
    private Toolbar toolbar;
    private TextView message;
    private StickyListHeadersListView list;

    private long groupID;
    private String groupName;
    private List<Exercise> exerciseList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getActivityTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_exercise_view);

        TABLE_GROUP_EXERCISES = getString(R.string.TABLE_GROUP_EXERCISES);
        TABLE_GROUP = getString(R.string.TABLE_GROUP);
        TABLE_EXERCISE = getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);
        prefs = getString(R.string.shared_preferences);
        DB = new DBHandler(getApplicationContext());

        toolbar =  (Toolbar) findViewById(R.id.toolbar);
        message =(TextView) findViewById(R.id.message);
        list = (StickyListHeadersListView) findViewById(R.id.List);

        Intent i = getIntent();
        groupID = i.getLongExtra("Group_ID", 99999);

        sharedpreferences = getSharedPreferences(prefs, MODE_PRIVATE);


        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditGroupDialog(v);
            }
        });




        String message = "New in this release";





    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){

            case android.R.id.home:
                onBackPressed();
                break;

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        requestBackup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new refreshList(this).execute();
    }

    public void requestBackup(){
        if (sharedpreferences.getBoolean("backup", false)){
            BackupManager bm = new BackupManager(this);
            bm.dataChanged();
        }
    }

    public class refreshList extends AsyncTask {
        Context context;

        public refreshList(Context c){
            context = c;
        }



        @Override
        protected Object doInBackground(Object[] objects) {


            exerciseList = new ArrayList<Exercise>();

            DB.open();

            // ------------------------------------------------------------------------
            // Fill exercise list with all exercises from exercise table
            // ------------------------------------------------------------------------
            String sql = "SELECT e._id, e.exercise_name, e.icon_path FROM " + TABLE_GROUP_EXERCISES + " AS ge INNER JOIN " + TABLE_EXERCISE + " AS e ON ge.exercise_id = e._id WHERE ge.group_id = " + groupID + " AND e.archived = 0;";
            Cursor c = DB.rawQuery(sql);
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                exerciseList.add(new Exercise(getApplicationContext(), TABLE_WORKOUT, c.getLong(0), c.getString(1), c.getString(2), null, null, null, null));
            }
            c.close();

            c = DB.getRow(TABLE_GROUP, groupID, new String[]{"_id, group_name"});
            c.moveToPosition(0);
            groupName = c.getString(1);

            c.close();

            DB.close();


            exerciseList = ExerciseView.sortByName(exerciseList);

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refreshListView();
        }
    }

    public void refreshListView(){


        actionBar.setTitle(groupName);

        //------------------------------------------------------------------------------
        // Remember the current scroll position of the list
        //------------------------------------------------------------------------------
        Parcelable listState = list.onSaveInstanceState();


        // ------------------------------------------------------------------------
        // Set the adapter, and both click listeners for the list
        // ------------------------------------------------------------------------
        list.setAdapter(new ExerciseListAdapter(GroupExerciseView.this, exerciseList, "name"));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openExercise(position);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                openEditExerciseDialog(i);
                return true;
            }
        });




        //------------------------------------------------------------------------------
        // If the list is empty, then make the welcome message visible
        //------------------------------------------------------------------------------
        if (exerciseList.size() <= 0) {
            message.setText("Group is empty");
            message.setVisibility(View.VISIBLE);
        }
        else {
            message.setVisibility(View.GONE);
        }


        //------------------------------------------------------------------------------
        // Restore position of list
        //------------------------------------------------------------------------------
        if (listState != null) {
            list.onRestoreInstanceState(listState);
        }

    }


    private void openExercise(int position) {
        Intent i = new Intent(this, WorkoutView.class);
        i.putExtra("Exercise_ID", exerciseList.get(position).getID());
        startActivity(i);
    }


    public void openEditExerciseDialog(int position){
        Intent i = new Intent(this, edit_exercise.class);
        i.putExtra("EditMode", true);
        i.putExtra("Exercise_ID", exerciseList.get(position).getID());
        startActivity(i);
    }

    public void openEditGroupDialog(View view){
        Intent i = new Intent(this, edit_group.class);
        i.putExtra("EditMode", true);
        i.putExtra("Group_ID", groupID);
        startActivity(i);
    }


}
