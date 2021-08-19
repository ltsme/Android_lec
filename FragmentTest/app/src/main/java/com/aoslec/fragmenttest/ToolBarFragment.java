package com.aoslec.fragmenttest;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;


public class ToolBarFragment extends Fragment {
    EditText editText = null;
    Button button = null;
    SeekBar seekBar = null;
    int seekValue = 10;

    ToolbarListener activityCallback;

    //MainActivity와 통신을 위해 interface를 사용.
    //MainActivity에서는 implements ToolbarListener ~ 를 통해 사용.

    public interface ToolbarListener{
        public void onButtonCLick(int position, String text);
    }

    //fragment 에서는 onAttach()가 제일 처음으로 실행
    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        try {
            activityCallback = (ToolbarListener) context;
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toolbar, container, false);
        editText = view.findViewById(R.id.f1_edit);
        button = view.findViewById(R.id.f1_button);
        seekBar = view.findViewById(R.id.f1_seek);

        button.setOnClickListener(mClickListener);
        seekBar.setOnSeekBarChangeListener(mSeekBarChangedListener);

        return view;
    }

    SeekBar.OnSeekBarChangeListener mSeekBarChangedListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            seekValue = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    // MainActivity의 onButtonClick()에서 실행
    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            activityCallback.onButtonCLick(seekValue, editText.getText().toString());
        }
    };
}