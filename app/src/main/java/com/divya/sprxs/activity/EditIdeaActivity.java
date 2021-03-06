package com.divya.sprxs.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.divya.sprxs.R;
import com.divya.sprxs.api.RetrofitClient;
import com.divya.sprxs.model.EditIdeaRequest;
import com.divya.sprxs.model.EditIdeaResponse;
import com.divya.sprxs.model.RefreshTokenResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.divya.sprxs.activity.LoginActivity.MY_PREFS_NAME;

public class EditIdeaActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText ideaNameEditTextView, ideaDescriptionEditTextView, filenameEditTextView, filenameFileEditTextView;
    private ImageView attachEditButton, attachFileEditButton,image;
    private Button confirmEditButton, cancelEditButton;
    private TextView ideaIdEditTextView;
    private ProgressBar progressBar;
    private Spinner spinnerEditText;
    private int mySpinnerValue;
    private String attachmentFile = null;
    private String attachmentFileName = null;
    private Uri uri = null;
    private Uri uriFile = null;
    private static final int PICK_FROM_GALLERY = 1;
    private static final int PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_idea);

        this.setTitle("Edit Idea");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        image = findViewById(R.id.image);
        ideaNameEditTextView = findViewById(R.id.ideaNameEditTextView);
        ideaDescriptionEditTextView = findViewById(R.id.ideaDescriptionEditTextView);
        filenameEditTextView = findViewById(R.id.filenameEditTextView);
        filenameFileEditTextView = findViewById(R.id.filenameFileEditTextView);
        ideaIdEditTextView = findViewById(R.id.ideaIdEditTextView);
        attachEditButton = findViewById(R.id.attachEditButton);
        attachEditButton.setOnClickListener(this);
        attachFileEditButton = findViewById(R.id.attachFileEditButton);
        attachFileEditButton.setOnClickListener(this);
        confirmEditButton = findViewById(R.id.confirmEditButton);
        confirmEditButton.setOnClickListener(this);
        cancelEditButton = findViewById(R.id.cancelEditButton);
        cancelEditButton.setOnClickListener(this);

        ideaDescriptionEditTextView.setMovementMethod(new ScrollingMovementMethod());

        final String IdeaId = getIntent().getStringExtra("myList");
        final String IdeaDesc = getIntent().getStringExtra("myListIdeaDesc");
        final String IdeaName = getIntent().getStringExtra("myListIdeaName");
        ideaIdEditTextView.setText("#" + IdeaId);
        ideaNameEditTextView.setText(IdeaName);
        ideaDescriptionEditTextView.setText(IdeaDesc);

        progressBar = findViewById(R.id.loadingPanel);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FD7E14"), PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.GONE);


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

        spinnerEditText = findViewById(R.id.textSpinnerEdit);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerEditText.setAdapter(adapter);
        spinnerEditText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mySpinnerValue = spinnerEditText.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mySpinnerValue = 0;
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PICK_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    uri = data.getData();
                    attachmentFile = String.valueOf(uri);
