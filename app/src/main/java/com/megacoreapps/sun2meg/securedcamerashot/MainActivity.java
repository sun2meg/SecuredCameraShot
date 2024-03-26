package com.megacoreapps.sun2meg.securedcamerashot;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.VideoCapture;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.PinManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int PICK_FILE_REQUEST = 100;
    private static final int REQUEST_CODE_CAPTURE_VIDEO = 2;
    private static final int REQUEST_AUTHORIZATION = 5;

    static GoogleDriveServiceHelper mDriveServiceHelper;
    static String folderId="";

    private Button resendAud;
    private Button signInButton;
    private Vibrator v;
    private Button folderFilesButton;

    private Button signOutButton;
    GoogleSignInClient googleSignInClient;
    LoadToast loadToast;
    private ProgressBar progressBar;
    private TextView progressText;
    private Uri capturedImageUri;
    private Uri capturedVideoUri;
    private boolean captureInProgress = false;
    private static final int REQUEST_ACCOUNT_HINT = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;
    private static final int REQUEST_VIDEO_CAPTURE = 1003;
    private static final int REQUEST_IMAGE_GALLERY = 1004;
    private static final int REQUEST_VIDEO_GALLERY = 1005;
    private static final int REQUEST_PERMISSIONS = 1006;

    private CardView btnResendImg;
    private CardView btnResendVid;
    private CardView btnSelectImage;
    private CardView btnSelectVideo;
    private CardView btnCaptureVideo;
    private CardView btnCaptureImage;
    //    private ImageView btnResendImg;
//    private ImageView btnResendVid;
//    private ImageView btnSelectImage;
//    private ImageView btnSelectVideo;
//    private ImageView btnCaptureVideo;
//    private ImageView btnCaptureImage;
    private String selectedFilePath;
    private String selectedFilePath2;
    private static final long NETWORK_CHECK_INTERVAL = 5000; // 5 seconds
    private static final long INITIAL_NETWORK_CHECK_DELAY = 0; // 0 milliseconds
    private static final String PREFS_NAME = "MyPrefsFile"; // SharedPreferences file name
    private static final String PREF_SIGNED_IN = "isSignedIn";
    private static final String PREF_ACCOUNT_NAME = "accountName";
//    private static final String PREF_PERM = "permission check";
    private boolean isSignedIn = false;
    private String accountName = "";
    private int backPressCount = 0;
    private GoogleApiClient googleApiClient;
    TextView userLogged;

    private ProgressDialog mProgressDialog;
    private String currentPhotoPath;
    private Uri photoURI;
    //    DriveResourceClient driveResourceClient;
    private Executor executor = Executors.newSingleThreadExecutor();
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    PreviewView previewView;

    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private DriveContents driveContents;
    private Bitmap imageBitmap;
    private File localFile;
private Boolean hasPermissions=false;
    private String[] requiredPermissions;
    private final Map<Integer, ActivityResultLauncher<Intent>> activityResultLaunchers = new HashMap<>();

    private MediaRecorder mRecorder;
    private Boolean isRecording = false;
    private File storageDir;
    private String mFileName;


    private static final String PREFS_NM = "MyPrefs";
    private static final String TIME_KEY = "chosenTime";
    SharedPreferences settings;
    private SharedPreferences preferences;
    private Menu menu;
    public static MainActivity activity;
    private boolean isPinSet;

    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    private long lastFailedAttemptTime = 0;
    private static final String LOCK_PREFS_NAME = "lock_prefs";
    private static final String LOCK_STATUS_KEY = "lock_status";
    private static final String FAILED_ATTEMPTS_KEY = "failed_attempts";
    private static final String LAST_FAILED_ATTEMPT_TIME_KEY = "last_failed_attempt_time";
    MenuItem changePinMenuItem;

    private static final long LOCKOUT_DURATION = 10 * 60 * 1000; // 10 minutes in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_camerax);
        setContentView(R.layout.activity_main);
        showProgressDialog();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                if (null != activeNetwork) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) { }
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) { }
                } else {
                    showSnackbar("Mobile network switched Off");
//                    Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                    startActivity(intent);
//                    finish();

                }
                handler.postDelayed(this, 10000);//freq
            }
        };
        handler.postDelayed(runnable, 1000); //duration to start
        activity = this;
        if (!AppOnTopPermissionHelper.checkSystemAlertWindowPermission(this)) {
            AppOnTopPermissionHelper.requestSystemAlertWindowPermission(this);
        } else {
            // Permission is already granted
            // Your code to draw over other apps or perform other actions
//            showMessage("Permission already granted");
        }

        if (!foregroundServiceRunning()) {
            startService();
        }

        // Start checking for network connectivity with a delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNetworkConnectivityWithTimeout();
            }
        }, INITIAL_NETWORK_CHECK_DELAY);


//// Check for permissions
//

// Check for permissions
        /////////////////////////////////////////////////////2

       settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isSignedIn = settings.getBoolean(PREF_SIGNED_IN, false);
        userLogged = findViewById(R.id.textVw);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Retrieve the chosen time and use it as needed
        String chosenTime = preferences.getString(TIME_KEY, "30 seconds");
        isPinSet = PinManager.isPinSet(this);
        signInButton = findViewById(R.id.id_sign_in);
        folderFilesButton = findViewById(R.id.id_folder_files);
        signOutButton = findViewById(R.id.id_sign_out);

        btnCaptureImage = findViewById(R.id.btn_capture_image);
        btnCaptureVideo = findViewById(R.id.btn_capture_video);
        btnResendImg = findViewById(R.id.btnResendImg);
        btnResendVid = findViewById(R.id.btnResendVid);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectVideo = findViewById(R.id.btnSelectVideo);

        loadToast = new LoadToast(this);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
