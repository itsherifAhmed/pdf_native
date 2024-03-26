package com.androidmarket.pdfcreatoraaa.util;

import android.text.Editable;
import android.text.TextWatcher;

import com.androidmarket.pdfcreatoraaa.fragment.ImageToPdfFragment;

/**
 * A default Text watcher that does nothing. Useful when not all methods are needed
 * as in {@link ImageToPdfFragment}.passwordProtectPDF()
 */
public abstract class DefaultTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) { }
}
