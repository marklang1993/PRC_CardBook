package com.swws.marklang.prc_cardbook.activity.qrcode;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.activity.card.CardDetailActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class ScannerActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private final String[] mPermissions = {Manifest.permission.CAMERA};

    // Internal variables
    private QRCodeDecoder mQRCodeDecoder;
    private DecoratedBarcodeView mDecoratedBarcodeView;
    private TextView mQRCodeStatusTextView;
    private BarcodeCallback mBarcodeCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Init. mQRCodeDecoder
        mQRCodeDecoder = new QRCodeDecoder();

        // Init. mQRCodeStatusTextView
        mQRCodeStatusTextView = (TextView) findViewById(R.id.qrcodeStatusTextView);

        // Init. mDecoratedBarcodeView
        mDecoratedBarcodeView = (DecoratedBarcodeView) findViewById(R.id.scannerDecoratedBarcodeView);

        // Init. mBarcodeCallback
        mBarcodeCallback = new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    // Pause scanning
                    mDecoratedBarcodeView.pause();

                    // Get decode the raw bytes
                    byte[] rawBytes = result.getRawBytes();
                    QRCodeDecodeResult qrCodeDecodeResult = mQRCodeDecoder.decode(rawBytes);

                    // Check the result
                    switch (qrCodeDecodeResult.DecodingResult) {
                        case OK:
                            Intent startCardDetailActivityIntent = new Intent(ScannerActivity.this, CardDetailActivity.class);

                            // Passing params
                            startCardDetailActivityIntent.putExtra(
                                    CardDetailActivity.KEY_CARD_DETAIL_START_TYPE,
                                    CardDetailActivity.StartType.SCANNER.toString());
                            startCardDetailActivityIntent.putExtra(
                                    CardDetailActivity.KEY_ITEM_IMAGE_ID,
                                    qrCodeDecodeResult.ItemImageID);
                            startCardDetailActivityIntent.putExtra(
                                    CardDetailActivity.KEY_ITEM_SEASON_ID,
                                    qrCodeDecodeResult.ItemSeasonID);
                            startCardDetailActivityIntent.putExtra(
                                    CardDetailActivity.KEY_ITEM_INDICATED_JR_COLOR,
                                    qrCodeDecodeResult.JRColor);

                            // Start Activity
                            startActivityForResult(startCardDetailActivityIntent, Constants.REQUEST_AR_CARD_DETAIL_DISPLAY_DONE);
                            break;

                        case UNKNOWN:
                            mQRCodeStatusTextView.setText(R.string.qr_scanner_info_cannot_recognize);
                            // Scan again
                            mDecoratedBarcodeView.resume();
                            break;

                        case UPDATE:
                            mQRCodeStatusTextView.setText(R.string.qr_scanner_info_update_local_database);
                            // Scan again
                            mDecoratedBarcodeView.resume();
                            break;
                    }
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) { }
        };

        // Set activity title
        setTitle(R.string.qr_scanner_activity_name);

        // Check camera permission
        checkCameraPermission();
    }

    /**
     * Check and request CAMERA permission via EasyPermissions
     */
    private void checkCameraPermission() {
        if (EasyPermissions.hasPermissions(this, mPermissions)) {
            // Camera permission has already been grant
            mDecoratedBarcodeView.decodeContinuous(mBarcodeCallback);

        } else {
            // Request camera permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.permission_request_rationale_camera),
                    Constants.REQUEST_PERMISSION_CAMERA,
                    mPermissions
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDecoratedBarcodeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDecoratedBarcodeView.resume();
    }

    /**
     * Get result of requesting permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Camera permission is grant
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Constants.REQUEST_PERMISSION_CAMERA) {
            // Camera permission is grant
            mDecoratedBarcodeView.decodeContinuous(mBarcodeCallback);
        }
    }

    /**
     * Camera permission request is denied
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Permission is NOT granted by the user - show an error message
        Toast.makeText(this, R.string.exception_no_permission_runtime, Toast.LENGTH_LONG)
                .show();

        // Close this activity
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mDecoratedBarcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
