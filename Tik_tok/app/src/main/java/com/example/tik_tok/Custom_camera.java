package com.example.tik_tok;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tik_tok.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.example.tik_tok.utils.Utils.getOutputMediaFile;

public class Custom_camera extends AppCompatActivity {
    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera);
        mSurfaceView = findViewById(R.id.img);
        final Button picture_btn = findViewById(R.id.btn_picture);
        final Button record_btn = findViewById(R.id.btn_record);
        final Button facing_btn = findViewById(R.id.btn_facing);
        final Button zoom_btn = findViewById(R.id.btn_zoom);
        mCamera = getCamera(CAMERA_TYPE);
        final SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try{
                    mCamera.setPreviewDisplay(surfaceHolder);
                }catch (IOException e){
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        });

        picture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null,null,mPicture);
                Toast.makeText(Custom_camera.this,"picture taken",Toast.LENGTH_SHORT).show();
                Custom_camera.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE))));
            }
        });

        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    record_btn.setText("rec-off");
                    releaseMediaRecorder();
                    isRecording = false;
                } else {
                    record_btn.setText("rec-on");
                    prepareVideoRecorder();
                    try{
                        mMediaRecorder.prepare();
                        mMediaRecorder.start();
                    }catch (Exception e){
                        releaseMediaRecorder();
                        return;
                    }
                    isRecording = true;
                }
            }
        });

        facing_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_BACK){
                    CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_FRONT;
                }else {
                    CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                mCamera = getCamera(CAMERA_TYPE);
                try{
                    mCamera.setPreviewDisplay(surfaceHolder);
                }catch (IOException e){
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }
        });

        zoom_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        if (b){
                            camera.cancelAutoFocus();
                            doAutoFocus();
                        }
                    }
                });
            }
        });
    }

    private void doAutoFocus () {
        Camera.Parameters p = mCamera.getParameters();
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(p);
    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);
        cam.setDisplayOrientation(getCameraDisplayOrientation(CAMERA_TYPE));
        return cam;
    }

    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;
    private int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;
        } else {
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }

    private void releaseCameraAndPreview() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private MediaRecorder mMediaRecorder;

    private void prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile(getOutputMediaFile(Utils.MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
    }

    private void releaseMediaRecorder() {
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                return;
            }
            try{
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bytes);
                fos.close();
            }catch (IOException e){
                Log.d("mPicture", "Error accessing file: " + e.getMessage());
            }
            mCamera.startPreview();
        }
    };
}
