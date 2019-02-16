package sam.gymnotes.dialogs;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import sam.gymnotes.CustomCalendar;
import sam.gymnotes.DBHandler;
import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;


public class edit_note extends Activity {

    private String TABLE_WORKOUT;
    private DBHandler DB;

    private long workout_id;

    private TextView header;
    private EditText note;
    private TextView error;

    private static final int maxCharacters = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getDialogTheme());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_note);
        this.setFinishOnTouchOutside(false);

        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);


        DB = new DBHandler(getApplicationContext());

        Intent i = getIntent();
        workout_id = i.getLongExtra("Workout_ID", 99999);

        header = (TextView) findViewById(R.id.Header);
        note = (EditText) findViewById(R.id.note);
        error = (TextView) findViewById(R.id.Error);
        error.setVisibility(View.GONE);

        fillViews();
    }

    public void fillViews() {
        DB.open();
        Cursor c = DB.getRow(TABLE_WORKOUT, workout_id, new String[] {"_id", "date", "note"});
        c.moveToFirst();
        CustomCalendar d = new CustomCalendar(c.getString(1));
        String n = c.getString(2);
        c.close();
        DB.close();


        header.setText("Note: " + d.toLongString(0,1,1,2));
        if (n != null){
            note.setText(n);
        }
        note.setSelection(note.length());
    }

    public Boolean check(){
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

    public void cancel(View view){
        finish();
    }

    public void clear(View view){
        note.setText("");
    }

    public void done(View view){
        if (check()){
            String n = note.getText().toString().trim();
            if (n.trim().length() == 0){
                n = null;
            }

            DB.open();

            ContentValues cv = new ContentValues();
            cv.put("note", n);
            DB.editRow(TABLE_WORKOUT, cv, workout_id);

            DB.close();
            finish();
        }
    }
}
