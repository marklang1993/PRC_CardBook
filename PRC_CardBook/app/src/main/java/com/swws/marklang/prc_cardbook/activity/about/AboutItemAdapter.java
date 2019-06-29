package com.swws.marklang.prc_cardbook.activity.about;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;

public class AboutItemAdapter extends BaseAdapter {

    // Constants
    private static int ABOUT_ITEM_COUNT = 3;

    // Internal variables
    private LayoutInflater mInflater;
    private Context mContext;

    /**
     * Constructor
     * @param context
     */
    public AboutItemAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public int getCount() {
        return ABOUT_ITEM_COUNT;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.about_listview, null);
        TextView aboutItemNameTextView = (TextView) view.findViewById(R.id.aboutItemNameTextView);
        TextView aboutItemValueTextView = (TextView) view.findViewById(R.id.aboutItemValueTextView);

        // TODO: remove the hardcode contents by using an array of a Class
        switch (position) {
            case 0:
                aboutItemNameTextView.setText(R.string.about_version_number);
                // Get version
                String version;
                try {
                    PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    version = "Error";
                }
                aboutItemValueTextView.setText(version);
                break;

            case 1:
                aboutItemNameTextView.setText(R.string.about_user_agreement);
                aboutItemValueTextView.setVisibility(View.INVISIBLE);
                break;

            case 2:
                aboutItemNameTextView.setText(R.string.about_license_info);
                aboutItemValueTextView.setVisibility(View.INVISIBLE);
                break;

            default:
                aboutItemNameTextView.setVisibility(View.INVISIBLE);
                aboutItemValueTextView.setVisibility(View.INVISIBLE);
                break;
        }

        return view;
    }
}