//                    ContentResolver cR = getActivity().getApplicationContext().getContentResolver();
//                    MimeTypeMap mime = MimeTypeMap.getSingleton();
//                     type = mime.getExtensionFromMimeType(cR.getType(uri));

                        Cursor cursor = getContentResolver().query(uri, null, null, null);
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        cursor.moveToFirst();
                        filenameEditTextView.setText(cursor.getString(nameIndex));
                        cursor.close();
                }
                break;
            case PICK:
                if (resultCode == RESULT_OK) {
                    uriFile = data.getData();
                    attachmentFileName = String.valueOf(uriFile);
//                    ContentResolver cR = getActivity().getApplicationContext().getContentResolver();
//                    MimeTypeMap mime = MimeTypeMap.getSingleton();
//                     type = mime.getExtensionFromMimeType(cR.getType(uri));
                    Cursor cursor = getContentResolver().query(uriFile, null, null, null);
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    filenameFileEditTextView.setText(cursor.getString(nameIndex));
                    cursor.close();

                }
                break;

        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirmEditButton:
                editIdea();
                break;
            case R.id.cancelEditButton:
                onSupportNavigateUp();
                break;
            case R.id.attachEditButton:
                openActivity();
                break;
            case R.id.attachFileEditButton:
                openFileActivity();
        }

    }

    private void openFileActivity() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK);
    }

    private void openActivity() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_GALLERY);
    }

    private void editIdea() {
        String ideaName = ideaNameEditTextView.getText().toString().trim();
        String ideaDescription = ideaDescriptionEditTextView.getText().toString().trim();
        final String fileName = filenameEditTextView.getText().toString().trim();
        final String fileNameFile = filenameFileEditTextView.getText().toString().trim();

        final String AllUserFiles = "AllUsersFileData";
        final String ideasFolder = "myIdeas";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = user.getUid();

        if (ideaName.isEmpty()) {
            ideaNameEditTextView.setError("This Field is required");
            ideaNameEditTextView.requestFocus();
            return;
        } else if (ideaDescription.isEmpty()) {
            ideaDescriptionEditTextView.setError("This Field is required");
            ideaDescriptionEditTextView.requestFocus();
            return;
        }

        if (!fileName.isEmpty()) {

            StorageReference mStorageRef;

            mStorageRef = FirebaseStorage.getInstance().getReference();

            StorageReference riversRef = mStorageRef.child(AllUserFiles).child(UID).child("coverPhoto").child(fileName);

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
                            Log.e("FAILED TO UPLOAD FILE ", "TO FIREBASE");
                        }
                    });
            Log.d("IMAGE PATH ", uri.getPath());


           Log.e("......",mStorageRef.getDownloadUrl().toString()) ;



        } else if (!fileNameFile.isEmpty()) {
            StorageReference mStorageRef;

            mStorageRef = FirebaseStorage.getInstance().getReference();

            StorageReference riversRef = mStorageRef.child(AllUserFiles).child(UID).child(ideasFolder).child(fileNameFile);

            riversRef.putFile(uriFile)
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
                            Log.e("FAILED TO UPLOAD FILE ", "TO FIREBASE");
                        }
                    });
            Log.d("IMAGE PATH ", uriFile.getPath());
        }

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        final SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        final String token = prefs.getString("token", null);
        final String refresh_token = prefs.getString("refresh_token", null);
        final String IdeaId = getIntent().getStringExtra("myList");

        Call<EditIdeaResponse> call;
        progressBar.setVisibility(View.VISIBLE);
        call = RetrofitClient.getInstance().getApi().editIdea(
                "Bearer " + token,
                new EditIdeaRequest(IdeaId, mySpinnerValue, 2, 3, ideaName, ideaDescription, "", fileNameFile, "", fileName, ""));
        call.enqueue(new Callback<EditIdeaResponse>() {
            @Override
            public void onResponse(Call<EditIdeaResponse> call, Response<EditIdeaResponse> response) {
                EditIdeaResponse editIdeaResponse = response.body();
                if (response.code() == 200) {
                    final View successDialogView = LayoutInflater.from(EditIdeaActivity.this).inflate(R.layout.success_dialog, null);
                    final Dialog dialog = new Dialog(EditIdeaActivity.this);
                    dialog.setContentView(R.layout.success_dialog);
                    TextView textView;
                    textView = successDialogView.findViewById(R.id.dialogTextView);
                    textView.setText("Idea has been edited with ID " + editIdeaResponse.getIdea_ID());
                    Button button;
                    button = successDialogView.findViewById(R.id.okButton);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setContentView(successDialogView);
                    dialog.show();
                    progressBar.setVisibility(View.GONE);
                    filenameEditTextView.setText("");
                    filenameFileEditTextView.setText("");
                } else if (response.code() == 401) {
                    Call<RefreshTokenResponse> callrefresh;
                    callrefresh = RetrofitClient.getInstance().getApi().refreshToken(
                            "Bearer " + refresh_token);

                    callrefresh.enqueue(new Callback<RefreshTokenResponse>() {
                        @Override
                        public void onResponse(Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) {
                            if (response.code() == 200) {
                                RefreshTokenResponse refreshTokenResponse = response.body();
                                editor.putString("token", refreshTokenResponse.getAccess_token());
                                editor.apply();
                                editIdea();
                            } else {
//                                try {
//                                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                                final View errorDialogView = LayoutInflater.from(EditIdeaActivity.this).inflate(R.layout.error_dialog, null);
                                final Dialog dialog = new Dialog(EditIdeaActivity.this);
                                dialog.setContentView(R.layout.error_dialog);
                                TextView textView;
                                textView = errorDialogView.findViewById(R.id.dialogTextView);
                                textView.setText("Technical Error.Please try again later");
                                Button button;
                                button = errorDialogView.findViewById(R.id.okButton);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.setContentView(errorDialogView);
                                dialog.show();
                                progressBar.setVisibility(View.GONE);
//                                } catch (Exception e) {
//                                    final View errorDialogView = LayoutInflater.from(EditIdeaActivity.this).inflate(R.layout.error_dialog, null);
//                                    final Dialog dialog = new Dialog(EditIdeaActivity.this);
//                                    dialog.setContentView(R.layout.error_dialog);
//                                    TextView textView;
//                                    textView = errorDialogView.findViewById(R.id.dialogTextView);
//                                    textView.setText("Technical Error\nPlease try again later");
//                                    Button button;
//                                    button = errorDialogView.findViewById(R.id.okButton);
//                                    button.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            dialog.dismiss();
//                                        }
//                                    });
//                                    dialog.setContentView(errorDialogView);
//                                    dialog.show();
//                                    progressBar.setVisibility(View.GONE);
//                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
                        }
                    });

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        final View errorDialogView = LayoutInflater.from(EditIdeaActivity.this).inflate(R.layout.error_dialog, null);
                        final Dialog dialog = new Dialog(EditIdeaActivity.this);
                        dialog.setContentView(R.layout.error_dialog);
                        TextView textView;
                        textView = errorDialogView.findViewById(R.id.dialogTextView);
//                        textView.setText("Please complete all the fields");
                        textView.setText(jObjError.getString("error"));
                        Button button;
                        button = errorDialogView.findViewById(R.id.okButton);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setContentView(errorDialogView);
                        dialog.show();
                        progressBar.setVisibility(View.GONE);
                    } catch (Exception e) {
                        final View errorDialogView = LayoutInflater.from(EditIdeaActivity.this).inflate(R.layout.error_dialog, null);
                        final Dialog dialog = new Dialog(EditIdeaActivity.this);
                        dialog.setContentView(R.layout.error_dialog);
                        TextView textView;
                        textView = errorDialogView.findViewById(R.id.dialogTextView);
                        textView.setText("Technical Error.Please try again later");
                        Button button;
                        button = errorDialogView.findViewById(R.id.okButton);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setContentView(errorDialogView);
                        dialog.show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<EditIdeaResponse> call, Throwable t) {
            }
        });
    }


}
