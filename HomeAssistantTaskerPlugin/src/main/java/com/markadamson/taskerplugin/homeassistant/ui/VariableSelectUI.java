package com.markadamson.taskerplugin.homeassistant.ui;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

public class VariableSelectUI {

    public static void init(String[] variables, View button, final EditText destination) {
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
    private VariableSelectUI() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
