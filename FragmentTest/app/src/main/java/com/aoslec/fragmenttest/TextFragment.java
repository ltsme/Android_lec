package com.aoslec.fragmenttest;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class TextFragment extends Fragment {
    TextView textView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        //textView 연결
        textView = view.findViewById(R.id.f2_text);
        return view;
    }

    public void changeTextProperties(int fontSize, String str){
        textView.setTextSize(fontSize);
        textView.setText(str);
    }
}