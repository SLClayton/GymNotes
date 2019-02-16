package sam.gymnotes.dialogs;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Calendar;

import sam.gymnotes.CustomCalendar;
import sam.gymnotes.DBHandler;
import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.Workout;


public class edit_workout extends Activity {

    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private DBHandler DB;

    private long workout_ID;

    private DatePicker picker;
    private EditText note;
    private TextView error;

    private static final int maxCharacters = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getDialogTheme());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_workout);
        this.setFinishOnTouchOutside(false);

        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);

        DB = new DBHandler(getApplicationContext());

        picker = (DatePicker) findViewById(R.id.datePicker);
        note = (EditText) findViewById(R.id.note);
        error = (TextView) findViewById(R.id.error);
        error.setVisibility(View.GONE);

        // ------------------------------------------------------------------------
        // Get color from current theme
        // ------------------------------------------------------------------------
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int colorAccent = typedValue.data;

        setDatePickerDividerColor(picker, colorAccent);


        Intent i = getIntent();
        workout_ID = i.getLongExtra("Workout_ID", 9999);

        fillViews();
        note.requestFocus();
        note.setSelection(note.getText().length());
    }

    public void fillViews() {
        DB.open();

        Cursor c = DB.getRow(TABLE_WORKOUT, workout_ID, new String[] {"_id", "date", "note"});
        c.moveToFirst();
        Workout startWorkout = new Workout(c.getLong(0), new CustomCalendar(c.getString(1)), c.getString(2));
        c.close();

        DB.close();


        picker.updateDate(startWorkout.getDate().get(Calendar.YEAR), startWorkout.getDate().get(Calendar.MONTH), startWorkout.getDate().get(Calendar.DAY_OF_MONTH));

        if (startWorkout.getNote() != null){
            note.setText(startWorkout.getNote());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1 && resultCode == RESULT_OK){
            DB.open();
            DB.deleteAllRows(TABLE_SET, "workout_id = " + workout_ID);
            DB.deleteRow(TABLE_WORKOUT, workout_ID);
            DB.close();
            finish();
        }
    }

    public void delete(View view){
        Intent i = new Intent(this, are_you_sure.class);
        i.putExtra("header", "Delete Workout");
        i.putExtra("message", "Are you sure you would like to delete this Workout along with all Sets inside it?");
        i.putExtra("YES", "Delete");
        i.putExtra("NO", "Cancel");
        startActivityForResult(i, 1);
    }

    public Boolean checkValues(){
        error.setVisibility(View.GONE);
        if (note.getText().toString().length() <= maxCharacters){
            return true;
        }
        else{
            error.setText("Note must not exceed " + maxCharacters + " characters.");
            error.setVisibility(View.VISIBLE);
            return false;
        }
    }

    public void YES(View view){
        CustomCalendar newDate = new CustomCalendar(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());

        String newNote = note.getText().toString().trim();
        if (newNote.length() == 0){
            newNote = null;
        }

        ContentValues cv = new ContentValues();
        cv.put("date", newDate.toShortString());
        cv.put("note", newNote);

        DB.open();
        DB.editRow(TABLE_WORKOUT, cv, workout_ID);
        DB.close();
        finish();
    }

    public void NO(View view){
        finish();
    }


    public static void setDatePickerDividerColor(DatePicker datePicker, int color) {
        Resources system = Resources.getSystem();
        int dayId = system.getIdentifier("day", "id", "android");
        int monthId = system.getIdentifier("month", "id", "android");
        int yearId = system.getIdentifier("year", "id", "android");

        NumberPicker dayPicker = (NumberPicker) datePicker.findViewById(dayId);
        NumberPicker monthPicker = (NumberPicker) datePicker.findViewById(monthId);
        NumberPicker yearPicker = (NumberPicker) datePicker.findViewById(yearId);

        setDividerColor(dayPicker, color);
        setDividerColor(monthPicker, color);
        setDividerColor(yearPicker, color);
    }

    private static void setDividerColor(NumberPicker picker, int color) {
        if (picker == null)
            return;

        final int count = picker.getChildCount();
        for (int i = 0; i < count; i++) {
            try {
                Field dividerField = picker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(color);
                dividerField.set(picker, colorDrawable);
                picker.invalidate();
            } catch (Exception e) {
                Log.w("setDividerColor", e);
            }
        }
    }

}
