package com.androidmarket.pdfcreatoraaa.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import androidmarket.R;

import com.androidmarket.pdfcreatoraaa.activities.HomeActivity;
import com.androidmarket.pdfcreatoraaa.adapter.AdapterViewFiles;
import com.androidmarket.pdfcreatoraaa.interfaces.EmptyStateChangeListener;
import com.androidmarket.pdfcreatoraaa.interfaces.ItemSelectedListener;
import com.androidmarket.pdfcreatoraaa.util.AdsUtility;
import com.androidmarket.pdfcreatoraaa.util.DialogUtils;
import com.androidmarket.pdfcreatoraaa.util.DirectoryUtils;
import com.androidmarket.pdfcreatoraaa.util.FileSortUtils;
import com.androidmarket.pdfcreatoraaa.util.MergeHelper;
import com.androidmarket.pdfcreatoraaa.util.PermissionsUtils;
import com.androidmarket.pdfcreatoraaa.util.PopulateList;
import com.androidmarket.pdfcreatoraaa.util.StringUtils;
import com.androidmarket.pdfcreatoraaa.util.ViewFilesDividerItemDecoration;

import static com.androidmarket.pdfcreatoraaa.Constants.BUNDLE_DATA;
import static com.androidmarket.pdfcreatoraaa.Constants.REQUEST_CODE_FOR_WRITE_PERMISSION;
import static com.androidmarket.pdfcreatoraaa.Constants.SORTING_INDEX;
import static com.androidmarket.pdfcreatoraaa.Constants.WRITE_PERMISSIONS;

