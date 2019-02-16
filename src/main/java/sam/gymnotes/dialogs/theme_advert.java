package sam.gymnotes.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;


public class theme_advert extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getDialogTheme());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_theme_advert);
        this.setFinishOnTouchOutside(false);
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
