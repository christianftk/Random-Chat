package com.example.randomchat.GUI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.randomchat.Logic.Controller;
import com.example.randomchat.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity implements RecognitionListener {

    private EditText chatbox;
    private LinearLayout linearLayout;
    private NestedScrollView nestedScrollView;
    private MaterialTextView server;
    private MaterialButton pulsante;
    private long mLastClickTime = 0;
    private PrintWriter writer; // scrittura su socket
    private Socket socket;
    private BufferedInputStream in;
//    public final static String STARTCHAT = "/START";
    public final static String ENDCHAT = "/END";
//    public final static String ENDCONNECTION = "/DISCONNECT";
    private MaterialTextView timerView;
    private CountDownTimer timer = null;
    private final String INITIALTIMER = "01:00";
    private CircleImageView mic;
    private CircleImageView send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mic = findViewById(R.id.mic);
        send = findViewById(R.id.inviaButton);
        nestedScrollView = findViewById(R.id.scrollView);
        chatbox = (EditText) findViewById(R.id.chatBox);
        server = findViewById(R.id.server);
        pulsante = findViewById(R.id.cercaPulsante);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        abilitaChatbox(false);
        abilitaPulsantiChat(false);
        timerView = findViewById(R.id.timer);
        TextView edit_client = findViewById(R.id.utente);
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
        String chatStanza = sharedPreferences.getString("Stanza Selezionata", "App");
        int chatBackground;
        switch (chatStanza){
            case "Giochi":
                chatBackground = R.drawable.chat_giochi;
                break;
            case "Musica":
                chatBackground = R.drawable.chat_musica;
                break;
            case "Libri":
                chatBackground = R.drawable.chat_libri;
                break;
            case "Sport":
                chatBackground = R.drawable.chat_sport;
                break;
            default:
                chatBackground = R.drawable.app_background;
        }
        ConstraintLayout layout = findViewById(R.id.background);
        Drawable bg = ResourcesCompat.getDrawable(getResources(), chatBackground,null);
        if (bg != null) {
            bg.setAlpha(125);
        }
        layout.setBackground(bg);
        String nickname = sharedPreferences.getString("Nickname", "Nick");
        edit_client.setText(nickname);

        mic.setOnClickListener(v -> {
            SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(this);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "Parla");
            try {
                startActivityForResult(intent, 1000);

            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1000:{
                if(resultCode == RESULT_OK && null != data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    chatbox.setText(result.get(0));
                }
                break;
            }
        }
    }

    public void InviaPremuto(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        if(!chatbox.getText().toString().isEmpty()){
            Log.d("Client-Server", "Invio messaggio alla socket");
            creaViewMessaggio(chatbox.getText().toString(), Gravity.END);
            Controller.inviaMessaggio(chatbox.getText().toString());
            chatbox.setText("");
            nestedScrollView.scrollTo(0,60);
        }
    }

    public void creaViewMessaggio(String mex, int GRAVITY) {
        runOnUiThread(()->{
            TextView messaggio = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(16,0,16,12);
            params.gravity = GRAVITY;
            messaggio.setLayoutParams(params);
            messaggio.setMaxWidth(700);
            messaggio.setPadding(24,12,24,12);
            messaggio.setTextSize(20.0f);
            messaggio.setText(mex);

            switch (GRAVITY){
                case Gravity.START:
                    messaggio.setBackground(ContextCompat.getDrawable(this,R.drawable.message_left));
                    messaggio.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    break;
                case Gravity.END:
                    messaggio.setBackground(ContextCompat.getDrawable(this,R.drawable.message_right));
                    messaggio.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    messaggio.setBackground(ContextCompat.getDrawable(this,R.drawable.message_center));
                    messaggio.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    messaggio.setTextSize(14.0f);
                    break;
                default:
                    Log.e("Messaggio", "Errore background messaggio");
            }
            linearLayout.addView(messaggio);
        });
    }

    public void nuovaChatpremuto(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (pulsante.getText().toString()){
            case "Cerca":
                linearLayout.removeAllViews();
                pulsante.setEnabled(false);
                Controller.avviaConnessioneSocket(this);
                break;
            case "Stop?":
                aggiornaTestoPulsante("Sicuro?");

                break;
            case "Sicuro?":
                Controller.inviaMessaggio(ENDCHAT);
                terminaChat();
                Controller.terminaConnessioneScoket();
                break;
        }
    }

    public void terminaChat() {
        runOnUiThread(()->{
            chatbox.setText("");
            Controller.inviaMessaggio(ENDCHAT);
            aggiornaTestoPulsante("Cerca");
            abilitaChatbox(false);
            abilitaPulsantiChat(false);
            creaViewMessaggio("La chat è terminata", Gravity.CENTER_HORIZONTAL);
            server.setText("Server");
            cancellaTimer();
        });
    }

    public void aggiornaNicknameServer(String nickname) {
        runOnUiThread(()->{
            linearLayout.removeAllViews();
            server.setText(nickname);
            server.postInvalidate();
            creaViewMessaggio("Stai chattando con " + nickname, Gravity.CENTER_HORIZONTAL);
        });
    }

    public void abilitaChatbox(boolean enabled) {
        runOnUiThread(()->{
            chatbox.setEnabled(enabled);
        });
    }

    public void abilitaPulsanteCerca(boolean enabled) {
        runOnUiThread(()->{
            pulsante.setEnabled(enabled);
        });
    }

    public void abilitaPulsantiChat(boolean enabled) {
        runOnUiThread(()->{
            mic.setEnabled(enabled);
            send.setEnabled(enabled);
        });
    }

    public void aggiornaTestoPulsante(String messaggio) {
        runOnUiThread(()->{
            pulsante.setText(messaggio);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Controller.inviaMessaggio(ENDCHAT);
        cancellaTimer();
        Controller.terminaConnessioneScoket();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Controller.inviaMessaggio(ENDCHAT);
        Controller.terminaConnessioneScoket();
        this.finish();
    }

    // Avvia il timer
    public void avviaTimer() {
        runOnUiThread(()->{
            timerView.setText(INITIALTIMER);
            timer = new CountDownTimer(60000, 1000) {
                public void onTick(long millisMancanti) {
                    timerView.setText(aggiornaTimer(millisMancanti));
                }
                public void onFinish() {
                    terminaChat();
                }
            };
            timer.start();
        });
    }

    private String aggiornaTimer(long millisMancanti) {
        int minuti = (int) millisMancanti / 60000;
        int sec = (int) (millisMancanti % 60000) / 1000;

        String newTime = "0" + minuti + ":";
        if(sec < 10) newTime += "0";
        newTime += sec;

        return newTime;
    }

    // Cancella timer
    void cancellaTimer() {
        if(timer!=null)
            runOnUiThread(()->{
                timer.cancel();
            });
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}