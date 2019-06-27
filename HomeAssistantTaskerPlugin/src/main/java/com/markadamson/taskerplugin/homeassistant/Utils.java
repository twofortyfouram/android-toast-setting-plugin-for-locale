package com.markadamson.taskerplugin.homeassistant;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

public class Utils {

    public static void initVariableSelectUI(String[] variables, View button, final EditText destination) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(button.getContext(), android.R.layout.select_dialog_item, variables);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(v.getContext());
                builderSingle.setTitle("Variable Select");
                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String variable = adapter.getItem(which);
                        assert variable != null;

                        int start = Math.max(destination.getSelectionStart(), 0);
                        int end = Math.max(destination.getSelectionEnd(), 0);
                        destination.getText().replace(Math.min(start, end), Math.max(start, end),
                                variable, 0, variable.length());
                    }
                });
                builderSingle.show();
            }
        });
    }

    public static void checkBatteryOptimisation(final Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = activity.getPackageName();
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                new android.app.AlertDialog.Builder(activity)
                        .setTitle(R.string.disable_battery_optimization)
                        .setMessage(R.string.battery_optimization_dialog)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.startActivity(new Intent()
                                        .setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        }
    }

    private Utils() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
