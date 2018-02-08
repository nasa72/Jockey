package com.marverenic.music.ui.library.browse;

import android.content.Context;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import com.marverenic.music.BR;
import com.marverenic.music.R;
import com.marverenic.music.ui.BaseViewModel;
import com.marverenic.music.utils.Util;

import java.io.File;

import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class FileViewModel extends BaseViewModel {

    private File mFile;
    private Drawable mThumbnail;
    private Drawable mDefaultThumbnail;
    private int mArtworkSizePx;

    private Subscription mArtworkSubscription;

    @Nullable
    private OnFileSelectedListener mSelectionListener;

    public FileViewModel(Context context) {
        super(context);
        mArtworkSizePx = getDimensionPixelSize(R.dimen.list_thumbnail_size);
        Bitmap defaultArt = BitmapFactory.decodeResource(getResources(), R.drawable.art_default);
        mDefaultThumbnail = makeCircular(defaultArt);
    }

    private Drawable makeCircular(Bitmap image) {
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), image);
        drawable.setCircular(true);
        return drawable;
    }

    public void setFileSelectionListener(OnFileSelectedListener selectionListener) {
        mSelectionListener = selectionListener;
    }

    public void setFile(File file) {
        mFile = file;
        mThumbnail = null;

        if (mArtworkSubscription != null) {
            mArtworkSubscription.unsubscribe();
        }

        mArtworkSubscription = Util.fetchArtwork(getContext(), Uri.fromFile(file), mArtworkSizePx)
                .subscribeOn(Schedulers.io())
                .map(this::makeCircular)
                .subscribe(artwork -> {
                    mThumbnail = artwork;
                    notifyPropertyChanged(BR.thumbnail);
                }, throwable -> {
                    Timber.e(throwable, "Failed to load artwork thumbnail");
                });

        notifyPropertyChanged(BR.fileName);
        notifyPropertyChanged(BR.thumbnail);
    }

    @Bindable
    public String getFileName() {
        return mFile.getName();
    }

    @Bindable
    public Drawable getThumbnail() {
        if (mThumbnail == null) {
            return mDefaultThumbnail;
        } else {
            return mThumbnail;
        }
    }

    public void onClickFile() {
        if (mSelectionListener != null) {
            mSelectionListener.onFileSelected(mFile);
        }
    }

    interface OnFileSelectedListener {
        void onFileSelected(File file);
    }

}
