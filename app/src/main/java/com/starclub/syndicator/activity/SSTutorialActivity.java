package com.starclub.syndicator.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAppController;
import com.starclub.syndicator.SSConstants;
import com.starclub.syndicator.customcontrol.CustomFontTextView;
import com.viewpagerindicator.CirclePageIndicator;

public class SSTutorialActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        CirclePageIndicator pageIndicator = (CirclePageIndicator)findViewById(R.id.indicator);

        IntroAdapter adapter = new IntroAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        pageIndicator.setViewPager(viewPager);
        pageIndicator.setPageColor(0x66FFFFFF);
        pageIndicator.setFillColor(0xEEFFFFFF);
        pageIndicator.setStrokeWidth(0);

        pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                System.out.println("position = " + position + " positionOffset = " + positionOffset + " positionOffsetPixels = " + positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (SSAppController.sharedInstance().firstTimeLoaded()) {
            String urlString = SSConstants.WEB_SERVICE_ROOT + "/auth.php?fromApp=1";
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(urlString));
            startActivity(i);
        }
    }

    public static class IntroFragment extends android.support.v4.app.Fragment {

        private static final String KEY_IMAGE = "image";
        private static final String KEY_TITLE = "title";
        private static final String KEY_DESC = "description";

        public static Fragment newInstance(int image, String title, String desc) {
            Bundle args = new Bundle();

            args.putInt(KEY_IMAGE, image);
            args.putString(KEY_TITLE, title);
            args.putString(KEY_DESC, desc);

            Fragment fragment = new IntroFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.cell_tutorial, container, false);

            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            ImageView imageView = (ImageView) view.findViewById(R.id.tutorial_image);
            CustomFontTextView tvTitle = (CustomFontTextView) view.findViewById(R.id.tutorial_title);
            CustomFontTextView tvDesc = (CustomFontTextView) view.findViewById(R.id.tutorial_desc);
            CustomFontTextView btnStart = (CustomFontTextView) view.findViewById(R.id.tutorial_start);

            if (getArguments().getInt(KEY_IMAGE) == -1) { // last
                imageView.setVisibility(View.GONE);
                tvTitle.setVisibility(View.GONE);
                tvDesc.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SSAppController.sharedInstance().saveSeenTutorial();

                        getActivity().startActivity(new Intent(getActivity(), SSLoginActivity.class));
                        getActivity().finish();
                    }
                });
            }
            else {
                imageView.setVisibility(View.VISIBLE);
                tvTitle.setVisibility(View.VISIBLE);
                tvDesc.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);

                try {
                    imageView.setImageResource(getArguments().getInt(KEY_IMAGE));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    tvTitle.setText(getArguments().getString(KEY_TITLE));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    tvDesc.setText(getArguments().getString(KEY_DESC));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class IntroAdapter extends FragmentStatePagerAdapter {
        public IntroAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return IntroFragment.newInstance(R.drawable.tutorial_0, "Welcome to StarSite.", "Swipe to continue.");
                case 1:
                    return IntroFragment.newInstance(R.drawable.tutorial_1, "Create", "Capture new photos or videos directly or select existing content from your media library.");
                case 2:
                    return IntroFragment.newInstance(R.drawable.tutorial_2, "Syndicate", "Post your content automatically to Facebook, Twitter and Instagram to drive maximum traffic and revenue.");
                case 3:
                    return IntroFragment.newInstance(R.drawable.tutorial_3, "Track", "View revenue earned and detailed post performance.");
                case 4:
                    return IntroFragment.newInstance(R.drawable.tutorial_4, "Grow", "Estimate your future revenue potential based on posting frequency.");
                case 5:
                    return IntroFragment.newInstance(-1, "Start", "");
            }
            throw new RuntimeException("I like potatoes");
        }

        @Override
        public int getCount() {
            return 6;
        }
    }
}