//        progressBar.setMax(100);



        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        restoreLockStatus();
if (!hasPermissions) {
    chkPerm();
}
//        chkPerm();
        List<String> missingPermissions = new ArrayList<>();

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    missingPermissions.toArray(new String[0]),
                    REQUEST_PERMISSIONS);
        } else {
            // Permissions are already granted, proceed with capturing the image
//            startCamera();
            Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
            if (isSignedIn) {
                accountName = settings.getString(PREF_ACCOUNT_NAME, "");
                userLogged.setText("Logged as: "+ accountName);
                // User was previously signed in, so you can set up your app accordingly.
                GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
                if (googleAccount != null && googleAccount.getEmail().equals(accountName)) {
                    // Initialize mDriveServiceHelper using the existing GoogleSignInAccount
                    initializeDriveServiceHelper(googleAccount);
                } else {
                    // Handle the case where the stored accountName doesn't match the signed-in account.
                    // You may want to re-prompt the user to sign in.
                    requestSignIn();
                }

            } else {
                // User needs to sign in. Display the sign-in button or UI.
                requestSignIn();
                signInButton.setEnabled(true);
            }

        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes( com.google.android.gms.drive.Drive.SCOPE_FILE)
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionResult -> {
                    // Handle connection failure, if needed
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        ///////////////////////////////start

/////////////////////////////////////////////////////////////////////end
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
//                dispatchTakePictureIntent();
//                        captureImage();

            }
        });

        btnCaptureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//          dispatchTakeVideoIntent();
                captureVideo();
            }
        });
        btnResendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mDriveServiceHelper != null) {
                    if (capturedImageUri != null) {
                       sendImg();
                    } else {
                        Toast.makeText(MainActivity.this, "No media captured", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Not Signed in", Toast.LENGTH_SHORT).show();

                }

            }
        });

        btnResendVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mDriveServiceHelper != null) {
                    if (capturedVideoUri != null) {
                        resendVid(capturedVideoUri);
                    } else {
                        Toast.makeText(MainActivity.this, "No media captured", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Not Signed in", Toast.LENGTH_SHORT).show();

                }

            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDriveServiceHelper != null) {
                    selectImageFromGallery();
                } else {
                    Toast.makeText(getApplicationContext(), "Not Signed in", Toast.LENGTH_SHORT).show();

                }

            }
        });

        btnSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDriveServiceHelper != null) {
                    selectVideoFromGallery();
                } else {
                    Toast.makeText(getApplicationContext(), "Not Signed in", Toast.LENGTH_SHORT).show();

                }
            }
        });

        folderFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPinSet) {
                    showPinDialog();
                } else {
                    // PIN not set, proceed with action and set the PIN
                    setPin();
                }
            }
        });

    }

    private void chkPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions = new String[]{
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO};
            showMessage(String.valueOf(Build.VERSION.SDK_INT));
        } else {
            requiredPermissions =
                    new String[]{
                            Manifest.permission.GET_ACCOUNTS,
                            Manifest.permission.INTERNET,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    };
        }
    }
    private void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }
    private void showUIToast(String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    private void checkInternetConnectivity() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.google.com") // Use a reliable server URL
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Network request failed, indicating possible internet connectivity issues
                showSnackbar("Poor Network");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showMessage("Poor Network");
//                    }
//                });
//                showNetworkUnavailableMessage();
            }

            @Override
            public void onResponse(Call call, Response response) {
                // Network request successful, indicating internet connectivity
                if (response.isSuccessful()) {
                    // Internet is available
//                    showSnackbar("Good Network");
                } else {
                    // Internet is not available
                    // Network request failed, indicating possible internet connectivity issues
//                    showSnackbar("Poor Network");
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            showMessage("Poor Network");
//                        }
//                    });
//                    showNetworkUnavailableMessage();
                }
            }
        });
    }


    // Method to check network connectivity
    private void checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
            // Network is available
//            showNetworkAvailableMessage();
        } else {
            // Network is not available
            showNetworkUnavailableMessage();
        }
    }



    private void showNetworkUnavailableMessage() {
        // Handle network unavailable case
        // This method will be called if the network is unavailable
        Intent intent = new Intent(MainActivity.this, com.megacoreapps.sun2meg.securedcamerashot.NoInternetActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        Toast.makeText(getApplicationContext(), "Network outage", Toast.LENGTH_SHORT).show();
    }

    private void checkNetworkConnectivityWithTimeout() {
        checkInternetConnectivity();// detecting network outages
//        checkNetworkConnectivity();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNetworkConnectivityWithTimeout();
            }
        }, NETWORK_CHECK_INTERVAL); // Schedule the next check
    }

    private void checkAndRequestPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        List<String> requiredPermissionsList = new ArrayList<>(Arrays.asList(getRequiredPermissions()));
        if (Build.VERSION.SDK_INT >= 33) {
            String[] additionalPermissions = {
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };
            // Add additional permissions to the existing ones
            requiredPermissionsList.addAll(Arrays.asList(additionalPermissions));
//            requiredPermissions = appendArrays(getRequiredPermissions(), additionalPermissions);
        }
        requiredPermissions = requiredPermissionsList.toArray(new String[0]);

