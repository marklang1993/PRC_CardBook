package com.swws.marklang.prc_cardbook.activity.qrcode;

import android.util.Log;

import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.activity.qrcode.lut.QRCodeFileUtility;
import com.swws.marklang.prc_cardbook.activity.qrcode.lut.QRCodeLUTItem;
import com.swws.marklang.prc_cardbook.utility.database.Item;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;

public class QRCodeDecoder {

    // Debug only
    private static String lastDigest = "";

    // Constants
    private final int SIZE_MESSAGE_DIGEST = 40;
    private final String TYPE_MESSAGE_DIGEST = "SHA-1";

    /**
     * Generate MessageDigest
     * @param rawBytes
     * @return null if error occurs
     */
    private String generateMessageDigest(byte[] rawBytes) {
        StringBuilder sb = new StringBuilder(SIZE_MESSAGE_DIGEST);

        try {
            // Generate message digest
            MessageDigest md = MessageDigest.getInstance(TYPE_MESSAGE_DIGEST);
            md.update(rawBytes);
            byte[] digest = md.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            String hashText = bigInt.toString(16);

            // Now we need to zero pad it if you actually want the full SIZE_MESSAGE_DIGEST chars.
            for (int i = hashText.length(); i < SIZE_MESSAGE_DIGEST; ++i) {
                sb.append('0');
            }
            sb.append(hashText);

            //Log.i(getClass().getName(), "Message Digest: " + sb.toString());

        } catch (Exception ex) {
            return null;
        }

        return sb.toString();
    }

    /**
     * Decode raw bytes from QRCode
     * @param rawBytes
     */
    public QRCodeDecodeResult decode(byte[] rawBytes) {
        // Get message digest of the raw bytes
        String digest = generateMessageDigest(rawBytes);
        // Log.i(getClass().getName(), "Decoded: " + result.getText());

        // Init. decoding result
        QRCodeDecodeResult decodeResult = new QRCodeDecodeResult();

        // Find the possible corresponding item
        QRCodeFileUtility qrCodeFileUtility = QRCodeFileUtility.getInstance();
        QRCodeLUTItem lutItem = qrCodeFileUtility.getLUTItem(digest);
        if (lutItem == null) {
            // Item not found
            decodeResult.DecodingResult = QRCodeDecodeResult.Result.UNKNOWN;
            if (digest == null) {
                Log.e(getClass().getName(), "Digest is NULL");

            } else if (!digest.equals(lastDigest)) {
                lastDigest = digest;
                Log.e(getClass().getName(), "Item Not Found with Digest: " + digest);
            }

        } else {
            // Get corresponding Item
            HashMap<String, Item> itemIDLUT = MainActivity.getItemIDLUT();

            // Try to get item
            if (itemIDLUT.containsKey(lutItem.ImageID)) {
                // Item Found
                decodeResult.DecodingResult = QRCodeDecodeResult.Result.OK;
                decodeResult.ItemImageID = lutItem.ImageID;
                decodeResult.ItemSeasonID = lutItem.SeasonID;
                decodeResult.JRColor = lutItem.JRColor;

            } else {
                // Item not found -> Need to update local database
                decodeResult.DecodingResult = QRCodeDecodeResult.Result.UPDATE;
            }
        }

        return decodeResult;
    }
}
