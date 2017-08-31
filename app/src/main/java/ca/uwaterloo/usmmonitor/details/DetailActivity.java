package ca.uwaterloo.usmmonitor.details;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import ca.uwaterloo.usmmonitor.ListAdapter;
import ca.uwaterloo.usmmonitor.R;

/**
 * Created by liuyangren on 2017-08-31.
 */

public class DetailActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        TextView pidTextView = (TextView) findViewById(R.id.pid);
        Button killButton = (Button) findViewById(R.id.kill_button);
        Intent intent = getIntent();

        pidTextView.setText("PID = " + intent.getStringExtra(ListAdapter.PID));
    }
}
