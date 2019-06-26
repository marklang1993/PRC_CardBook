package com.swws.marklang.prc_cardbook.activity.setting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.SettingFileUtility;

public class SettingActivity extends AppCompatActivity {

    // Setting File Utility
    private SettingFileUtility mSettingFileUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Init. Setting File Utility
        mSettingFileUtility = SettingFileUtility.getInstance();

        // Init. UIs
        setTitle(R.string.setting_activity_name);
        initSettings();

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable
    }

    /**
     * Init. all settings
     */
    private void initSettings() {
        CheckBox cardNotPossessedWithoutColorCheckBox = (CheckBox) findViewById(R.id.cardNotPossessedWithoutColorCheckBox);
        cardNotPossessedWithoutColorCheckBox.setChecked(mSettingFileUtility.getBooleanValue(
                mSettingFileUtility.readItem("card_not_possessed_without_color")));
        cardNotPossessedWithoutColorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettingFileUtility.writeItem("card_not_possessed_without_color",
                        mSettingFileUtility.putBooleanValue(isChecked));
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
     * Save current setting once this activity closed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Save the current setting
        mSettingFileUtility.save();
    }
}
