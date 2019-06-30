package com.swws.marklang.prc_cardbook.activity.qrcode;

public class QRCodeDecodeResult {

    public enum Result {
        OK,         // Decoding OK
        UNKNOWN,    // Unknown QR code: maybe invalid prichan item QR, or the data is not in LUT
        UPDATE      // Need to update local database
    }

    public Result DecodingResult = null;
    public String ItemImageID = null;   // If result is not OK, it will be null
    public String ItemSeasonID = null;  // If result is not OK, the value will be invalid
    public String JRColor = null;       // Will be used if this item is an JR item
}
