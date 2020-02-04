// Copyright (c) 2020, Scala Project
//
// Please see the included LICENSE file for more information.

package io.scalaproject.androidminer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import java.util.Arrays;
import android.widget.SeekBar.OnSeekBarChangeListener;

import io.scalaproject.androidminer.api.PoolItem;
import io.scalaproject.androidminer.api.ProviderManager;


public class SettingsFragment extends Fragment {

    private static final String LOG_TAG = "MiningSvc";

    private EditText edPass;
    private TextView edUser;
    private Button bQrCode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ProviderManager.generate();
        Button bSave;
        EditText edPool, edPort, edDevFees, edMiningGoal;
        Spinner spPool;

        SeekBar sbCores;
        TextView tvCoresNb, tvCoresMax;

        PoolSpinAdapter poolAdapter;

        CheckBox chkPauseOnBattery;

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Context appContext = MainActivity.getContextOfApplication();
        bSave = view.findViewById(R.id.saveSettings);

        edUser = view.findViewById(R.id.username);
        edPool = view.findViewById(R.id.pool);
        edPort = view.findViewById(R.id.port);
        edPass = view.findViewById(R.id.pass);

        edDevFees = view.findViewById(R.id.devfees);
        edMiningGoal = view.findViewById(R.id.mininggoal);

        bQrCode = view.findViewById(R.id.btnQrCode);

        spPool = view.findViewById(R.id.poolSpinner);

        sbCores = view.findViewById(R.id.seekbarcores);
        tvCoresNb = view.findViewById(R.id.coresnb);
        tvCoresMax = view.findViewById(R.id.coresmax);

        chkPauseOnBattery = view.findViewById(R.id.chkPauseOnBattery);

        PoolItem[] pools = ProviderManager.getPools();
        String[] description = new String[pools.length];
        for(int i =0; i< pools.length;i++) {
            description[i] = pools[i].getKey();
        }

        poolAdapter = new PoolSpinAdapter(appContext, R.layout.spinner_text_color, description);
        spPool.setAdapter(poolAdapter);

        int cores = Runtime.getRuntime().availableProcessors();

        // write suggested cores usage into editText
        int suggested = cores / 2;
        if (suggested == 0) suggested = 1;

        sbCores.setMax(cores);
        tvCoresMax.setText(Integer.toString(cores));

        if (Config.read("cores").equals("") == true) {
            sbCores.setProgress(suggested);
            tvCoresNb.setText(Integer.toString(suggested));
        } else {
            int corenb = Integer.parseInt(Config.read("cores"));
            sbCores.setProgress(corenb);
            tvCoresNb.setText(Integer.toString(corenb));
        }

        if (Config.read("devfees").equals("") == false) {
            edDevFees.setText(Config.read("devfees"));
        }

        if (Config.read("mininggoal").equals("") == false) {
            edMiningGoal.setText(Config.read("mininggoal"));
        }

        boolean checkStatus = (Config.read("pauseonbattery").equals("1") == true);

        if(checkStatus){
            chkPauseOnBattery.setChecked(checkStatus);
        }

        if (Config.read("address").equals("") == false) {
            edUser.setText(Config.read("address"));
        }

        if (Config.read("pass").equals("") == false) {
            edPass.setText(Config.read("pass"));
        }

