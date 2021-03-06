package com.bhsd.bustayo.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhsd.bustayo.MainActivity;
import com.bhsd.bustayo.R;
import com.bhsd.bustayo.application.GpsTracker;
import com.bhsd.bustayo.application.RequestServer;
import com.bhsd.bustayo.application.SetAlarmDialog;
import com.bhsd.bustayo.adapter.StationListAdapter;
import com.bhsd.bustayo.application.APIManager;
import com.bhsd.bustayo.dto.BusPositions;
import com.bhsd.bustayo.dto.StationListItem;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class GetOffAlarmFragment extends Fragment {

    private MainActivity activity;
    private ArrayList<StationListItem> stationListItems;
    private AutoCompleteTextView busAuto;
    private ArrayList<String> stationitem;
    private ArrayList<BusPositions> busPos;
    private ImageView mapping;
    private RecyclerView busStation;
    private StationListAdapter slAdapter = new StationListAdapter();
    private StationListAdapter alarmAdapter;

    private ArrayList<HashMap<String, String>> busInfo;
    private ArrayList<HashMap<String, String>> bus;
    private ArrayList<HashMap<String, String>> stationInfo;

    private String busRouteId;
    private int routeType;

    private String myBus;

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSION_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    int alarmStation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        activity = (MainActivity) getActivity();
        stationListItems = new ArrayList<StationListItem>();
        busAuto = view.findViewById(R.id.search_bus_auto);
        mapping = view.findViewById(R.id.search_bus_button);
        busStation = view.findViewById(R.id.station_list);
        alarmAdapter = new StationListAdapter();
        busPos = new ArrayList<>();

        final RecyclerView slRecyclerView = view.findViewById(R.id.station_list);

        slRecyclerView.setHasFixedSize(true);
        slRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));


        new Thread() {
            @Override
            public void run() {
                busInfo = APIManager.getAPIArray(APIManager.GET_BUS_ROUTE_LIST, new String[]{""}, new String[]{"busRouteId", "routeType", "busRouteNm", "edStationNm"});

                stationitem = new ArrayList<>();
                for (HashMap<String, String> item : busInfo) {
                    stationitem.add(item.get("busRouteNm") + "  " + item.get("edStationNm") + "??????");
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter adapter = new ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, stationitem);
                        busAuto.setAdapter(adapter);
                    }
                });
            }
        }.start();

        mapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                busPos.clear();

                if (!checkLocationServicesStatus()) {
                    showDialogForLocationServiceSetting();
                } else {
                    checkRunTimePermission();
                }

                gpsTracker = new GpsTracker(getActivity());
//                          ??????
                final double latitude = Double.parseDouble(String.format("%.6f", gpsTracker.getLatitude()));
