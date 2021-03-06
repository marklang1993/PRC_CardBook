package com.swws.marklang.prc_cardbook.activity.about.license;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.activity.main.MainLoadActivity;

import java.util.ArrayList;

public class LicenseItemAdapter extends BaseAdapter {

    // Internal variables
    private LayoutInflater mInflater;
    private ArrayList<LicenseItem> mLicenseItems;

    /**
     * Constructor
     */
    public LicenseItemAdapter(ArrayList<LicenseItem> licenseItems) {
        mInflater = (LayoutInflater) MainLoadActivity.getCurrentApplicationContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLicenseItems = licenseItems;
    }

    @Override
    public int getCount() {
        return mLicenseItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mLicenseItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.license_listview, null);
        TextView licenseLibraryNameTextView = (TextView) view.findViewById(R.id.licenseLibraryNameTextView);
        TextView licenseCopyrightHolderTextView = (TextView) view.findViewById(R.id.licenseCopyrightHolderTextView);
        TextView licenseRepoLinkTextView = (TextView) view.findViewById(R.id.licenseRepoLinkTextView);
        TextView licenseContentTextView = (TextView) view.findViewById(R.id.licenseContentTextView);
        TextView licenseExpandTipTextView = (TextView) view.findViewById(R.id.licenseExpandTipTextView);

        // Set contents
        final LicenseItem licenseItem = (LicenseItem) getItem(position);
        licenseLibraryNameTextView.setText(licenseItem.LibraryName);
        licenseCopyrightHolderTextView.setText(licenseItem.CopyrightHolder);
        licenseRepoLinkTextView.setText(licenseItem.RepositoryLink);
        licenseContentTextView.setText(licenseItem.LicenseContent);

        if (licenseItem.IsShrinkLicenseContentDisplay) {
            licenseContentTextView.setMaxLines(3);
            licenseExpandTipTextView.setText("＋");

        } else {
            licenseExpandTipTextView.setText("ー");
        }

        // Set onClickListeners
        licenseExpandTipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Expand/Shrink "licenseContentTextView"
                licenseItem.IsShrinkLicenseContentDisplay = !licenseItem.IsShrinkLicenseContentDisplay;
                // Update
                LicenseItemAdapter.this.notifyDataSetChanged();
            }
        });

        licenseRepoLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String repoLink = licenseItem.RepositoryLink;
                if (repoLink != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(repoLink));
                    MainActivity.getMainActivity().startActivity(intent);
                }
            }
        });

        return view;
    }
}
