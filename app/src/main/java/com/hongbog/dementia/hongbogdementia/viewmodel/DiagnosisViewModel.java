package com.hongbog.dementia.hongbogdementia.viewmodel;

import android.content.Intent;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import com.hongbog.dementia.hongbogdementia.util.Dlog;
import com.hongbog.dementia.hongbogdementia.R;
import com.hongbog.dementia.hongbogdementia.contract.DiagnosisContract;
import com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;
import static com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier.HEIGHT;
import static com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier.INPUT_NAMES;
import static com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier.LABEL_FILE;
import static com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier.MODEL_FILE;
import static com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier.OUTPUT_NAMES;
import static com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier.WIDTH;
import static com.hongbog.dementia.hongbogdementia.view.DiagnosisActivity.PICTURE_REQUEST_CODE;

/**
 * Created by taein on 2018-07-20.
 */
public class DiagnosisViewModel implements View.OnClickListener {

    final DiagnosisContract diagnosisView;
    private final TensorFlowClassifier mTensorFlowClassifier;
    public Bitmap mScaledBitmapTmp;
    public ObservableField<String> imgOriUrl = new ObservableField<>();
    public ObservableField<Bitmap> imgCamUrl = new ObservableField<>();
    public ObservableField<String> txtResult = new ObservableField<>();


    private void initTensorFlowAndLoadModel() {

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    mTensorFlowClassifier.createClassifier(
                            diagnosisView.getActivityAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            WIDTH,
                            HEIGHT,
                            INPUT_NAMES,
                            OUTPUT_NAMES);
                    Dlog.d("Load Success");
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }


    public DiagnosisViewModel(DiagnosisContract diagnosisView, TensorFlowClassifier tensorFlowClassifier) {
        this.diagnosisView = diagnosisView;
        this.mTensorFlowClassifier = tensorFlowClassifier;

        initTensorFlowAndLoadModel();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();

                if (uri != null) {
                    Dlog.d("uri : " + uri.toString());
                    imgOriUrl.set(uri.toString());
                }
            }
        }
    }


    public void getScaledBitmap(Bitmap scaledBitmap){
        mScaledBitmapTmp = scaledBitmap;
    }


    public void setDiagnosisResult(View v) {

        // Classification 결과값 출력
        float[] imgData = new float[WIDTH * HEIGHT * 3];
        imgData = mTensorFlowClassifier.normalize(mScaledBitmapTmp);
        String result = mTensorFlowClassifier.dementiaDiagnosis(imgData);
        Dlog.d("result : "  + result);
        txtResult.set("Diagnostic results: " + result);

        // Cam image 출력
        Bitmap bitmap = mTensorFlowClassifier.gradcamVisualization(mScaledBitmapTmp);
        imgCamUrl.set(bitmap);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.btn_gallery:
                diagnosisView.showGallery();
                break;

            case R.id.btn_filming:
                diagnosisView.showCapture();
                break;
        }
    }
}