//        if (Build.VERSION.SDK_INT >= 33) {
//            String[] additionalPermissions = {
//                    Manifest.permission.READ_MEDIA_IMAGES,
//                    Manifest.permission.READ_MEDIA_VIDEO,
//                    Manifest.permission.READ_MEDIA_AUDIO
//            };
//
//            // Add additional permissions to the existing ones
//            requiredPermissions = appendArrays(getRequiredPermissions(), additionalPermissions);
//        } else {
//            requiredPermissions = getRequiredPermissions();
//        }


   for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    missingPermissions.toArray(new String[0]),
                    REQUEST_PERMISSIONS);
        } else {
            // Permissions are already granted, proceed with the app functionality
            handlePermissionsGranted();
        }
    }

    private String[] getRequiredPermissions() {
        return new String[]{
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }
    private String[] appendArrays(String[] array1, String[] array2) {
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    private void handlePermissionsGranted() {
//        Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
        hasPermissions = true;
        // Proceed with further initialization or functionality
        requestSignIn();
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (areAllPermissionsGranted(grantResults)) {
                // All permissions granted, proceed with capturing the image
                handlePermissionsGranted();
//                Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
//                requestSignIn();
            } else {
                // Permissions denied, show a message or handle accordingly
                Toast.makeText(this, "Permissions denied. Cannot capture .", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                requestForStoragePermission();

            }
        }

    }

    // Helper method to check if all requested permissions are granted
    private boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public void onRequestPermissionsResultx(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allPermissionsGranted = true;

            // Check if all requested permissions are granted
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All permissions are granted, proceed with the app functionality
                handlePermissionsGranted();
            } else {
                // Some or all permissions were denied
                Toast.makeText(this, "Permissions denied. App functionality may be limited.", Toast.LENGTH_SHORT).show();
                // Handle the case where permissions are denied
                hideProgressDialog();
                requestForStoragePermission();
            }
        }
    }

    public boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (com.megacoreapps.sun2meg.securedcamerashot.LockService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void startService() {


        Intent notificationIntent = new Intent(this, com.megacoreapps.sun2meg.securedcamerashot.LockService.class);
        notificationIntent.setAction("Start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(notificationIntent);
        } else
            ContextCompat.startForegroundService(this, notificationIntent);
    }

    private static final String KEY_IMAGE_URI = "image_uri";


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (capturedImageUri != null) {
            outState.putString(KEY_IMAGE_URI, capturedImageUri.toString());
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String savedImageUri = savedInstanceState.getString(KEY_IMAGE_URI);
        if (savedImageUri != null) {
            capturedImageUri = Uri.parse(savedImageUri);
        }
    }



   private void openCamera() {
       if (mDriveServiceHelper != null) {
           ContentValues values = new ContentValues();
//        values.put(MediaStore.Audio.Media.DATA,"ss");
        values.put(MediaStore.Images.Media.TITLE,"MegaCam");
        values.put(MediaStore.Images.Media.DESCRIPTION,"MegaCam Test");
    capturedImageUri =  getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
//       Intent camIntent =new Intent(MediaStore.ACTION_PICK_IMAGES);
        Intent camIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT,capturedImageUri);
        captureImageLauncher.launch(camIntent);
         } else {
           Toast.makeText(getApplicationContext(), "Not Signed in", Toast.LENGTH_SHORT).show();

       }
    }

    private String getFilePathFromUri(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);

        if (documentFile != null && documentFile.exists()) {
            return documentFile.getUri().getPath();
        } else {
            showMessage("doc is null");
            return null;
        }
    }



    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating video file", ex);
            }
            if (videoFile != null) {
                capturedVideoUri = FileProvider.getUriForFile(this, "com.android.sun2meg.securedcamerashots.fileprovider", videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedVideoUri);
                startActivityForResult(takeVideoIntent, REQUEST_CODE_CAPTURE_VIDEO);
            }
        }
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        if (!storageDir.exists()) {
            Toast.makeText(getApplicationContext(), "doesnt exist", Toast.LENGTH_SHORT).show();
            boolean mkdirsResult = storageDir.mkdirs();
            if (!mkdirsResult) {
                Toast.makeText(getApplicationContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                // Handle the case where directory creation failed
                Log.e("YourTag", "Failed to create directory");
            }
        }

        File videoFile = File.createTempFile(videoFileName, ".mp4", storageDir);
        selectedFilePath = videoFile.getAbsolutePath();
        return videoFile;
    }


    private void captureImage() {
        if (mDriveServiceHelper != null) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(this, "Error while creating image file.", Toast.LENGTH_SHORT).show();
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(this,
                            "com.android.sun2meg.securedcamerashots.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                    photoURI.getPath();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempImageFile(Bitmap bitmap) {
        File tempFile;
        try {
            // Get the cache directory
            File cacheDir = getCacheDir();

            // Create a temporary file with a unique name
            String fileName = "temp_image" + System.currentTimeMillis() + ".jpg";
            tempFile = new File(cacheDir, fileName);

            // Write the Bitmap to the file
            OutputStream os = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException("Error creating temp image file", e);
        }
        return tempFile;
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

//        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "images");

// Get the Downloads directory

//        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "images");
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "images");


// Check if the directory doesn't exist, create it
        if (!storageDir.exists()) {
            Toast.makeText(getApplicationContext(), "doesnt exist", Toast.LENGTH_SHORT).show();
            boolean mkdirsResult = storageDir.mkdirs();
            if (!mkdirsResult) {
                Toast.makeText(getApplicationContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                // Handle the case where directory creation failed
                Log.e("YourTag", "Failed to create directory");
            }
        }

//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        Toast.makeText(getApplicationContext(), currentPhotoPath, Toast.LENGTH_SHORT).show();

        return new File(storageDir, imageFileName + ".jpg");
//        return image;
    }

    private void captureImage0() {
//        signIn();
        if (mDriveServiceHelper != null) {
//            createFolder();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
            // Continue with the rest of your code that uses driveResourceClient
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed", Toast.LENGTH_SHORT).show();

        }

    }


    private void resendMedia(String filePath) {
        if (mDriveServiceHelper != null) {
            if (filePath != null && !filePath.equals("")) {
                loadToast.setText("Uploading file...");
                loadToast.show();

                mDriveServiceHelper.uploadFileToGoogleDrive(filePath, progressBar, progressText)
                        .addOnSuccessListener(result -> {
                            loadToast.success();
                            showMessage("File uploaded ...!!");
                        })
                        .addOnFailureListener(e -> {
                            loadToast.error();
                            showMessage("Couldn't able to upload file, error: " + e);
                        });
            } else {
                Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Not Signed successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectMediaFromGallery(int requestCode) {
        if (mDriveServiceHelper != null) {
            Intent intent;
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            } else {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            }
            startActivityForResult(intent, requestCode);
        }  else {
            Toast.makeText(this, "Not Signed successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private int getVideoQualityPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getInt("video_quality", 0); // Default to low quality if not found
    }



    public void captureVideo() {
//        signIn();
        if (mDriveServiceHelper != null) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // Set video quality (0 for low quality, 1 for high quality)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, getVideoQualityPreference());
//            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            if (intent.resolveActivity(getPackageManager()) != null) {
                captureVideoLauncher.launch(intent);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed in", Toast.LENGTH_SHORT).show();

        }

    }
    private void resendImg() {

        if(capturedImageUri != null && !capturedImageUri.equals("")){
//        if(selectedFilePath2 != null && !selectedFilePath2.equals("")){
            if (mDriveServiceHelper != null) {
                loadToast.setText("Uploading file...");
                loadToast.show();
                mDriveServiceHelper.uploadFileToGoogleDrivex(capturedImageUri,progressBar,progressText)
//                mDriveServiceHelper.UploadImage(localFile,progressBar,progressText)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                loadToast.success();
                                showMessage("File uploaded ...!!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                loadToast.error();
                                showMessage("Couldn't able to upload file, error: "+e);
                            }
                        });
            }
        }else{
            loadToast.error();
            Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
        }
    }

    private void resendVid(Uri uri) {


        loadToast.setText("Uploading file...");
        loadToast.show();

        if (capturedVideoUri != null) {
            if (mDriveServiceHelper != null) {
                mDriveServiceHelper.uploadFileToGoogleDrive3(capturedVideoUri, progressBar, progressText)
                        .addOnSuccessListener(uploadResult -> {
                            loadToast.success();
                            showMessage("File uploaded ...!!");
                        })
                        .addOnFailureListener(e -> {
                            loadToast.error();
                            showMessage("err: " + e);
                        });
            }
        } else {
            loadToast.error();
            Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
        }
    }


    public void sendVid(Uri uri) {

        loadToast.setText("Uploading file...");
        loadToast.show();

        // Get the Uri of the selected file
//        capturedVideoUri = uri;
        Log.e(TAG, "selected File Uri: " + capturedVideoUri);

        if (capturedVideoUri != null) {
            if (mDriveServiceHelper != null) {
                mDriveServiceHelper.uploadFileToGoogleDrive3(capturedVideoUri, progressBar, progressText)
                        .addOnSuccessListener(uploadResult -> {
                            loadToast.success();
                            showMessage("File uploaded ...!!");
                        })
                        .addOnFailureListener(e -> {
                            loadToast.error();
                            showMessage("err: " + e);
                        });
            }
        } else {
            loadToast.error();
            Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
        }

    }
    private void handleSelectedImage(Intent resultData) {
        if (resultData == null) {
            // Handle the case where resultData is null
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Result data is null", Toast.LENGTH_SHORT).show());
            return;
        }

        // Check if the resultData contains the path specified by MediaStore.EXTRA_OUTPUT
        String imagePath = resultData.getStringExtra(MediaStore.EXTRA_OUTPUT);

        if (imagePath != null && !imagePath.isEmpty()) {
            // Continue with your code to process the selected image
            loadToast.setText("Uploading file...");
            loadToast.show();

            // Use the imagePath as the selected file path
            selectedFilePath2 = imagePath;

            if (mDriveServiceHelper != null) {
                mDriveServiceHelper.uploadFileToGoogleDriveImg(selectedFilePath2, progressBar, progressText)
                        .addOnSuccessListener(result -> {
                            runOnUiThread(() -> {
                                loadToast.success();
                                showMessage("File uploaded ...!!");
                            });
                        })
                        .addOnFailureListener(e -> {
                            runOnUiThread(() -> {
                                loadToast.error();
                                showMessage("Couldn't be able to upload file, error: " + e);
                            });
                        });
            }
        } else {
            runOnUiThread(() -> {
                loadToast.error();
                Toast.makeText(MainActivity.this, "Selected image path is null or empty", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Helper method to get the file path from URI
    private String getPathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }



    public void sendImg() {
        loadToast.setText("Uploading file...");
        loadToast.show();

        // Use the file you provided to the gallery intent
        if (capturedImageUri != null) {
            // Continue with your code to upload or process the selected image
            if (mDriveServiceHelper != null) {
                mDriveServiceHelper.uploadFileToGoogleDrivex(capturedImageUri, progressBar, progressText)
//                mDriveServiceHelper.UploadImage(localFile, progressBar, progressText)
                        .addOnSuccessListener(result -> {
                            runOnUiThread(() -> {
                                loadToast.success();
                                showMessage("File uploaded ...!!");
                            });
                        })
                        .addOnFailureListener(e -> {
                            runOnUiThread(() -> {
                                loadToast.error();
                                showMessage("Couldn't be able to upload file, error: " + e);
                            });
                        });
            }
        } else {
            runOnUiThread(() -> {
                loadToast.error();
                Toast.makeText(MainActivity.this, "Selected image path is null or empty", Toast.LENGTH_SHORT).show();
            });
        }

    }


    // Read/Write permission
    private void requestForStoragePermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                        // Additional permissions for Android 13
//                        Manifest.permission.READ_MEDIA_IMAGES,
//                        Manifest.permission.READ_MEDIA_VIDEO,
//                        Manifest.permission.READ_MEDIA_AUDIO
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
//                            Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                            requestSignIn();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }


    private void showSplash() {
        Intent intent = new Intent(MainActivity.this,SplashActivity.class);
        startActivity(intent);

    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */

    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");
          showProgressDialog();
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .requestScopes( com.google.android.gms.drive.Drive.SCOPE_FILE)
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        signInLauncher.launch(googleSignInClient.getSignInIntent());
        // The result of the sign-in Intent is handled in onActivityResult.
//        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please wait!");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    private String getRealImgPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    // Helper method to get the real path from URI
    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    // Helper method to convert video URI to file path using FileProvider
    private String getRealPathFromURI0(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(columnIndex);
            } finally {
                cursor.close();
            }
        } else {
            // Use the provided URI directly if no cursor is available
            return uri.getPath();
        }
    }

    private void signIn() {
        try {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMessage(String.valueOf(e));
                }
            });
        }
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        capturedImageUri = data.getData();
//                        Uri selectedImageUri = data.getData();
                        sendImgx(capturedImageUri);
                    }
                }
            });


    public void sendImgx(Uri imageUri) {
        loadToast.setText("Uploading file...");
        loadToast.show();

        if (imageUri != null && mDriveServiceHelper != null) {
            mDriveServiceHelper.uploadFileToGoogleDrivex(imageUri,  progressBar, progressText)

//            mDriveServiceHelper.uploadFileToGoogleDrivex(getContentResolver().openInputStream(imageUri), "FileName", "image/jpeg")
                    .addOnSuccessListener(result -> {
                        runOnUiThread(() -> {
                            loadToast.success();
                            showMessage("File uploaded successfully" );
                        });
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() -> {
                            loadToast.error();
                            showMessage("error: " + e.getMessage());
                        });
                    });
        } else {
            runOnUiThread(() -> {
                loadToast.error();
                Toast.makeText(MainActivity.this, "Selected image URI is null", Toast.LENGTH_SHORT).show();
            });
        }
    }



    private final ActivityResultLauncher<Intent> videoSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Handle the selected video URI from the result
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        // Do something with the selected video URI
//
//                        loadToast.setText("Uploading file...");
//                        loadToast.show();
                        capturedVideoUri = data.getData();

                        Toast.makeText(MainActivity.this, "Video selected", Toast.LENGTH_SHORT).show();
                        sendVid(capturedVideoUri);

                    }
                }
            });

    private final ActivityResultLauncher<Intent> captureImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                //            result -> {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Audio recording was successful, now retrieve the audio file
                        try {
                            if (capturedImageUri != null) {
                                // Now you can use the filePath as the actual path to the audio file
                              sendImgx(capturedImageUri);
                                // Perform further actions with the audio file as needed
                            } else {
                               showMessage("image not captured");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Handle the case where audio recording was canceled or failed
                        showMessage("record cancelled or failed");
                    }
                }
            });


    private ActivityResultLauncher<Intent> captureVideoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) {
                        // No data present
                        return;
                    }

                    loadToast.setText("Uploading file...");
                    loadToast.show();

                    // Get the Uri of the selected file
                    capturedVideoUri = data.getData();
                    Log.e(TAG, "selected File Uri: " + capturedVideoUri);

                    if (capturedVideoUri != null) {
                        if (mDriveServiceHelper != null) {
                            mDriveServiceHelper.uploadFileToGoogleDrive3(capturedVideoUri, progressBar, progressText)
                                    .addOnSuccessListener(uploadResult -> {
                                        loadToast.success();
                                        showMessage("File uploaded ...!!");
                                    })
                                    .addOnFailureListener(e -> {
                                        loadToast.error();
                                        showMessage("err: " + e);
                                    });
                        }
                    } else {
                        loadToast.error();
                        Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    //RECENT RECOMM PRACTICE
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            handleSignInResult(result.getData());
                        }
