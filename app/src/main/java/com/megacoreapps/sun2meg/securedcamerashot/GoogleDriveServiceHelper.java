package com.megacoreapps.sun2meg.securedcamerashot;

import static com.megacoreapps.sun2meg.securedcamerashot.MainActivity.folderId;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
//import com.google.api.services.drive.model.ParentReference;

//import static com.dreamappsstore.googledrive_demo.MainActivity.folderId;

/**
 * A utility for performing creating folder if not present, get the file, upload the file, download the file and
 * delete the file from google drive
 */
public class GoogleDriveServiceHelper {

    private static final String TAG = "GoogleDriveService";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    private final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private final String SHEET_MIME_TYPE = "video/mp4";
    private final String FOLDER_NAME = "Secured_Cam_Shots";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static final int CHUNK_SIZE = 1024 * 1024; // 1 MB chunk size

    private Context context;
//    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
//
//    private DriveResourceClient mDriveResourceClient =
// com.google.android.gms.drive.Drive.getDriveResourceClient(context, account);

    public GoogleDriveServiceHelper(Drive driveService, Context context) {
        mDriveService = driveService;
        this.context = context;
    }


    /**
     * Check Folder present or not in the user's My Drive.
     */
    public Task<String> isFolderPresent() {
        return Tasks.call(mExecutor, () -> {
            FileList result = mDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and trashed=false").execute();
            for (File file : result.getFiles()) {
                if (file.getName().equals(FOLDER_NAME))
                    return file.getId();
            }
            return "";
        });
    }

    /**
     * Creates a Folder in the user's My Drive.
     */
    public Task<String> createFolder() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType(FOLDER_MIME_TYPE)
                    .setName(FOLDER_NAME);

            File googleFolder = mDriveService.files().create(metadata).execute();
            if (googleFolder == null) {
                throw new IOException("Null result when requesting Folder creation.");
            }

            return googleFolder.getId();
        });
    }

    /**
     * Get all the file present in the user's My Drive Folder.
     */

    public Task<Boolean> uploadFileToGoogleDrive0(String path, ProgressBar progressBar, TextView tv) {
        return Tasks.call(mExecutor, () -> {
            java.io.File filePath = new java.io.File(path);
            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("video/mp4")
                    .setName(filePath.getName());

            FileContent mediaContent = new FileContent("video/mp4", filePath);

            // Use a chunk size that works well for your use case
            int chunkSize = 512 * 512; // 512MB chunk size

//            int chunkSize = 10 * 1024 * 1024; // 10MB chunk size

            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent);

            // Enable chunked resumable upload with the specified chunk size
            createRequest.getMediaHttpUploader().setDirectUploadEnabled(false);
            createRequest.getMediaHttpUploader().setChunkSize(chunkSize);

            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();

            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            // Handle initiation started
                            break;
                        case INITIATION_COMPLETE:
                            // Handle initiation complete
                            break;
                        case MEDIA_IN_PROGRESS:
                            int percentage = (int) (uploader.getProgress() * 100);
                            // Update the progress bar
                            progressBar.setProgress(percentage);
                            // Set the percentage as a String in the TextView
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(String.valueOf(percentage));
                                }
                            });
                            break;
                        case MEDIA_COMPLETE:
                            // Upload complete, file has been successfully uploaded
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    tv.setText("Uploaded");
                                    progressBar.setProgress(0);
                                }
                            });
                            break;
                        case NOT_STARTED:
                            // Handle not started
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            File uploadedFile = null;
            try {
                // Execute the upload request and get the uploaded file information
                uploadedFile = createRequest.execute();
            } catch (IOException e) {
                // Handle the upload error and implement resuming logic here
                e.printStackTrace();
                // Resume the upload from where it left off
            }

            return uploadedFile != null;
        });
    }

    public Task<Boolean> UploadAudio(java.io.File localFile , ProgressBar progressBar, TextView tv) {
        return Tasks.call(mExecutor, () -> {

            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("audio/jpeg")
                    .setName(localFile.getName());

            FileContent mediaContent = new FileContent("image/3gpp", localFile);
            try {
                File file = mDriveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                System.out.println("File ID: " + file.getId());
//                        uploadedFile != null;
                return true;
            } catch (GoogleJsonResponseException e) {
                // TODO(developer) - handle error appropriately
                System.err.println("Unable to move file: " + e.getDetails());
                throw e;
            }


        });
    }
