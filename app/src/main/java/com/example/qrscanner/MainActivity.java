package com.example.qrscanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    ArrayList<String> data;
    Adapter adapter;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        data = new ArrayList<>();

        adapter = new Adapter(this,data);
        recyclerView.setAdapter(adapter);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        if(mAuth.getCurrentUser() != null){
            progressBar.setVisibility(View.VISIBLE);
            EventChangeListener();
        }else{
            startActivity(new Intent(this,LoginActivity.class));
        }


    }

    private void EventChangeListener() {
        firestore.collection(mAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            progressBar.setVisibility(View.GONE);
                            return;
                        }
                        for(DocumentChange dc: value.getDocumentChanges()){
                            if(dc.getType() == DocumentChange.Type.ADDED){
                                //data.add(dc.getDocument().toObject(DataModel.class));
                                data.add(dc.getDocument().getData().toString());
                            }
                            progressBar.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void ScanQR(){
        //Initializing barcode here with some options
        ScanOptions options = new ScanOptions();
        options.setPrompt("Press Volume up to turn on flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);

        options.setCaptureActivity(CaptureClass.class);
        barLauncher.launch(options);

    }
    private void SendDataToBackEnd(String value){
        Map<String,Object> data = new HashMap<>();
        data.put("qrvalue",value);

        firestore.collection(mAuth.getCurrentUser().getUid())
                .add(data)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                        ScanQR();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        ScanQR();
                    }
                });
    }

    //QRScanner Implementation with response handling
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
       if(result.getContents() != null){

           if(mAuth.getCurrentUser() == null){
               startActivity(new Intent(this,LoginActivity.class));
           }else{
              // checking the connection here to safely transfer data to database
               if(checkConnection()){
                   SendDataToBackEnd(result.getContents());
               }else{
                   Toast.makeText(this, "You're not connected to internet", Toast.LENGTH_SHORT).show();
               }
           }
       }
    });



    private boolean checkConnection() {

        // initialize intent filter
        IntentFilter intentFilter = new IntentFilter();

        // add action
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE");

        // register receiver
        registerReceiver(new ConnectionReceiver(), intentFilter);

        // Initialize listener
        ConnectionReceiver.Listener = this;

        // Initialize connectivity manager
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize network info
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        // get connection status
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        return isConnected;

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Checking if the user is logged in or not
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(this,LoginActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            finish();
        }

        if(id == R.id.scanner){
            ScanQR();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        if(isConnected){
            Toast.makeText(this, "Connected to internet!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "No active internet connection", Toast.LENGTH_SHORT).show();
        }
    }
}