//                        handleSignInResultx(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
                    } else {
                        showMessage(String.valueOf(result.getResultCode()));
                        showMessage("sign failure");
                        hideProgressDialog();
                        // Handle sign-in failure or cancellation
                        requestSignIn();
                    }
                }
            });

    private ActivityResultLauncher<Intent> authLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Handle the result of the authorization request

                    // User has resolved the authentication issue, retry the sign-in
                    showMessage("Authentication issue resolved. Retrying sign-in...");
                    requestSignIn();
                }
            });


    public void captureVideo(View view) {
//        signIn();
        if (mDriveServiceHelper != null) {

//            createFolder();
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // Set video quality (0 for low quality, 1 for high quality)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO);
            }
            // Continue with the rest of your code that uses driveResourceClient
        } else {
            Toast.makeText(getApplicationContext(), "Not Signed successfully", Toast.LENGTH_SHORT).show();

        }

    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void initializeDriveServiceHelper(GoogleSignInAccount googleAccount) {
        if (googleAccount == null) {
            Log.e(TAG, "GoogleSignInAccount is null. Unable to initialize Drive service.");
            return;
        }

        try {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
//                        this, Collections.singleton(DriveScopes.DRIVE)); // broader scope that encompasses ALL
                    this, Collections.singleton("https://www.googleapis.com/auth/drive.file"));
            credential.setSelectedAccount(googleAccount.getAccount());

            driveClient = com.google.android.gms.drive.Drive.getDriveClient(this, googleAccount);
            driveResourceClient = com.google.android.gms.drive.Drive.getDriveResourceClient(this, googleAccount);

            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Drive googleDriveService = new Drive.Builder(
                    transport,
                    jsonFactory,
                    credential)
                    .setApplicationName("Drive API Migration")
                    .build();

            mDriveServiceHelper = new GoogleDriveServiceHelper(googleDriveService, getApplicationContext());
            signInButton.setEnabled(false);
            signOutButton.setEnabled(true);
            createFolder();
        } catch (Exception e) {
//            showUIToast(String.valueOf(e));
            Log.e(TAG, "Error initializing Drive service helper", e);
            // Handle error gracefully, e.g., show an error message to the user
            requestSignIn();
        }
    }


    private void uploadImageToDrive() {
        if (imageBitmap == null) {
            Toast.makeText(this, "Capture an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bitmapData = byteArrayOutputStream.toByteArray();

        driveResourceClient
                .createContents()
                .continueWithTask(task -> {
                    driveContents = task.getResult(); // Assign driveContents at this point
                    try (OutputStream outputStream = driveContents.getOutputStream()) {
                        outputStream.write(bitmapData);
                    }

                    // Obtain the reference to the root folder
                    return driveResourceClient.getRootFolder();
                })
                .continueWithTask(rootFolderTask -> {
                    DriveFolder rootFolder = rootFolderTask.getResult();

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("Image.jpg")
                            .setMimeType("image/jpeg")
                            .setStarred(true)
                            .build();

                    // Call createFile on the DriveResourceClient
                    return driveResourceClient.createFile(rootFolder, changeSet, driveContents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {
                            Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Image uploaded: " + driveFile.getDriveId());
                        })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Unable to upload image", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Unable to upload image", e);
                });
    }


    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());
                    // Save user's sign-in information to SharedPreferences
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(PREF_SIGNED_IN, true);
                    editor.putString(PREF_ACCOUNT_NAME, googleAccount.getEmail());
                    editor.apply();

                    userLogged.setText("Logged as: " + googleAccount.getEmail());

                    initializeDriveServiceHelper(googleAccount); // Move initialization here

                    hideProgressDialog();
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Unable to sign in.", exception);
                    showMessage("Unable to sign in.");
                    signInButton.setEnabled(true);
                    signOutButton.setEnabled(false);
                    hideProgressDialog();
                });
    }



    // This method will get call when user click on sign-in button
    public void signIn(View view) {
        requestSignIn();
    }
