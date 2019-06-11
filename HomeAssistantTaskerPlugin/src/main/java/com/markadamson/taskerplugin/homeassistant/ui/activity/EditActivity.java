/*
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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.markadamson.taskerplugin.homeassistant.bundle.PluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.R;
import com.markadamson.taskerplugin.homeassistant.model.HAAPITask;
import com.markadamson.taskerplugin.homeassistant.model.HAServer;
import com.markadamson.taskerplugin.homeassistant.model.HAServerStore;
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity;
import com.twofortyfouram.log.Lumberjack;

import net.jcip.annotations.NotThreadSafe;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NotThreadSafe
public final class EditActivity extends AbstractAppCompatPluginActivity {

    private static final int REQ_NEW_SERVER = 1;
    private static final int REQ_EDIT_SERVER = 2;

    private HAServerStore mServerStore;
    private List<UUID> mIds;
    private List<HAServer> mServers;
    private List<String> mServices;
    private ArrayAdapter<HAServer> mServerAdapter;
    private ArrayAdapter<String> mServiceAdapter;
    private Spinner spnServers, spnServices;
    private EditText etServiceData;

    private UUID mId;
    private String mService, mServiceData;

    private abstract static class MyAPITask<Params,Progress,Result> extends HAAPITask<Params,Progress,Result> {
        WeakReference<EditActivity> activityReference;

        MyAPITask(EditActivity context, HAServer server) {
            super(server);
            activityReference = new WeakReference<>(context);
        }
    }

    private static class GetServicesTask extends MyAPITask<Void,Void,List<String>> {
        GetServicesTask(EditActivity context, HAServer server) {
            super(context, server);
            context.mServices.clear();
            context.mServiceAdapter.notifyDataSetChanged();
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            return mAPI.getServices();
        }

        @Override
        protected void onPostExecute(List<String> services) {
            EditActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mServices.clear();
            activity.mServices.addAll(services);
            activity.mServiceAdapter.notifyDataSetChanged();

            if (activity.mServices.contains(activity.mService))
                activity.spnServices.setSelection(activity.mServices.indexOf(activity.mService));
        }
    }

    private static class TestServiceTask extends MyAPITask<String,Void,Void> {

        TestServiceTask(EditActivity context, HAServer server) {
            super(context, server);
        }

        @Override
        protected Void doInBackground(String... strings) {
            mAPI.callService(strings[0], strings[1], strings[2]);
            return null;
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

        getSupportActionBar().setSubtitle(R.string.plugin_name);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mServerStore = HAServerStore.getInstance();

        Map<UUID,HAServer> serverMap = mServerStore.getServers();
        mIds = new ArrayList<>(serverMap.keySet());
        mServers = new ArrayList<>();
        for(UUID id : mIds)
            mServers.add(serverMap.get(id));
        mServerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mServers);

        spnServers = (Spinner) findViewById(R.id.spn_server);
        spnServers.setAdapter(mServerAdapter);

        spnServers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                new GetServicesTask(EditActivity.this, mServers.get(position)).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mServices.clear();
                mServiceAdapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.btn_add_server).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivityForResult(new Intent(EditActivity.this, EditServerActivity.class), REQ_NEW_SERVER);
                    }
                }
        );

        findViewById(R.id.btn_edit_server).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mServerAdapter.getCount() > 0) {
                            HAServer server = mServerAdapter.getItem(spnServers.getSelectedItemPosition());
                            startActivityForResult(
                                    new Intent(EditActivity.this, EditServerActivity.class)
                                            .putExtra(EditServerActivity.EXT_SERVER_NAME, server.getName())
                                            .putExtra(EditServerActivity.EXT_BASE_URL, server.getBaseURL())
                                            .putExtra(EditServerActivity.EXT_ACCESS_TOKEN, server.getAccessToken()),
                                    REQ_EDIT_SERVER
                            );
                        }
                    }
                }
        );

        findViewById(R.id.btn_delete_server).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mServers.size() > 0) {
                            int idx = spnServers.getSelectedItemPosition();
                            mServerStore.deleteServer(mIds.get(idx));
                            mIds.remove(idx);
                            mServers.remove(idx);
                            mServerAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );

        mServices = new ArrayList<>();
        mServiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mServices);

        spnServices = (Spinner) findViewById(R.id.spn_service);
        spnServices.setAdapter(mServiceAdapter);

        etServiceData = (EditText) findViewById(R.id.et_service_data);

        findViewById(R.id.btn_test_service).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] service = mServices.get(spnServices.getSelectedItemPosition()).split("\\.");
                        new TestServiceTask(EditActivity.this, mServers.get(spnServers.getSelectedItemPosition()))
                                .execute(service[0], service[1], etServiceData.getText().toString());
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            HAServer server = new HAServer(
                    data.getStringExtra(EditServerActivity.EXT_SERVER_NAME),
                    data.getStringExtra(EditServerActivity.EXT_BASE_URL),
                    data.getStringExtra(EditServerActivity.EXT_ACCESS_TOKEN)
            );

            switch (requestCode) {
                case REQ_NEW_SERVER:
                    mIds.add(mServerStore.addServer(server));
                    mServerAdapter.add(server);
                    break;
                case REQ_EDIT_SERVER:
                    int idx = spnServers.getSelectedItemPosition();
                    mServerStore.updateServer(mIds.get(idx), server);
                    mServers.set(idx, server);
                    mServerAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mServers.size() > 0) {
            outState.putString("server", mIds.get(spnServers.getSelectedItemPosition()).toString());
            outState.putString("service", mServices.get(spnServices.getSelectedItemPosition()));
            outState.putString("data", etServiceData.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("server"))
            restoreState(
                    UUID.fromString(savedInstanceState.getString("server")),
                    savedInstanceState.getString("service"),
                    savedInstanceState.getString("data")
            );
    }

    private void restoreState(UUID id, String service, String data) {
        mId = id;
        mService = service;
        mServiceData = data;

        spnServers.setSelection(mIds.indexOf(mId));
        etServiceData.setText(mServiceData);
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

    @Nullable
    @Override
    public Bundle getResultBundle() {
        Bundle result = null;

        result = PluginBundleValues.generateBundle(getApplicationContext(),
                mIds.get(spnServers.getSelectedItemPosition()),
                mServices.get(spnServices.getSelectedItemPosition()),
                etServiceData.getText().toString());

        return result;
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull final Bundle bundle) {
        final String message = PluginBundleValues.getService(bundle);

        final int maxBlurbLength = getResources().getInteger(
                R.integer.com_twofortyfouram_locale_sdk_client_maximum_blurb_length);

        if (message.length() > maxBlurbLength) {
            return message.substring(0, maxBlurbLength);
        }

        return message;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            // Signal to AbstractAppCompatPluginActivity that the user canceled.
            mIsCancelled = true;
            finish();
        }
        else if (R.id.menu_save_changes == item.getItemId()) {
            if (mServers.isEmpty())
                Toast.makeText(this, "Please select a Server", Toast.LENGTH_SHORT).show();
            else if (mServices.isEmpty())
                Toast.makeText(this, "Please select a Service", Toast.LENGTH_SHORT).show();
            else {
                if (!etServiceData.getText().toString().isEmpty())
                    try {
                        JSONObject jsonData = new JSONObject(etServiceData.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid Service Data JSON", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
