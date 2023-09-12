package com.example.randomchat.Logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;

import com.example.randomchat.GUI.Chat;
import com.example.randomchat.GUI.SplashPage;
import com.example.randomchat.GUI.Stanze;
import com.example.randomchat.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Controller {

    private static Socket socket;
    private static int port;
    private static PrintWriter writer; // per scrivere sulla socket
    private static String nickname;
    private static BufferedInputStream in; // per leggere dalla socket
    private static final String IPSERVER = "35.180.192.103";
    private static final String IPSERVERPAOLO = "172.31.95.164";
    private static String lastClient = "NESSUNO";
    private static final int PORTGIOCHI = 10000;
    private static final int PORTMUSICA = 10001;
    private static final int PORTLIBRI = 10002;
    private static final int PORTSPORT = 10003;
    // Main -> Stanze
    public static void entraPremuto(SplashPage splashPage, String nick) {
        Intent i = new Intent(splashPage, Stanze.class);
        splashPage.startActivity(i);
        nickname = nick;
    }

    // Stanze -> Chat
    public static void EntraStanzaPremuto(Stanze stanze, String stanza) {

        Log.d("Stanza", "Stanza selezionata: " + stanze.getString(R.string.giochi));

        if (stanze.getString(R.string.giochi).equals(stanza)) {
            port = PORTGIOCHI;
        } else if (stanze.getString(R.string.musica).equals(stanza)) {
            port = PORTMUSICA;
        } else if (stanze.getString(R.string.libri).equals(stanza)) {
            port = PORTLIBRI;
        } else if (stanze.getString(R.string.sport).equals(stanza)) {
            port = PORTSPORT;
        } else {
            Log.e("Stanza", "Errore stanza selezionata");
            connectionError(stanze);
            return;
        }
        Intent i = new Intent(stanze, Chat.class);
        stanze.startActivity(i);
    }

    private static void connectionError(Activity stanze) {
        stanze.runOnUiThread(()->{
            new AlertDialog.Builder(stanze)
                    .setMessage("Errore connessione al server")
                    .setNegativeButton("Ok", (dialog, id)->
                    {
                        dialog.dismiss();
                    }).create().show();
        });
    }

    public static void inviaMessaggio(String messaggio) {
        // Invia messaggio alla socket
        Log.d("Socket-write", "Scrivo sul socket: " + messaggio);
        new Thread(()->{
            writer.flush();
            writer.println(messaggio);
        }).start();
    }

    public static void avviaChat(Chat chat) {
        new Thread(()->{
            try{
                // Metto il client in ascolto
                char c;
                StringBuilder stringBuilder = new StringBuilder();
                while((c = (char)in.read()) != '\0' && in.available() > 0){
                    Log.d("Nickname", "char nick: " + c);
                    stringBuilder.append(c);
                }
                // La prima nickname letta è il nickname ricevuto dal server
                String nickname = stringBuilder.toString();
                lastClient = nickname;
                chat.aggiornaTestoPulsante("Stop?");
                chat.abilitaPulsanteCerca(true);
                chat.aggiornaNicknameServer(nickname);
                chat.abilitaChatbox(true);
                chat.abilitaPulsantiChat(true);

                Log.d("Socket-Read", "Nickname client connesso: " + nickname);
                Log.d("Socket-Read", "Avvio chat");
                chat.avviaTimer();
                while (true){
                    Log.d("Socket-Read", "Sono in attesa di un messaggio");
                    stringBuilder.setLength(0);
                    while((c = (char)in.read()) != '\0' && in.available() > 0){
                        Log.d("Messaggio", "char: " + c);
                        stringBuilder.append(c);
                    }
                    if(stringBuilder.length() > 0){
                        if(stringBuilder.toString().contains("/END")){
                            Log.d("Messaggio", "Stringa: " + stringBuilder.toString());
                            chat.terminaChat();
                        }
                        else
                            chat.creaViewMessaggio(stringBuilder.toString(), Gravity.START);
                    }
                }
            } catch (IOException e) {
                chat.terminaChat();
                terminaConnessioneScoket();
                Log.d("Socket-Read", "L'utente si è disconnesso");
                e.printStackTrace();
            }
        }).start();
    }
    public static void terminaConnessioneScoket(){
        try {
            if(writer != null) writer.close();
            if(in != null) in.close();
            if(socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void avviaConnessioneSocket(Chat chat) {
        new Thread(()->{
            try {
                //socket = new Socket(IPSERVER, port);
                Log.d("Client-Server", "Mi connetto al socket ");
                socket = new Socket();
                socket.connect(new InetSocketAddress(IPSERVER,port), 5000);
                in = new BufferedInputStream(socket.getInputStream());
                OutputStream output = socket.getOutputStream();
                writer = new PrintWriter(output, true);
                // Invio il nickname del client e last client al server
                Log.d("LAST CLIENT", lastClient);
                writer.println(nickname+"@"+lastClient);
                Log.d("STRINGA FORMATTATA", nickname+"@"+lastClient);
                chat.creaViewMessaggio("Sto cercando qualcuno. . .", Gravity.CENTER_HORIZONTAL);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                avviaChat(chat);
            }
            catch (IOException e) {
                terminaConnessioneScoket();
                Log.e("Socket-connection", "Errore connessione alla socket");
                connectionError(chat);
                chat.abilitaPulsanteCerca(true);
            }
        }).start();
    }
}