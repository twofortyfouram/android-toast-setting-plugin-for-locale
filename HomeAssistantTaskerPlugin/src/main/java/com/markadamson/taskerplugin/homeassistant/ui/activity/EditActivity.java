/*
 * home-assistant-plugin-for-tasker <https://github.com/MarkAdamson/home-assistant-plugin-for-tasker>
 * Copyright 2019 Mark Adamson
 *
 * Original author:
 * android-toast-setting-plugin-for-locale <https://github.com/twofortyfouram/android-toast-setting-plugin-for-locale>
 * Copyright 2014 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.markadamson.taskerplugin.homeassistant.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.markadamson.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity;
import com.markadamson.taskerplugin.homeassistant.R;
import com.markadamson.taskerplugin.homeassistant.TaskerPlugin;
import com.markadamson.taskerplugin.homeassistant.Utils;
import com.markadamson.taskerplugin.homeassistant.bundle.PluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.model.HAAPI;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIException;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIResult;
import com.markadamson.taskerplugin.homeassistant.model.HAAPITask;
import com.markadamson.taskerplugin.homeassistant.model.HAServer;
import com.markadamson.taskerplugin.homeassistant.ui.ServerSelectionUI;
import com.twofortyfouram.log.Lumberjack;

import net.jcip.annotations.NotThreadSafe;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NotThreadSafe
public final class EditActivity extends AbstractAppCompatPluginActivity {
    private ServerSelectionUI mServerUI;

    private ArrayAdapter<String> mServiceAdapter;
    private AutoCompleteTextView atvService;
    private EditText etServiceData;

    private abstract static class MyAPITask<Params,Progress,Result> extends HAAPITask<Params,Progress,Result> {
        WeakReference<EditActivity> activityReference;

        MyAPITask(EditActivity activity, HAServer server) {
            super(server);
            activityReference = new WeakReference<>(activity);
        }
    }

    private static class GetServicesTask extends MyAPITask<Void,Void,List<String>> {
        GetServicesTask(EditActivity activity, HAServer server) {
            super(activity, server);
            activity.mServiceAdapter.clear();
        }

        @Override
        protected List<String> doAPIInBackground(HAAPI api, Void... voids) throws HAAPIException {
            return api.getServices();
        }

        @Override
        protected void onPostExecute(HAAPIResult<List<String>> services) {
            EditActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (services.getException() != null) {
                services.getException().printStackTrace();
                Toast.makeText(activity, services.getException().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            activity.mServiceAdapter.clear();
            activity.mServiceAdapter.addAll(services.getResult());
        }
    }

    private static class TestServiceTask extends MyAPITask<String,Void,Void> {

        TestServiceTask(EditActivity context, HAServer server) {
            super(context, server);
        }

        @Override
        protected Void doAPIInBackground(HAAPI api, String... strings) throws HAAPIException {
            api.callService(strings[0], strings[1], strings[2]);
            return null;
        }

        @Override
        protected void onPostExecute(HAAPIResult<Void> result) {
            EditActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (result.getException() != null) {
                result.getException().printStackTrace();
                Toast.makeText(activity, result.getException().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

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

        Resources r = getResources();
        getSupportActionBar().setSubtitle(r.getString(R.string.activity_subtitle, r.getString(R.string.call_service)));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mServerUI = new ServerSelectionUI(this, callingApplicationLabel, new ServerSelectionUI.OnServerSelectedListener() {
            @Override
            public void onServerSelected(HAServer server) {
                new GetServicesTask(EditActivity.this, server).execute();
            }

            @Override
            public void onNothingSelected() {
                mServiceAdapter.clear();
            }
        });


        String[] variablesFromHost = TaskerPlugin.getRelevantVariableList(getIntent().getExtras());

        mServiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<String>());
        atvService = findViewById(R.id.atv_service);
        atvService.setAdapter(mServiceAdapter);
        Utils.initVariableSelectUI(variablesFromHost, findViewById(R.id.btn_service_variable), atvService);

        etServiceData = findViewById(R.id.et_service_data);
        Utils.initVariableSelectUI(variablesFromHost, findViewById(R.id.btn_service_data_variable), etServiceData);

        findViewById(R.id.btn_test_service).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (atvService.getText().toString().contains("%")) {
                            Toast.makeText(EditActivity.this, "Cannot test using variables!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] service = atvService.getText().toString().split("\\.");
                        if (service.length != 2) {
                            Toast.makeText(EditActivity.this, "Invalid service!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new TestServiceTask(EditActivity.this, mServerUI.currentServer())
                                .execute(service[0], service[1], etServiceData.getText().toString());
                    }
                }
        );

        Utils.checkBatteryOptimisation(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mServerUI.onActivityResult(requestCode, resultCode, data);
    }

    private void restoreState(UUID id, String service, String data) {
        mServerUI.setSelection(id);
        atvService.setText(service);
        etServiceData.setText(data);
    }

    @Override
    public void onPostCreateWithPreviousResult(@NonNull final Bundle previousBundle,
            @NonNull final String previousBlurb) {
        restoreState(
                PluginBundleValues.getServer(previousBundle),
                PluginBundleValues.getService(previousBundle),
                PluginBundleValues.getData(previousBundle)
        );
    }

    @Override
    public boolean isBundleValid(@NonNull final Bundle bundle) {
        return PluginBundleValues.isBundleValid(bundle);
    }

    @Override
    public Bundle getResultBundle() {
        return PluginBundleValues.generateBundle(getApplicationContext(),
                mServerUI.currentId(),
                atvService.getText().toString(),
                etServiceData.getText().toString());
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull final Bundle bundle) {
        final String message = PluginBundleValues.getService(bundle);

        final int maxBlurbLength = getResources().getInteger(
                R.integer.com_twofortyfouram_locale_sdk_client_maximum_blurb_length);

        if (message.length() > maxBlurbLength) {
            return message.substring(0, maxBlurbLength - 1).concat("…");
        }

        return message;
    }

    @NonNull
    @Override
    public String[] getRelevantVariableList() {
        return new String[0];
    }

    @Override
    public int requestedTimeoutMS() {
        return 10000;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    private boolean canSave() {
        if (mServerUI.serverCount() == 0) {
            Toast.makeText(this, "Please select a Server", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (atvService.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a Service", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (canSave())
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            if (canSave())
                finish();
        }
        else if (R.id.menu_cancel == item.getItemId()) {
            mIsCancelled = true;

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
