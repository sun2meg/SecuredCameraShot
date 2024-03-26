package com.megacoreapps.sun2meg.securedcamerashot;

//import android.support.v7.app.AppCompatActivity;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import net.steamcrafted.loadtoast.LoadToast;

import javax.annotation.Nullable;

public class GoogleDriveActivity extends AppCompatActivity
            implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static final int REQUEST_CODE_PERMISSIONS = 1;
        private static final int REQUEST_CODE_CAPTURE_VIDEO = 2;
    private static final int REQUEST_PERMISSIONS = 4;
        private GoogleDriveHelper googleDriveHelper;
//        private String videoFilePath;
//    private DriveResourceClient driveResourceClient;
    private GoogleApiClient googleApiClient;
    private String capturedVideoFilePath; // Initialize this
    GoogleSignInAccount signInAccount;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    static GoogleDriveServiceHelper mDriveServiceHelper;
    static String folderId="";
    LoadToast loadToast;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_google_drive);
            requestPermissionsIfNeeded();
            googleDriveHelper = new GoogleDriveHelper(this, mainHandler);
//            googleDriveHelper.signIn();
            loadToast = new LoadToast(this);
            // Initialize DriveResourceClient
//            signInAccount = googleDriveHelper.getSignInAccount();
//            GoogleSignInAccount signInAccount = googleDriveHelper.getSignInAccount();
//            if (signInAccount != null) {
//                driveResourceClient = Drive.getDriveResourceClient(this, signInAccount);
//                // Continue with the rest of your code that uses driveResourceClient
//            } else {
//                Toast.makeText(getApplicationContext(), "Not Signed successfully", Toast.LENGTH_SHORT).show();
//                googleDriveHelper.signIn();
//                // Handle the case where the user is not signed in
//                // For example, you can prompt the user to sign in or handle it based on your app's logic
//            }


            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();

        }


    private void requestPermissionsIfNeeded() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    showPermissionDeniedDialog();
                    return;
                }
            }
        }
    }


    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage("Some permissions were denied. The app may not function properly without them.")
                .setPositiveButton("OK", null)
                .show();
    }

    // This method will get call when user click on create folder button
    public void createFolder2(View view) {
        if (mDriveServiceHelper != null) {

            // check folder present or not
            mDriveServiceHelper.isFolderPresent()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
                        @Override
                        public void onSuccess(String id) {
                            if (id.isEmpty()){
                                mDriveServiceHelper.createFolder()
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String fileId) {
                                                Log.e(TAG, "folder id: "+fileId );
                                                folderId=fileId;
                                                showMessage("Folder Created with id: "+fileId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception exception) {
                                                showMessage("Couldn't create file.");
                                                Log.e(TAG, "Couldn't create file.", exception);
                                            }
                                        });
                            }
//                            else {
//                                folderId=id;
//                                showMessage("Folder already present");
//                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception exception) {
                            showMessage("Couldn't create file..");
                            Log.e(TAG, "Couldn't create file..", exception);
                        }
                    });
        }
    }

    public void createFolder() {
        if (mDriveServiceHelper != null) {

            // check folder present or not
            mDriveServiceHelper.isFolderPresent()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String id) {
                            if (id.isEmpty()){
                                mDriveServiceHelper.createFolder()
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String fileId) {
                                                Log.e(TAG, "folder id: "+fileId );
                                                folderId=fileId;
                                                showMessage("Folder Created with id: "+fileId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception exception) {
                                                showMessage("Couldn't create file.");
                                                Log.e(TAG, "Couldn't create file.", exception);
                                            }
                                        });
                            }else {
                                folderId=id;
                                showMessage("Folder already present");
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception exception) {
                            showMessage("Couldn't create file..");
                            Log.e(TAG, "Couldn't create file..", exception);
                        }
                    });
        }
    }

    public void captureVideo(View view) {
        googleDriveHelper.signIn();
        signInAccount = googleDriveHelper.getSignInAccount();
        if (signInAccount != null) {
            createFolder();
//            googleDriveHelper.driveResourceClient = Drive.getDriveResourceClient(this, signInAccount);
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO);
            }
            // Continue with the rest of your code that uses driveResourceClient
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed successfully", Toast.LENGTH_SHORT).show();
            googleDriveHelper.signIn();
            signInAccount = googleDriveHelper.getSignInAccount();
            // Handle the case where the user is not signed in
            // For example, you can prompt the user to sign in or handle it based on your app's logic
        }

    }

