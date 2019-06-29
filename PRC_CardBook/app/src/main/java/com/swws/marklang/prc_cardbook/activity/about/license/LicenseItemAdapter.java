package com.swws.marklang.prc_cardbook.activity.about.license;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;

import java.util.ArrayList;

public class LicenseItemAdapter extends BaseAdapter {

    private ArrayList<LicenseItem> mLicenseItems;

    // Internal variables
    private LayoutInflater mInflater;

    /**
     * Constructor
     */
    public LicenseItemAdapter(Context context, ArrayList<LicenseItem> licenseItems) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        TextView licenseContentTextView = (TextView)view.findViewById(R.id.licenseContentTextView);

        LicenseItem licenseItem = (LicenseItem) getItem(position);
        licenseLibraryNameTextView.setText(licenseItem.LibraryName);
        licenseCopyrightHolderTextView.setText(licenseItem.CopyrightHolder);
        licenseRepoLinkTextView.setText(licenseItem.RepositoryLink);
        licenseContentTextView.setText(licenseItem.LicenseContent);

        return view;
    }
}
