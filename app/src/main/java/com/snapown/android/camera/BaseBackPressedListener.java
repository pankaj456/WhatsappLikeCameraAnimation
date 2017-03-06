package com.snapown.android.camera;

import android.app.Activity;
import android.app.FragmentManager;
 import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.snapown.android.R;
import com.snapown.android.listeners.OnBackPressedListener;

/**
 * Created by admin on 06/03/2017.
 */

public class BaseBackPressedListener implements OnBackPressedListener {
    private final Activity activity;
    private final ImageView imageView;
    private Animation rotation;

    public BaseBackPressedListener(Activity activity,ImageView imageView) {
        this.activity = activity;
        this.imageView=imageView;
    }

    @Override
    public void doBack() {

        rotation = AnimationUtils.loadAnimation(activity, R.anim.rotator);
        imageView.startAnimation(rotation);
        activity.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}