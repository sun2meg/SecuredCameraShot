package com.megacoreapps.sun2meg.securedcamerashot;

//import static com.android.sun2meg.securedcamerashots.MainActivity.folderId;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.api.services.drive.DriveScopes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GoogleDriveHelper {

 private static final String TAG = "GoogleDriveHelper";
 public static final int REQUEST_CODE_SIGN_IN = 1;

 private Context context;
    private Drive mDriveService;
 private Drive driveService;
 private GoogleSignInClient googleSignInClient;
    DriveResourceClient driveResourceClient;
    Handler mainHandler;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    public GoogleDriveHelper(Context context, Handler mainHandler) {
        this.context = context;
        this.mainHandler = mainHandler;
//        initDriveService();
    }

    private void initDriveService() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
        GoogleSignInAccount signInAccount = googleSignInClient.silentSignIn().getResult();

        if (signInAccount != null) {
            driveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
            // Use the driveResourceClient to interact with Google Drive services
        }
    }


//    private void initDriveService() {
//        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestScopes(new Scope("https://www.googleapis.com/auth/drive.file"))
//                .build();
//        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
//        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);
//
//        if (signInAccount != null) {
//         driveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
//            // Use the driveResourceClient to interact with Google Drive services
//        }
//    }



    public GoogleSignInAccount getSignInAccount() {
        return GoogleSignIn.getLastSignedInAccount(context);
    }

public void signIn() {



    GoogleSignInOptions signInOptions =
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                    .requestEmail()
                    .build();
    googleSignInClient = GoogleSignIn.getClient(context, signInOptions);

    // The result of the sign-in Intent is handled in onActivityResult.
    ((Activity) context).startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);



//    GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestScopes(new Scope("https://www.googleapis.com/auth/drive.file"))
//            .build();
//     googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
//
//    Intent signInIntent = googleSignInClient.getSignInIntent();
//   ((Activity) context).startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

   public void handleSignInResult(Intent resultIntent) {
                          try {
    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(resultIntent).getResult(ApiException.class);
           if (account != null) {
      initDriveService();
      Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show();
      }  else
           showToast("Signed in Not successfully");
    } catch (ApiException e) {
     Log.e(TAG, "Sign-in failed", e);
     Toast.makeText(context, "Sign-in failed", Toast.LENGTH_SHORT).show();
      }
       }

//    public void uploadVideo(String videoFilePath, GoogleApiClient googleApiClient) {
    public void uploadVideo(String videoFilePath, DriveResourceClient driveResourceClient, GoogleApiClient googleApiClient) {
        java.io.File videoFile = new java.io.File(videoFilePath);
showToast("upload stage");
if (driveResourceClient==null){
    showToast("drive is null");
}
        // Create metadata for the file
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle("My Video")
                .setMimeType("video/mp4")
                .build();


        // Create a new empty DriveContents resource
        driveResourceClient.createContents()
                .addOnSuccessListener(driveContents -> {
                    try {
                        // Open an OutputStream for the DriveContents
                        OutputStream outputStream = driveContents.getOutputStream();

                        // Read the content of the video file and write it to the DriveContents
                        FileInputStream inputStream = new FileInputStream(videoFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        // Commit the DriveContents with the video content
                        driveContents.commit(googleApiClient, metadataChangeSet)
                                .setResultCallback(status -> {
                                    if (status.isSuccess()) {

                                        mainHandler.post(() -> {
                                            showToast("Uploaded successfully");
                                        });

                                         // File created and content uploaded successfully
//                                        DriveFile driveFile = Drive.DriveApi.getFile(googleApiClient, status.getDriveFile().getDriveId());
//                                        String fileId = driveFile.getDriveId().encodeToString();
//                                        // Handle fileId as needed
                                    } else {
                                        // Handle failure

                                        mainHandler.post(() -> {
                                            showToast("Uploaded successfully");
                                        });

                                        Log.e(TAG, "Error committing DriveContents: " + status.getStatusMessage());
                                    }
                                });

                    } catch (IOException e) {
                        // Handle IO exception

                        mainHandler.post(() -> {
                            showToast("Error reading file");
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    mainHandler.post(() -> {
                        showToast("Error creating DriveContents");
                    });

                });
    }
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

//         public void uploadVideo2(String videoFilePath) {
//         new UploadVideoTask().execute(videoFilePath);
//         }

//    public Task<String> isFolderPresent() {
//        return Tasks.call(mExecutor, () -> {
//            FileList result = mDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and trashed=false").execute();
//            for (com.google.api.services.drive.model.File file : result.getFiles()) {
//                if (file.getName().equals(FOLDER_NAME))
//                    return file.getId();
//            }
//            return "";
//        });
//    }
//
//    public Task<Boolean> uploadFileToGoogleDrive(String path) {
//
//        if (folderId.isEmpty()){
//            Log.e(TAG, "uploadFileToGoogleDrive: folder id not present" );
//            isFolderPresent().addOnSuccessListener(id -> folderId=id)
//                    .addOnFailureListener(exception -> Log.e(TAG, "Couldn't create file.", exception));
//        }
//
//        return Tasks.call(mExecutor, () -> {
//
//            Log.e(TAG, "uploadFileToGoogleDrive: path: "+path );
//            java.io.File filePath = new java.io.File(path);
//
//            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
//            fileMetadata.setName(filePath.getName());
//            fileMetadata.setParents(Collections.singletonList(folderId));
//            fileMetadata.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//
//            FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filePath);
//            com.google.api.services.drive.model.File file = mDriveService.files().create(fileMetadata, mediaContent)
//                    .setFields("id")
//                    .execute();
//            System.out.println("File ID: " + file.getId());
//
//            return false;
//        });
//    }

//
//    private class UploadVideoTask extends AsyncTask<String, Void, String> {
//
//@Override
// protected String doInBackground(String... params) {
//String videoFilePath = params[0];
//
// try {
// File fileMetadata = new File();
// fileMetadata.setName("My Video");
//fileMetadata.setMimeType("video/mp4");
//
// java.io.File videoFile = new java.io.File(videoFilePath);
// FileContent mediaContent = new FileContent("video/mp4", videoFile);
//
// File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
// .setFields("id")
// .execute();
//
//return uploadedFile.getId();
// } catch (IOException e) {
// Log.e(TAG, "Error uploading video", e);
//return null;
// }
// }
//
// @Override
// protected void onPostExecute(String fileId) {
// if (fileId != null) {
// Toast.makeText(context, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
// } else {
//     Toast.makeText(context, "Failed to upload video", Toast.LENGTH_SHORT).show();
// }
// }
//}
}
