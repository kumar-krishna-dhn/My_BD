package com.example.mybd;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class OnboardingsFragment extends Fragment {
    private List<DataItem> dataItemList;
    private RecyclerView recyclerView;
    private DataAdapter dataAdapter;

    public static OnboardingsFragment newInstance() {
        return new OnboardingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboardings, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dataItemList = new ArrayList<>();
        dataAdapter = new DataAdapter(dataItemList);
        recyclerView.setAdapter(dataAdapter);
        fetchDataItems();
        return view;
    }
    public interface ApiService {
        @GET("api/data") // Replace with your API endpoint
        Call<List<DataItem>> getDataItems();
    }
    private void fetchDataItems() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://carrierguru.in/aconfig.php")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<DataItem>> call = apiService.getDataItems();
        call.enqueue(new Callback<List<DataItem>>() {
            @Override
            public void onResponse(Call<List<DataItem>> call, Response<List<DataItem>> response) {
                if (response.isSuccessful()) {
                    List<DataItem> itemList = response.body();
                    if (itemList != null) {
                        dataItemList.clear();
                        dataItemList.addAll(itemList);
                        dataAdapter.notifyDataSetChanged();
                    }
                } else {
                    // Handle error response
                    Toast.makeText(getActivity(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DataItem>> call, Throwable t) {
                // Handle network error
                Toast.makeText(getActivity(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
        private List<DataItem> dataItemList;

        public DataAdapter(List<DataItem> dataItemList) {
            this.dataItemList = dataItemList;
        }

        public void setData(List<DataItem> dataItemList) {
            this.dataItemList = dataItemList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_onboardings, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DataItem item = dataItemList.get(position);
            holder.tvId.setText(item.getId());
            holder.tvName.setText(item.getName());
            // Bind other data properties to the corresponding views
        }

        @Override
        public int getItemCount() {
            return dataItemList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvId, tvName;
            // Add other TextViews or views for other properties

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.tvId);
                tvName = itemView.findViewById(R.id.tvName);
                // Initialize other TextViews or views
            }
        }
    }
}
