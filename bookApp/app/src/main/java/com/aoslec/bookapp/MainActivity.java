package com.aoslec.bookapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    private void initBookService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BookService bookService = retrofit.create(BookService.class);
    }

    private void bookServiceLoadBestSellers() {

        //베스트 셀러 가져오기
        
    }

}