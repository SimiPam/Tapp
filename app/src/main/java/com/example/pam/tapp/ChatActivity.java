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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;



public class ChatActivity extends AppCompatActivity implements PictureCapturingListener, ActivityCompat.OnRequestPermissionsResultCallback
{

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
    private DatabaseReference RootRef;
    private final static List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private int letterCount;
    private final String KEY = "AKIAJJ6YMQVHRA5XQXNQ";
    private final String SECRET = "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";
    // private com.amazonaws.services.rekognition.model.Image image;
    private final String apiEndpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0";
    private final String subscriptionKey = "03044fedc7d241cfa00d555dc3c0d7a4";
    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    private String emotion="";

    private  float smileProb;
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


        DisplayLastSeen();

        MessageInputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (letterCount == 0) {

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
    protected void onStart()
    {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
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

    private void SendMessage()
    {
        String messageText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this, "Please enter your message", Toast.LENGTH_SHORT).show(); //notify user
        }
        else
        {

            detectText(messageText);

            if (smileProb==0.0)
            {
                emotion="face not captured";
                showToast("pls click text box to face");
            }
            else {

                if (smileProb < 0.35) {
                    emotion = "sad";
                } else if (smileProb > 0.65) {
                    emotion = "happy";
                } else {
                    emotion = "neutral";
                }
            }

            //create reference in message node with send and receiver ids
            String messageSenderRef = "Messages/"+messageSenderID+ "/"+messageReceiverID;
            String messageReceiverRef = "Messages/"+messageReceiverID+ "/"+ messageSenderID;

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
            messageBodyDetails.put(messageSenderRef+"/"+messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushID, messageTextBody);
            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show(); //notify user

                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, " Error", Toast.LENGTH_SHORT).show(); //notify user

                    }
                    MessageInputText.setText("");
                }
            });
        }
    }

    private void InitializeControllers()
    {


        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
//to access custom chat bar
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
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

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state =dataSnapshot.child("userState").child("state").getValue().toString();
                            String date =dataSnapshot.child("userState").child("date").getValue().toString();
                            String time =dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: " + date +" " +time);
                            }
                        }
                        else
                        {
                            userLastSeen.setText("offline");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    public void detectText(String text) {

        String edittedText = text.replaceAll(" ","+");
        new getApiResponse().execute(edittedText);
    }

    private static class getApiResponse extends AsyncTask<String, String, String>
    {

        @Override
        protected String doInBackground(String... text) {
            try {
                Log.d("debug1","three");
                URL apiUrl = new URL("https://lecrcr8edk.execute-api.us-east-1.amazonaws.com/alpha/awsApiFunction?text="+text[0]);
                HttpsURLConnection connection = (HttpsURLConnection) apiUrl.openConnection();

                connection.setDoOutput(true);
                connection.setRequestMethod("POST");


                Log.d("debug1","one");
                /*
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));


                StringBuilder stringBuilder = new StringBuilder();

                String jsonOutput;
                while ((jsonOutput = bufferedReader.readLine())!= null)
                {
                    stringBuilder.append(jsonOutput);
                }

                //String message = org.apache.commons.io.IOUtils.toString(bufferedReader);

                //Log.d("answer 2", message);
                return stringBuilder.toString();
                */
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                Log.d("debug1","four");
                StringBuilder stringBuilder = new StringBuilder();
                String jsonOutput = null;
                Log.d("debug1","two");

                while ((jsonOutput = bufferedReader.readLine())!= null)
                {
                    stringBuilder.append(jsonOutput);
                }
                Log.d("debug1","five");
                return stringBuilder.toString();

            } catch (java.io.IOException e) {
                e.printStackTrace();
                Log.d("debug1", "error"+e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (response == null || response.equals("null")) {
                Log.d("results", "results not found 0");
            }
            else
            {
                ApiResponse apiResponse = new Gson().fromJson(response, ApiResponse.class);
                if (apiResponse != null) {
                    if (apiResponse.getText() != null) {
                        Log.d("results", apiResponse.getText());
                    } else {
                        Log.d("results", "results not found 1");

                        //showToast("results not found 1 ");
                    }
                } else {
                    Log.d("results", "results not found 2");
                    // showToast("results not found 2");
                }
            }
        }
    }


    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (pictureData != null && pictureUrl != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //convert byte array 'pictureData' to a bitmap (no need to read the file from the external storage)
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                    //scale image to avoid POTENTIAL "Bitmap too large to be uploaded into a texture" when displaying into an ImageView
                    final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));

                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    final Bitmap bitmapRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    final Bitmap scaled = Bitmap.createScaledBitmap(bitmapRotated, 512, nh, true);

                    if (pictureUrl.contains("1_pic.jpg")) {
                        Log.d("answer photo", pictureUrl);
                        UploadFile(scaled);
                        //detectAndFrame(scaled);

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
                                                  smileProb = face.getSmilingProbability();

                                                Log.d("answer google", " : "+smileProb);
                                            }
                                            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                            }

                                            // If face tracking was enabled:
                                            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                                int id = face.getTrackingId();
                                            }
                                        }

                                        Log.d("answer google", " : "+smileProb);

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
}


