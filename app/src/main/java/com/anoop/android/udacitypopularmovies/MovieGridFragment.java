package com.anoop.android.udacitypopularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

public class MovieGridFragment extends Fragment implements ApiCall.MoviesApiCallResults {

    private GridView movieGrid;
    private String sort;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);
        movieGrid = (GridView) rootView.findViewById(R.id.moviesGrid);

        if (savedInstanceState != null) {
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(Constants.MOVIE_LIST);
            MoviesAdapter adapter = new MoviesAdapter(getActivity(), movies);
            movieGrid.setAdapter(adapter);
            sort = savedInstanceState.getString(Constants.MOVIE_SORT);
        } else {
            reLoadGrid();
        }

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = (Movie) movieGrid.getItemAtPosition(position);
                Intent details = new Intent(getActivity(), MovieDetailActivity.class);
                details.putExtra(Constants.MOVIE, movie);
                startActivity(details);
            }
        });

        return rootView;
    }

    @Override
    public void moviesApiCallResultsCallback(ArrayList<Movie> movies) {
        movieGrid.setAdapter(new MoviesAdapter(getActivity(), movies));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.MOVIE_SORT, sort);
        MoviesAdapter adapter = (MoviesAdapter) movieGrid.getAdapter();
        outState.putParcelableArrayList(Constants.MOVIE_LIST, adapter.getMovies());
    }

    @Override
    public void onStart() {
        super.onStart();
        reLoadGrid();
    }

    public void reLoadGrid() {
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.PREFS_NAME, getActivity().MODE_PRIVATE);
        String value = settings.getString("key", Constants.POPULARITY_DESC);
        ApiCall popularTask = new ApiCall(this);
        popularTask.execute(value);
    }


}
