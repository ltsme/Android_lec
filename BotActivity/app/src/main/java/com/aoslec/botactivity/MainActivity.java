package com.aoslec.botactivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aoslec.botactivity.ui.db.DB;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aoslec.botactivity.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    EditText nameView;
    EditText phoneView;
    EditText emailView;
    Button addBtn;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nameView = findViewById(R.id.edit_name);
        phoneView = findViewById(R.id.edit_phone);
        emailView = findViewById(R.id.edit_email);
        addBtn = findViewById(R.id.btn_add);

       addBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(v==addBtn){
                   String name = nameView.getText().toString();
                   String email = emailView.getText().toString();
                   String phone = phoneView.getText().toString();

                   if(name==null || name.equals("")){ // 비어있을 경우

                       Toast toast = Toast.makeText(MainActivity.this, "이름이 입력되지 않았습니다. ", Toast.LENGTH_SHORT);
                       toast.show();
                   }else {
                       DB helper = new DB(MainActivity.this);
                       SQLiteDatabase db = helper.getWritableDatabase();
                       db.execSQL("insert into tb_content (name, phone, email) values (?,?,?)", new String[] {name, phone, email});
                       db.close();

                       Toast toast = Toast.makeText(MainActivity.this, name + "주소록에 입력 되었습니다.", Toast.LENGTH_SHORT);
                       toast.show();
                   }
               }
           }
       });


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_add_address, R.id.navigation_notifications2)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}