package sam.gymnotes.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.IOException;

import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;


public class IconChooser extends AppCompatActivity {

    private String ICON_DIRECTORY;

    private String[] iconDirectories;
    private String[][] iconPaths;

    private Toolbar toolbar;
    private Spinner directorySpinner;
    private GridView grid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getActivityTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_chooser);

        ICON_DIRECTORY = getString(R.string.ICON_DIRECTORY) + "/black";

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        directorySpinner = (Spinner) findViewById(R.id.spinner);
        grid = (GridView) findViewById(R.id.gridView);

        setupToolbar();

        listDirectories();
        refreshGrid();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        // ends activity, with no iconPath in the intent.
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }





    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar a = getSupportActionBar();
        a.setTitle(R.string.IconChooser);
        a.setDisplayHomeAsUpEnabled(true);

    }






    public void listDirectories() {

        // Fills iconDirectories and iconPaths lists.
        try {
            iconDirectories = getAssets().list(ICON_DIRECTORY);
            int NumDirectories = iconDirectories.length;
            iconPaths = new String[NumDirectories][];

            for (int i = 0; i < NumDirectories; i++){
                String[] filenames = getAssets().list(ICON_DIRECTORY + "/" + iconDirectories[i]);
                int NumIcons = filenames.length;
                iconPaths[i] = new String[NumIcons];

                for (int j = 0; j < NumIcons; j++){
                    iconPaths[i][j] = iconDirectories[i] + "/" + filenames[j];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




        // Uses Directory list to fill drop down spinner.
        ArrayAdapter AA = new ArrayAdapter(this, R.layout.spinner_layout_icontype, iconDirectories);
        AA.setDropDownViewResource(R.layout.spinner_dropdown_layout_icontype);
        directorySpinner.setAdapter(AA);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshGrid();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


    }

    public void refreshGrid(){
        // Resets the grid adapter, creating a new one.
        grid.setAdapter(new CustomAdapter(this));
    }

    public void sendBack(int Directory, int pathIndex){
        // ends activity, putting iconPath into the send back intent.
        Intent i = new Intent();
        i.putExtra("IconPath", iconPaths[Directory][pathIndex]);
        setResult(RESULT_OK, i);
        finish();
    }




    // Custom Grid Adapter ------------------------------------------------------------------
    public class CustomAdapter extends BaseAdapter {

        Context context;

        private LayoutInflater inflater = null;

        public CustomAdapter(Context mainActivity) {
            context = mainActivity;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return iconPaths[directorySpinner.getSelectedItemPosition()].length;
        }
        @Override
        public Object getItem(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {;
            View rowView;

            rowView = inflater.inflate(R.layout.gridview_layout, null);
            ImageView im = (ImageView) rowView.findViewById(R.id.imageView);


            try {
                im.setImageDrawable(Drawable.createFromStream(getAssets().open(ICON_DIRECTORY + "/" + iconPaths[directorySpinner.getSelectedItemPosition()][position]), null));
            } catch (IOException e) {
                e.printStackTrace();
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendBack(directorySpinner.getSelectedItemPosition(), position);
                }
            });

            return rowView;
        }
    }


    }
