package com.snapown.android.camera;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.snapown.android.R;
import com.snapown.android.listeners.OnBackPressedListener;
import com.snapown.android.widgets.CircularProgressBar;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by smritibharti on 09/02/17.
 */

public class CameraActivity extends AppCompatActivity  implements ActivityCompat.OnRequestPermissionsResultCallback
{
    private RelativeLayout mPreviewLayout;
    private static final String TAG = "CameraActivity";
    private CameraView mCameraView;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    Button shutter;
    private Context mContext;
    private Handler mHandler;
    private Handler mBackgroundHandler;
    FrameLayout camera_view;
    Scene current;
    Scene  image_view;
    private Transition mFadein;
    protected OnBackPressedListener onBackPressedListener;
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_SECONDS = 60;
    private ThreadPoolExecutor threadPool;

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS =
            {
                    R.drawable.flsh_icon_off,
                    R.drawable.flash_icon_on,
            };


    private int mCurrentFlash;
    private ImageButton flash_mode;
    private ImageButton switch_camera;


    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");

        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
            mCameraView.stop();

        }





        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data)
        {

           final boolean isFront;
            int facing = cameraView.getFacing();


            if(facing==0)
            {
                isFront = false;

            }

            else
            {
                isFront = true;


            }
            threadPool.execute(new Runnable() {
                @Override
                public void run() {

                    PreviewFragment previewFragment = PreviewFragment.newInstance(data,isFront);
                    getFragmentManager().beginTransaction().addToBackStack(null).add(R.id.replace,previewFragment).commit();



                }
            });

            threadPool.execute(new Runnable() {
                @Override
                public void run() {

                    mCameraView.start();
                }
            });



        }

    };
    private ImageView image;


    @Override
    protected void onStart() {
        super.onStart();
        shutter.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        shutter.setVisibility(View.VISIBLE);
    }



    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop is called");
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        Log.d(TAG,"onPause is called");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG,"onDestroy is called");
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }


    private Handler getBackgroundHandler()
    {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        mPreviewLayout = (RelativeLayout)findViewById(R.id.camera_layout);
        mContext = this;
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());


        initUi();

        initCamera();
        initListeners();
        initHandler();


    }




    private void initCamera()
    {
        if (mCameraView != null)
        {
            mCameraView.addCallback(mCallback);
        }


    }


    private void initListeners()
    {
        shutter.setOnClickListener(getShutterOnClickListener());
        shutter.setOnTouchListener(getShutterOnTouchListener());
        flash_mode.setOnClickListener(getFlashOnclickListener());
        switch_camera.setOnClickListener(getSwitchOnClickListener());



    }

    private void initUi()
    {


        mCameraView = (CameraView) findViewById(R.id.camera);
        shutter = (Button) findViewById(R.id.capture);
        shutter.setVisibility(View.VISIBLE);
        flash_mode = (ImageButton)findViewById(R.id.flash_mode);
        switch_camera = (ImageButton)findViewById(R.id.switch_camera);
        mCameraView = (CameraView) findViewById(R.id.camera);



    }
    private View.OnLongClickListener getShutterOnLongClickListener()
    {
        View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                return false;
            }
        };
        return mOnLongClickListener;

    }


    private class Animation implements Runnable
    {
        @Override
        public void run()
        {

            if (mCameraView != null)
            {

               threadPool.execute(new Runnable() {
                   @Override
                   public void run()
                   {
                       mCameraView.takePicture();

                   }
               });



            }
        }
    }
    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }



    @Override
public void onBackPressed() {
        if (onBackPressedListener != null)
            onBackPressedListener.doBack();
        else
            super.onBackPressed();

    }

    private View.OnTouchListener getShutterOnTouchListener()
    {

        final Runnable r = new Animation();
        View.OnTouchListener mOnTouchListener = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent)
            {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN)
                {
                    v.animate().scaleXBy(0.5f).setDuration(100).start();
                    v.animate().scaleYBy(0.5f).setDuration(100).start();
                    getHandler().post(r);


                    return true;
                }
                else if (action == MotionEvent.ACTION_UP)
                {
                    v.animate().cancel();
                    v.animate().scaleX(1f).setDuration(100).start();
                    v.animate().scaleY(1f).setDuration(100).start();
                    return true;
                }

                return false;
            }
        };
        return mOnTouchListener;

    }

    private View.OnClickListener getShutterOnClickListener()
    {
        View.OnClickListener mShutterOnclicklister = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                makeFlashAnimation();
                if (mCameraView != null) {
                    mCameraView.takePicture();
                }


            }
        } ;
        return mShutterOnclicklister;

    }

    private View.OnClickListener getSwitchOnClickListener()
    {
        View.OnClickListener mSwitchClickListener= new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (mCameraView != null) {
                   final int facing = mCameraView.getFacing();
                    threadPool.execute(new Runnable() {
                        @Override
                        public void run()
                        {

                            mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                                    CameraView.FACING_BACK : CameraView.FACING_FRONT);


                        }
                    });

                }



            }
        };
        return mSwitchClickListener;

    }

    private View.OnClickListener getFlashOnclickListener()
    {

        View.OnClickListener mListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    flash_mode.setImageResource(FLASH_ICONS[mCurrentFlash]);

                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }


            }
        };
        return mListener;

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        shutter.setVisibility(View.VISIBLE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {

                    mCameraView.start();


        }
        else if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA))
        {
            ConfirmationDialogFragment.newInstance(R.string.camera_permission_confirmation,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION,
                    R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION);
        }



    }



    public static class ConfirmationDialogFragment extends DialogFragment
    {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message, String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null)
                                    {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }
    private void initHandler()
    {
        synchronized(this)
        {
            mHandler = new Handler(Looper.getMainLooper());

            this.notifyAll();
        }
    }

    private synchronized Handler getHandler()
    {
        while (mHandler == null)
        {
            try
            {
                this.wait();
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }
        return mHandler;
    }


    public void makeFlashAnimation()
    {
       threadPool.execute(new Runnable()


        {


            @Override
            public void run()
            {
                Flasher flasher = new Flasher(mContext, mPreviewLayout);
                flasher.flash(1);
            }
        });
    }




}