//active good but not progress
    public Task<Boolean> UploadImage(java.io.File localFile , ProgressBar progressBar, TextView tv) {
        return Tasks.call(mExecutor, () -> {

            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("image/jpeg")
                    .setName(localFile.getName());

            FileContent mediaContent = new FileContent("image/jpeg", localFile);
            try {
                File file = mDriveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                System.out.println("File ID: " + file.getId());
//                        uploadedFile != null;
                return true;
            } catch (GoogleJsonResponseException e) {
                // TODO(developer) - handle error appropriately
                System.err.println("Unable to move file: " + e.getDetails());
                throw e;
            }


        });
    }

    public Task<Boolean> uploadFileToGoogleDriveImg(String path, ProgressBar progressBar, TextView tv) {
        return Tasks.call(mExecutor, () -> {
            java.io.File filePath = new java.io.File(path);

//            fileMetadata.setName(filePath.getName());
            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("image/jpeg")
                    .setName(filePath.getName());
//                    .setName(new File(path).getName());

//            java.io.File filePath = new java.io.File(path);
            FileContent mediaContent = new FileContent("image/jpeg", filePath);

            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent);
            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false); // Enable resumable uploads

            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            // Handle initiation started
                            break;
                        case INITIATION_COMPLETE:
                            // Handle initiation complete
                            break;

                        case MEDIA_IN_PROGRESS:
                            int percentage = (int) (uploader.getProgress() * 100);
                            // Update the progress bar
                            progressBar.setProgress(percentage);
                            // Set the percentage as a String in the TextView
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(String.valueOf(percentage));
                                }
                            });
                            break;
                        case MEDIA_COMPLETE:
                            // Upload complete, file has been successfully uploaded
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                    tv.setText("Uploaded");
                                }
                            });
                            break;
                        case NOT_STARTED:
                            // Handle not started
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            File uploadedFile = createRequest.execute();
            return uploadedFile != null;
        });
    }


    public Task<Boolean> uploadImageToGoogleDrive(java.io.File localFile , ProgressBar progressBar, TextView tv) {
        return Tasks.call(mExecutor, () -> {

            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("image/jpeg")
                    .setName(localFile.getName());

            FileContent mediaContent = new FileContent("image/jpeg", localFile);

//            // Create file metadata
//            File fileMetadata = new File()
//                    .setParents(Collections.singletonList(folderId))
//                    .setMimeType("image/jpeg")
//                    .setName(tempFile.getName());
//
//            // Create file content
//            FileContent mediaContent = new FileContent("image/jpeg", tempFile);// io.file

            // Create the Drive Files.Create request
            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent);
            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false); // Enable resumable uploads

            // Set up progress listener
            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case MEDIA_IN_PROGRESS:
                            int percentage = (int) (uploader.getProgress() * 100);
                            // Update the progress bar
                            progressBar.setProgress(percentage);
                            // Set the percentage as a String in the TextView
                            mHandler.post(() -> tv.setText(String.valueOf(percentage)));
                            break;
                        case MEDIA_COMPLETE:
                            // Upload complete, file has been successfully uploaded
                            mHandler.post(() -> {
                                progressBar.setProgress(0);
                                tv.setText("Uploaded");
                            });
                            break;
                        default:
                            // Handle other states if needed
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            // Execute the request and get the uploaded file
            File uploadedFile = createRequest.execute();
            return uploadedFile != null;
        });
    }


    //ACTIVE. uploads fine
    public Task<Boolean> uploadFileToGoogleDrivex(Uri uri, ProgressBar progressBar, TextView tv) {
        return Tasks.call(mExecutor, () -> {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new FileNotFoundException("Unable to open InputStream for URI: " + uri);
            }

            // Convert InputStream to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byte[] data = byteArrayOutputStream.toByteArray();

            // Set up metadata for the file
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg"; // Change the extension based on your image type

            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("image/jpeg") // Change the MIME type based on your image type
                    .setName(fileName);

            // Set up content for the file using ByteArrayContent
            ByteArrayContent mediaContent = new ByteArrayContent("image/jpeg", data);
            // Use a chunk size that works well for your use case
//            int chunkSize = 256 * 256; // 1MB chunk size
            int chunkSize = 512 * 512;
            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent).setFields("id");
            createRequest.getMediaHttpUploader().setDirectUploadEnabled(false);
            createRequest.getMediaHttpUploader().setChunkSize(chunkSize);

            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();

            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                   switch (uploader.getUploadState()) {
                       case INITIATION_STARTED:
                           // Handle initiation started
                           break;
                       case INITIATION_COMPLETE:
                           // Handle initiation complete
                           break;
                       case MEDIA_IN_PROGRESS:
                           int percentage = (int) (uploader.getProgress() * 100);
                           // Update the progress bar
                           progressBar.setProgress(percentage);
                           // Set the percentage as a String in the TextView
                           String prog =String.valueOf(percentage)+"%";
                           mHandler.post(new Runnable() {
                               @Override
                               public void run() {
                                   tv.setText(prog);
//                                    tv.setText(String.valueOf(percentage));
                               }
                           });
                           break;
                       case MEDIA_COMPLETE:
                           // Upload complete, file has been successfully uploaded
                           mHandler.post(new Runnable() {
                               @Override
                               public void run() {
                                   progressBar.setProgress(100);
                                   tv.setText("Uploaded");
                                   progressBar.setProgress(0);
                               }
                           });
                           break;
                       case NOT_STARTED:
                           // Handle not started
                           break;
                   }
                }
            };
            uploader.setProgressListener(progressListener);

            File uploadedFile = null;
            try {
                // Execute the upload request and get the uploaded file information
                uploadedFile = createRequest.execute();
            } catch (GoogleJsonResponseException e) {
                System.err.println("Unable to move file: " + e.getDetails());
                throw e;
            } finally {
                inputStream.close(); // Close the InputStream when done
            }

            return uploadedFile != null;
        });
    }

    //ACTIVE. uploads fine
    public Task<Boolean> uploadFileToGoogleDrive2(Uri uri, ProgressBar progressBar, TextView tv) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String VidFileName = "VID_" + timeStamp;
        return Tasks.call(mExecutor, () -> {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new FileNotFoundException("Unable to open InputStream for URI: " + uri);
            }

            // Convert InputStream to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byte[] data = byteArrayOutputStream.toByteArray();

            // Set up metadata for the file
            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("video/mp4")
                    .setName(VidFileName+".mp4");
//                    .setName("your_file_name.mp4"); // Set an appropriate name for the file

            // Set up content for the file using ByteArrayContent
            ByteArrayContent mediaContent = new ByteArrayContent("video/mp4", data);


            try {
                File file = mDriveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                System.out.println("File ID: " + file.getId());
                return true;
            } catch (GoogleJsonResponseException e) {
                System.err.println("Unable to move file: " + e.getDetails());
                throw e;
            } finally {
                inputStream.close(); // Close the InputStream when done
            }
        });
    }
    public Task<Boolean> uploadFileToGoogleDrive3(Uri uri, ProgressBar progressBar, TextView tv) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String VidFileName = "VID_" + timeStamp;

        return Tasks.call(mExecutor, () -> {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new FileNotFoundException("Unable to open InputStream for URI: " + uri);
            }

            // Get the length of the file
            long fileLength = getFileLength(uri);
            if (fileLength <= 0) {
                throw new IOException("Unable to determine file length for URI: " + uri);
            }

            // Set up metadata for the file
            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("video/mp4")
                    .setName(VidFileName + ".mp4");

            // Set up content for the file using InputStreamContent
            InputStreamContent mediaContent = new InputStreamContent("video/mp4", inputStream);
            mediaContent.setLength(fileLength); // Set the content length

            // Create the upload request
            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent).setFields("id");

            // Enable chunked resumable upload with the specified chunk size
            int chunkSize = 512 * 512; // 256KB chunk size
            createRequest.getMediaHttpUploader().setDirectUploadEnabled(false);
            createRequest.getMediaHttpUploader().setChunkSize(chunkSize);


            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();
            Handler mainHandler = new Handler(Looper.getMainLooper());
            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            // Handle initiation started
                            Log.d(TAG, "Upload initiation started");
                            break;
                        case INITIATION_COMPLETE:
                            // Handle initiation complete
                            Log.d(TAG, "Upload initiation complete");
                            // Post update to main UI thread
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(". . . ");
                                }
                            });

                            break;
                        case MEDIA_IN_PROGRESS:
                            // Handle media in progress
                            int percentage = (int) (uploader.getProgress() * 100);
                            Log.d(TAG, "Upload progress: " + percentage + "%");

                            // Post update to main UI thread
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // Update progress bar
                                    progressBar.setProgress(percentage);
                                    // Update TextView
                                    tv.setText("Uploading: "+percentage + "%"+ " of "+ fileLength / 1000000+"mb");
                                }
                            });
                            break;
                        case MEDIA_COMPLETE:
                            // Handle media complete
                            Log.d(TAG, "Upload complete");
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // Update UI for completion
                                    progressBar.setProgress(0);
                                    tv.setText("Uploaded");
                                }
                            });
                            break;
                        case NOT_STARTED:
                            // Handle not started
                            Log.d(TAG, "Upload not started");
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            //GOOD
//            // Set progress listener
//            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();
//            uploader.setProgressListener(new MediaHttpUploaderProgressListener() {
//                @Override
//                public void progressChanged(MediaHttpUploader uploader) throws IOException {
//                    // Handle progress change
//                    int percentage = (int) (uploader.getProgress() * 100);
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Update progress bar
//                            progressBar.setProgress(percentage);
//                            // Update TextView
//                            tv.setText(percentage + "%");
//
//                        }
//                    });
//
//                }
//            });

            // Execute the upload request and get the uploaded file information
            File uploadedFile = createRequest.execute();

            // Close the InputStream when done
            inputStream.close();

            return uploadedFile != null;
        });
    }

    private long getFileLength(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }


    //ACTIVE. uploads fine
    public Task<Boolean> uploadFileToGoogleDrive30(Uri uri, ProgressBar progressBar, TextView tv) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String VidFileName = "VID_" + timeStamp;
        return Tasks.call(mExecutor, () -> {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new FileNotFoundException("Unable to open InputStream for URI: " + uri);
            }

            // Convert InputStream to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byte[] data = byteArrayOutputStream.toByteArray();

            // Set up metadata for the file
            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("video/mp4")
                    .setName(VidFileName+".mp4");
//                    .setName("your_file_name.mp4"); // Set an appropriate name for the file

            // Set up content for the file using ByteArrayContent
            ByteArrayContent mediaContent = new ByteArrayContent("video/mp4", data);
            // Use a chunk size that works well for your use case
            int chunkSize = 512 * 512; // 500kb chunk size
//            int chunkSize = 1024 * 1024; // 1MB chunk size
            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent).setFields("id"); // just added
