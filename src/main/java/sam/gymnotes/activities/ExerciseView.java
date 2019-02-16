package sam.gymnotes.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sam.gymnotes.CustomCalendar;
import sam.gymnotes.DBHandler;
import sam.gymnotes.Exercise;
import sam.gymnotes.ExerciseGroup;
import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.array_adapters.ExerciseListAdapter;
import sam.gymnotes.array_adapters.GroupListAdapter;
import sam.gymnotes.dialogs.are_you_sure;
import sam.gymnotes.dialogs.edit_exercise;
import sam.gymnotes.dialogs.edit_group;
import sam.gymnotes.dialogs.theme_advert;
import sam.gymnotes.exampleGenerator;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static sam.gymnotes.R.string.Exercise_View;


public class ExerciseView extends AppCompatActivity {

    private static final String log = "ExerciseView";

    private String TABLE_GROUP_EXERCISES;
    private String TABLE_GROUP;
    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private DBHandler DB;

    Context context;

    public String prefs;
    public static final String sortPref = "ExerciseSort";
    public static final String listType = "ListType";
    public static final String logtag = "ExerciseView";

    SharedPreferences sharedpreferences;

    private Toolbar toolbar;
    private Menu menu;
    private List<Exercise> exerciseList;
    private List<ExerciseGroup> groupList;
    private TextView message;
    private StickyListHeadersListView list;
    private TextView sortButton;



    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);

            new updatePro().execute();
        }
    };

    private void setupBilling() {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

    }


    public void buyPro() {
        if (hasPro()){
            Toast.makeText(this, "Pro unlocked", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), getString(R.string.sku_pro), "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            startIntentSenderForResult(pendingIntent.getIntentSender(), 2, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));


        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void openPurchaseIntent(Intent data) {
        int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
        String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
        String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

        String toastMessage = "";

        switch (responseCode){
            case 0:
                toastMessage = "Purchase successful!";
                break;

            case 1:
                toastMessage  = "Purchase Cancelled";
                break;

            default:
                toastMessage = "Cannot purchase pro right now";
                break;
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

        new updatePro().execute();
    }

    public Boolean hasPro(){
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            ArrayList<String> purchases = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            if (purchases != null && purchases.contains(getString(R.string.sku_pro))){
                Log.d("billing", "User has pro");
                return true;
            }
        } catch (RemoteException e) {
            Log.d("billing", "Could not find if user has pro or not");
            e.printStackTrace();
            return null;
        }
        Log.d("billing", "User does not have pro");
        return false;
    }

    public String getPurchaseToken(String sku){
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    JSONObject purchaseData = new JSONObject(purchaseDataList.get(i));
                    if (purchaseData.getString("productId").equals(sku)){
                        return purchaseData.getString("purchaseToken");
                    }
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void checkPurchaseInfo() {
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);

                    Log.d("Owned info", "Data: " + purchaseData);
                    Log.d("Owned info", "signature: " + signature);
                    Log.d("Owned info", "sku: " + sku);

                }
                Log.d("Owned info", "Purchases owned: " + purchaseDataList.size());
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public class removePurchase extends AsyncTask {
        private Context context;
        private boolean hadPro;

        public removePurchase(Context c){
            context = c;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            hadPro = hasPro();
            if (hadPro){
                try {
                    mService.consumePurchase(3, getPackageName(), getPurchaseToken(getString(R.string.sku_pro)));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (hadPro){
                Toast.makeText(context, "Pro has been removed.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context, "You do not have Pro.", Toast.LENGTH_SHORT).show();
            }

            new updatePro().execute();
        }
    }

    public void getOptions(){
        ArrayList<String> skuList = new ArrayList<String> ();
        skuList.add(getString(R.string.sku_pro));
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        Bundle skuDetails = null;
        try {
            skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        if (skuDetails.getInt("RESPONSE_CODE") == 0) {
            ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

            for (String thisResponse : responseList) {
                JSONObject object = null;
                try {
                    object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");

                    Log.d("Billing API", "id: " + sku);
                    Log.d("Billing API", "price: " + price);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class updatePro extends AsyncTask{
        boolean pro;

        @Override
        protected Object doInBackground(Object[] objects) {
            pro = hasPro();
            if (pro == true) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(getString(R.string.shared_preferences_pro), true);
                editor.commit();
            }
            else if (pro == false){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(getString(R.string.shared_preferences_pro), false);
                editor.commit();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }






    // ------------------------------------------------------------------------
    // OVERRIDE METHODS
    // ------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getActivityTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_view);

        context = this;

        TABLE_GROUP_EXERCISES = getString(R.string.TABLE_GROUP_EXERCISES);
        TABLE_GROUP = getString(R.string.TABLE_GROUP);
        TABLE_EXERCISE = getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);
        prefs = getString(R.string.shared_preferences);
        DB = new DBHandler(getApplicationContext());

        toolbar =  (Toolbar) findViewById(R.id.toolbar);
        message =(TextView) findViewById(R.id.message);
        sortButton = (TextView) findViewById(R.id.sort);
        list = (StickyListHeadersListView) findViewById(R.id.List);

        setupToolbar();



        // ------------------------------------------------------------------------
        // Get preference for list sort, or set to date if it doesn't exist.
        // ------------------------------------------------------------------------
        sharedpreferences = getSharedPreferences(prefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(sortPref, sharedpreferences.getString(sortPref, getString(R.string.date)));
        editor.apply();



        setupBilling();




        String message = "- BACKUP/RESTORE: Enable auto cloud backups via the options menu!\n" +
                "- ARCHIVE: Archive exercises to remove them from the list without permanently deleting them!\n" +
                "- THEMES: new colour themes available!";

        if (!sharedpreferences.getString("message", "None").equals(message)) {

            sharedpreferences.edit().putString("message", message).apply();

            Intent i = new Intent(this, are_you_sure.class);
            i.putExtra("header", "What's new?");
            i.putExtra("message", message);
            i.putExtra("YES", "Awesome!");
            i.putExtra("NO", "Nice!");
            startActivityForResult(i, 32);
        }





    }


    public void requestBackup(){
        if (sharedpreferences.getBoolean("backup", false)){
            BackupManager bm = new BackupManager(this);
            bm.dataChanged();
            Log.i(logtag, "Backup Requested");
        }
    }

    public void requestRestore(){
        RestoreObserver ro = new RestoreObserver() {
            @Override
            public void restoreStarting(int numPackages) {
                super.restoreStarting(numPackages);
                Log.i("RESTORE", "RESTORE STARTED");
            }

            @Override
            public void restoreFinished(int error) {
                super.restoreFinished(error);

                Log.i("RESTORE", "RESTORE FINISHED");
                Toast.makeText(context, "Restore Complete!", Toast.LENGTH_SHORT).show();


                Intent mStartActivity = new Intent(context, ExerciseView.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, mPendingIntent);
                System.exit(0);
            }
        };

        BackupManager bm = new BackupManager(this);
        bm.requestRestore(ro);

        Toast.makeText(this, "Restore requested", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPause() {
        super.onPause();

        requestBackup();
    }

    @Override
    public void onResume() {
        super.onResume();
        new refreshList(this).execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exercise_view, menu);
        this.menu = menu;

        // ------------------------------------------------------------------------
        // Change the text on the sort button depending on which sorting pref is
        // chosen
        // ------------------------------------------------------------------------
        if (sharedpreferences.getString(sortPref, "").equals(getString(R.string.date))){
            menu.findItem(R.id.sort).setTitle(R.string.SortByName);
        }
        else{
            menu.findItem(R.id.sort).setTitle(R.string.SortByDate);
        }

        // ------------------------------------------------------------------------
        // Same as above but with list type (Group/exercises)
        // ------------------------------------------------------------------------
        if (sharedpreferences.getString(listType, "").equals(getString(R.string.GROUP_VIEW))){
            menu.findItem(R.id.list_type).setTitle(R.string.Exercise_View);
            menu.findItem(R.id.sort).setVisible(false);
        }
        else{
            menu.findItem(R.id.list_type).setTitle(R.string.GROUP_VIEW);
            menu.findItem(R.id.sort).setVisible(true);
        }

        // ------------------------------------------------------------------------
        // Same as above but with list type (Group/exercises)
        // ------------------------------------------------------------------------
        if (sharedpreferences.getBoolean("backup", false)){
            menu.findItem(R.id.backup).setTitle("Backup: Auto");
        }
        else{
            menu.findItem(R.id.backup).setTitle("Backup: Off");
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){

            case R.id.list_type:
                changeListType();
                break;


            case R.id.sort:
                changeSort();
                break;

            case R.id.themes:
                if (sharedpreferences.getBoolean(getString(R.string.shared_preferences_pro), false)){
                    openThemeChooser();
                }
                else{
                    Intent i = new Intent(this, theme_advert.class);
                    startActivityForResult(i, 3);
                }
                break;

            case R.id.buy_pro:

                if (sharedpreferences.getBoolean(getString(R.string.shared_preferences_pro), false)){
                    Toast.makeText(this, "Pro Unlocked", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent i = new Intent(this, theme_advert.class);
                    startActivityForResult(i, 3);
                }
                break;



            //case R.id.remove_pro:
            //    new removePurchase(this).execute();
            //    break;

            case R.id.info:
                openInfo();
                break;

            case R.id.generate_example:
                new generateExample(this).execute();
                break;

            case R.id.archive:
                startActivity(new Intent(this, ArchivedExerciseView.class));
                break;

            case R.id.backup:

                if (sharedpreferences.getBoolean(getString(R.string.shared_preferences_pro), false)){
                    Intent i = new Intent(this, are_you_sure.class);
                    i.putExtra("header", "Auto Backup");
                    i.putExtra("message", "Do you want Gym Notes to take cloud backups automatically? (requires internet connection)");
                    i.putExtra("YES", "Backup");
                    i.putExtra("NO", "Do Not Backup");
                    startActivityForResult(i, 5);
                }
                else {
                    Intent i = new Intent(this, theme_advert.class);
                    startActivityForResult(i, 3);
                }


                break;

            case R.id.restore:

                if (sharedpreferences.getBoolean(getString(R.string.shared_preferences_pro), false)){
                    Intent i = new Intent(this, are_you_sure.class);
                    i.putExtra("header", "Restore");
                    i.putExtra("message", "You are about to restore Gym Notes to the last backed up state, losing all unsaved data. Do you wish to continue? (Ensure you have a stable internet connection)");
                    i.putExtra("YES", "Restore");
                    i.putExtra("NO", "Cancel");
                    startActivityForResult(i, 4);

                }
                else{
                    Intent i = new Intent(this, theme_advert.class);
                    startActivityForResult(i, 3);
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1){
            recreate();
        }
        else if (requestCode == 2 && resultCode == RESULT_OK){
            openPurchaseIntent(data);
        }
        else if(requestCode == 3 && resultCode == RESULT_OK){
            buyPro();
        }
        else if(requestCode == 4 && resultCode == RESULT_OK){
            requestRestore();
        }
        else if(requestCode == 5){

            if (resultCode == RESULT_OK){
                sharedpreferences.edit().putBoolean("backup", true).apply();
                menu.findItem(R.id.backup).setTitle("Backup: Auto");
            }
            else if (resultCode == RESULT_CANCELED){
                sharedpreferences.edit().putBoolean("backup", false).apply();
                menu.findItem(R.id.backup).setTitle("Backup: Off");
            }
        }


    }

    // ------------------------------------------------------------------------
    // MAIN METHODS
    // ------------------------------------------------------------------------

    private class generateExample extends AsyncTask{
        Context c;
        public generateExample(Context context){
            c = context;
        }
        @Override
        protected Object doInBackground(Object[] params) {
            new exampleGenerator(c, "Example Exercise", new BigDecimal("60"), "Chest/GN-bench-press.png", 500).generate();
            return null;
        }
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            new refreshList(c).execute();
            Toast.makeText(c, "New Example exercise generated", Toast.LENGTH_SHORT).show();
        }
    }

    public void setupToolbar(){
        setSupportActionBar(toolbar);
        ActionBar a = getSupportActionBar();

        a.setTitle(R.string.app_name);
        a.setIcon(R.mipmap.ic_app_icon_clear);

    }

    public class refreshList extends AsyncTask {
        Context context;
        String ExerciseOrGroup;

        public refreshList(Context c){
            context = c;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            ExerciseOrGroup = sharedpreferences.getString(listType, "");

            exerciseList = new ArrayList<Exercise>();
            groupList = new ArrayList<ExerciseGroup>();

            DB.open();

            if (ExerciseOrGroup.equals(getString(R.string.GROUP_VIEW))){



                // ------------------------------------------------------------------------
                // Get all Groups into a map from DB with ID as key
                // ------------------------------------------------------------------------
                Map<Long, ExerciseGroup> groupMap = new HashMap<Long, ExerciseGroup>();

                Cursor c = DB.getAllRows(TABLE_GROUP, new String[]{"_id", "group_name"});
                c.moveToPosition(-1);
                while (c.moveToNext()){

                    Long groupID = c.getLong(0);
                    String groupName = c.getString(1);

                    ExerciseGroup group = new ExerciseGroup(groupID, groupName);
                    groupMap.put(group.getID(), group);
                }
                c.close();

                // ------------------------------------------------------------------------
                // Join Group/Exercise link Table and Exercise table to find all
                // exercises that are in groups and add them into appropriate group in
                // group map
                // ------------------------------------------------------------------------
                String sql = "SELECT ge.group_id, ge.exercise_id, e._id, e.exercise_name, e.icon_path FROM " + TABLE_GROUP_EXERCISES + " AS ge JOIN " + TABLE_EXERCISE + " AS e ON ge.exercise_id = e._id WHERE e.archived != 1 ORDER BY ge.group_id;";
                c = DB.rawQuery(sql);
                c.moveToPosition(-1);
                while (c.moveToNext()){

                    Long groupID = c.getLong(0);
                    Long geID = c.getLong(1);
                    Long exerciseID = c.getLong(2);
                    String exerciseName = c.getString(3);
                    String iconPath = c.getString(4);

                    String groupName = "NONAMEFOUND";
                    Exercise exercise = new Exercise(exerciseID, exerciseName, iconPath);
                    if (groupMap.containsKey(groupID)){
                        groupMap.get(groupID).addExercise(exercise);

                        groupName = groupMap.get(groupID).getName();
                    }

                    Log.i("XXXXXX", groupID + "\t\t" + groupName + "\t\t" + geID + "  --  " + exerciseID + "\t\t\t" + exerciseName + "\t\t\t");
                }

                // ------------------------------------------------------------------------
                // Fill groupList with all the values from the group map
                // ------------------------------------------------------------------------
                groupList = new ArrayList<ExerciseGroup>(groupMap.values());
                groupList = sortGroupsByName(groupList);

            }
            else{


                // ------------------------------------------------------------------------
                // Fill exercise list with all exercises from exercise table
                // ------------------------------------------------------------------------
                Cursor c = DB.getAllRows(TABLE_EXERCISE, new String[]{"_id", "exercise_name", "icon_path"}, " archived = 0 ");
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    exerciseList.add(new Exercise(getApplicationContext(), TABLE_WORKOUT, c.getLong(0), c.getString(1), c.getString(2), null, null, null, null));
                }
                c.close();

                sortList();
            }

            DB.close();



            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refreshListView();

            if (ExerciseOrGroup.equals(getString(R.string.GROUP_VIEW))){
                if (groupList.size() <=0 && sharedpreferences.getBoolean(getString(R.string.first_open), true)){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(context.getString(R.string.first_open), false);
                    editor.commit();

                }

            }
            else{
                if (exerciseList.size() <=0 && sharedpreferences.getBoolean(getString(R.string.first_open), true)){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(context.getString(R.string.first_open), false);
                    editor.commit();

                    Toast.makeText(context, context.getString(R.string.Welcome), Toast.LENGTH_SHORT).show();

                    new generateExample(context).execute();
                }
            }



        }
    }


    public void sortList(){
        // ------------------------------------------------------------------------
        // Always sort by name first, then sort by date if preference set as date
        // ------------------------------------------------------------------------
        String sortType = sharedpreferences.getString(sortPref, null);
        exerciseList = sortByName(exerciseList);
        if (sortType.equals(getString(R.string.date))) {
            exerciseList = sortByLastWorkout(exerciseList);
        }

    }

    public void refreshListView(){

        String ExerciseOrGroup = sharedpreferences.getString(listType, "");

        if (ExerciseOrGroup.equals(getString(R.string.GROUP_VIEW))){

            //------------------------------------------------------------------------------
            // Remember the current scroll position of the list
            //------------------------------------------------------------------------------
            Parcelable listState = list.onSaveInstanceState();

            list.setAdapter(new GroupListAdapter(ExerciseView.this, groupList, "name"));


            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openGroup(position);
                }
            });
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                    openEditGroupDialog(position);
                    return true;
                }
            });

            //------------------------------------------------------------------------------
            // Restore position of list
            //------------------------------------------------------------------------------
            if (listState != null) {
                list.onRestoreInstanceState(listState);
            }

            //------------------------------------------------------------------------------
            // If the list is empty, then make the welcome message visible
            //------------------------------------------------------------------------------
            if (groupList.size() <= 0) {
                message.setText("No Groups");
                message.setVisibility(View.VISIBLE);
            }
            else{
                message.setVisibility(View.GONE);
            }

        }

        else{

            //------------------------------------------------------------------------------
            // Remember the current scroll position of the list
            //------------------------------------------------------------------------------
            Parcelable listState = list.onSaveInstanceState();


            // ------------------------------------------------------------------------
            // Set the adapter, and both click listeners for the list
            // ------------------------------------------------------------------------
            list.setAdapter(new ExerciseListAdapter(ExerciseView.this, exerciseList, sharedpreferences.getString(sortPref, null)));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openExercise(position);
                }
            });
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    openEditExerciseDialog(i);
                    return true;
                }
            });


            //------------------------------------------------------------------------------
            // Restore position of list
            //------------------------------------------------------------------------------
            if (listState != null) {
                list.onRestoreInstanceState(listState);
            }

            //------------------------------------------------------------------------------
            // If the list is empty, then make the welcome message visible
            //------------------------------------------------------------------------------
            if (exerciseList.size() <= 0) {
                message.setText(R.string.Welcome);
                message.setVisibility(View.VISIBLE);
            }
            else{
                message.setVisibility(View.GONE);
            }
        }





    }

    private void openExercise(int position) {
        Intent i = new Intent(this, WorkoutView.class);
        i.putExtra("Exercise_ID", exerciseList.get(position).getID());
        startActivity(i);
    }

    private void openGroup(int position) {
        Intent i = new Intent(this, GroupExerciseView.class);
        i.putExtra("Group_ID", groupList.get(position).getID());
        startActivity(i);
    }

    public void openEditExerciseDialog(int position){
        Intent i = new Intent(this, edit_exercise.class);
        i.putExtra("EditMode", true);
        i.putExtra("Exercise_ID", exerciseList.get(position).getID());
        startActivity(i);
    }

    public void openEditGroupDialog(int position){
        Intent i = new Intent(this, edit_group.class);
        i.putExtra("EditMode", true);
        Log.i(log, "ID put into intent = " + groupList.get(position).getID());
        i.putExtra("Group_ID", groupList.get(position).getID());
        startActivity(i);
    }

    public void FABButton(View view){

        String ExerciseOrGroup = sharedpreferences.getString(listType, "");

        if (ExerciseOrGroup.equals(getString(R.string.GROUP_VIEW))){
            Intent i = new Intent(this, edit_group.class);
            i.putExtra("EditMode", false);
            startActivity(i);
        }
        else{
            Intent i = new Intent(this, edit_exercise.class);
            i.putExtra("EditMode", false);
            startActivity(i);
        }
    }

    public void openInfo(){
        Intent i = new Intent(this, InfoView.class);
        startActivity(i);
    }

    public void openThemeChooser(){
        Intent i = new Intent(this, ThemeChooser.class);
        startActivityForResult(i, 1);
    }

    public static List<ExerciseGroup> sortGroupsByName(List<ExerciseGroup> groups){
        if (groups.size() <= 1){
            return groups;
        }

        List<ExerciseGroup> first = new ArrayList<ExerciseGroup>(groups.subList(0, groups.size()/2));
        List<ExerciseGroup> second = new ArrayList<ExerciseGroup>(groups.subList(groups.size()/2, groups.size()));

        first = sortGroupsByName(first);
        second = sortGroupsByName(second);


        int iFirst = 0;
        int iSecond = 0;
        int iMerged = 0;

        while (iFirst < first.size() && iSecond < second.size()){
            String firstName = first.get(iFirst).getName().toLowerCase();
            String secondName = second.get(iSecond).getName().toLowerCase();

            if (firstName.compareTo(secondName) <= 0){
                groups.set(iMerged, first.get(iFirst));
                iFirst++;
            }
            else {
                groups.set(iMerged, second.get(iSecond));
                iSecond++;
            }
            iMerged++;
        }

        List<ExerciseGroup> merged = new ArrayList<ExerciseGroup>();
        merged.addAll(groups.subList(0, iMerged));
        if (iFirst >= first.size()){
            merged.addAll(second.subList(iSecond, second.size()));
        }
        else{
            merged.addAll(first.subList(iFirst, first.size()));
        }

        return merged;
    }

    public static List<Exercise> sortByName(List<Exercise> exercises){
        if (exercises.size() <= 1){
            return exercises;
        }

        List<Exercise> first = new ArrayList<Exercise>(exercises.subList(0, exercises.size()/2));
        List<Exercise> second = new ArrayList<Exercise>(exercises.subList(exercises.size()/2, exercises.size()));

        first = sortByName(first);
        second = sortByName(second);


        int iFirst = 0;
        int iSecond = 0;
        int iMerged = 0;

        while (iFirst < first.size() && iSecond < second.size()){
            String firstName = first.get(iFirst).getName().toLowerCase();
            String secondName = second.get(iSecond).getName().toLowerCase();

            if (firstName.compareTo(secondName) <= 0){
                exercises.set(iMerged, first.get(iFirst));
                iFirst++;
            }
            else {
                exercises.set(iMerged, second.get(iSecond));
                iSecond++;
            }
            iMerged++;
        }

        List<Exercise> merged = new ArrayList<Exercise>();
        merged.addAll(exercises.subList(0, iMerged));
        if (iFirst >= first.size()){
            merged.addAll(second.subList(iSecond, second.size()));
        }
        else{
            merged.addAll(first.subList(iFirst, first.size()));
        }

        return merged;
    }

    public static List<Exercise> sortByLastWorkout(List<Exercise> exercises){
        if (exercises.size() <= 1){
            return exercises;
        }

        List<Exercise> first = new ArrayList<Exercise>(exercises.subList(0, exercises.size()/2));
        List<Exercise> second = new ArrayList<Exercise>(exercises.subList(exercises.size()/2, exercises.size()));

        first = sortByLastWorkout(first);
        second = sortByLastWorkout(second);


        int iFirst = 0;
        int iSecond = 0;
        int iMerged = 0;

        while (iFirst < first.size() && iSecond < second.size()){
            CustomCalendar firstLastWorkout = first.get(iFirst).getLastWorkout();
            CustomCalendar secondLastWorkout = second.get(iSecond).getLastWorkout();
            Calendar today = Calendar.getInstance();

            if (firstLastWorkout == null){
                exercises.set(iMerged, first.get(iFirst));
                iFirst++;
            }
            else if (secondLastWorkout == null){
                exercises.set(iMerged, second.get(iSecond));
                iSecond++;
            }
            else if (firstLastWorkout.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    firstLastWorkout.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)){

                exercises.set(iMerged, first.get(iFirst));
                iFirst++;
            }
            else if (secondLastWorkout.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    secondLastWorkout.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)){

                exercises.set(iMerged, second.get(iSecond));
                iSecond++;
            }

            else if (!firstLastWorkout.after(secondLastWorkout)){
                exercises.set(iMerged, first.get(iFirst));
                iFirst++;
            }
            else {
                exercises.set(iMerged, second.get(iSecond));
                iSecond++;
            }
            iMerged++;
        }

        List<Exercise> merged = new ArrayList<Exercise>();
        merged.addAll(exercises.subList(0, iMerged));
        if (iFirst >= first.size()){
            merged.addAll(second.subList(iSecond, second.size()));
        }
        else{
            merged.addAll(first.subList(iFirst, first.size()));
        }

        return merged;
    }

    public void changeSort(){
        // ------------------------------------------------------------------------
        // Changes preference for list sort then refreshes list
        // ------------------------------------------------------------------------

        String current = sharedpreferences.getString(sortPref, null);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (current == null || current.equals(getString(R.string.date))){
            editor.putString(sortPref, getString(R.string.name));
            menu.findItem(R.id.sort).setTitle(R.string.SortByDate);
        }
        else {
            editor.putString(sortPref, getString(R.string.date));
            menu.findItem(R.id.sort).setTitle(R.string.SortByName);
        }
        editor.commit();
        sortList();
        refreshListView();
    }

    public void changeListType(){
        // ------------------------------------------------------------------------
        // Changes preference for group view or exercise view
        // ------------------------------------------------------------------------

        String current = sharedpreferences.getString(listType, null);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (current == null || current.equals(getString(R.string.Exercise_View))){

            menu.findItem(R.id.sort).setVisible(false);
            menu.findItem(R.id.generate_example).setVisible(false);

            editor.putString(listType, getString(R.string.GROUP_VIEW));
            menu.findItem(R.id.list_type).setTitle(Exercise_View);
        }
        else {
            menu.findItem(R.id.sort).setVisible(true);
            menu.findItem(R.id.generate_example).setVisible(true);

            editor.putString(listType, getString(R.string.Exercise_View));
            menu.findItem(R.id.list_type).setTitle(R.string.GROUP_VIEW);
        }
        editor.commit();

        new refreshList(this).execute();
    }
}
