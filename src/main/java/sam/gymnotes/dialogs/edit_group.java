package sam.gymnotes.dialogs;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sam.gymnotes.DBHandler;
import sam.gymnotes.Exercise;
import sam.gymnotes.ExerciseGroup;
import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.activities.ExerciseView;


public class edit_group extends Activity {

    public static final String logtag = "edit_group";

    private String TABLE_GROUP_EXERCISES;
    private String TABLE_GROUP;
    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private String ICON_DIRECTORY;
    private DBHandler DB;

    private ExerciseGroup group;
    private List<Exercise> exercises;
    private ArrayList<CheckBox> checkBoxes;
    private Long new_checked;

    private Boolean editMode;

    private TextView header;
    private EditText insertName;
    private LinearLayout exercise_list;
    private TextView finishButton;
    private TextView errorMessage;


    private static final int maxChars = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getDialogTheme());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_new_group);
        this.setFinishOnTouchOutside(false);

        TABLE_GROUP_EXERCISES = getString(R.string.TABLE_GROUP_EXERCISES);
        TABLE_GROUP = getString(R.string.TABLE_GROUP);
        TABLE_EXERCISE = getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);
        ICON_DIRECTORY = getString(R.string.ICON_DIRECTORY);

        DB = new DBHandler(getApplicationContext());

        Intent i = getIntent();
        editMode = i.getBooleanExtra("EditMode", true);
        group = new ExerciseGroup(i.getLongExtra("Group_ID", 9999999));



        header = (TextView) findViewById(R.id.Header);
        insertName = (EditText) findViewById(R.id.insertName);
        exercise_list = (LinearLayout) findViewById(R.id.choose_exercise_list);
        finishButton = (TextView) findViewById(R.id.FinishButton);
        errorMessage = (TextView) findViewById(R.id.ErrorMessage);

        errorMessage.setVisibility(View.GONE);


        // ------------------------------------------------------------------------
        // Get colors from current theme
        // ------------------------------------------------------------------------
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int colorAccent = typedValue.data;

        insertName.getBackground().mutate().setColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP);




    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
        new refreshList(this).execute();
    }

    public class refreshList extends AsyncTask {
        Context context;
        List<Long> in_group;



        public refreshList(Context c){
            context = c;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            in_group = new ArrayList<Long>();

            // Save all the currently checked boxes
            if(checkBoxes != null && checkBoxes.size() > 0){
                for (int i=0; i<exercises.size(); i++){
                    if (checkBoxes.get(i).isChecked()){
                        in_group.add(exercises.get(i).getID());
                    }
                }
            }

            // If there's a new one to add, add it to the list.
            if (new_checked != null){
                in_group.add(new_checked);
            }

            if (editMode){
                header.setText("Edit Group");
                finishButton.setText("Done");
            }
            else{
                header.setText("New Group");
                finishButton.setText("Create");
                ImageView I = (ImageView) findViewById(R.id.delete_button);
                I.setVisibility(View.GONE);
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            DB.open();

            // Get all exercises to choose from
            exercises = new ArrayList<Exercise>();
            Cursor c = DB.getAllRows(TABLE_EXERCISE, new String[] {"_id", "exercise_name"}, " archived != 1 ");
            c.moveToPosition(-1);
            while (c.moveToNext()){
                exercises.add(new Exercise(c.getLong(0), c.getString(1)));
            }
            exercises = ExerciseView.sortByName(exercises);


            // For edit mode
            if (editMode){

                // Get Group info
                c = DB.getAllRows(TABLE_GROUP, new String[] {"_id", "group_name"}, "_id = " + group.getID());
                c.moveToPosition(0);
                group = new ExerciseGroup(c.getLong(0), c.getString(1));

                // Find which exercises are currently chosen
                c = DB.getAllRows(TABLE_GROUP_EXERCISES, new String[] {"_id", "exercise_id"}, "group_id = " + group.getID());
                c.moveToPosition(-1);
                while (c.moveToNext()){
                    in_group.add(c.getLong(1));
                }


            }

            DB.close();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (editMode){

                // Set Exercise Name
                insertName.setText(group.getName());
                insertName.setSelection(insertName.getText().toString().length() - 1);

            }

            LayoutInflater inflater = LayoutInflater.from(context);
            checkBoxes = new ArrayList<CheckBox>();
            exercise_list.removeAllViews();
            for (int a=0; a<exercises.size(); a++){
                Exercise ex = exercises.get(a);

                View row = inflater.inflate(R.layout.edit_group_exercise_list_item, exercise_list, false);
                CheckBox cb = (CheckBox) row.findViewById(R.id.checkBox);
                checkBoxes.add(cb);
                cb.setText(ex.getName());
                cb.setChecked(in_group.contains(ex.getID()));

                exercise_list.addView(row);
            }

        }
    }

    public void gotoAreYouSure(View view){
        Intent i = new Intent(getApplicationContext(), are_you_sure.class);
        i.putExtra("header", "Delete " + group.getName() + "?");
        i.putExtra("message", "Are you sure you want to delete this Group? The Exercises within will not be affected.");
        i.putExtra("NO", "Cancel");
        i.putExtra("YES", "Delete");
        startActivityForResult(i, 2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK){
            delete();
        }

        else if(requestCode == 3){
            if (resultCode == RESULT_OK){
                editDB();
            }
            else if (resultCode == RESULT_FIRST_USER){
                editDB();
            }
        }

        else if (requestCode == 4 && resultCode ==RESULT_OK){
            new_checked = data.getLongExtra("Exercise_ID", 123456);
        }
    }


    public boolean checkFields(){
        // Clears and hides error message
        errorMessage.setVisibility(View.GONE);
        errorMessage.setText("");

        String groupName = insertName.getText().toString();
        ArrayList<String> mes = new ArrayList<String>();
        boolean okay = true;

        // Checks exercise name for correct length
        if (groupName.trim().length() < 1){
            mes.add("Please insert a group name.");
            okay = false;
        }
        else if (groupName.length() > maxChars){
            mes.add("Group name cannot be more than " + maxChars + " characters.");
            okay = false;
        }

        // Sends error message and makes it visible
        if (!okay){
            String m = "";
            for (int i = 0; i < mes.size(); i++){
                if (i != 0){
                    m += "\n";
                }
                m += mes.get(i);
            }
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText(m);
        }

        return okay;
    }

    public void delete(){
        DB.open();
        DB.deleteAllRows(TABLE_GROUP, "_id = " + group.getID());
        DB.deleteAllRows(TABLE_GROUP_EXERCISES, " group_id = " + group.getID());
        DB.close();

        finish();
    }

    public void cancel(View view){
        finish();
    }

    public void done(View view){
        if (checkFields()){
            editDB();
        }
    }

    public void editDB(){
        String groupName = insertName.getText().toString();

        ContentValues cv = new ContentValues();
        cv.put("group_name", groupName);

        DB.open();
        long groupID;
        if (editMode){
            groupID = group.getID();
            DB.editRow(TABLE_GROUP, cv, group.getID());
        }
        else{
            groupID = DB.insertRow(TABLE_GROUP, cv);
        }

        // Delete all exercise/group links in this group in DB
        DB.deleteAllRows(TABLE_GROUP_EXERCISES, "group_id = " + groupID);

        // Collect values which are checked
        String values = "";
        for (int i=0; i<checkBoxes.size(); i++){
            if (checkBoxes.get(i).isChecked()){
                values += " (" + groupID + ", " + exercises.get(i).getID() + "),";
            }
        }

        // Add exercise/group links in DB
        if (!values.equals("")){
            values = values.substring(0, values.length()-1);
            String sql = "INSERT INTO " + TABLE_GROUP_EXERCISES + " (group_id, exercise_id) VALUES " + values + ";";
            DB.execSQL(sql);
        }

        DB.close();
        finish();
    }

    public void openNewExerciseDialog(View view){
        new_checked = null;
        Intent i = new Intent(this, edit_exercise.class);
        i.putExtra("EditMode", false);
        //i.putExtra("Group_ID", group.getID());
        startActivityForResult(i, 4);
    }



}
