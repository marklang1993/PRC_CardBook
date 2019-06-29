package com.swws.marklang.prc_cardbook.activity.setting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.swws.marklang.prc_cardbook.R;

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
        initButtons();
        initSettings();
    }

    /**
     * Init. all buttons in this activity
     */
    public void initButtons() {
        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable
    }

    /**
     * Init. all settings
     */
    private void initSettings() {
        // Item 1
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

        // Item 2
        CheckBox jrCardDisplayByNumberCheckBox = (CheckBox) findViewById(R.id.jrCardDisplayByNumberCheckBox);
        jrCardDisplayByNumberCheckBox.setChecked(mSettingFileUtility.getBooleanValue(
                mSettingFileUtility.readItem("jr_card_display_by_number")));
        jrCardDisplayByNumberCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettingFileUtility.writeItem("jr_card_display_by_number",
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
