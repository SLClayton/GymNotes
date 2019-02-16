package sam.gymnotes.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;


public class ThreeOption extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getDialogTheme());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_3option);
        this.setFinishOnTouchOutside(false);

        TextView header = (TextView) findViewById(R.id.Header);
        TextView message = (TextView) findViewById(R.id.Message);
        TextView MAYBE = (TextView) findViewById(R.id.MAYBE);
        TextView NO = (TextView) findViewById(R.id.NO);
        TextView YES = (TextView) findViewById(R.id.YES);

        Intent i = getIntent();
        header.setText(i.getStringExtra("header"));
        message.setText(i.getStringExtra("message"));

        NO.setText(i.getStringExtra("NO"));
        MAYBE.setText(i.getStringExtra("MAYBE"));
        YES.setText(i.getStringExtra("YES"));
    }


    public void NO(View view){
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }

    public void MAYBE(View view){
        Intent i = new Intent();
        setResult(RESULT_FIRST_USER, i);
        finish();
    }

    public void YES(View view){
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }

}
