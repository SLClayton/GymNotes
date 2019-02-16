package sam.gymnotes.array_adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;

/**
 * Created by Sam on 22/07/2016.
 */
public class ThemeListAdapter extends ArrayAdapter {

    String currentTheme;

    public ThemeListAdapter(Context c, ArrayList<String> themes) {
        super(c, new Integer(999), themes);

        currentTheme = new ThemeHandler(getContext()).getThemeName();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        View row = inflater.inflate(R.layout.list_theme_row, parent, false);

        String themeName = (String) getItem(position);

        // ------------------------------------------------------------------------
        // Get colors from current theme
        // ------------------------------------------------------------------------
        Resources r = getContext().getResources();
        int colorPrimary = r.getColor(r.getIdentifier(themeName + "_colorPrimary", "color", getContext().getPackageName()));
        int colorAccent = r.getColor(r.getIdentifier(themeName + "_colorAccent", "color", getContext().getPackageName()));

        // ------------------------------------------------------------------------
        // Format list row
        // ------------------------------------------------------------------------
        TextView name = (TextView) row.findViewById(R.id.name);
        String label = themeName.substring(0, 1).toUpperCase() + themeName.substring(1);
        if (themeName.equals(ThemeHandler.defaultTheme)){
            label += " (" + getContext().getString(R.string._default) + ")";
        }
        name.setText(label);

        FrameLayout primaryColor = (FrameLayout) row.findViewById(R.id.primaryColor);
        primaryColor.setBackgroundColor(colorPrimary);

        FrameLayout accentColor = (FrameLayout) row.findViewById(R.id.accentColor);
        accentColor.setBackgroundColor(colorAccent);

        ImageView selected = (ImageView) row.findViewById(R.id.selected);
        if (themeName.equals(currentTheme)){
            selected.setVisibility(View.VISIBLE);
        }
        else{
            selected.setVisibility(View.GONE);
        }



        return row;
    }

}