//    public void signIn() {
//        requestSignIn();
//    }

    // This method will get call when user click on create folder button

    public void createFolder() {
        if (mDriveServiceHelper != null) {
            if (!isLockedOut()) {
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
//                                                hideProgressDialog();
                                                folderFilesButton.setEnabled(true);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception exception) {
                                                showMessage("Couldn't create file.");
                                                Log.e(TAG, "emptyid Couldn't create file .", exception);
                                            }
                                        });
                            }else {
                                folderId=id;
                                folderFilesButton.setEnabled(true);
                                showMessage("Ready!");
//                                hideProgressDialog();
                                showMessage("Sign-In Successfully...!!");
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception exception) {
                            showMessage("Registering ...");
//                            showMessage("exception:"+String.valueOf(exception));
                            Log.e(TAG, "Couldn't create file..", exception);

                            if (exception instanceof UserRecoverableAuthIOException) {
                                authLauncher.launch(((UserRecoverableAuthIOException) exception).getIntent());

                                // UserRecoverableAuthIOException: Guide the user to resolve the issue
//                                startActivityForResult(((UserRecoverableAuthIOException) exception).getIntent(), REQUEST_AUTHORIZATION);
                            }
                        }
                    });

        } else {
            // If locked out, show appropriate message and keep the button disabled
            showMessage("Button locked due to too many failed attempts.");
            folderFilesButton.setEnabled(false);
                if (changePinMenuItem != null) {
                    changePinMenuItem.setEnabled(false);
                }
     }

        } else {
            showMessage("mDriveServiceHelper is null");
        }

        hideProgressDialog();
    }

    private void viewGoogleDriveFolder() {
        if (folderId != null && mDriveServiceHelper != null) {
            String folderUrl = "https://drive.google.com/drive/folders/" + folderId;
            Uri uri = Uri.parse(folderUrl);

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.docs"); // Open in Google Drive app if available
            intent.setDataAndType(uri, "text/html");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // If Google Drive app is not installed, open in a web browser
                intent.setPackage(null);
                startActivity(intent);
            }
        } else {
            showMessage("Folder ID is not available or not signed in.");
        }
    }



    // This method will get call when user click on folder data button
    public void getFolderData() {
        viewGoogleDriveFolder();
    }

