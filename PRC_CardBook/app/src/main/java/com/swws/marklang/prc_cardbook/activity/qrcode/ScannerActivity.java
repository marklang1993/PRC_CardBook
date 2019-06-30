package com.swws.marklang.prc_cardbook.activity.qrcode;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.activity.card.CardDetailActivity;

import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    // Internal variables
    private QRCodeDecoder mQRCodeDecoder;
    private DecoratedBarcodeView mDecoratedBarcodeView;
    private TextView mQRCodeStatusTextView;


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
        mDecoratedBarcodeView.decodeContinuous(new BarcodeCallback() {
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
                            startActivityForResult(startCardDetailActivityIntent, Constants.REQUEST_CARD_DETAIL_DISPLAY_DONE);
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
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mDecoratedBarcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
