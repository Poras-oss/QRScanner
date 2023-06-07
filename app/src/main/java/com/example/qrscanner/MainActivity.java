package com.example.qrscanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        ScanQR();
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

    //Barcode Implementation with response handling
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
       if(result.getContents() != null){

           if(mAuth.getCurrentUser() == null){
               startActivity(new Intent(this,LoginActivity.class));
           }else{
               SendDataToBackEnd(result.getContents());
           }
       }
    });

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
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "Back Pressed", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.entries){
            Toast.makeText(this, "show entries", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}