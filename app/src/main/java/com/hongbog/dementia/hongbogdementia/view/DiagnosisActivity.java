package com.hongbog.dementia.hongbogdementia.view;

import android.content.Intent;
import android.content.res.AssetManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongbog.dementia.hongbogdementia.R;
import com.hongbog.dementia.hongbogdementia.databinding.ActivityDiagnosisBinding;
import com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier;
import com.hongbog.dementia.hongbogdementia.contract.DiagnosisContract;
import com.hongbog.dementia.hongbogdementia.viewmodel.DiagnosisViewModel;


public class DiagnosisActivity extends AppCompatActivity implements DiagnosisContract{

    private DiagnosisViewModel mDiagnosisViewModel;

    private static final String TAG = "DiagnosisActivity";

    public static final int PICTURE_REQUEST_CODE = 100;

    public static native void Divied(long matAddrInput, long matAddrResult);

    public static native void Nomal(long matAddrInput, long matAddrResult);

    public native String stringFromJNI();

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDiagnosisBinding binding = DataBindingUtil.setContentView((DiagnosisActivity)this, R.layout.activity_diagnosis);
        final TensorFlowClassifier tensorFlowClassifier = ((DementiaApplication) getApplication()).getTensorFlowClassifier();
        mDiagnosisViewModel = new DiagnosisViewModel(this, tensorFlowClassifier);
        binding.setViewModel(mDiagnosisViewModel);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDiagnosisViewModel.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void showGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICTURE_REQUEST_CODE);
    }


    @Override
    public void showCapture() {

    }


    @Override
    public AssetManager getActivityAssets(){
        return getAssets();
    }
}