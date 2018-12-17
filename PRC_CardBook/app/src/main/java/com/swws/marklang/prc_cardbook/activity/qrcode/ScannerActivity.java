package com.swws.marklang.prc_cardbook.activity.qrcode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
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

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.swws.marklang.prc_cardbook.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class ScannerActivity extends AppCompatActivity {

    private TextureView mQRCodeTextureView;
    private QRCodeAreaPreviewView mQRCodeAreaPreviewView;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mCaptureRequestBuilderSurfaceTexture;
    private CaptureRequest.Builder mCaptureRequestBuilderQRCodeReader;
    private ImageReader mImageReader;
    private Size mImageSize;
    private Handler mBackgroundHandler;
    private ArrayList<Surface> mSurfaces;
    private Semaphore mCapturePictureSemaphore = new Semaphore(1);
    private Rect mCropAreaQRCode;

    // Some constants
    private static final int CAMERA_INDEX = 0;      // Back camera index
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int QR_CODE_SIDE = 250;    // Side length of QR code image (square) in pixel



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
        mQRCodeTextureView = (TextureView) findViewById(R.id.qrcodeTextureView);
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

        // Init. mQRCodeAreaPreviewView
        mQRCodeAreaPreviewView = (QRCodeAreaPreviewView) findViewById(R.id.qrcodeImageView);
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
     * @return Surface that links to CaptureRequestBuilder
     */
    private Surface createCameraPreview() {
        try {
            // Init. SurfaceTexture
            SurfaceTexture surfaceTexture = mQRCodeTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mImageSize.getWidth(), mImageSize.getHeight());

            // Init. mCaptureRequestBuilderSurfaceTexture
            Surface surface = new Surface(surfaceTexture);
            mCaptureRequestBuilderSurfaceTexture = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilderSurfaceTexture.addTarget(surface); // Add the surfaceTexture (preview) to the capture target

            return surface;

        } catch (CameraAccessException ex) {
            // Failed to access camera
            Toast.makeText(getApplicationContext(), R.string.exception_access_camera_failed, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Create QRCode Image
     * @return Surface that links to CaptureRequestBuilder
     */
    private Surface createQRCodeImage() {
        try {
            // Init. mCaptureRequestBuilderQRCodeReader
            Surface surface = mImageReader.getSurface();
            mCaptureRequestBuilderQRCodeReader = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilderQRCodeReader.addTarget(surface); // Add the mImageReader (zxing) to the capture target

            return surface;

        } catch (CameraAccessException ex) {
            // Failed to access camera
            Toast.makeText(getApplicationContext(), R.string.exception_access_camera_failed, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Callback function fur init. surfaces and
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;

            // Linked & Get Surfaces
            Surface cameraPreviewSurface = createCameraPreview();
            Surface QRCodeImageSurface = createQRCodeImage();

            mSurfaces = new ArrayList<>(2);
            mSurfaces.add(cameraPreviewSurface);
            mSurfaces.add(QRCodeImageSurface);

            // Start an image capture session
            startImageCaptureSession(mSurfaces);
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
     * Bind Surfaces
     * @param surfaces
     */
    private void startImageCaptureSession(ArrayList<Surface> surfaces)
    {
        try {
            // Create image capture session
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (mCameraDevice == null) {
                        Log.e(getClass().getName(), "mCameraDevice is NULL!");
                        return;
                    }

                    // Entry Point: Start capturing images from updating preview
                    updatePreview(session);
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

    /**
     * Update Preview
     */
    private void updatePreview(CameraCaptureSession cameraCaptureSession) {
        if (mCameraDevice == null)  {
            Log.e(getClass().getName(), "mCameraDevice is NULL!");
            return;
        }
        mCaptureRequestBuilderSurfaceTexture.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            // Capture a low-quality image for preview
            cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilderSurfaceTexture.build(),
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            // Once finished, try to update QRCode reader
                            try {
                                if (mCapturePictureSemaphore.tryAcquire()) {
                                    /**
                                     * If QRCode processing is finished (semaphore is released),
                                     * 1. Stop generating preview by capturing images from camera;
                                     * 2. Start generate image for QRCode Reader
                                     */
                                    session.stopRepeating();
                                    updateQRCodeReader(session);
                                }
                            } catch (CameraAccessException e) {
                                // Failed to access camera
                                Toast.makeText(getApplicationContext(), R.string.exception_access_camera_failed, Toast.LENGTH_LONG).show();
                            }
                        }
                    }, mBackgroundHandler);

        } catch (CameraAccessException ex) {
            // Failed to access camera
            Toast.makeText(getApplicationContext(), R.string.exception_access_camera_failed, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Update QRCode Reader
     * @param cameraCaptureSession
     */
    private void updateQRCodeReader(CameraCaptureSession cameraCaptureSession) {
        if (mCameraDevice == null)  {
            Log.e(getClass().getName(), "mCameraDevice is NULL!");
            return;
        }
        mCaptureRequestBuilderQRCodeReader.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            // Capture a high-quality image once for QRCode decoder
            cameraCaptureSession.capture(mCaptureRequestBuilderQRCodeReader.build(),
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            // Once finished, restart updating preview
                            updatePreview(session);
                        }
                    }, mBackgroundHandler);

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
            String cameraId = cameraManager.getCameraIdList()[CAMERA_INDEX];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] availableImageSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
            mImageSize = availableImageSizes[4];  // Choose the highest resolution
            mCropAreaQRCode = getCropArea(mImageSize);

            // Init. mQRCodeAreaPreviewView
            mQRCodeAreaPreviewView.setCropAreaSize(new Size(QR_CODE_SIDE, QR_CODE_SIDE));
            mQRCodeAreaPreviewView.setPreviewImageSize(mImageSize);

            // Init. ImageReader (YUV420 format, maximum 1 image)
            mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.YUV_420_888, 1);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    // When an image is available, use zxing to decode this image
                    Image previewImage = imageReader.acquireLatestImage();
                    ByteBuffer previewByteBuffer = previewImage.getPlanes()[0].getBuffer();
                    byte[] previewBytes = new byte[previewByteBuffer.capacity()];
                    previewByteBuffer.get(previewBytes);

                    // Get the image size
                    int imageWidth = previewImage.getWidth();
                    int imageHeight = previewImage.getHeight();

                    // Must release the image after all data are achieved
                    previewImage.close();

                    // Convert the image (cropping should be done here)
                    PlanarYUVLuminanceSource luminanceSource = new PlanarYUVLuminanceSource(
                            previewBytes, imageWidth, imageHeight,  // image data
                            mCropAreaQRCode.left, mCropAreaQRCode.top, mCropAreaQRCode.width(), mCropAreaQRCode.height(), // crop area
                            false);
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
                    Reader qrReader = new MultiFormatReader();
                    try {
                        Result result = qrReader.decode(binaryBitmap);
                        Log.i(getClass().getName(), "Decoded: " + result.getText());

                    } catch (FormatException e) {
                        Log.e(getClass().getName(), "FormatException");

                    } catch (ChecksumException e) {
                        Log.e(getClass().getName(), "ChecksumException");

                    } catch (NotFoundException e) {
                        Log.e(getClass().getName(), "NotFoundException");
                    }

                    // Release the semaphore
                    mCapturePictureSemaphore.release();
                }
            }, mBackgroundHandler);

            // Check runtime camera permission since API 23
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Not grant this permission, request it
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }

            // Open Camera Device
            cameraManager.openCamera(cameraId, stateCallback, null);

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


    /**
     * Calculate the crop area for QRCode
     * @param previewSize
     * @return
     */
    private Rect getCropArea(Size previewSize) {
        int shorterSide;
        int finalSide;
        Rect cropArea = new Rect();

        // Find the shorter side
        if (previewSize.getWidth() < previewSize.getHeight()) {
            shorterSide = previewSize.getWidth();
        } else {
            shorterSide = previewSize.getHeight();
        }

        // Decode the final side (the shortest one)
        if (shorterSide < QR_CODE_SIDE) {
            finalSide = shorterSide;
        } else {
            finalSide = QR_CODE_SIDE;
        }

        // Set crop area
        cropArea.left = (int)((previewSize.getWidth() - finalSide) / 2.0f);
        cropArea.top = (int)((previewSize.getHeight() - finalSide) / 2.0f);
        cropArea.right = cropArea.left + finalSide;
        cropArea.bottom = cropArea.top + finalSide;

        return cropArea;
    }
}

