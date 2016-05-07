package fictionalpancake.turbospork.android;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import fictionalpancake.turbospork.GameHandler;
import fictionalpancake.turbospork.RoomInfoListener;

public class MainActivity extends AppCompatActivity implements RoomInfoListener {

    private GameHandler gameHandler;
    private ArrayAdapter<String> listModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameHandler = LoginActivity.lastGameHandler;
        gameHandler.setRoomInfoListener(this);
        listModel = new PlayerColorArrayAdapter<String>(this, android.R.layout.test_list_item);
        ((ListView) findViewById(R.id.userList)).setAdapter(listModel);
        updateButtonState();
    }

    private void updateButtonState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((Button) findViewById(R.id.joinButton)).setText(gameHandler.getUsers().size() == 0 ? R.string.joinRoom : R.string.switchRoom);
                findViewById(R.id.startButton).setEnabled(gameHandler.getPosition() == 0 && !gameHandler.isMatchMeRoom() && !gameHandler.hasGameData());
            }
        });
    }

    @Override
    public void onLeftRoom(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (s.equals(gameHandler.getUserID())) {
                    listModel.clear();
                }
                else {
                    listModel.remove(s);
                }
                updateButtonState();
            }
        });
    }

    @Override
    public void onJoinedRoom(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listModel.add(s);
            }
        });
        updateButtonState();
    }

    @Override
    public void onGameStart() {

    }

    @Override
    public void onGameEnd() {

    }

    @Override
    public void onChat(String s, String s1) {

    }

    @Override
    public void onError(String s) {
        System.err.println(s);
    }

    public void onClick(View view) {
        if(view.getId() == R.id.joinButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.joinPrompt);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println("joining");
                    gameHandler.join(String.valueOf(input.getText()));
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
        else if(view.getId() == R.id.startButton) {
            gameHandler.startGame();
        }
        else if(view.getId() == R.id.playButton) {
            gameHandler.join("matchme");
        }
    }

    private class PlayerColorArrayAdapter<T> extends ArrayAdapter<String> {
        public PlayerColorArrayAdapter(Context context, int layout_type) {
            super(context, layout_type);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View tr = super.getView(position, convertView, parent);
            ((TextView) tr).setTextColor(GameColors.getColorForOwner(gameHandler.adjustForRemoved(position)));
            ((TextView) tr).setSingleLine();
            return tr;
        }
    }
}