public class ViewFilesFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        EmptyStateChangeListener,
        ItemSelectedListener {

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT = 10;

    @BindView(R.id.filesRecyclerView)
    RecyclerView mViewFilesListRecyclerView;
    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipeView;
    @BindView(R.id.emptyStatusView)
    RelativeLayout emptyView;
    @BindView(R.id.no_permissions_view)
    RelativeLayout noPermissionsLayout;

    private Activity mActivity;
    private AdapterViewFiles mAdapterViewFiles;

    private DirectoryUtils mDirectoryUtils;
    private SearchView mSearchView;
    private int mCurrentSortingIndex;
    private SharedPreferences mSharedPreferences;
    private boolean mIsAllFilesSelected = false;
    private boolean mIsMergeRequired = false;
    private AlertDialog.Builder mAlertDialogBuilder;

    private int mCountFiles = 0;
    int t1;

    private MergeHelper mMergeHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_view_files, container, false);
        ButterKnife.bind(this, root);
        // Initialize variables
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mCurrentSortingIndex = mSharedPreferences.getInt(SORTING_INDEX, FileSortUtils.getInstance().NAME_INDEX);
        mAdapterViewFiles = new AdapterViewFiles(mActivity, null, this, this);
        mAlertDialogBuilder = new AlertDialog.Builder(mActivity)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(root.getContext());
        mViewFilesListRecyclerView.setLayoutManager(mLayoutManager);
        mViewFilesListRecyclerView.setAdapter(mAdapterViewFiles);
        mViewFilesListRecyclerView.addItemDecoration(new ViewFilesDividerItemDecoration(root.getContext()));
        mSwipeView.setOnRefreshListener(this);

        ImageView ivBack = root.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HomeActivity.class));
                getActivity().finish();
            }
        });


        FrameLayout nativeAdFrameOne = root.findViewById(R.id.nativeAdFrameLayout);
        AdsUtility.loadNativeAd(getActivity(), nativeAdFrameOne);


        TextView title, titleTwo;
        title = root.findViewById(R.id.title);
        titleTwo = root.findViewById(R.id.titleTwo);

        Bundle bundle = getArguments();

        try {
            t1 = bundle.getInt(BUNDLE_DATA);
            title.setText("Add Watermark");
            titleTwo.setText("Add watermark to your PDF files.");
        } catch (Exception e){
            title.setText("View Files");
            titleTwo.setText("Preview your PDF Files");
        }

        int dialogId;
        if (getArguments() != null) {
            dialogId = getArguments().getInt(BUNDLE_DATA);
            DialogUtils.getInstance().showFilesInfoDialog(mActivity, dialogId);
        }

        checkIfListEmpty();
        mMergeHelper = new MergeHelper(mActivity, mAdapterViewFiles);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem;
        if (!mIsMergeRequired) {
            // menu to inflate the view where search and select all icon is there.
            inflater.inflate(R.menu.activity_view_files_actions, menu);
            MenuItem item = menu.findItem(R.id.action_search);
            menuItem = menu.findItem(R.id.select_all);
            mSearchView = (SearchView) item.getActionView();
            mSearchView.setQueryHint(getString(R.string.search_hint));
            mSearchView.setSubmitButtonEnabled(true);
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    setDataForQueryChange(s);
                    mSearchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    setDataForQueryChange(s);
                    return true;
                }
            });
            mSearchView.setOnCloseListener(() -> {
                populatePdfList(null);
                return false;
            });
            mSearchView.setIconifiedByDefault(true);
        } else {
            inflater.inflate(R.menu.activity_view_files_actions_if_selected, menu);
            MenuItem item = menu.findItem(R.id.item_merge);
            item.setVisible(mCountFiles > 1); //Show Merge icon when two or more files was selected
            menuItem = menu.findItem(R.id.select_all);
        }

        if (mIsAllFilesSelected) {
            menuItem.setIcon(R.drawable.ic_check_box_24dp);
        }

    }

    private void setDataForQueryChange(String s) {
        populatePdfList(s);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_sort:
                displaySortDialog();
                break;
            case R.id.item_delete:
                if (mAdapterViewFiles.areItemsSelected())
                    deleteFiles();
                else
                    StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_no_pdfs_selected);
                break;
            case R.id.item_share:
                if (mAdapterViewFiles.areItemsSelected())
                    mAdapterViewFiles.shareFiles();
                else
                    StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_no_pdfs_selected);
                break;
            case R.id.select_all:
                if (mAdapterViewFiles.getItemCount() > 0) {
                    if (mIsAllFilesSelected) {
                        mAdapterViewFiles.unCheckAll();
                    } else {
                        mAdapterViewFiles.checkAll();
                    }
                } else {
                    StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_no_pdfs_selected);
                }
                break;
            case R.id.item_merge:
                if (mAdapterViewFiles.getItemCount() > 1) {
                    mMergeHelper.mergeFiles();
                }
                break;
        }
        return true;
    }


    private void deleteFiles() {
        mAdapterViewFiles.deleteFile();
    }

    /**
     * Checks if there are no elements in the list
     * and shows the icons appropriately
     */
    private void checkIfListEmpty() {
        onRefresh();
        final File[] files = mDirectoryUtils.getOrCreatePdfDirectory().listFiles();
        int count = 0;

        if (files == null) {
            showNoPermissionsView();
            return;
        }

        for (File file : files)
            if (!file.isDirectory()) {
                count++;
                break;
            }
        if (count == 0) {
            setEmptyStateVisible();
            mCountFiles = 0;
            updateToolbar();
        }
    }

    @Override
    public void onRefresh() {
        populatePdfList(null);
        mSwipeView.setRefreshing(false);
    }

    /**
     * populate pdf files with search query
     *
     * @param query to filter pdf files, {@code null} to get all
     */
    private void populatePdfList(@Nullable String query) {
        new PopulateList(mAdapterViewFiles, this,
                new DirectoryUtils(mActivity), mCurrentSortingIndex, query).execute();
    }

    private void displaySortDialog() {
        mAlertDialogBuilder.setTitle(R.string.sort_by_title)
                .setItems(R.array.sort_options, (dialog, which) -> {
                    mCurrentSortingIndex = which;
                    mSharedPreferences.edit().putInt(SORTING_INDEX, which).apply();
                    populatePdfList(null);
                });
        mAlertDialogBuilder.create().show();
    }

    @Override
    public void setEmptyStateVisible() {
        emptyView.setVisibility(View.VISIBLE);
        noPermissionsLayout.setVisibility(View.GONE);
    }

    @Override
    public void setEmptyStateInvisible() {
        emptyView.setVisibility(View.GONE);
        noPermissionsLayout.setVisibility(View.GONE);
    }

    @Override
    public void showNoPermissionsView() {
        emptyView.setVisibility(View.GONE);
        noPermissionsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoPermissionsView() {
        noPermissionsLayout.setVisibility(View.GONE);
    }

    @Override
    public void filesPopulated() {
        //refresh everything and invalidate the menu.
        if (mIsMergeRequired) {
            mIsMergeRequired = false;
            mIsAllFilesSelected = false;
            mActivity.invalidateOptionsMenu();
        }
    }

    //When the "GET STARTED" button is clicked, the user is taken to home

    @OnClick(R.id.provide_permissions)
    public void providePermissions() {
        if (!isStoragePermissionGranted()) {
            getRuntimePermissions();
        }
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29) {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
    private void getRuntimePermissions() {
        if (Build.VERSION.SDK_INT < 29) {
            PermissionsUtils.getInstance().requestRuntimePermissions(this,
                    WRITE_PERMISSIONS,
                    REQUEST_CODE_FOR_WRITE_PERMISSION);
        }
    }

    /**
     * Called after user is asked to grant permissions
     *
     * @param requestCode  REQUEST Code for opening permissions
     * @param permissions  permissions asked to user
     * @param grantResults bool array indicating if permission is granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsUtils.getInstance().handleRequestPermissionsResult(mActivity, grantResults,
                requestCode, REQUEST_CODE_FOR_WRITE_PERMISSION, this::onRefresh);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mDirectoryUtils = new DirectoryUtils(mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void isSelected(Boolean isSelected, int countFiles) {
        mCountFiles = countFiles;
        updateToolbar();
    }

    /**
     * Updates the toolbar with respective the number of files selected.
     */
    private void updateToolbar() {
        AppCompatActivity activity = ((AppCompatActivity)
                Objects.requireNonNull(mActivity));
        ActionBar toolbar = activity.getSupportActionBar();
        if (toolbar != null) {
            mActivity.setTitle(mCountFiles == 0 ?
                    mActivity.getResources().getString(R.string.viewFiles)
                    : String.valueOf(mCountFiles));
            //When one or more than one files are selected refresh
            //ActionBar: set Merge option invisible or visible
            mIsMergeRequired = mCountFiles > 1;
            mIsAllFilesSelected = mCountFiles == mAdapterViewFiles.getItemCount();
            activity.invalidateOptionsMenu();
        }
    }



    /*
    Just for reference
    private void moveFilesToDirectory(int operation) {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.directory_dialog, null);
        final ArrayList<String> filePath = mAdapterViewFiles.getSelectedFilePath();
        if (filePath == null) {
            showSnackbar(mActivity, R.string.snackbar_no_pdfs_selected);
        } else {
            final EditText input = alertView.findViewById(R.id.directory_editText);
            TextView message = alertView.findViewById(R.id.directory_textView);
            if (operation == NEW_DIR) {
                message.setText(R.string.dialog_new_dir);
                mAlertDialogBuilder.setTitle(R.string.new_directory)
                        .setView(alertView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            String fileName = input.getText().toString();
                            new MoveFilesToDirectory(mActivity
                                    , filePath
                                    , fileName
                                    , MoveFilesToDirectory.MOVE_FILES)
                                    .execute();
                            populatePdfList();
                        });
            } else if (operation == EXISTING_DIR) {
                message.setText(R.string.dialog_dir);
                mAlertDialogBuilder.setTitle(R.string.directory)
                        .setView(alertView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            String fileName = input.getText().toString();
                            File directory = mDirectoryUtils.getDirectory(fileName);
                            if (directory != null) {
                                new MoveFilesToDirectory(mActivity, filePath, fileName, MoveFilesToDirectory.MOVE_FILES)
                                        .execute();
                                populatePdfList();
                            } else {
                                showSnackbar(mActivity, R.string.dir_does_not_exists);
                                dialogInterface.dismiss();
                            }
                        });
            }
            mAlertDialogBuilder.create().show();
        }
    }*/

}