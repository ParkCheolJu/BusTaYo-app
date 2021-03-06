package com.bhsd.bustayo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bhsd.bustayo.MainActivity;
import com.bhsd.bustayo.R;
import com.bhsd.bustayo.activity.StationActivity;
import com.bhsd.bustayo.dto.StationListItem;

import java.util.ArrayList;
import java.util.HashMap;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.ItemViewHolder> {

    private ArrayList<StationListItem> list;
    private static OnListItemSelected listener = null;

    public StationListAdapter() {
        list = new ArrayList<>();
    }

    public StationListAdapter(ArrayList<StationListItem> stationListItems) {
        list = stationListItems;
    }

    public interface OnListItemSelected{
        void onItemSelected(View v, int position);
    }

    public void setOnListItemSelected(OnListItemSelected listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.station_list, parent, false);
        return new ItemViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addItem(StationListItem item) {
        list.add(item);
    }

    public ArrayList<StationListItem> getList(){
        return list;
    }

    public void clearList() {
        this.list.clear();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private Context context;
        private View itemView;
        private TextView stationName;
        private ImageView previous;
        private ImageView next;
        private ImageView busImage, manImage;
        private String stationId, arsId, routeId;
        private int busType;
        ArrayList<HashMap<String, String>> bus;

        ItemViewHolder(View itemView, final Context context) {
            super(itemView);

            this.itemView = itemView;
            this.context = context;

            stationName = itemView.findViewById(R.id.busstop_name);
            previous = itemView.findViewById(R.id.previous_busstop);
            next = itemView.findViewById(R.id.next_busstop);

            busImage = itemView.findViewById(R.id.bus);
            manImage = itemView.findViewById(R.id.man);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        if(context == MainActivity.context_main)
                            //??????????????? ???????????? ??????
                            listener.onItemSelected(v, position);
                        else
                        {
                            if(arsId.equals("0") || arsId.equals(" ")) {
                                Toast.makeText(context, "????????? ????????? ????????? ??? ????????????.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Intent intent = new Intent(context, StationActivity.class);
                            intent.putExtra("stationNm", stationName.getText().toString());
                            intent.putExtra("arsId", arsId);
                            context.startActivity(intent);
                        }
                    }
                }
            });
        }

        void onBind(StationListItem item) {
            stationName.setText(item.getStationName());
            arsId = item.getArsId();
            stationId = item.getStationId();
            routeId = item.getRouteId();
            busType = item.getBusType();
            bus = item.getBus();
            previous.setBackgroundColor(item.getPreviousColor());
            next.setBackgroundColor(item.getNextColor());

            busImage.setVisibility(View.GONE);
            manImage.setVisibility(View.GONE);

            for(int i = 0; i < bus.size(); i++) {
                if(item.getThisStation(i)) {
                    busImage.setVisibility(View.VISIBLE);
                    manImage.setVisibility(View.VISIBLE);
                    addBusIcon(item.getCongestion(i), busType);
                    break;
                }
            }
        }
        void addBusIcon(int congestion, int type) {
            manImage.setColorFilter(getCongestionColor(congestion));
            busImage.setColorFilter(getTypeColor(type));
        }

        int getCongestionColor(int congestion) {
            int color_id;
            switch(congestion) {
                case 0: // ????????? ??????
                case 3: // ??????
                    color_id = R.color.busy_empty;
                    break;
                case 4: // ??????
                    color_id = R.color.busy_half;
                    break;
                case 5: // ??????
                case 6:
                    color_id = R.color.busy_full;
                    break;
                default:
                    color_id = R.color.import_error;
            }
            return context.getColor(color_id);
        }
        int getTypeColor(int type) {
            int color = 0;
            switch (type) {
                case 1: // ??????
                    color = context.getColor(R.color.bus_skyblue);
                    break;
                case 2: // ??????
                case 4: // ??????
                    color = context.getColor(R.color.bus_green);
                    break;
                case 3: // ??????
                    color = context.getColor(R.color.bus_blue);
                    break;
                case 5: // ??????
                    color = context.getColor(R.color.bus_yellow);
                    break;
                case 6: // ??????
                case 0: // ??????
                    color = context.getColor(R.color.bus_red);
                    break;
                case 7: // ??????
                case 8: // ??????
                case 9: // ??????
                default:
                    color = context.getColor(R.color.import_error);
            }
            return color;
        }
    }
}
