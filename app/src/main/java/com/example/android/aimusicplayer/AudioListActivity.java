package com.example.android.aimusicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AudioListActivity extends AppCompatActivity {

    private String[] itemsAll;
    private int searchPosition = 0 ;

    private ListView listView;

    private ArrayAdapter<String> arrayAdapter;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private  ArrayList<File> audioSongs;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_list);

        speechRecognizer = speechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        checkVoiceCommandPermissions();

        listView = findViewById(R.id.songsList);
        listView.setTextFilterEnabled(true);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        appExternalStoragePermission();

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                searchPosition = 0;
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matchesFound = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matchesFound != null) {
                    keeper = matchesFound.get(0);

                    /*String strArray[] = keeper.split(" ");
                    String play = strArray[0];
                    String keyword = "";
                    for(int k=1; k<strArray.length;k++){
                        keyword = keyword + " " + strArray[k];
                    }
                    Log.e("Message######", play);*/

                    Toast.makeText(AudioListActivity.this, keeper, Toast.LENGTH_SHORT).show();

                    for (int i=0; i<audioSongs.size(); i++) {
                        if(itemsAll[i].toUpperCase().contains(keeper.toUpperCase())){
                            searchPosition = i;
                            Log.e("Message _______", itemsAll[i]);
                            break;
                        }

                    }
                    if(searchPosition != 0){
                        final String songName = listView.getItemAtPosition(searchPosition).toString();

                        Intent intent = new Intent(AudioListActivity.this, MainActivity.class);
                        intent.putExtra("song", audioSongs);
                        intent.putExtra("name", songName);
                        intent.putExtra("position", searchPosition);

//                        tts = new TextToSpeech(AudioListActivity.this, new TextToSpeech.OnInitListener() {
//                            @Override
//                            public void onInit(int status) {
//                                if (status == TextToSpeech.SUCCESS) {
//                                    String textToSay = "Playing " + songName;
//                                    tts.speak(textToSay, TextToSpeech.QUEUE_ADD, null);
//                                }
//                            }
//                        });

                        searchPosition = 0;

                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(AudioListActivity.this, "Song not found", Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_search) {
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
            speechRecognizer.startListening(speechRecognizerIntent);
            keeper = "";
            return true;
        }

        if (id == R.id.menu_refresh) {
            Toast.makeText(this, "Please wait crunching data", Toast.LENGTH_SHORT).show();
            displayAudioNames();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void appExternalStoragePermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        displayAudioNames();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> readOnlyAudioSongs(File file) {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] allFiles = file.listFiles();

        for (File individualFile : allFiles) {
            if (individualFile.isDirectory() && !individualFile.isHidden()) {
                arrayList.addAll(readOnlyAudioSongs(individualFile));
            } else {
                if (individualFile.getName().endsWith(".mp3") || individualFile.getName().endsWith(".aac")
                        || individualFile.getName().endsWith(".wav") || individualFile.getName().endsWith(".wma")) {
                    arrayList.add(individualFile);
                }
            }
        }

        return arrayList;
    }

    private void displayAudioNames() {
        audioSongs = readOnlyAudioSongs(Environment.getExternalStorageDirectory());
        itemsAll = new String[audioSongs.size()];

        for (int songCounter = 0; songCounter < audioSongs.size(); songCounter++) {
            itemsAll[songCounter] = audioSongs.get(songCounter).getName();
        }

        arrayAdapter = new ArrayAdapter<String>(AudioListActivity.this, android.R.layout.simple_list_item_1, itemsAll);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = listView.getItemAtPosition(i).toString();

                Intent intent = new Intent(AudioListActivity.this, MainActivity.class);
                intent.putExtra("song", audioSongs);
                intent.putExtra("name", songName);
                intent.putExtra("position", i);
                startActivity(intent);
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                speechRecognizer.stopListening();
                return false;
            }
        });

    }

    private void checkVoiceCommandPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(AudioListActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

}
