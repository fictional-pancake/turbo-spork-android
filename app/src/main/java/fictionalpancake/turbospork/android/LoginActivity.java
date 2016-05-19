package fictionalpancake.turbospork.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.NotYetConnectedException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Scanner;

import fictionalpancake.turbospork.DataListener;
import fictionalpancake.turbospork.GameConstants;
import fictionalpancake.turbospork.GameHandler;

public class LoginActivity extends AppCompatActivity {

    public static GameHandler lastGameHandler;
    private String uriBase;
    private JSONParser parser;
    private SharedPreferences tokens;

    public LoginActivity() {
        super();
        uriBase = BuildConfig.DEBUG?"://turbo-spork-test.herokuapp.com":"://turbo-spork.herokuapp.com";
        parser = new JSONParser();
        if(GameConstants.PROTOCOL_VERSION == 12) GameConstants.PROTOCOL_VERSION = 13;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tokens = getSharedPreferences("tokens", MODE_PRIVATE);
        if(tokens.contains(uriBase)) {
            login("auth:"+tokens.getString(uriBase, null)+":"+GameConstants.PROTOCOL_VERSION);
        }
    }

    public void onClick(final View view) {
        if (view.getId() == R.id.buttonGuest) {
            login("auth:"+GameConstants.PROTOCOL_VERSION);
        }
        else {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        URLConnection conn = new URL("http"+uriBase+"/loginaction").openConnection();
                        String charset = "UTF-8";
                        byte[] data = ("username="+ URLEncoder.encode(((EditText) findViewById(R.id.username)).getText().toString(), charset)+"&password="+URLEncoder.encode(((EditText) findViewById(R.id.password)).getText().toString(), charset)).getBytes(Charset.forName(charset));
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("charset", "utf-8");
                        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                        conn.setDoOutput(true);
                        conn.getOutputStream().write(data);
                        System.out.println("about to connect");
                        conn.connect();
                        System.out.println("connected");
                        Map<String,Object> obj = (Map<String, Object>) parser.parse(new InputStreamReader(conn.getInputStream()));
                        System.out.println("parsed");
                        if(obj.get("success").equals(true)) {
                            String token = obj.get("result").toString();
                            tokens.edit().putString(uriBase, token).commit();
                            login("auth:"+token+":"+GameConstants.PROTOCOL_VERSION);
                        }
                        else {
                            showError(obj.get("result").toString());
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                        showError("An unexpected error occurred.");
                    }
                }
            }).start();
        }
    }

    public void login(final String authMsg) {
        System.out.println(authMsg);
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
                    }, new URI("ws"+uriBase));
                    lastGameHandler.connectBlocking();
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
