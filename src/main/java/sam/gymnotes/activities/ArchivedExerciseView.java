package sam.gymnotes.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sam.gymnotes.CustomCalendar;
import sam.gymnotes.DBHandler;
import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.dialogs.are_you_sure;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Sam on 03/06/2017.
 */

public class ArchivedExerciseView extends AppCompatActivity {

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

    private Long toBeDeleted;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getActivityTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_exercise_view);

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


        toolbar.setTitle("Archive");
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Archive");





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
    }

    @Override
    protected void onResume() {
        super.onResume();
        new refreshList(this).execute();
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
            String sql = "SELECT e._id, e.exercise_name, e.icon_path FROM " + TABLE_EXERCISE + " AS e WHERE e.archived = 1;";
            Cursor c = DB.rawQuery(sql);
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                exerciseList.add(new Exercise(getApplicationContext(), TABLE_WORKOUT, c.getLong(0), c.getString(1), c.getString(2), null, null, null, null));
            }
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

        //------------------------------------------------------------------------------
        // Remember the current scroll position of the list
        //------------------------------------------------------------------------------
        Parcelable listState = list.onSaveInstanceState();


        // ------------------------------------------------------------------------
        // Set the adapter, and both click listeners for the list
        // ------------------------------------------------------------------------
        list.setAdapter(new ArchivedExerciseListAdapter(ArchivedExerciseView.this, exerciseList, "name"));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                return true;
            }
        });




        //------------------------------------------------------------------------------
        // If the list is empty, then make the welcome message visible
        //------------------------------------------------------------------------------
        if (exerciseList.size() <= 0) {
            message.setText("Archive is empty");
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


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 2 && resultCode == RESULT_OK){
            deleteExercise(toBeDeleted);
        }

    }


    public void gotoAreYouSure(int index){
        toBeDeleted = null;

        String exerciseName = exerciseList.get(index).getName();
        toBeDeleted = exerciseList.get(index).getID();


        Intent i = new Intent(getApplicationContext(), are_you_sure.class);
        i.putExtra("header", "Delete '" + exerciseName + "'?");
        i.putExtra("message", "Are you sure you want to permanently delete this Exercise along with all the Workouts & Sets inside it?");
        i.putExtra("NO", "Cancel");
        i.putExtra("YES", "Delete");
        startActivityForResult(i, 2);
    }

    public void deleteExercise(long exerciseID){
        DB.open();
        DB.deleteAllRows(TABLE_SET, "exercise_id = " + exerciseID);
        DB.deleteAllRows(TABLE_WORKOUT, "exercise_id = " + exerciseID);
        DB.deleteAllRows(TABLE_GROUP_EXERCISES, "exercise_id = " + exerciseID);
        DB.deleteAllRows(TABLE_EXERCISE, "_id = " + exerciseID);

        DB.close();

        new refreshList(this).execute();
    }

    public void restoreExercise(int index){
        long exerciseID = exerciseList.get(index).getID();
        String exerciseName = exerciseList.get(index).getName();

        ContentValues cv =  new ContentValues();
        cv.put("archived", 0);

        DB.open();

        DB.editRow(TABLE_EXERCISE, cv, exerciseID);

        DB.close();

        Toast.makeText(this, "'" + exerciseName + "' successfully restored!", Toast.LENGTH_SHORT).show();

        new refreshList(this).execute();
    }





    public class ArchivedExerciseListAdapter extends ArrayAdapter implements StickyListHeadersAdapter {

        private String ICON_DIRECTORY;

        private String sortPref;

        public ArchivedExerciseListAdapter(Context context, List<Exercise> exercises, String sort) {
            super(context, Integer.valueOf(9999), exercises);
            ICON_DIRECTORY = this.getContext().getString(R.string.ICON_DIRECTORY);
            sortPref = sort;
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(this.getContext());
            View row = inflater.inflate(R.layout.list_archived_exercise_row, parent, false);


            Exercise exercise = (Exercise) getItem(position);


            // ------------------------------------------------------------------------
            // Set exercise name
            // ------------------------------------------------------------------------
            TextView exerciseName = (TextView) row.findViewById(R.id.exerciseName);
            exerciseName.setText(exercise.getName());


            // ------------------------------------------------------------------------
            // Set the last workout field
            // ------------------------------------------------------------------------
            TextView LastWorkout = (TextView) row.findViewById(R.id.LastWorkout);
            CustomCalendar last = exercise.getLastWorkout();
            if (last == null){
                LastWorkout.setText(this.getContext().getString(R.string.Never));
            }
            else{
                String str = last.daysAgoString() + ". " + last.toLongString(1,1,1,2);
                LastWorkout.setText(str.substring(0, 1).toUpperCase() + str.substring(1));
            }


            // ------------------------------------------------------------------------
            // If iconPath not null, then set the icon from resources, otherwise
            // make it transparent.
            // ------------------------------------------------------------------------
            ImageView icon = (ImageView) row.findViewById(R.id.icon);
            String iconPath = exercise.getIconPath();
            if (iconPath != null) {
                try {
                    icon.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + iconPath), null));
                } catch (IOException e) {e.printStackTrace();}
            }
            else{
                icon.setImageResource(android.R.color.transparent);
            }


            // ------------------------------------------------------------------------
            // Set Buttons
            // ------------------------------------------------------------------------
            Button restoreButton = (Button) row.findViewById(R.id.restore_button);
            restoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restoreExercise(position);
                }
            });

            Button deleteButton = (Button) row.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoAreYouSure(position);
                }
            });


            return row;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(this.getContext());
            View v = inflater.inflate(R.layout.list_exercise_header, parent, false);
            TextView text = (TextView) v.findViewById(R.id.text);

            long headerID = getHeaderId(position);

            if (headerID == 3){
                text.setText(R.string.AtoZ);
            }
            else if (headerID == 2){
                text.setText(R.string.New);
            }
            else if (headerID == 1){
                text.setText(R.string.Today);
            }
            else if (headerID == 0){
                text.setText(R.string.upNext);
            }


            return v;
        }

        @Override
        public long getHeaderId(int position) {

            CustomCalendar lastWorkout = ((Exercise) getItem(position)).getLastWorkout();

            if (!sortPref.equals(this.getContext().getString(R.string.date))){
                return 3;
            }
            else if (lastWorkout == null){
                return 2;
            }
            else if (lastWorkout.isToday()){
                return 1;
            }
            else if (lastWorkout.daysAgo() > 0){
                return 0;
            }
            return 0;
        }
    }



}
