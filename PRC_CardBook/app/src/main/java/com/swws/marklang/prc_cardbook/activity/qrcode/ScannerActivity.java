package com.swws.marklang.prc_cardbook.activity.qrcode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.swws.marklang.prc_cardbook.R;

import java.util.Arrays;

public class ScannerActivity extends AppCompatActivity {

    private TextureView mQRCodeTextureView;

    private String mCameraId;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private ImageReader mImageReader;
    private Size mImageSize;
    private Handler mBackgroundHandler;

    private static final int CAMERA_INDEX = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Set title
        setTitle(R.string.qr_scanner_activity_name);

        // Back Button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable

        // Check is camera device presented
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // Error: No camera
            Toast.makeText(this, R.string.exception_no_camera_device, Toast.LENGTH_LONG).show();
            finish();
        }

        // Init. mQRCodeTextureView
        mQRCodeTextureView = findViewById(R.id.qrcodeTextureView);
        mQRCodeTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                closeCamera();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    /**
     * Close this activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    /**
     * Create Camera Preview
     */
    private void createCameraPreview() {
        try {
            // Init. SurfaceTexture
            SurfaceTexture surfaceTexture = mQRCodeTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mImageSize.getWidth(), mImageSize.getHeight());

            // Init. Surface and mCaptureRequestBuilder
            Surface surface = new Surface(surfaceTexture);
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(surface);

            // Create image capture session
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (mCameraDevice == null) {
                        Log.e(getClass().getName(), "mCameraDevice is NULL!");
                        return;
                    }
                    mCameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(getClass().getName(), "Configure failed when create capture session!");
                    Toast.makeText(getApplicationContext(), R.string.exception_configure_failed_camera_capture_session, Toast.LENGTH_LONG).show();
                }
            }, null);

        } catch (CameraAccessException ex) {
            // Failed to access camera
            Toast.makeText(getApplicationContext(), R.string.exception_access_camera_failed, Toast.LENGTH_LONG).show();
        }
    }

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };

    /**
     * Update Preview
     */
    private void updatePreview() {
        if (mCameraDevice == null)  {
            Log.e(getClass().getName(), "mCameraDevice is NULL!");
            return;
        }
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);

        } catch (CameraAccessException ex) {
            // Failed to access camera
            Toast.makeText(getApplicationContext(), R.string.exception_access_camera_failed, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Open Camera Device
     */
    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Enumerate all camera devices & Get the size of video stream
            mCameraId = cameraManager.getCameraIdList()[CAMERA_INDEX];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mImageSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];

            // Check runtime camera permission since API 23
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Not grant this permission, request it
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }

            // Open Camera Device
            cameraManager.openCamera(mCameraId, stateCallback, null);

        } catch (CameraAccessException ex) {
            // Failed to access camera
            Toast.makeText(getApplicationContext(), R.string.exception_get_camera_list_failed, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Close Camera Device
     */
    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }
}

