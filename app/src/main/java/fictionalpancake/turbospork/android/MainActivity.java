package fictionalpancake.turbospork.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import fictionalpancake.turbospork.GameHandler;

public class MainActivity extends AppCompatActivity {

    private GameHandler gameHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameHandler = LoginActivity.lastGameHandler;
        TextView tv = (TextView) findViewById(R.id.textView2);
        tv.setText("Welcome, "+gameHandler.getUserID()+", to the absence of a game!");
    }
}
