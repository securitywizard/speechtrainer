package com.example.speechtrainer;


import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;
import java.util.Locale;
import java.lang.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import 	java.util.StringTokenizer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.*;

public class ToDoActivity extends Activity implements View.OnClickListener{
    protected static final int REQUEST_OK = 1;
    public TextToSpeech ttobj;
    public TextView text1;
    public TextView text2;
    public TextView text3;
    public TextView text4;
    public String inputtext;
    public Button bready;
    public Button breadyo;
    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;

    /**
     * Mobile Service Table used to access data
     */
    private MobileServiceTable<ToDoItem> mToDoTable;


    /**
     * Adapter to sync the items list with the view
     */
    private ToDoItemAdapter mAdapter;

    /**
     * EditText containing the "New To Do" text
     */
    private TextView mTextNewToDo;

    /**
     * Progress spinner to use for table operations
     */
    private ProgressBar mProgressBar;

    /**
     * Initializes the activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);
        findViewById(R.id.button1).setOnClickListener(this);
        //Setting up the Button and Edittext
        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);
        text4 = (TextView) findViewById(R.id.text4);
        bready = (Button) findViewById(R.id.bready);
        breadyo = (Button) findViewById(R.id.breadyo);

        //Set up Button Click Listener
        bready.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Start a text to speech function
                texttospeech(text1.getText().toString());

            }
        });
        breadyo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Start a text to speech function
                texttospeech(text2.getText().toString());

            }
        });
        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);

        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://speechtrainer.azure-mobile.net/",
                    "sWXINtuxdPLAEgjQZblMlVDgABmjfk14",
                    this).withFilter(new ProgressFilter());

            // Get the Mobile Service Table instance to use
            mToDoTable = mClient.getTable(ToDoItem.class);

            mTextNewToDo = (TextView) findViewById(R.id.text4);

            // Create an adapter to bind the items with the view
            mAdapter = new ToDoItemAdapter(this, R.layout.row_list_to_do);
            ListView listViewToDo = (ListView) findViewById(R.id.listViewToDo);
            listViewToDo.setAdapter(mAdapter);

            // Load the items from the Mobile Service
            refreshItemsFromTable();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        }
        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {

                if(status != TextToSpeech.ERROR)
                {
                    //Can pick what language you want
                    ttobj.setLanguage(Locale.US);
                }
            }
        });


    }
    public void onClick(View v) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, REQUEST_OK);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ((TextView) findViewById(R.id.text1)).setText(thingsYouSaid.get(0));
        }
        Set setOriginal = new HashSet<String>();
        Set setSpeech = new HashSet<String>();
        String t2= text2.getText().toString();
        String t1= text1.getText().toString();
        for (String token : t2.split(" ")) {
            setOriginal.add(token);

        }
        for (String token : t1.split(" ")) {
            setSpeech.add(token);

        }
        setOriginal.removeAll(setSpeech);
        Iterator iterator = setOriginal.iterator();
        while(iterator.hasNext()){
            String word=(String) iterator.next();
            ((TextView) findViewById(R.id.text4)).setText(word);
            mTextNewToDo = (TextView) findViewById(R.id.text4);
        }
    }
    public void texttospeech(String input)
    {
        //Will speak the input
        ttobj.speak(input, TextToSpeech.QUEUE_FLUSH, null);
    }
    public class Item {
        public String Id;
        public String Text;
    }

    /**
     * Initializes the activity menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * Select an option from the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String[] sentences= new String[]{"Doubt kills more dreams than failure ever will","If nobody sees you eating it it doesn’t contain any calories","The harder you work the harder it is to surrender","His story is History My Story is Mystery","Life is a game let’s make a high score"};
        Random rand = new Random();
        if (item.getItemId() == R.id.menu_refresh) {
            ((TextView) findViewById(R.id.text2)).setText(sentences[rand.nextInt(4)]);
            refreshItemsFromTable();
        }

        return true;
    }

    /**
     * Mark an item as completed
     *
     * @param item
     *            The item to mark
     */
    public void checkItem(final ToDoItem item) {
        if (mClient == null) {
            return;
        }

        // Set the item as completed and update it in the table
        item.setComplete(true);


        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final ToDoItem entity = mToDoTable.update(item).get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (entity.isComplete()) {
                                mAdapter.remove(entity);
                            }
                        }
                    });
                } catch (Exception e){
                    createAndShowDialog(e, "Error");
                }

                return null;
            }
        }.execute();
    }

    /**
     * Add a new item
     *
     * @param view
     *            The view that originated the call
     */
    public void addItem(View view) {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final ToDoItem item = new ToDoItem();

        item.setText(mTextNewToDo.getText().toString());
        item.setComplete(false);

        // Insert the new item
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final ToDoItem entity = mToDoTable.insert(item).get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!entity.isComplete()){
                                mAdapter.add(entity);
                            }
                        }
                    });
                } catch (Exception e){
                    createAndShowDialog(e, "Error");

                }

                return null;
            }
        }.execute();

        mTextNewToDo.setText("");
    }

    /**
     * Refresh the list with the items in the Mobile Service Table
     */
    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<ToDoItem> results =
                            mToDoTable.where().field("complete").
                                    eq(val(false)).execute().get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();

                            for(ToDoItem item : results){
                                mAdapter.add(item);
                            }
                        }
                    });
                } catch (Exception e){
                    createAndShowDialog(e, "Error");

                }

                return null;
            }
        }.execute();

    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }
}