//                            ??????
                final double longitude = Double.parseDouble(String.format("%.6f", gpsTracker.getLongitude()));

                new Thread() {
                    @Override
                    public void run() {
                        HashMap<String, String> busmap = APIManager.getAPIMap(APIManager.GET_BUS_ROUTE_LIST, new String[]{busAuto.getText().toString().split(" ")[0]}, new String[]{"busRouteId", "routeType"});
                        if (busmap.isEmpty())
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "?????? ????????? ??????????????????!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        else {
                            busRouteId = busmap.get("busRouteId");
                            routeType = Integer.parseInt(busmap.get("routeType"));
                            bus = APIManager.getAPIArray(APIManager.GET_BUSPOS_BY_RT_ID, new String[]{busRouteId}, new String[]{"lastStnId", "congetion", "plainNo", "gpsX", "gpsY", "sectionId"});
                            stationInfo = APIManager.getAPIArray(APIManager.GET_STATION_BY_ROUTE, new String[]{busRouteId}, new String[]{"station", "arsId", "stationNm", "section"});
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    slAdapter = new StationListAdapter();
                                    StationListItem stationListItem;
                                    for (HashMap<String, String> map : stationInfo) {
                                        stationListItem = new StationListItem(map.get("stationNm"), map.get("station"), map.get("arsId"), busRouteId, routeType, 4, 5, bus);
                                        stationListItem.setSectionId(map.get("section"));
                                        slAdapter.addItem(stationListItem);
                                    }
                                    slAdapter.notifyDataSetChanged();
                                }
                            });

                            //?????? ????????? ????????? ?????? ????????? ????????? ?????????
                            for (HashMap<String, String> item : bus) {
                                String plainNo = item.get("plainNo");
                                String posX = item.get("gpsX");
                                String posY = item.get("gpsY");
                                String sectionId = item.get("sectionId");
                                busPos.add(new BusPositions(plainNo, posX, posY, sectionId));
                            }

                            //?????? ????????? ????????? ??????

                            myBus = "";

                            for (final BusPositions item : busPos) {
                                double currentBusPosX = item.getPosX();
                                double currentBusPosY = item.getPosy();

                                if ((longitude - 0.001 <= currentBusPosX && longitude + 0.001 >= currentBusPosX) && (currentBusPosY >= latitude - 0.001 && latitude + 0.001 >= currentBusPosY)) {
                                    myBus = item.getPlainNo();
                                    final String mySectionId = item.getSectionId();
                                    alarmAdapter.clearList();

                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Iterator<StationListItem> iter = slAdapter.getList().iterator();

                                            while (iter.hasNext()) {
                                                StationListItem stationItem = iter.next();
                                                if (stationItem.getSectionId().equals(mySectionId)) {
                                                    alarmAdapter.addItem(stationItem);
                                                    while (iter.hasNext()) {
                                                        stationItem = iter.next();
                                                        alarmAdapter.addItem(stationItem);
                                                        alarmAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                            slRecyclerView.setAdapter(alarmAdapter);
                                        }
                                    });

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), myBus + "??? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                            busStation.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    break;
                                }
                            }

                            if (myBus.equals(""))
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                        if (busStation.getVisibility() == View.VISIBLE)
                                            busStation.setVisibility(View.INVISIBLE);
                                    }
                                });
                        }
                    }
                }.start();
                //?????? ????????? ???????????? ?????? ?????? ?????? ???????????? ???????????? ????????? ???????????? ?????? ???????????? ?????????
            }
        });

        alarmAdapter.setOnListItemSelected(new StationListAdapter.OnListItemSelected() {
            @Override
            public void onItemSelected(View v, final int position) {

                //??? ?????????????????? ????????? ????????? ??????
                final SetAlarmDialog alarmsetting = new SetAlarmDialog();
                alarmsetting.Dialog(getContext(), activity, new SetAlarmDialog.DialogListener() {
                    @Override
                    public void setAlarm(int data) {
                        alarmStation = data;
                        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(activity);
                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(activity, new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                String newToken = instanceIdResult.getToken();
                                //???????????? ???????????? getout???????????? ????????????
                                RequestServer requestServer = new RequestServer(activity);
                                requestServer.requestGetOffAlarm(alarmAdapter.getList().get(position).getRouteId(),alarmAdapter.getList().get(position).getArsId() , myBus , alarmStation, newToken, position);
                            }
                        });
                    }
                });
            }
        });
        return view;
    }

    //        ActivityCompat.requestPermission??? ????????? ????????? ????????? ????????? ???????????? ?????????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSION.length) {
            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                //?????? ?????? ???????????? ??? ??????
                ;
            } else {
//                ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSION[0]) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSION[1])) {
                    Toast.makeText(getContext(), "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "???????????? ?????????????????????. ???????????? ???????????? ???????????? ?????????.", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    void checkRunTimePermission() {
//        ????????? ????????? ??????
//        1.?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
//            2. ?????? ???????????? ????????? ????????? ???????????? ???????????? ??? ??????
        } else {
//            ????????? ????????? ????????? ?????? ????????? ????????? ????????? ????????????.
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSION[0])) {
//                ???????????? ????????? ????????? ??? ?????? ?????? ?????? ????????? ???????????? ?????? ??????????????? ???????????? ????????? ????????? ??????
                Toast.makeText(getActivity(), "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
            } else
//                ???????????? ????????? ????????? ????????? ?????? ?????? ????????? ??????
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
        }
    }

    //    GPS???????????? ?????? ?????????
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n" + "?????? ????????? ?????????????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
//                ???????????? GPS??? ????????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "GPS ????????? ?????????");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
