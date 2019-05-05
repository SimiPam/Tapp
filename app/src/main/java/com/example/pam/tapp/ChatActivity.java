package com.example.pam.tapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.example.pam.tapp.listeners.PictureCapturingListener;
import com.example.pam.tapp.services.APictureCapturingService;
import com.example.pam.tapp.services.PictureCapturingServiceImpl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.L;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.gson.Gson;
import com.ibm.cloud.sdk.core.http.HttpConfigOptions;
import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Tone;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;
import com.ibm.watson.discovery.v1.Discovery;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;



public class ChatActivity extends AppCompatActivity implements PictureCapturingListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private APictureCapturingService pictureService;

    private static final String[] requiredPermissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;


    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText MessageInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, NotificationRef;
    private final static List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private int letterCount;


    private String emotionText = "";
    private String emotion = "";
    private String emotionFace = "";
    private TransferUtility transferUtility;
    private AmazonS3 s3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // AWSMobileClient.getInstance().initialize(this).execute();

        //assess the name and id sent wby using its key
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        Toast.makeText(ChatActivity.this, "ID: "
                + messageReceiverID + "\n Name:"
                + messageReceiverName, Toast.LENGTH_SHORT)
                .show();
        InitializeControllers();


        //checkPermissions();
        pictureService = PictureCapturingServiceImpl.getInstance(this);

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                letterCount = 0;
                SendMessage();
            }
        });
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        DisplayLastSeen();

        //check if the text box was clicked
        MessageInputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the user has already typed a few words
                if (letterCount == 0) {
                    //if no - meaning beginning of the sentence then
                    ChatActivity.this.showToast("Starting capture!");
                    pictureService.startCapturing(ChatActivity.this);
                }
                letterCount++;
            }
        });