//            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent);

            // Enable chunked resumable upload with the specified chunk size
            createRequest.getMediaHttpUploader().setDirectUploadEnabled(false);
            createRequest.getMediaHttpUploader().setChunkSize(chunkSize);

            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();

            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            // Handle initiation started
                            break;
                        case INITIATION_COMPLETE:
                            // Handle initiation complete
                            break;
                        case MEDIA_IN_PROGRESS:
                            int percentage = (int) (uploader.getProgress() * 100);
                            // Update the progress bar
                            progressBar.setProgress(percentage);
                            // Set the percentage as a String in the TextView
                            String prog =String.valueOf(percentage)+"%";
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(prog);
//                                    tv.setText(String.valueOf(percentage));
                                }
                            });
                            break;
                        case MEDIA_COMPLETE:
                            // Upload complete, file has been successfully uploaded
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(100);
                                    tv.setText("Uploaded");
                                    progressBar.setProgress(0);
                                }
                            });
                            break;
                        case NOT_STARTED:
                            // Handle not started
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            File uploadedFile = null;
            try {
                // Execute the upload request and get the uploaded file information
                uploadedFile = createRequest.execute();
            } catch (GoogleJsonResponseException e) {
                System.err.println("Unable to move file: " + e.getDetails());
                throw e;
            } finally {
                inputStream.close(); // Close the InputStream when done
            }

            return uploadedFile != null;
        });
    }
