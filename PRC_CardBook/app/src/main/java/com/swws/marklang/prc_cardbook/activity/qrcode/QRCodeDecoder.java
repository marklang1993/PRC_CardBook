package com.swws.marklang.prc_cardbook.activity.qrcode;

import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

public class QRCodeDecoder extends Thread {

    private ImageReader mImageReader;
    private Semaphore mCapturePictureSemaphore;
    private Rect mCropAreaQRCode;

    public QRCodeDecoder(
            ImageReader imageReader,
            Semaphore capturePictureSemaphore,
            Rect cropAreaQRCode
    ) {
        super();

        // Set member variables
        mImageReader = imageReader;
        mCapturePictureSemaphore = capturePictureSemaphore;
        mCropAreaQRCode = cropAreaQRCode;
    }

    @Override
    public void run() {
        // When an image is available, use zxing to decode this image
        Image previewImage = mImageReader.acquireLatestImage();
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

}
