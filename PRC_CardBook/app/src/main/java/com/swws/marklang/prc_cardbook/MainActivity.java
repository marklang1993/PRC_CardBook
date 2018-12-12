package com.swws.marklang.prc_cardbook;

import android.app.Activity;
import android.os.Bundle;

import com.swws.marklang.prc_cardbook.utility.FileUtility;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FileUtility fileUtility = new FileUtility(getApplicationContext());
        fileUtility.ReadAllMetaData(true);
    }
}
