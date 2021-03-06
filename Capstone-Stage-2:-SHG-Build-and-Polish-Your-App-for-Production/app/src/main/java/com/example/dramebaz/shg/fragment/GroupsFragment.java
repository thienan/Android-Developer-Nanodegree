package com.example.dramebaz.shg.fragment;

/**
 * Created by dramebaz on 20/8/16.
 */


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.dramebaz.shg.adapter.GroupAdapter;
import com.example.dramebaz.shg.R;
import com.example.dramebaz.shg.RestApplication;
import com.example.dramebaz.shg.activity.ExpensesActivity;
import com.example.dramebaz.shg.client.SplitwiseRestClient;
import com.example.dramebaz.shg.splitwise.Group;
import com.google.firebase.crash.FirebaseCrash;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {
    public static final String GRP_PAGE = "GRP_PAGE";
    private ArrayAdapter<Group> groupAdapter;
    private List<Group> groups;
    private int mPage;
    private SplitwiseRestClient client;
    private ListView lvGroups;
    private SwipeRefreshLayout swipeContainer;

    public GroupsFragment() {
        // Required empty public constructor
    }

    private String title;

    public static GroupsFragment newInstance(int page) {
        GroupsFragment fragment = new GroupsFragment();
        Bundle args = new Bundle();
        args.putInt(GRP_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(GRP_PAGE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.balance_per_contact_grp, container, false);
        client = RestApplication.getSplitwiseRestClient();
        groups = new ArrayList<>();
        groupAdapter = new GroupAdapter(getContext(), groups);
        lvGroups = (ListView) view.findViewById(R.id.lvBalanceAll);
        lvGroups.setAdapter(groupAdapter);
        getGroupList();

        lvGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int groupId = groups.get(position).id;
                String groupName = groups.get(position).name;
                Intent i = new Intent(getContext(), ExpensesActivity.class);
                i.putExtra(getResources().getString(R.string.type),getResources().getString(R.string.group).toLowerCase());
                i.putExtra(getResources().getString(R.string.id), groupId);
                i.putExtra(getResources().getString(R.string.name), groupName);
                startActivity(i);
            }
        });
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                getGroupList();
            }
        });

        return view;
    }


    private void getGroupList(){
        client.getGroups(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                try {
                    groupAdapter.clear();
                    Log.i(getResources().getString(R.string.get_groups), json.toString());
                    groups = Group.fromJSONArray(json.getJSONArray(getResources().getString(R.string.groups).toLowerCase()));
                    Button noDataWarning = (Button) getActivity().findViewById(R.id.noGrpDataWarning);
                    noDataWarning.setText(getResources().getString(R.string.add_group));
                    if(groups.size()== 0){
                        noDataWarning.setVisibility(View.VISIBLE);
                    }else {
                        noDataWarning.setVisibility(View.INVISIBLE);
                    }

                    Log.i(getResources().getString(R.string.get_groups), groups.toString());
                    for (int i = 0; i<groups.size();i++){
                        Group group = groups.get(i);
                            groupAdapter.add(group);

                    }
                    swipeContainer.setRefreshing(false);
                    groupAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    FirebaseCrash.report(e);
                    Log.e(getResources().getString(R.string.get_groups), getResources().getString(R.string.json_parsing), e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

}
