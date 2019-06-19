/*
 * home-assistant-plugin-for-tasker <https://github.com/MarkAdamson/home-assistant-plugin-for-tasker>
 * Copyright 2019 Mark Adamson
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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.markadamson.taskerplugin.homeassistant.R;
import com.markadamson.taskerplugin.homeassistant.bundle.GetStatePluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.model.HAAPI;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIException;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIResult;
import com.markadamson.taskerplugin.homeassistant.model.HAAPITask;
import com.markadamson.taskerplugin.homeassistant.model.HAServer;
import com.markadamson.taskerplugin.homeassistant.ui.ServerSelectionUI;
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity;
import com.twofortyfouram.log.Lumberjack;

import net.jcip.annotations.NotThreadSafe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NotThreadSafe
public final class EditGetStateActivity extends AbstractAppCompatPluginActivity {
    private ServerSelectionUI mServerUI;

    private List<String> mEntities;
    private ArrayAdapter<String> mEntityAdapter;
    private Spinner spnEntities;
    private EditText etVariable;

    private String mEntity;

    private abstract static class MyAPITask<Params,Progress,Result> extends HAAPITask<Params,Progress,Result> {
        WeakReference<EditGetStateActivity> activityReference;

        MyAPITask(EditGetStateActivity context, HAServer server) {
            super(server);
            activityReference = new WeakReference<>(context);
        }
    }

    private static class GetEntitiesTask extends MyAPITask<Void,Void,List<String>> {
        GetEntitiesTask(EditGetStateActivity context, HAServer server) {
            super(context, server);
            context.mEntities.clear();
            context.mEntityAdapter.notifyDataSetChanged();
        }

        @Override
        protected List<String> doAPIInBackground(HAAPI api, Void... voids) throws HAAPIException {
            return api.getEntities();
        }

        @Override
        protected void onPostExecute(HAAPIResult<List<String>> entities) {
            EditGetStateActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (entities.getException() != null) {
                entities.getException().printStackTrace();
                Toast.makeText(activity, entities.getException().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            activity.mEntities.clear();
            activity.mEntities.addAll(entities.getResult());
            activity.mEntityAdapter.notifyDataSetChanged();

            if (activity.mEntities.contains(activity.mEntity))
                activity.spnEntities.setSelection(activity.mEntities.indexOf(activity.mEntity));
        }
    }

    private static class TestEntityTask extends MyAPITask<String,Void,String> {

        TestEntityTask(EditGetStateActivity context, HAServer server) {
            super(context, server);
        }

        @Override
        protected String doAPIInBackground(HAAPI api, String... strings) throws HAAPIException {
            return api.getState(strings[0]);
        }

        @Override
        protected void onPostExecute(HAAPIResult<String> state) {
            EditGetStateActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (state.getException() != null) {
                state.getException().printStackTrace();
                Toast.makeText(activity, state.getException().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(activity, state.getResult(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_get_state);

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

        mServerUI = new ServerSelectionUI(this, new ServerSelectionUI.OnServerSelectedListener() {
            @Override
            public void onServerSelected(HAServer server) {
                new GetEntitiesTask(EditGetStateActivity.this, server).execute();
            }

            @Override
            public void onNothingSelected() {
                mEntities.clear();
                mEntityAdapter.notifyDataSetChanged();
            }
        });

        mEntities = new ArrayList<>();
        mEntityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mEntities);

        spnEntities = (Spinner) findViewById(R.id.spn_entity);
        spnEntities.setAdapter(mEntityAdapter);

        etVariable = (EditText) findViewById(R.id.et_variable);

        findViewById(R.id.btn_test_entity).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new TestEntityTask(EditGetStateActivity.this, mServerUI.currentServer())
                                .execute(mEntities.get(spnEntities.getSelectedItemPosition()));
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mServerUI.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mServerUI.serverCount() > 0) {
            outState.putString("server", mServerUI.currentId().toString());
            outState.putString("entity", mEntities.get(spnEntities.getSelectedItemPosition()));
            outState.putString("variable", etVariable.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("server"))
            restoreState(
                    UUID.fromString(savedInstanceState.getString("server")),
                    savedInstanceState.getString("entity"),
                    savedInstanceState.getString("variable")
            );
    }

    private void restoreState(UUID id, String service, String data) {
        mEntity = service;

        mServerUI.setSelection(id);
        etVariable.setText(data);
    }

    @Override
    public void onPostCreateWithPreviousResult(@NonNull final Bundle previousBundle,
                                               @NonNull final String previousBlurb) {
        restoreState(
                GetStatePluginBundleValues.getServer(previousBundle),
                GetStatePluginBundleValues.getEntity(previousBundle),
                GetStatePluginBundleValues.getVariable(previousBundle)
        );
    }

    @Override
    public boolean isBundleValid(@NonNull final Bundle bundle) {
        return GetStatePluginBundleValues.isBundleValid(bundle);
    }

    @Override
    public Bundle getResultBundle() {
        return GetStatePluginBundleValues.generateBundle(getApplicationContext(),
                mServerUI.currentId(),
                mEntities.get(spnEntities.getSelectedItemPosition()),
                etVariable.getText().toString());
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull final Bundle bundle) {
        final String message = GetStatePluginBundleValues.getEntity(bundle);

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

    private boolean canSave() {
        boolean result = false;

        if (mServerUI.serverCount() == 0)
            Toast.makeText(this, "Please select a Server", Toast.LENGTH_SHORT).show();
        else if (mEntities.isEmpty())
            Toast.makeText(this, "Please select an Entity", Toast.LENGTH_SHORT).show();
        else {
            result = true;

//            if (!etVariable.getText().toString().isEmpty())
//                try {
//                    new JSONObject(etVariable.getText().toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, "Invalid Service Data JSON", Toast.LENGTH_SHORT).show();
//                    result = false;
//                }
        }

        return result;
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
