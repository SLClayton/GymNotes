package sam.gymnotes.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;


public class are_you_sure extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getDialogTheme());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_are_you_sure);
        this.setFinishOnTouchOutside(false);

        TextView header = (TextView) findViewById(R.id.Header);
        TextView message = (TextView) findViewById(R.id.Message);
        TextView YES = (TextView) findViewById(R.id.YES);
        TextView NO = (TextView) findViewById(R.id.NO);

        Intent i = getIntent();
        header.setText(i.getStringExtra("header"));
        message.setText(i.getStringExtra("message"));
        YES.setText(i.getStringExtra("YES"));
        NO.setText(i.getStringExtra("NO"));
    }

    public void YES(View view){
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }

    public void NO(View view){
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }

}
