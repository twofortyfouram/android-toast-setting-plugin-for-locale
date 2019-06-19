package com.markadamson.taskerplugin.homeassistant.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.markadamson.taskerplugin.homeassistant.R;
import com.markadamson.taskerplugin.homeassistant.model.HAAPI;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIException;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIResult;
import com.markadamson.taskerplugin.homeassistant.model.HAAPITask;
import com.markadamson.taskerplugin.homeassistant.model.HAServer;

import java.lang.ref.WeakReference;

public class EditServerActivity extends AppCompatActivity {

    public static final int REQ_NEW_SERVER = 0;
    public static final int REQ_EDIT_SERVER = 1;

    private static final int[] MODE_TITLE = {R.string.add_a_server, R.string.edit_server};

    public static final String EXT_HOST_NAME = "com.markadamson.taskerplugin.homeassistant.ui.activity.EditServerActivity.EXT_HOST_NAME";
    public static final String EXT_REQUEST_CODE = "com.markadamson.taskerplugin.homeassistant.ui.activity.EditServerActivity.EXT_REQUEST CODE";
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
        protected Boolean doAPIInBackground(HAAPI api, Void... voids) throws HAAPIException {
            return api.testServer();
        }

        @Override
        protected void onPostExecute(HAAPIResult<Boolean> result) {
            EditServerActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (result.getException() != null) {
                result.getException().printStackTrace();
                Toast.makeText(activity, result.getException().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(activity, result.getResult() ? R.string.connection_successful : R.string.connection_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_server);

        etServerName = findViewById(R.id.et_server_name);
        etBaseURL = findViewById(R.id.et_base_url);
        etAccessToken = findViewById(R.id.et_access_token);

        Intent i = getIntent();
        if (i.hasExtra(EXT_SERVER_NAME))
            etServerName.setText(i.getStringExtra(EXT_SERVER_NAME));
        if (i.hasExtra(EXT_BASE_URL))
            etBaseURL.setText(i.getStringExtra(EXT_BASE_URL));
        if (i.hasExtra(EXT_ACCESS_TOKEN))
            etAccessToken.setText(i.getStringExtra(EXT_ACCESS_TOKEN));

        if (i.hasExtra(EXT_HOST_NAME))
            setTitle(i.getCharSequenceExtra(EXT_HOST_NAME));

        Resources r = getResources();
        String mode = r.getString(MODE_TITLE[i.getIntExtra(EXT_REQUEST_CODE, REQ_EDIT_SERVER)]);
        getSupportActionBar().setSubtitle(r.getString(R.string.activity_subtitle, mode));

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
    public void onBackPressed() {
        setResult(Activity.RESULT_OK,
                new Intent()
                        .putExtra(EXT_SERVER_NAME, etServerName.getText().toString())
                        .putExtra(EXT_BASE_URL, etBaseURL.getText().toString())
                        .putExtra(EXT_ACCESS_TOKEN, etAccessToken.getText().toString()));
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            setResult(Activity.RESULT_OK,
                    new Intent()
                            .putExtra(EXT_SERVER_NAME, etServerName.getText().toString())
                            .putExtra(EXT_BASE_URL, etBaseURL.getText().toString())
                            .putExtra(EXT_ACCESS_TOKEN, etAccessToken.getText().toString()));
            finish();
        }
        else if (R.id.menu_cancel == item.getItemId()) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
