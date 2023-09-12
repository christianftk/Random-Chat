package com.example.randomchat.GUI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;

import com.example.randomchat.Logic.Controller;
import com.example.randomchat.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class Stanze extends AppCompatActivity {

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stanze);

        // Personalizzo le stanze
        View stanzaGiochi = findViewById(R.id.StanzaGiochi);
        creaStanzaView(stanzaGiochi, R.drawable.stanze_giochi, R.string.giochi);
        View stanzaMusica = findViewById(R.id.StanzaMusica);
        creaStanzaView(stanzaMusica, R.drawable.stanze_musica, R.string.musica);
        View stanzaLibri = findViewById(R.id.StanzaLibri);
        creaStanzaView(stanzaLibri, R.drawable.stanze_libri, R.string.libri);
        View stanzaSport = findViewById(R.id.StanzaSport);
        creaStanzaView(stanzaSport, R.drawable.stanze_sport, R.string.sport);

    }

    private void creaStanzaView(View stanza, int idDrawable, int idStringText){
        // Immagine stanza
        ((ImageView)stanza.findViewById(R.id.imageView)).setImageDrawable(ContextCompat.getDrawable(this,idDrawable));

        // Nome stanza
        MaterialTextView nomeStanza = stanza.findViewById(R.id.nomestanza);
        nomeStanza.setText(idStringText);

        // onClick pulsante stanza
        MaterialButton pulsante = stanza.findViewById(R.id.entrachat);
        Stanze context = this;
        pulsante.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Stanza Selezionata", getApplicationContext().getString(idStringText));
            editor.apply();
            Controller.EntraStanzaPremuto(context, getApplicationContext().getString(idStringText));
        });
    }
}