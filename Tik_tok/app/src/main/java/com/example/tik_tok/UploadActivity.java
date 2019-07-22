package com.example.tik_tok;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.tik_tok.bean.PostVideoResponse;
import com.example.tik_tok.utils.NetworkUtils;
import com.example.tik_tok.utils.ResourceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int GRANT_PERMISSION = 3;
    private static final String TAG = "UploadActivity";
    private Button mBtn;
    public Uri mSelectedImage;
    public Uri mSelectedVideo;

    List<Call> mCallList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initBtns();
    }

    private void initBtns() {
        mBtn = findViewById(R.id.button);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (s.equals("select_an_image")) {
                    if (requestReadExternalStoragePermission("select an image")) {
                        chooseImage();
                    }
                } else if (s.equals("select_a_video")) {
                    if (requestReadExternalStoragePermission("select a video")) {
                        chooseVideo();
                    }
                } else if (s.equals("post_it")) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                    }
                } else if (s.equals("success_try_refresh")) {
                    mBtn.setText("select_an_image");
                }
            }
        });
    }

    private boolean requestReadExternalStoragePermission(String explanation) {
        if (ActivityCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "You should grant external storage permission to continue " + explanation, Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(UploadActivity.this, new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, GRANT_PERMISSION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText("select_a_video");
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText("post_it");
            }
        }
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }


    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(UploadActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);

        MultipartBody.Part partImage = getMultipartFromUri("cover_image",mSelectedImage);
        MultipartBody.Part partVideo = getMultipartFromUri("video",mSelectedVideo);
        System.out.println(partImage.toString());
        Call<PostVideoResponse> postVideoResponseCall = NetworkUtils.getResponseWithRetrofitAsyncPost(
                "1120162023","WCY",partImage,partVideo
        );
        mCallList.add(postVideoResponseCall);
        System.out.println("123");

        postVideoResponseCall.enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                if(response.isSuccessful())
                {
                    //System.out.println("success");
                    Toast.makeText(UploadActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(UploadActivity.this,"发送失败",Toast.LENGTH_SHORT).show();
                }
                mBtn.setText("select_an_image");
                mBtn.setEnabled(true);
                mCallList.remove(call);
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable t) {

                System.out.println(t);
                System.out.println("fail!!!");
                mBtn.setText("select_an_image");
                mBtn.setEnabled(true);
                mCallList.remove(call);
            }
        });

    }
}