//        MessageInputText.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_UP) {
//
//                    if (letterCount == 0) {
//
//                        ChatActivity.this.showToast("Starting capture!");
//                        //pictureService.startCapturing(ChatActivity.this);
//                    }
//                    letterCount++;
//                }
//                return false;
//            }
//        });
    }

    private void showToast(final String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage() {
        String messageText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(ChatActivity.this, "Please enter your message", Toast.LENGTH_SHORT).show(); //notify user
        } else {
            //detect text semantic
            detectText(messageText);


            //detect final emotions
            FinalEmotions finalEmotions = new FinalEmotions();
            emotion =  finalEmotions.getEMotion();

            /*
            if (smileProb == 0.0) {
                emotion = "face not captured";
                showToast("pls click text box to face");
            } else {

                if (smileProb < 0.35) {
                    emotion = "negative";
                } else if (smileProb > 0.65) {
                    emotion = "positive";
                } else {
                    emotion = "neutral";
                }
            }
            */

            //create reference in message node with send and receiver ids
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            //create a unique key for each chat created to enable users send several messages in one chat
            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();       //push key to database

            //get the unique key
            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("emotion", emotion);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);
            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show(); //notify user
                        //notification message
                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from", messageSenderID);
                        chatNotificationMap.put("type", "message");

                        NotificationRef.child(messageReceiverID).child(messageSenderID).push()
                                .setValue(chatNotificationMap);

                    } else {
                        Toast.makeText(ChatActivity.this, " Error", Toast.LENGTH_SHORT).show(); //notify user

                    }
                    MessageInputText.setText("");
                }
            });
        }
    }


    public void detectText(String text) {

        String edittedText = text.replaceAll(" ", "+");
        //new getApiResponse().execute(edittedText);
        //test(edittedText);
        //testRequest(edittedText);
        new testRequestClass().execute(edittedText);
    }
    //ibm emotion text
    private static class testRequestClass extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String api_key = "apikey:WPR5MsYLDrCl02b4S9UtR_4y42wqzx82HdDJN2dLhi8D";

            byte[] authEncBytes = Base64.encode(api_key.getBytes(), Base64.DEFAULT);
            String authStringEnc = new String(authEncBytes);
            URL url = null;
            try {
                url = new URL("https://gateway.watsonplatform.net/tone-analyzer/api/v3/tone?version=2017-09-21&text=" + strings[0]); // remember not to hardcode this

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
                connection.setRequestMethod("GET");
                Log.d("testRequest Log", "" + connection.getResponseCode());
                try {
                    BufferedReader responseInputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String jsonOutput = null;
                    Log.d("testRequest inputStream", "Output from Server .... \n");
                    while ((jsonOutput = responseInputStream.readLine()) != null) {
                        stringBuilder.append(jsonOutput);
                        //DEBUG
                        Log.d("testRequest inputStream", "cresting final response string");
                    }

                    String response = String.valueOf(stringBuilder);
                    Log.d("testRequest Log", response);
                    FinalEmotions finalEmotions = new FinalEmotions();
                    finalEmotions.setTextEmotion(response);

                } finally {
                    connection.disconnect();
                }


            } catch (Exception e) {
                e.printStackTrace();
                Log.d("testRequest Log", "" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
        }
    }


    public static String getQueryString(Map<String, String> params) {
        /* This function is responsible for creating a query string given the parameters */
        StringBuilder sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0) {
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), "UTF-8"));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }

        return String.valueOf(sbParams);
    }

    private static class getEmotionFromImage extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            // get image and convert to base 64 ( this should actually be moved to another function )
            String imagePath = strings[0];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            //rotate image
            Matrix matrix = new Matrix();
            matrix.postRotate(180);

            Bitmap bitmapRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            bitmapRotated.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //Bitmap scaled = Bitmap.createScaledBitmap(bitmapRotated, bitmapRotated.getWidth(), bitmapRotated.getHeight(), true);

            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            String result ="";
            FinalEmotions finalEmotions = new FinalEmotions();

            URL url = null;
            try {
                url = new URL("https://api.cloudinary.com/v1_1/simi-pam/auto/upload");

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Accept-Charset", "UTF-8");

                // create the post parameters
                Map params = new HashMap();
                params.put("upload_preset", "recognize");
                params.put("file", "data:image/jpeg;base64," + base64Image);

                connection.connect();

                Log.d("emotionRequest ->", getQueryString((params)));
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(getQueryString(params));
                wr.flush();
                wr.close();

                Log.d("emotionRequest ResCode", "" + connection.getResponseCode());
                try {
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder stringBuilder = new StringBuilder();
                    String jsonOutput = null;
                    Log.d("emotionReq inputStream", "Output from Server .... \n");
                    while ((jsonOutput = reader.readLine()) != null) {
                        stringBuilder.append(jsonOutput);
                        //DEBUG
                        Log.d("emotionReq inputStream", "cresting final response string");
                    }

                    Log.d("emotionReq Log", String.valueOf(stringBuilder));
                    result=String.valueOf(stringBuilder);
                    finalEmotions.setSmileProb(result);

                } finally {
                    connection.disconnect();
                }


            } catch (Exception e) {
                e.printStackTrace();
                Log.d("emotionReq Log", "" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }



    private void InitializeControllers() {


        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
//to access custom chat bar
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


    }

    private void DisplayLastSeen() {
        //check database for user status
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child("userState").hasChild("state")) {
                            //retrieving state last seen time and date
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            //if the retrived state is online
                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            }
                            //if the retrived state is offline
                            else if (state.equals("offline")) {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        } else {
                            //if user has no state
                            userLastSeen.setText("offline");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (pictureData != null && pictureUrl != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //convert byte array 'pictureData'
                    // to a bitmap (no need to read the file from the external storage)
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                    //scale image to avoid POTENTIAL
                    // "Bitmap too large to be uploaded into a texture" when displaying into an ImageView
                    final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));

                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    final Bitmap bitmapRotated = Bitmap
                            .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    final Bitmap scaled = Bitmap
                            .createScaledBitmap(bitmapRotated, 512, nh, true);
                    //check if  front camera pic
                    if (pictureUrl.contains("1_pic.jpg")) {
                        Log.d("answer photo", pictureUrl);
                        //send the image to be detected
                        // UploadFile(scaled);
                        //UploadPhoto(new File(pictureUrl));
                        //detectEmotionFromFace(pictureUrl);
                        Log.d("emotionRequest", "about to get emotion");
                        new getEmotionFromImage().execute(pictureUrl);
                        Log.d("emotionRequest", "emotion obtained");

                    }
                }
            });
            showToast("Picture saved to " + pictureUrl);

        }
    }

    private void UploadFile(Bitmap scaled) {

        // High-accuracy landmark detection and face classification
        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(scaled);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(highAccuracyOpts);

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        // ...
                                        // [START_EXCLUDE]
                                        // [START get_face_info]
                                        for (FirebaseVisionFace face : faces) {
                                            Rect bounds = face.getBoundingBox();
                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                            // nose available):
                                            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                            if (leftEar != null) {
                                                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                            }

                                            // If classification was enabled:
                                            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                               // smileProb = face.getSmilingProbability();

                                                //Log.d("answer google", " : " + smileProb);
                                            }
                                            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                            }

                                            // If face tracking was enabled:
                                            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                                int id = face.getTrackingId();
                                            }
                                        }

                                        //Log.d("answer google", " : " + smileProb);

                                        // [END get_face_info]
                                        // [END_EXCLUDE]

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }


    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {

        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            showToast("Done capturing all photos!");
            return;
        }
        showToast("No camera detected!");

    }


    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }


    private void showError(String message) {
        Log.d("Error", message);
    }


    private void credentialProvider() {
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:d9d96f07-4b4f-4cfb-981d-7b40de3afcd2", // Identity pool ID
                Regions.US_EAST_1 // Region
        );
        setAmazonS3Client(credentialsProvider);
    }

    private void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider) {
        s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        transferUtility = new TransferUtility(s3, getApplicationContext());
    }

    public void UploadPhoto(File file) {
        // KEY and SECRET are gotten when we create an IAM user above
        //BasicAWSCredentials credentials = new BasicAWSCredentials(KEY, SECRET);
       // AmazonS3Client s3Client = new AmazonS3Client(credentials);
        Log.d("debug ag", "" + file);
        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        //.s3Client(s3Client)
                        .build();
        Log.d("debug ag", "12");
// "jsaS3" will be the folder that contains the file
        TransferObserver uploadObserver =
                transferUtility.upload("image-rekog101/", file);
        s3:
///20171126_170418.jpg
        Log.d("debug ag", "345");
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    if (TransferState.COMPLETED == state) {
                        Toast.makeText(getApplicationContext(), "Upload Completed!", Toast.LENGTH_SHORT).show();

                        file.delete();
                    } else if (TransferState.FAILED == state) {
                        file.delete();
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors

                showToast("ERROR IN bucket transfer");
            }

        });

// If your upload does not trigger the onStateChanged method inside your
// TransferListener, you can directly check the transfer state as shown here.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }
    }
}