/////cut portion ////////////
//            try {
//                File file = mDriveService.files().create(fileMetadata, mediaContent)
//                        .setFields("id")
//                        .execute();
//
//                System.out.println("File ID: " + file.getId());
//                return true;
//            } catch (GoogleJsonResponseException e) {
//                System.err.println("Unable to move file: " + e.getDetails());
//                throw e;
//            } finally {
//                inputStream.close(); // Close the InputStream when done
//            }
//        });
//    }
////end cut ////////////////////////////////

    //former error file isnt seen using "stringpath"
    public Task<Boolean> uploadFileToGoogleDrive(String path, ProgressBar progressBar, TextView tv) {
        return Tasks.call(mExecutor, () -> {

            java.io.File filePath = new java.io.File(path);
            FileContent mediaContent = new FileContent("video/mp4", filePath);
            File fileMetadata = new File()
                    .setParents(Collections.singletonList(folderId))
                    .setMimeType("video/mp4")
                    .setName(filePath.getName());
            ///  modification start point  //////////////

//            try {
//                File file = mDriveService.files().create(fileMetadata, mediaContent)
//                        .setFields("id")
//                        .execute();
//                System.out.println("File ID: " + file.getId());
////                        uploadedFile != null;
//                return true;
//            } catch (GoogleJsonResponseException e) {
//                // TODO(developer) - handle error appropriately
//                System.err.println("Unable to move file: " + e.getDetails());
//                throw e;
//            }
            //////// mod end point /////////////////

//            java.io.File filePath = new java.io.File(path);
//            File fileMetadata = new File()
//                    .setParents(Collections.singletonList(folderId))
//                    .setMimeType("video/mp4")
//                    .setName(filePath.getName());
//
//            FileContent mediaContent = new FileContent("video/mp4", filePath);
//
            // Use a chunk size that works well for your use case
            int chunkSize = 10 * 1024 * 1024; // 10MB chunk size
            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent).setFields("id"); // just added
//            Drive.Files.Create createRequest = mDriveService.files().create(fileMetadata, mediaContent);

            // Enable chunked resumable upload with the specified chunk size
            createRequest.getMediaHttpUploader().setDirectUploadEnabled(false);
            createRequest.getMediaHttpUploader().setChunkSize(chunkSize);

            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();

            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            // Handle initiation started
                            break;
                        case INITIATION_COMPLETE:
                            // Handle initiation complete
                            break;
                        case MEDIA_IN_PROGRESS:
                            int percentage = (int) (uploader.getProgress() * 100);
                            // Update the progress bar
                            progressBar.setProgress(percentage);
                            // Set the percentage as a String in the TextView
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(String.valueOf(percentage));
                                }
                            });
                            break;
                        case MEDIA_COMPLETE:
                            // Upload complete, file has been successfully uploaded
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(100);
                                    tv.setText("Uploaded");
                                    progressBar.setProgress(0);
                                }
                            });
                            break;
