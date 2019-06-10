package com.divya.sprxs.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.divya.sprxs.R;
import com.divya.sprxs.activity.HomeActivity;
import com.divya.sprxs.api.RetrofitClient;
import com.divya.sprxs.model.CreateIdeasRequest;
import com.divya.sprxs.model.CreateIdeasResponse;
import com.divya.sprxs.model.RefreshTokenResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class CreateIdeasFragment extends Fragment implements View.OnClickListener {

    private Button submitButton;
    private Button dismissButton;
    private ImageView attachButton;
    private String attachmentFile = null;
    private EditText ideaNameTextView;
    private EditText ideaDescriptionTextView;
    private TextView fileNameTextView;
    private int mySpinnerValue;
    Uri uri = null;
    private static final int PICK_FROM_GALLERY = 101;
    int columnIndex;
    FirebaseStorage firebaseStorage;
    FirebaseDatabase firebaseDatabase;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_create_ideas, container, false);

        getActivity().setTitle("Create Idea");
        ideaNameTextView = v.findViewById(R.id.ideaNameTextView);
        ideaDescriptionTextView = v.findViewById(R.id.ideaDescriptionTextView);
        fileNameTextView = v.findViewById(R.id.filenameTextView);
        submitButton = v.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this);
        dismissButton = v.findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(this);
        attachButton = v.findViewById(R.id.attachButton);
        attachButton.setOnClickListener(this);
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        List<String> categories = new ArrayList<>();
        categories.add(0, "I have a ");
        categories.add(1, "Technology idea");
        categories.add(2, "Lifestyle & Wellbeing idea");
        categories.add(3, "Food & Drink idea");
        categories.add(4, "Gaming idea");
        categories.add(5, "Business & Finance idea");
        categories.add(6, "Art and Fashion idea");
        categories.add(7, "Film,Theatre & Music idea");
        categories.add(8, "Media & Journalism idea");

        final Spinner spinner = v.findViewById(R.id.textSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mySpinnerValue = spinner.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mySpinnerValue = 0;
            }
        });

        return v;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            attachmentFile = cursor.getString(columnIndex);
            uri = Uri.parse("file://" + attachmentFile);
            if (attachmentFile != null) {
                attachmentFile = uri.getPath();
                int cut = attachmentFile.lastIndexOf('/');
                if (cut != -1) {
                    attachmentFile = attachmentFile.substring(cut + 1);
                }
            }
            fileNameTextView.setText(attachmentFile);
            cursor.close();


        } else if (resultCode != RESULT_CANCELED) {
            if (requestCode == PICK_FROM_GALLERY) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ImageView imageView = null;
                imageView.setImageBitmap(photo);
            }
        }

    }


    public void onClick(View v1) {

        switch (v1.getId()) {
            case R.id.submitButton:
                createIdea();
                break;
            case R.id.dismissButton:
                myHome();
                break;
            case R.id.attachButton:
                openActivity();
                break;
        }
    }

    private void openActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return_data", true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_GALLERY);
    }


    public void myHome() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);

    }


    public void createIdea() {
        String ideaName = ideaNameTextView.getText().toString().trim();
        String ideaDescription = ideaDescriptionTextView.getText().toString().trim();
        String fileName = fileNameTextView.getText().toString().trim();
        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String lkp_email = prefs.getString("email", null);
        final String token = prefs.getString("token", null);
        final String refresh_token = prefs.getString("refresh_token", null);
        final String AllUserFiles = "AllUsersFileData";
        final String ideasFolder = "myIdeas";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = user.getUid();
        Log.e("UID", UID);

        if (!fileName.isEmpty()) {
            StorageReference mStorageRef;
            mStorageRef = FirebaseStorage.getInstance().getReference();

            StorageReference riversRef = mStorageRef.child(AllUserFiles).child(UID).child(ideasFolder).child(attachmentFile);

            riversRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Log.d("IMAGE UPLOAD", "SUCCESS");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Log.e("FAILED TO UPLOAD FILE ", "TO FIREBASE");
                        }
                    });

            Log.d("IMAGE PATH ", uri.getPath());
        }

        final SharedPreferences.Editor editor = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        Call<CreateIdeasResponse> call;
        call = RetrofitClient.getInstance().getApi().createIdea(
                "Bearer " + token,
                new CreateIdeasRequest(mySpinnerValue, 2, 3, ideaName, ideaDescription, "", fileName, ""));

        call.enqueue(new Callback<CreateIdeasResponse>() {
            @Override
            public void onResponse(Call<CreateIdeasResponse> call, Response<CreateIdeasResponse> response) {

                CreateIdeasResponse createIdeasResponse = response.body();

                if (response.code() == 201 || response.code() == 200) {
                    if (createIdeasResponse.getCreateIdea_response().contentEquals("PASS")) {
                        Toast.makeText(getContext(), "Idea minted on blockchain with ID " + createIdeasResponse.getIdea_ID(), Toast.LENGTH_LONG).show();
                        Log.e("token+",token);
                    }

                } else if (response.code() == 401) {
                    Call<RefreshTokenResponse> callrefresh;
                    callrefresh = RetrofitClient.getInstance().getApi().refreshToken(
                            "Bearer " + refresh_token);

                    callrefresh.enqueue(new Callback<RefreshTokenResponse>() {
                        @Override
                        public void onResponse(Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) {
                            RefreshTokenResponse refreshTokenResponse = response.body();
                            editor.putString("token", refreshTokenResponse.getAccess_token());
                            editor.apply();
                            createIdea();
                        }
                        @Override
                        public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
                        }
                    });
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(getActivity(), jObjError.getString("createIdea_message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<CreateIdeasResponse> call, Throwable t) {
            }
        });
    }


}