//
//    public void getFolderData(View view) {
//        if (mDriveServiceHelper != null) {
//            Intent intent = new Intent(this, ListActivity.class);
//
//            mDriveServiceHelper.getFolderFileList()
//                    .addOnSuccessListener(new OnSuccessListener<ArrayList<GoogleDriveFileHolder>>() {
//                        @Override
//                        public void onSuccess(ArrayList<GoogleDriveFileHolder> result) {
//                            Log.e(TAG, "onSuccess: result: "+result.size() );
//                            intent.putParcelableArrayListExtra("fileList", result);
//                            startActivity(intent);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(Exception e) {
//                            showMessage("Not able to access Folder data.");
//                            Log.e(TAG, "Not able to access Folder data.", e);
//                        }
//                    });
//        }
//    }

private void selectImageFromGallery() {
    Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
    pickImageIntent.setType("image/*");
//    Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    if (pickImageIntent.resolveActivity(getPackageManager()) != null) {
        pickImageLauncher.launch(pickImageIntent);
    }
}

    private void selectVideoFromGallery() {
        Intent takeVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        takeVideoIntent.setType("video/*");
//        Intent takeVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            videoSelectionLauncher.launch(takeVideoIntent);
        }
    }
    private void selectVideoFromGallery0() {
        Intent takeVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//        , the video URI is already provided in the resultData Intent, and you don't need to set MediaStore.EXTRA_OUTPUT
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_GALLERY);
        }
    }
    private void selectVideoFromGallery1() {
        Intent takeVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating video file", ex);
            }
            if (videoFile != null) {
                capturedVideoUri = FileProvider.getUriForFile(this, "com.android.sun2meg.securedcamerashots.fileprovider", videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedVideoUri);
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_GALLERY);
            }
        }
    }

    private void clearUserCredentials() {
        // Clear user's sign-in information from SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_SIGNED_IN, false);
        editor.remove(PREF_ACCOUNT_NAME);
//        editor.remove(PREF_PERM);
        editor.apply();
        // You may also want to clear other user-specific data as needed.
    }

    public void signOut(View view) {
        // Sign out the user
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                status -> {
                    if (status.isSuccess()) {
                        // Clear user credentials and update UI
                        clearUserCredentials();
                        // Disable UI elements, if needed
                        signInButton.setEnabled(true);
                        folderFilesButton.setEnabled(false);
                        signOutButton.setEnabled(false);
                        showMessage("Signed-Out...!!");
                        mDriveServiceHelper=null;
                        userLogged.setText("");
                        // Show a message or update UI to indicate the sign-out
                    } else {
                        // Handle sign-out failure
                        signInButton.setEnabled(false);
                        showMessage("Failed to sign out");
                    }
                }
        );
    }

    // This method will get call when user click on sign-out button
    public void signOut2(View view) {
        if (googleSignInClient != null){
            googleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete( Task<Void> task) {
                            signInButton.setEnabled(true);

                            folderFilesButton.setEnabled(false);

                            signOutButton.setEnabled(false);
                            showMessage("Signed-Out...!!");
                            mDriveServiceHelper=null;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception exception) {
                            signInButton.setEnabled(false);
                            showMessage("Unable to sign out.");
                            Log.e(TAG, "Unable to sign out.", exception);
                        }
                    });
        }
        else
            showMessage("googleSignInClient is null.");
    }

    public void showMessage(String message) {
        Log.i(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setPin2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create PIN");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPin = input.getText().toString();
                PinManager.setPin(MainActivity.this, newPin);
                isPinSet = true;
//                getFolderData();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void setPin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create PIN");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPin = input.getText().toString();
                PinManager.setPin(MainActivity.this, newPin);
                isPinSet = true;
                getFolderData();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void changePin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change PIN");

        // Inflate the custom layout for the dialog
        View view = getLayoutInflater().inflate(R.layout.change_pin_dialog, null);
        builder.setView(view);

        final EditText currentPinInput = view.findViewById(R.id.current_pin_input);
        final EditText newPinInput = view.findViewById(R.id.new_pin_input);
        final EditText confirmPinInput = view.findViewById(R.id.confirm_pin_input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String currentPin = currentPinInput.getText().toString();
                String newPin = newPinInput.getText().toString();
                String confirmPin = confirmPinInput.getText().toString();

                if (!newPin.equals(confirmPin)) {
                    // Display error message if new PIN and confirm PIN don't match
                    Toast.makeText(MainActivity.this, "New PINs do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (PinManager.checkPin(MainActivity.this, currentPin)) {
                    PinManager.setPin(MainActivity.this, newPin);
                    isPinSet = true;
                    failedAttempts = 0;
                    Toast.makeText(MainActivity.this, "PIN changed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    failedAttempts++;
                    if (isLockedOut()) {
                        lockApp();
                    } else {
                        Toast.makeText(MainActivity.this, "Incorrect current PIN", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void changePin0() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change PIN");

        // Inflate the custom layout for the dialog
        View view = getLayoutInflater().inflate(R.layout.change_pin_dialog, null);
        builder.setView(view);

        final EditText currentPinInput = view.findViewById(R.id.current_pin_input);
        final EditText newPinInput = view.findViewById(R.id.new_pin_input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String currentPin = currentPinInput.getText().toString();
                String newPin = newPinInput.getText().toString();

                if (PinManager.checkPin(MainActivity.this, currentPin)) {
                    PinManager.setPin(MainActivity.this, newPin);
                    isPinSet = true;
                    failedAttempts=0;
                    Toast.makeText(MainActivity.this, "PIN changed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    failedAttempts++;
                    if (isLockedOut()) {
                        lockApp();
                    } else {
                        Toast.makeText(MainActivity.this, "Incorrect current PIN", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void restoreLockStatus() {
        SharedPreferences lockPrefs = getSharedPreferences(LOCK_PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLocked = lockPrefs.getBoolean(LOCK_STATUS_KEY, false);
        if (isLocked) {
            long lastAttemptTime = lockPrefs.getLong(LAST_FAILED_ATTEMPT_TIME_KEY, 0);
            long elapsedTime = System.currentTimeMillis() - lastAttemptTime;
            if (elapsedTime < LOCKOUT_DURATION) {
                // App is still locked out, disable the button and set failed attempts
                folderFilesButton.setEnabled(false);
                if (changePinMenuItem != null) {
                    changePinMenuItem.setEnabled(false);
                }
                failedAttempts = lockPrefs.getInt(FAILED_ATTEMPTS_KEY, 0);
                lastFailedAttemptTime = lastAttemptTime;
            } else {
                // Lockout duration has passed, reset lock status
                clearLockStatus();
            }
        }
    }
    private void clearLockStatus() {
        SharedPreferences lockPrefs = getSharedPreferences(LOCK_PREFS_NAME, Context.MODE_PRIVATE);
        lockPrefs.edit().clear().apply();
        // Reset lockout related variables
        failedAttempts = 0;
        lastFailedAttemptTime = 0;
    }

    private void lockApp() {
        // Update lock status in SharedPreferences
        SharedPreferences lockPrefs = getSharedPreferences(LOCK_PREFS_NAME, Context.MODE_PRIVATE);
        lockPrefs.edit()
                .putBoolean(LOCK_STATUS_KEY, true)
                .putInt(FAILED_ATTEMPTS_KEY, failedAttempts)
                .putLong(LAST_FAILED_ATTEMPT_TIME_KEY, lastFailedAttemptTime)
                .apply();
        if (changePinMenuItem != null) {
            changePinMenuItem.setEnabled(false);
        }
        folderFilesButton.setEnabled(false);
        new CountDownTimer(LOCKOUT_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                // Timer is ticking, you can show a countdown timer or any other indication
            }

            public void onFinish() {
                clearLockStatus();
                if (changePinMenuItem != null) {
                    changePinMenuItem.setEnabled(true);
                }
                // Timer finished, enable the button
                folderFilesButton.setEnabled(true);

                failedAttempts = 0; // Reset failed attempts
                Toast.makeText(MainActivity.this, "App Button unlocked.", Toast.LENGTH_SHORT).show();
            }
        }.start();
//        showSnackbar("Too many failed attempts. Folder access locked for 10 minutes.");
        Toast.makeText(this, "Too many failed attempts. Button locked for 10 minutes.", Toast.LENGTH_LONG).show();
    }
    private void showPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPin = input.getText().toString();
                if (PinManager.checkPin(MainActivity.this, enteredPin)) {
                    getFolderData();
                    failedAttempts=0;
                } else {
                    failedAttempts++;
                    if (isLockedOut()) {
                        lockApp();
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid PIN", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, failedAttempts + " failed Attempts", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private boolean isLockedOut() {
        long currentTime = System.currentTimeMillis();
        if (failedAttempts >= MAX_FAILED_ATTEMPTS && currentTime - lastFailedAttemptTime < LOCKOUT_DURATION) {
            return true; // Locked out
        } else if (currentTime - lastFailedAttemptTime >= LOCKOUT_DURATION) {
            failedAttempts = 1; // Reset attempts if lockout duration has passed
            lastFailedAttemptTime = currentTime;
        }
        return false;
    }


    //    @Override
//    public void onBackPressed() {
//        if (backPressCount == 0) {
//            // Show a message to press back again
//            Toast.makeText(this, "Press back button again to exit", Toast.LENGTH_SHORT).show();
//
//            // Increment the back press count
//            backPressCount++;
//
//            // Set a delayed handler to reset the back press count
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    backPressCount = 0;
//                }
//            }, 2000); // You can adjust the time window for double press
//        } else {
//            // If back is pressed again within the time window, exit the app
////            super.onBackPressed();
//        Intent setIntent = new Intent(Intent.ACTION_MAIN);
//        setIntent.addCategory(Intent.CATEGORY_HOME);
//        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(setIntent);
//        }
//    }
private void showHelpDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    LayoutInflater inflater = getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.dialog_help, null);
    builder.setView(dialogView);
    builder.setTitle("Video Quality Help");

    TextView textHelp = dialogView.findViewById(R.id.text_help);
    textHelp.setText(R.string.video_quality_help_text); // Use string resource for help text

    builder.setPositiveButton("OK", null); // Add OK button

    AlertDialog dialog = builder.create();
    dialog.show();
}
    private void showHelpContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_contact, null);
        builder.setView(dialogView);
        builder.setTitle("Contact Us");

        TextView textHelp = dialogView.findViewById(R.id.text_help);
        textHelp.setText(R.string.contact_us); // Use string resource for help text

        builder.setPositiveButton("OK", null); // Add OK button

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu; // Save the menu reference
        changePinMenuItem = menu.findItem(R.id.action_change_pin);
        // Update selected quality menu item title
        int qualityPreference = getVideoQualityPreference();
        MenuItem selectedQualityItem = menu.findItem(qualityPreference == 1 ? R.id.menu_item_high_quality : R.id.menu_item_low_quality);
        selectedQualityItem.setChecked(true); // Check the selected item
//          return true;
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), com.megacoreapps.sun2meg.securedcamerashot.Navigation.class);
                startActivity(intent);
                return true;
            case R.id.exitapp:
                exit();
                return true;
            case R.id.menu_item_high_quality:
                // Handle high quality selection
                saveVideoQualityPreference(1);
                Toast.makeText(getApplicationContext(), "High quality selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_item_low_quality:
                // Handle low quality selection
                saveVideoQualityPreference(0);
                Toast.makeText(getApplicationContext(), "Low quality selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_change_pin:
                if (isPinSet) {
                    changePin();
                } else {
                    // PIN not set, proceed with action and set the PIN
                    setPin2();
                }
//                changePin();
                return true;
            case R.id.contact:
                showHelpContact();
                return true;
            case R.id.menu_item_quality_help:
                showHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
public void exit(){
    Intent serviceIntent = new Intent(this, com.megacoreapps.sun2meg.securedcamerashot.LockService.class);
    stopService(serviceIntent);
    finishAffinity();

}
    private void saveVideoQualityPreference(int quality) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("video_quality", quality);
        editor.apply();
        MenuItem selectedQualityItem = menu.findItem(quality == 1 ? R.id.menu_item_high_quality : R.id.menu_item_low_quality);
        selectedQualityItem.setChecked(true);
    }

}
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}