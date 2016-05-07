package fictionalpancake.turbospork.android;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.NotYetConnectedException;

import fictionalpancake.turbospork.DataListener;
import fictionalpancake.turbospork.GameConstants;
import fictionalpancake.turbospork.GameHandler;

public class LoginActivity extends AppCompatActivity {

    public static GameHandler lastGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onClick(final View view) {
        final Activity self = this;
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            public void run() {
                try {
                    lastGameHandler = new GameHandler(new DataListener<String>() {
                        @Override
                        public void onData(String data) {
                            int colonPos = data.indexOf(':');
                            String firstPart = data.substring(0, colonPos);
                            String lastPart = data.substring(colonPos + 1);
                            if (firstPart.equals("join")) {
                                Intent intent = new Intent(self, MainActivity.class);
                                startActivity(intent);
                            } else {
                                String error = "Could not parse response";
                                if (firstPart.equals("error")) {
                                    error = lastPart;
                                }
                                showError(error);
                            }
                        }
                    }, new URI("ws://turbo-spork-test.herokuapp.com"));
                    lastGameHandler.connectBlocking();
                    String authMsg = "auth:";
                    if (view.getId() != R.id.buttonGuest) {
                        authMsg += ((TextView) findViewById(R.id.username)).getText() + ":" + ((TextView) findViewById(R.id.password)).getText() + ":";
                    }
                    authMsg += GameConstants.PROTOCOL_VERSION;
                    lastGameHandler.send(authMsg);
                } catch (URISyntaxException | InterruptedException e) {
                    e.printStackTrace();
                    showError("Unexpected error");
                } catch (NotYetConnectedException e) {
                    showError("Could not connect to server");
                }
            }
        }).start();
    }

    private void showError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.errorText)).setText(error);
            }
        });
    }
}
