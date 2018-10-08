package com.example.simon.myapp.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.simon.myapp.R;
import com.example.simon.myapp.views.CircleMenuLayout;

public class MainActivity extends Activity {
    private CircleMenuLayout mCircleLayout;
    public static String[] mItemTexts = new String[]{"20", "40", "60", "80", "100", "120", "140", "160", "180"};
    Button buttonSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_layout);
        mCircleLayout = (CircleMenuLayout) findViewById(R.id.circle_menu_view);
        mCircleLayout.setMenuItemIconsAndTexts(mItemTexts, true);

        buttonSwitch = findViewById(R.id.bt_switch);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isAlignment = !mCircleLayout.getIsAlignment();
                mCircleLayout.setAlignment(isAlignment);
                buttonSwitch.setBackgroundColor(isAlignment ? getResources().getColor(android.R.color.holo_green_dark) :
                        getResources().getColor(android.R.color.holo_red_dark));
            }
        });
        setOnItemClickListener();
    }

    int circleType = -1;

    private void setOnItemClickListener() {
        mCircleLayout.setOnMenuItemClickListener(new CircleMenuLayout.OnMenuItemClickListener() {
            @Override
            public void itemClick(View view, int pos, boolean isSingle, int circleType) {
                Toast.makeText(MainActivity.this, mItemTexts[pos], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void autoScrollBack(int pos, boolean isSingle, int circleType) {
                Toast.makeText(MainActivity.this, "auto  " + mItemTexts[pos], Toast.LENGTH_SHORT).show();
            }
        });
    }


}