        sbCores.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCoresNb.setText(Integer.toString(progress));
            }
        });

        spPool.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                if (Config.read("init").equals("1") == true) {
                    edUser.setText(Config.read("address"));
                    edPass.setText(Config.read("pass"));
                }

                if (position == 0){
                    edPool.setText(Config.read("custom_pool"));
                    edPort.setText(Config.read("custom_port"));
                    return;
                }

                PoolItem poolItem = ProviderManager.getPoolById(position);

                if(poolItem != null){
                    edPool.setText(poolItem.getPool());
                    edPort.setText(poolItem.getPort());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });

        PoolItem poolItem = null;
        String poolSelected = Config.read("selected_pool");
        int sp = Config.DefaultPoolIndex;
        if (poolSelected.equals("")) {
            poolSelected = Integer.toString(sp);
        }
        poolItem = ProviderManager.getPoolById(poolSelected);

        if(poolItem == null) {
            poolSelected = Integer.toString(sp);
        }

        poolItem = ProviderManager.getPoolById(poolSelected);
        if (Config.read("init").equals("1") == false) {
            poolSelected = Integer.toString(sp);
            edUser.setText(Config.DefaultWallet);
            edPass.setText(Config.DefaultPassword);
        }

        if(poolSelected.equals("0")) {
            edPool.setText(Config.read("custom_pool"));
            edPort.setText(Config.read("custom_port"));
        } else if(!Config.read("custom_port").equals("")) {
            edPool.setText(poolItem.getKey());
            edPort.setText(Config.read("custom_port"));
        }else{
            Config.write("custom_pool","");
            Config.write("custom_port","");
            edPool.setText(poolItem.getKey());
            edPort.setText(poolItem.getPort());
        }

        spPool.setSelection(Integer.valueOf(poolSelected));

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = edUser.getText().toString().trim();
                if (!Utils.verifyAddress(address)) {
                    Toast.makeText(appContext, "Invalid wallet address.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Config.write("address", address);

                String password = edPass.getText().toString().trim();
                if(password.equals("")) {
                    password = Tools.getDeviceName();
                }

                Log.i(LOG_TAG,"Password : " + password);
                Config.write("pass", password);
                edPass.setText(password);
                String key = (String)spPool.getSelectedItem();

                int selectedPosition = Config.DefaultPoolIndex;
                PoolItem[] pools = ProviderManager.getPools();
                for(int i = 0;i< pools.length;i++){
                    PoolItem pi = pools[i];
                    if(pi.getKey().equals(key)) {
                        selectedPosition = i;
                        break;
                    }
                }
                
                PoolItem pi = ProviderManager.getPoolById(selectedPosition);
                String port = edPort.getText().toString().trim();
                String pool = edPool.getText().toString().trim();

                Log.i(LOG_TAG,"PoolType : " + Integer.toString(pi.getPoolType()));
                if(pi.getPoolType() == 0) {
                    Config.write("custom_pool", pool);
                    Config.write("custom_port", port);
                } else if(!port.equals("") && !pi.getPort().equals(port)) {
                    Config.write("custom_pool", "");
                    Config.write("custom_port", port);
                } else {
                    Config.write("custom_port", "");
                    Config.write("custom_pool", "");
                }

                Log.i(LOG_TAG,"SelectedPool : " + Integer.toString(selectedPosition));
                Config.write("selected_pool", Integer.toString(selectedPosition));
                Config.write("cores", Integer.toString(sbCores.getProgress()));
                Config.write("threads", "1"); // Default value
                Config.write("intensity", "1"); // Default value

                String devfees = edDevFees.getText().toString().trim();
                if(devfees.equals("")) devfees = "0";
                Config.write("devfees", devfees);

                String mininggoal = edMiningGoal.getText().toString().trim();
                if(mininggoal.equals("") == false) {
                    Config.write("mininggoal", mininggoal);
                }

                Config.write("pauseonbattery", (chkPauseOnBattery.isChecked() ? "1" : "0"));

                Config.write("init", "1");

                Toast.makeText(appContext, "Settings Saved", Toast.LENGTH_SHORT).show();

                MainActivity main = (MainActivity) getActivity();
                for (Fragment fragment : getFragmentManager().getFragments()) {
                    if (fragment != null) {
                        getFragmentManager().beginTransaction().remove(fragment).commit();
                        ProviderManager.afterSave();
                    }
                }

                NavigationView nav = main.findViewById(R.id.nav_view);
                nav.getMenu().getItem(0).setChecked(true);
                main.setTitle(getResources().getString(R.string.miner));
                main.updateUI();
            }
        });

        edPool.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String poolAddress = edPool.getText().toString().trim();
                PoolItem[] pools = ProviderManager.getPools();
                int position  = spPool.getSelectedItemPosition();

                if (s.length() > 0) {
                    int poolSelected = 0;
                    for (int i = 1; i < pools.length; i++) {
                        PoolItem itemPool = pools[i];
                        if (itemPool.getPool().equals(poolAddress)) {
                            poolSelected = i;
                            break;
                        }
                    }
                    if(position != poolSelected){
                        spPool.setSelection(poolSelected);
                    }
                }
            }
        });

        bQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context appContext = MainActivity.getContextOfApplication();
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                    }
                    else {
                        startQrCodeActivity();
                    }
                }
                else {
                    Toast.makeText(appContext, "This version of Android does not support Qr Code", Toast.LENGTH_LONG);
                }
            }
        });

        Button bDonateHelp = view.findViewById(R.id.btnDonateHelp);
        bDonateHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate the layout of the popup window
                View popupView = inflater.inflate(R.layout.helper_donate, null);
                showPopup(v, inflater, popupView);
            }
        });

        Button bMiningGoalHelp = view.findViewById(R.id.btnMiningGoalHelp);
        bMiningGoalHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate the layout of the popup window
                View popupView = inflater.inflate(R.layout.helper_mining_goal, null);
                showPopup(v, inflater, popupView);
            }
        });

        return view;
    }

    private void showPopup(View view, LayoutInflater inflater, View popupView) {
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void startQrCodeActivity() {
        Context appContext = MainActivity.getContextOfApplication();
        try {
            Intent intent = new Intent(appContext, QrCodeScannerActivity.class);
            startActivity(intent);
        }catch (Exception e) {
            Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Context appContext = MainActivity.getContextOfApplication();
        if (requestCode == 100) {
            if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQrCodeActivity();
            }
            else {
                Toast.makeText(appContext,"Camera Permission Denied", Toast.LENGTH_LONG);
            }
        }
    }

    public void updateAddress() {
        String address =  Config.read("address");
        if (edUser == null || address.equals("")) {
            return;
        }

        edUser.setText(address);
    }

    public class PoolSpinAdapter extends ArrayAdapter<String> {

        private Context context;
        private String[] values;

        public PoolSpinAdapter(Context c, int textViewResourceId, String[] values) {
            super(c, textViewResourceId, values);
            this.context = c;
            this.values = values;
        }

        @Override
        public int getCount() {
            return values.length;
        }

        @Override
        public String getItem(int position) {
            return values[position];
        }

        public int getPosition(String item){
            return Arrays.asList(values).indexOf(item);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setText(values[position]);
            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            label.setText(values[position]);
            label.setPadding(5, 10, 5, 10);
            return label;
        }
    }
}