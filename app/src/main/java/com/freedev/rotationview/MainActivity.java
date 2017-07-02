package com.freedev.rotationview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tvTips = (TextView) findViewById(R.id.tv_tips);
        RotationView rotationView = (RotationView) findViewById(R.id.rotation_view);
        rotationView.setOnRotateListener(new RotationView.OnRotateListener() {
            @Override
            public void onRotated(float curDegree) {
                tvTips.setText("onRotation: " + curDegree);
            }

            @Override
            public void onRotateEnd(float curDegree) {
                tvTips.setText("onRotateEnd: " + curDegree);
            }
        });
    }
}
