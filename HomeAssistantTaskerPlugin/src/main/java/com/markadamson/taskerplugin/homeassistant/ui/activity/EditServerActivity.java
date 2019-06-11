package com.markadamson.taskerplugin.homeassistant.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.markadamson.taskerplugin.homeassistant.R;
import com.markadamson.taskerplugin.homeassistant.model.HAAPITask;
import com.markadamson.taskerplugin.homeassistant.model.HAServer;
import com.twofortyfouram.log.Lumberjack;

import java.lang.ref.WeakReference;

public class EditServerActivity extends AppCompatActivity {

    public static final String EXT_SERVER_NAME = "com.markadamson.taskerplugin.homeassistant.ui.activity.EditServerActivity.EXT_SERVER_NAME";
    public static final String EXT_BASE_URL = "com.markadamson.taskerplugin.homeassistant.ui.activity.EditServerActivity.EXT_BASE_URL";
    public static final String EXT_ACCESS_TOKEN = "com.markadamson.taskerplugin.homeassistant.ui.activity.EditServerActivity.EXT_ACCESS_TOKEN";

    EditText etServerName, etBaseURL, etAccessToken;

    private abstract static class MyAPITask<Params,Progress,Result> extends HAAPITask<Params,Progress,Result> {
        WeakReference<EditServerActivity> activityReference;

        MyAPITask(EditServerActivity context, HAServer server) {
            super(server);
            activityReference = new WeakReference<>(context);
        }
    }

    private static class TestServerTask extends MyAPITask<Void,Void,Boolean> {
        TestServerTask(EditServerActivity context, HAServer server) {
            super(context, server);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return mAPI.testServer();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            EditServerActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            Toast.makeText(activity, result ? R.string.connection_successful : R.string.connection_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_server);

        etServerName = (EditText) findViewById(R.id.et_server_name);
        etBaseURL = (EditText) findViewById(R.id.et_base_url);
        etAccessToken = (EditText) findViewById(R.id.et_access_token);

        Intent i = getIntent();
        if (i.hasExtra(EXT_SERVER_NAME))
            etServerName.setText(i.getStringExtra(EXT_SERVER_NAME));
        if (i.hasExtra(EXT_BASE_URL))
            etBaseURL.setText(i.getStringExtra(EXT_BASE_URL));
        if (i.hasExtra(EXT_ACCESS_TOKEN))
            etAccessToken.setText(i.getStringExtra(EXT_ACCESS_TOKEN));

        /*
         * To help the user keep context, the title shows the host's name and the subtitle
         * shows the plug-in's name.
         */
        CharSequence callingApplicationLabel = null;
        try {
            callingApplicationLabel =
                    getPackageManager().getApplicationLabel(
                            getPackageManager().getApplicationInfo(getCallingPackage(),
                                    0));
        } catch (final PackageManager.NameNotFoundException e) {
            Lumberjack.e("Calling package couldn't be found%s", e); //$NON-NLS-1$
        }
        if (null != callingApplicationLabel) {
            setTitle(callingApplicationLabel);
        }

        getSupportActionBar().setSubtitle(R.string.plugin_name);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.btn_test_server).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new TestServerTask(EditServerActivity.this,
                                new HAServer(
                                        etServerName.getText().toString(),
                                        etBaseURL.getText().toString(),
                                        etAccessToken.getText().toString()
                                )).execute();
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
        }
        else if (R.id.menu_save_changes == item.getItemId()) {
            setResult(Activity.RESULT_OK,
                    new Intent()
                            .putExtra(EXT_SERVER_NAME, etServerName.getText().toString())
                            .putExtra(EXT_BASE_URL, etBaseURL.getText().toString())
                            .putExtra(EXT_ACCESS_TOKEN, etAccessToken.getText().toString()));
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
