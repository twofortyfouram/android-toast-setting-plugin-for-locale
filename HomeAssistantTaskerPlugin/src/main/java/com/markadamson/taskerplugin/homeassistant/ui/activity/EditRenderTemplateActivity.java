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
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.markadamson.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity;
import com.markadamson.taskerplugin.homeassistant.R;
import com.markadamson.taskerplugin.homeassistant.TaskerPlugin;
import com.markadamson.taskerplugin.homeassistant.bundle.RenderTemplatePluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.model.HAAPI;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIException;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIResult;
import com.markadamson.taskerplugin.homeassistant.model.HAAPITask;
import com.markadamson.taskerplugin.homeassistant.model.HAServer;
import com.markadamson.taskerplugin.homeassistant.ui.ServerSelectionUI;
import com.markadamson.taskerplugin.homeassistant.ui.VariableSelectUI;
import com.twofortyfouram.log.Lumberjack;

import net.jcip.annotations.NotThreadSafe;

import java.lang.ref.WeakReference;
import java.util.UUID;

@NotThreadSafe
public final class EditRenderTemplateActivity extends AbstractAppCompatPluginActivity {
    private ServerSelectionUI mServerUI;

    private EditText etTemplate, etVariable;

    private abstract static class MyAPITask<Params,Progress,Result> extends HAAPITask<Params,Progress,Result> {
        WeakReference<EditRenderTemplateActivity> activityReference;

        MyAPITask(EditRenderTemplateActivity activity, HAServer server) {
            super(server);
            activityReference = new WeakReference<>(activity);
        }
    }

    private static class TestTemplateTask extends MyAPITask<String,Void,String> {

        TestTemplateTask(EditRenderTemplateActivity context, HAServer server) {
            super(context, server);
        }

        @Override
        protected String doAPIInBackground(HAAPI api, String... strings) throws HAAPIException {
            return api.renderTemplate(strings[0]);
        }

        @Override
        protected void onPostExecute(HAAPIResult<String> result) {
            EditRenderTemplateActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (result.getException() != null) {
                result.getException().printStackTrace();
                Toast.makeText(activity, result.getException().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(activity, result.getResult(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_render_template);

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
        getSupportActionBar().setSubtitle(r.getString(R.string.activity_subtitle, r.getString(R.string.render_template)));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mServerUI = new ServerSelectionUI(this, callingApplicationLabel, new ServerSelectionUI.OnServerSelectedListener() {
            @Override
            public void onServerSelected(HAServer server) {
            }

            @Override
            public void onNothingSelected() {
            }
        });

        String[] variablesFromHost = TaskerPlugin.getRelevantVariableList(getIntent().getExtras());

        etTemplate = findViewById(R.id.et_template);
        VariableSelectUI.init(variablesFromHost, findViewById(R.id.btn_template_variable), etTemplate);

        etVariable = findViewById(R.id.et_variable);
        VariableSelectUI.init(variablesFromHost, findViewById(R.id.btn_variable), etVariable);

        findViewById(R.id.btn_test_template).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etTemplate.getText().toString().contains("%")) {
                            Toast.makeText(EditRenderTemplateActivity.this, "Cannot test using variables!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new TestTemplateTask(EditRenderTemplateActivity.this, mServerUI.currentServer())
                                .execute(etTemplate.getText().toString());
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mServerUI.onActivityResult(requestCode, resultCode, data);
    }

    private void restoreState(UUID id, String template, String variable) {
        mServerUI.setSelection(id);
        etTemplate.setText(template);
        etVariable.setText(variable);
    }

    @Override
    public void onPostCreateWithPreviousResult(@NonNull final Bundle previousBundle,
                                               @NonNull final String previousBlurb) {
        restoreState(
                RenderTemplatePluginBundleValues.getServer(previousBundle),
                RenderTemplatePluginBundleValues.getTemplate(previousBundle),
                RenderTemplatePluginBundleValues.getVariable(previousBundle)
        );
    }

    @Override
    public boolean isBundleValid(@NonNull final Bundle bundle) {
        return RenderTemplatePluginBundleValues.isBundleValid(bundle);
    }

    @Override
    public Bundle getResultBundle() {
        return RenderTemplatePluginBundleValues.generateBundle(getApplicationContext(),
                mServerUI.currentId(),
                etTemplate.getText().toString(),
                etVariable.getText().toString());
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull final Bundle bundle) {
        final String message = RenderTemplatePluginBundleValues.getTemplate(bundle);

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
        return new String[] {String.format("%s\nRendered Template\nThe rendered template from %s", etVariable.getText().toString(), mServerUI.currentServer().getName())};
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
        boolean result = false;

        if (mServerUI.serverCount() == 0)
            Toast.makeText(this, "Please select a Server", Toast.LENGTH_SHORT).show();
        else if (!TaskerPlugin.variableNameValid(etVariable.getText().toString()))
            Toast.makeText(this, "Not a valid variable name", Toast.LENGTH_SHORT).show();
        else
            result = true;

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
