package com.example.tugasfilm;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.tugasfilm.model.Api;
import com.example.tugasfilm.model.Network;
import com.example.tugasfilm.presenter.Tv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TvAiringTodayFragment extends Fragment implements SearchView.OnQueryTextListener {

    private RecyclerView rvMain;
    private ProgressBar pbMain;
    private ArrayList<Tv> listTv;
    private ArrayList<Tv> tempTv;
    private ListTvAdapter listFilmAdapter;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tv_airing_today, container, false);
        rvMain = view.findViewById(R.id.rv_main);
        pbMain = view.findViewById(R.id.pb_main);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        listTv = new ArrayList<>();
        tempTv = new ArrayList<>();
        showRecyclerList();
        loadData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showRecyclerList(){
        listFilmAdapter = new ListTvAdapter(getActivity());
        listFilmAdapter.setListTv(listTv);
        rvMain.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvMain.setAdapter(listFilmAdapter);
    }

    private void loadData() {
        URL url = Api.getAiring();
        Log.e("url", url.toString());
        new TvAsyncTask().execute(url);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.isEmpty()){
            listTv.clear();
            newText = newText.toLowerCase();
            for (int i =0; i<tempTv.size(); i++){
                String title = tempTv.get(i).getName().toLowerCase();
                if (title.contains(newText)){
                    listTv.add(tempTv.get(i));
                }
            }
        } else {
            listTv.clear();
            listTv.addAll(tempTv);
        }
        listFilmAdapter.setListTv(listTv);
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class TvAsyncTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbMain.setVisibility(View.VISIBLE);
            rvMain.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String result = null;
            try {
                result = Network.getFromNetwork(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            rvMain.setVisibility(View.VISIBLE);
            pbMain.setVisibility(View.GONE);

            try {
                tempTv.clear();
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    Tv tvList = new Tv(object);
                    listTv.add(tvList);
                }
                tempTv.addAll(listTv);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listFilmAdapter.setListTv(listTv);
        }


    }

}
