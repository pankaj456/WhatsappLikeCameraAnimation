package com.snapown.android.camera;

import android.animation.Animator;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.snapown.android.R;
import com.snapown.android.widgets.CircularProgressBar;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Praveen on 06/03/17.
 */

public class PreviewFragment extends Fragment
{
    private byte[] data;
    private ImageView imageView;
    CircularProgressBar button;
    private boolean isFront;
    private   WindowManager windowManager;
    private   Display display;
    private   Point size;
    private int imageViewHeight;
    private int imageViewWidth;
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_SECONDS = 60;
    private ThreadPoolExecutor threadPool;


    public static PreviewFragment newInstance(byte[] data,boolean isFront)
    {
        PreviewFragment previewFragment = new PreviewFragment();
        previewFragment.data = data;
        previewFragment.isFront = isFront;

        return previewFragment;


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.image_preview,container,false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        threadPool = new ThreadPoolExecutor( 4, MAX_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {

        imageView = (ImageView) view.findViewById(R.id.img);

        ((CameraActivity)getActivity()).setOnBackPressedListener(new BaseBackPressedListener(getActivity(),imageView));


        threadPool.execute(new Runnable() {
            @Override
            public void run() {

                new LoadImageTask(data,isFront,imageView,720,1080).execute(data) ;
            }
        });





    }

    private static class ViewHolder
    {

        ImageView Image;

        public ViewHolder(View view)
        {
            Image = (ImageView) view.findViewById(R.id.image);
        }
    }





}