////
//                        case MEDIA_IN_PROGRESS:
//                            int percentage = (int) (uploader.getProgress() * 100);
//                            progressBar.setProgress(percentage);
//                            tv.setText(String.valueOf(percentage));
//                            break;
//                        case MEDIA_COMPLETE:
//                            // Upload complete, file has been successfully uploaded
//                            progressBar.setProgress(0);
//                            tv.setText("Uploaded");
//                            break;
                        case NOT_STARTED:
                            // Handle not started
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            File uploadedFile = null;
            try {
                // Execute the upload request and get the uploaded file information
                uploadedFile = createRequest.execute();
            } catch (IOException e) {
                // Handle the upload error and implement resuming logic here
                e.printStackTrace();
                // Resume the upload from where it left off
            }

            return uploadedFile != null;
        });
    }

//    public static void uploadFileToDrive(java.io.File file, String folderId) throws IOException, GeneralSecurityException {
//        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//
//        // Load client secrets.
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
//                new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));
//
//        // Set up authorization code flow.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                httpTransport, JSON_FACTORY, clientSecrets, Collections.singletonList(DriveScopes.DRIVE))
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
//                .setAccessType("offline")
//                .build();
//
//        // Authorize with user's Google account.
//        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
//                .authorize("user");
//
//        // Create a Drive service using the authorized credentials.
//        Drive driveService = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//
//        // Set file metadata.
//        File fileMetadata = new File();
//        fileMetadata.setTitle(file.getName());
//
//        // Set the parent folder if available
//        if (folderId != null) {
//            fileMetadata.setParents(Collections.singletonList(new ParentReference().setId(folderId)));
//        }
//
//        // Set the content of the file to be uploaded.
//        FileContent mediaContent = new FileContent("application/octet-stream", file);
//
//        // Create the file in Google Drive.
//        File uploadedFile = driveService.files().insert(fileMetadata, mediaContent).execute();
//
//        System.out.println("File ID: " + uploadedFile.getId());
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
//            File fileMetadata = new File();
//            fileMetadata.setName(filePath.getName());
//            fileMetadata.setParents(Collections.singletonList(folderId));
//            fileMetadata.setMimeType("video/mp4");
////            fileMetadata.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//
//            FileContent mediaContent = new FileContent("video/mp4", filePath);
////            FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filePath);
//            File file = mDriveService.files().create(fileMetadata, mediaContent)
//                    .setFields("id")
//                    .execute();
//            System.out.println("File ID: " + file.getId());
//
//            return false;
//        });
//    }

    /**
     * Download file from the user's My Drive Folder.
     */


    public Task<Boolean> downloadFile(final java.io.File fileSaveLocation, final String fileId) {
        return Tasks.call(mExecutor, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Retrieve the metadata as a File object.
                OutputStream outputStream = new FileOutputStream(fileSaveLocation);
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                return true;
            }
        });
    }

    /**
     * delete file from the user's My Drive Folder.
     */
    public Task<Boolean> deleteFolderFile(final String fileId) {
        return Tasks.call(mExecutor, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Retrieve the metadata as a File object.
                if (fileId != null) {
                    mDriveService.files().delete(fileId).execute();
                    return true;
                }
                return false;
            }
        });
    }

}
