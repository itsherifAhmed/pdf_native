package com.androidmarket.pdfcreatoraaa.util;

import android.app.Activity;

import com.androidmarket.pdfcreatoraaa.adapter.AdapterExtractImages;
import com.androidmarket.pdfcreatoraaa.adapter.AdapterMergeFiles;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import androidmarket.R;

import com.androidmarket.pdfcreatoraaa.pdfModel.HomePageItem;

import java.util.ArrayList;
import java.util.Map;


public class CommonCodeUtils {

    Map<Integer, HomePageItem> mFragmentPositionMap;

    /**
     * updates the output recycler view if paths.size > 0
     * else give the main view
     */
    public void populateUtil(Activity mActivity, ArrayList<String> paths,
                             AdapterMergeFiles.OnClickListener onClickListener,
                             RelativeLayout layout, LottieAnimationView animationView,
                             RecyclerView recyclerView) {

        if (paths == null || paths.size() == 0) {
            layout.setVisibility(View.GONE);
        } else {
            // Init recycler view
            recyclerView.setVisibility(View.VISIBLE);
            AdapterMergeFiles adapterMergeFiles = new AdapterMergeFiles(mActivity,
                    paths, false, onClickListener);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(adapterMergeFiles);
            recyclerView.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));
        }
        animationView.setVisibility(View.GONE);
    }

    /**
     * sets the appropriate text to success Text View & display images in adapter
     */
    public void updateView(Activity mActivity, int imageCount, ArrayList<String> outputFilePaths,
                           TextView successTextView, LinearLayout options, RecyclerView mCreatedImages,
                           AdapterExtractImages.OnFileItemClickedListener listener) {

        if (imageCount == 0) {
            StringUtils.getInstance().showSnackbar(mActivity, R.string.extract_images_failed);
            return;
        }

        String text = String.format(mActivity.getString(R.string.extract_images_success), imageCount);
        StringUtils.getInstance().showSnackbar(mActivity, text);
        successTextView.setVisibility(View.VISIBLE);
        options.setVisibility(View.VISIBLE);
        AdapterExtractImages adapterExtractImages = new AdapterExtractImages(mActivity, outputFilePaths, listener);
        // init recycler view for displaying generated image list
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        successTextView.setText(text);
        mCreatedImages.setVisibility(View.VISIBLE);
        mCreatedImages.setLayoutManager(mLayoutManager);
        // set up adapter
        mCreatedImages.setAdapter(adapterExtractImages);
        mCreatedImages.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));
    }

    public void closeBottomSheetUtil(BottomSheetBehavior sheetBehavior) {
        if (checkSheetBehaviourUtil(sheetBehavior))
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public boolean checkSheetBehaviourUtil(BottomSheetBehavior sheetBehavior) {
        return sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void addFragmentPosition(boolean homePageItems, int iconA, int iconB,
                                     int iconId, int drawableId, int titleString) {
        mFragmentPositionMap.put(homePageItems ? iconA : iconB, new HomePageItem(iconId, drawableId, titleString));
    }
    

    private static class SingletonHolder {
        static final CommonCodeUtils INSTANCE = new CommonCodeUtils();
    }

    public static CommonCodeUtils getInstance() {
        return CommonCodeUtils.SingletonHolder.INSTANCE;
    }
}