//    public void captureVideo(View view) {
//        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO);
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GoogleDriveHelper.REQUEST_CODE_SIGN_IN) {
            googleDriveHelper.handleSignInResult(data);
        } else if (requestCode == REQUEST_CODE_CAPTURE_VIDEO && resultCode == RESULT_OK) {
            // Get the Uri of the selected file

            loadToast.setText("Uploading file...");
            loadToast.show();
            Uri selectedFileUri = data.getData();
            Log.e(TAG, "selected File Uri: "+selectedFileUri );
            // Get the path
            String selectedFilePath = FileUtils.getPath(this, selectedFileUri);
            Log.e(TAG,"Selected File Path:" + selectedFilePath);
//            if(selectedFilePath != null && !selectedFilePath.equals("")){
//                if (mDriveServiceHelper != null) {
//                    mDriveServiceHelper.uploadFileToGoogleDrive(selectedFilePath)
//                            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
//                                @Override
//                                public void onSuccess(Boolean result) {
//                                    loadToast.success();
//                                    showMessage("File uploaded ...!!");
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(Exception e) {
//                                    loadToast.error();
//                                    showMessage("Couldn't able to upload file, error: "+e);
//                                }
//                            });
//                }
//            }else{
//                Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
//            }

            if (  googleDriveHelper.driveResourceClient != null && googleApiClient != null) {
                // Retrieve the captured video's Uri from the Intent
                Uri capturedVideoUri = data.getData();
                // Convert the Uri to a file path using a helper method
                capturedVideoFilePath = getRealPathFromURI(capturedVideoUri);
                // Call the uploadVideo() method from GoogleDriveHelper
                googleDriveHelper.uploadVideo(capturedVideoFilePath, googleDriveHelper.driveResourceClient, googleApiClient);
            } else {
                Toast.makeText(this, "Drive resource client or Google API client is null", Toast.LENGTH_SHORT).show();
            }

//            googleDriveHelper.uploadVideo(capturedVideoFilePath, driveResourceClient, googleApiClient);
        }

//
//        if (requestCode == GoogleDriveHelper.REQUEST_CODE_SIGN_IN) {
//            googleDriveHelper.handleSignInResult(data);
//
//            // Initialize DriveResourceClient
//            GoogleSignInAccount signInAccount = googleDriveHelper.getSignInAccount();
////            if (signInAccount != null) {
////                driveResourceClient = Drive.getDriveResourceClient(this, signInAccount);
////            } else
////                Toast.makeText(getApplicationContext(), "Not Signed In", Toast.LENGTH_SHORT).show();
//
//        } else if (requestCode == REQUEST_CODE_CAPTURE_VIDEO && resultCode == RESULT_OK) {
//            // Retrieve the captured video's Uri from the Intent
//            Uri capturedVideoUri = data.getData();
//            // Convert the Uri to a file path using a helper method
//            capturedVideoFilePath = getRealPathFromURI(capturedVideoUri);
//            // Call the uploadVideo() method from GoogleDriveHelper
//            googleDriveHelper.uploadVideo(capturedVideoFilePath, driveResourceClient, googleApiClient);
//        }
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "GoogleApiClient connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "GoogleApiClient connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "GoogleApiClient connection failed", Toast.LENGTH_SHORT).show();
    }

    public void showMessage(String message) {
        Log.i(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    }
