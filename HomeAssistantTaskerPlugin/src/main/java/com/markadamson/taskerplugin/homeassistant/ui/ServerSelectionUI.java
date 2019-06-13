package com.markadamson.taskerplugin.homeassistant.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.markadamson.taskerplugin.homeassistant.R;
import com.markadamson.taskerplugin.homeassistant.model.HAServer;
import com.markadamson.taskerplugin.homeassistant.model.HAServerStore;
import com.markadamson.taskerplugin.homeassistant.ui.activity.EditServerActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerSelectionUI {

    private static final int REQ_NEW_SERVER = 1;
    private static final int REQ_EDIT_SERVER = 2;

    private final HAServerStore mServerStore;
    private final Spinner spnServers;

    private List<UUID> mIds;
    private List<HAServer> mServers;
    private ArrayAdapter<HAServer> mServerAdapter;

    public ServerSelectionUI(final Activity activity, final OnServerSelectedListener serverListener) {
        mServerStore = HAServerStore.getInstance();

        Map<UUID,HAServer> serverMap = mServerStore.getServers();
        mIds = new ArrayList<>(serverMap.keySet());
        mServers = new ArrayList<>();
        for(UUID id : mIds)
            mServers.add(serverMap.get(id));
        mServerAdapter = new ArrayAdapter<HAServer>(activity, android.R.layout.simple_spinner_dropdown_item, mServers);

        spnServers = (Spinner) activity.findViewById(R.id.spn_server);
        spnServers.setAdapter(mServerAdapter);

        spnServers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (serverListener != null)
                    serverListener.onServerSelected(mServers.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (serverListener != null)
                    serverListener.onNothingSelected();
            }
        });

        activity.findViewById(R.id.btn_add_server).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity.startActivityForResult(new Intent(activity, EditServerActivity.class), REQ_NEW_SERVER);
                    }
                }
        );

        activity.findViewById(R.id.btn_edit_server).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mServerAdapter.getCount() > 0) {
                            HAServer server = mServerAdapter.getItem(spnServers.getSelectedItemPosition());
                            activity.startActivityForResult(
                                    new Intent(activity, EditServerActivity.class)
                                            .putExtra(EditServerActivity.EXT_SERVER_NAME, server.getName())
                                            .putExtra(EditServerActivity.EXT_BASE_URL, server.getBaseURL())
                                            .putExtra(EditServerActivity.EXT_ACCESS_TOKEN, server.getAccessToken()),
                                    REQ_EDIT_SERVER
                            );
                        }
                    }
                }
        );

        activity.findViewById(R.id.btn_delete_server).setOnClickListener(
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
    }

    public int serverCount() {
        return mServers.size();
    }

    public HAServer currentServer() {
        return mServers.get(spnServers.getSelectedItemPosition());
    }

    public UUID currentId() {
        return mIds.get(spnServers.getSelectedItemPosition());
    }

    public void setSelection(UUID id) {
        spnServers.setSelection(mIds.indexOf(id));
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!Arrays.asList(new Integer[]{REQ_NEW_SERVER,REQ_EDIT_SERVER}).contains(requestCode))
            return false;

        if (resultCode == Activity.RESULT_OK) {
            HAServer server = new HAServer(
                    data.getStringExtra(EditServerActivity.EXT_SERVER_NAME),
                    data.getStringExtra(EditServerActivity.EXT_BASE_URL),
                    data.getStringExtra(EditServerActivity.EXT_ACCESS_TOKEN)
            );

            switch (requestCode) {
                case ServerSelectionUI.REQ_NEW_SERVER:
                    mIds.add(mServerStore.addServer(server));
                    mServerAdapter.add(server);
                    break;
                case ServerSelectionUI.REQ_EDIT_SERVER:
                    int idx = spnServers.getSelectedItemPosition();
                    mServerStore.updateServer(mIds.get(idx), server);
                    mServers.set(idx, server);
                    mServerAdapter.notifyDataSetChanged();
                    break;
            }
        }

        return true;
    }

    public interface OnServerSelectedListener {
        void onServerSelected(HAServer server);
        void onNothingSelected();
    }
}
