package sam.gymnotes.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.array_adapters.ThemeListAdapter;

/**
 * Created by Sam on 16/09/2016.
 */
public class ThemeChooser extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView list;
    private String currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHandler t = new ThemeHandler(this);
        currentTheme = t.getThemeName();
        setTheme(t.getActivityTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_chooser);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (ListView) findViewById(R.id.list);

        setupToolbar();

        setupList();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar a = getSupportActionBar();
        a.setTitle(R.string.ThemeChooser);
        a.setDisplayHomeAsUpEnabled(true);

    }

    private void setupList() {
        ArrayList<String> themes = new ArrayList<String>(Arrays.asList(ThemeHandler.themes));

        list.setAdapter(new ThemeListAdapter(this, themes));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String chosenTheme = (String) list.getItemAtPosition(i);
                if (!chosenTheme.equals(currentTheme)){
                    changeTheme(chosenTheme);
                }
            }
        });
    }

    private void changeTheme(String themeName){
        new ThemeHandler(this).setTheme(themeName);

        recreate();
    }
}
