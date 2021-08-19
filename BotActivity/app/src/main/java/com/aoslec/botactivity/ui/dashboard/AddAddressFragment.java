package com.aoslec.botactivity.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aoslec.botactivity.databinding.FragmentAddAddressBinding;

public class AddAddressFragment extends Fragment {

    private AddAddressViewModel addAddressViewModel;
    private FragmentAddAddressBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        addAddressViewModel =
                new ViewModelProvider(this).get(AddAddressViewModel.class);

        binding = FragmentAddAddressBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textDashboard;
//        addAddressViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}