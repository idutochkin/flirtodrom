package com.example.run18.withtoolbar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SliderFragment extends Fragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    int pageNumber;

    static SliderFragment newInstance(int page) {
        SliderFragment pageFragment = new SliderFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slider, null);
        TextView tvPage = (TextView) view.findViewById(R.id.image);

        if((pageNumber + 1) == 1)
            tvPage.setBackgroundResource(R.drawable.slide_auth_1);
        if((pageNumber + 1) == 2)
            tvPage.setBackgroundResource(R.drawable.slide_auth_2);
        if((pageNumber + 1) == 3)
            tvPage.setBackgroundResource(R.drawable.slide_auth_3);

        return view;
    }
}