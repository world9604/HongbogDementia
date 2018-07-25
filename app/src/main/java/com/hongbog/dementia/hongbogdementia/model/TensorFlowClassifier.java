package com.hongbog.dementia.hongbogdementia.model;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.os.TraceCompat;
import android.util.Log;
import android.widget.ImageView;

import com.hongbog.dementia.hongbogdementia.view.DiagnosisActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC1;


public class TensorFlowClassifier {
    private static final String TAG = "TFClassifier";
    private static final int MAX_RESULTS = 3;  // result 개수 제한
    private static final float THRESHOLD = 0.1f;  // outputs 값의 threshold 설정

    // Tensorflow parameter
    public static final String[] INPUT_NAMES = {"model/input_module/x"};
    public static final String[] OUTPUT_NAMES = {"model/output_module/softmax","grad_cam/outputs"};
    public static final int WIDTH = 224;
    public static final int HEIGHT = 224;
    public static final String MODEL_FILE = "file:///android_asset/model_graph.pb";
    public static final String LABEL_FILE = "file:///android_asset/label_strings.txt";

    // neural network 관련 parameters
    private String[] inputNames;  // neural network 입력 노드 이름
    private String[] outputNames;  // neural network 출력 노드 이름
    private int[] imgSize = new int[2];
    private Vector<String> labels = new Vector<>();  // label 정보
    private int numClasses = 2;
    private int[] camSize = new int[]{7, 7};
    private float[] logits = new float[numClasses];  // logit 정보
    private float[] cam_outputs;
    private boolean runStats = false;

    private TensorFlowInferenceInterface tii;

    private TensorFlowClassifier() {}

    private static class SingleToneHolder {
        static final TensorFlowClassifier instance = new TensorFlowClassifier();
    }

    public static TensorFlowClassifier getInstance() {
        return SingleToneHolder.instance;
    }


    /**
     * 텐서플로우 classifier 생성 관련 초기화 함수
     * @param assetManager
     * @param modelFilename
     * @param labelFilename
     * @param width
     * @param height
     * @param inputNames
     * @param outputNames
     */
    public void createClassifier(
            AssetManager assetManager,
            String modelFilename,
            String labelFilename,
            int width,
            int height,
            String[] inputNames,
            String[] outputNames) {
        this.inputNames = inputNames;
        this.outputNames = outputNames;
        this.imgSize[0] = width;
        this.imgSize[1] = height;
        this.cam_outputs = new float[6 * camSize[0] * camSize[1]];

        // label names 설정
        BufferedReader br = null;
        try {
            String actualFilename = labelFilename.split("file:///android_asset/")[1];
            br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
            String line = "";
            while((line = br.readLine()) != null) {
                this.labels.add(line);
            }
        } catch (IOException e) {
            Log.d(TensorFlowClassifier.TAG, e.toString());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.d(TensorFlowClassifier.TAG, e.toString());
            }
        }

        this.tii = new TensorFlowInferenceInterface(assetManager, modelFilename);
    }


    public float[] normalize(Bitmap bitmap) {
        int mWidth = bitmap.getWidth();
        int mHeight = bitmap.getHeight();

        int[] ori_pixels = new int[mWidth * mHeight];
        float[] norm_pixels = new float[mWidth * mHeight * 3];

        bitmap.getPixels(ori_pixels, 0, mWidth, 0, 0, mWidth, mHeight);
        for (int i = 0; i < ori_pixels.length; i++) {
            int R = (ori_pixels[i] >> 16) & 0xff;
            int G = (ori_pixels[i] >> 8) & 0xff;
            int B = ori_pixels[i] & 0xff;

            norm_pixels[(i * 3) + 0] = (float) R / 255.0f;
            norm_pixels[(i * 3) + 1] = (float) G / 255.0f;
            norm_pixels[(i * 3) + 2] = (float) B / 255.0f;
        }
        return norm_pixels;
    }


   /**
     * 치매 진단을 수행하는 함수
     * @param imgData
     * @return
     */
    public String dementiaDiagnosis(float[] imgData) {
        long startTime = System.currentTimeMillis();

        TraceCompat.beginSection("dementiaDiagnosis");

        TraceCompat.beginSection("feed");
        tii.feed(this.inputNames[0], imgData, 1, this.imgSize[1], this.imgSize[0], 3);
        TraceCompat.endSection();

        TraceCompat.beginSection("run");
        tii.run(this.outputNames, this.runStats);
        TraceCompat.endSection();

        TraceCompat.beginSection("fetch");
        tii.fetch(this.outputNames[0], this.logits);
        tii.fetch(this.outputNames[1], this.cam_outputs);
        TraceCompat.endSection();

        //gradcamVisualization();

        float maxValue = -1;
        int maxIndex = -1;
        for (int i = 0; i < this.numClasses; i++) {
            if (maxValue < this.logits[i]) {
                maxValue = this.logits[i];
                maxIndex = i;
            }
        }
        long endTime = System.currentTimeMillis();

        Log.d(TAG, "소요 시간: " + (endTime - startTime));

        String result_txt = "";
        if (maxIndex == 0) {
            result_txt += "정상, ";
        } else {
            result_txt += "치매, ";
        }
        result_txt += Math.round(maxValue * 100);
        return result_txt;
    }


    public Bitmap gradcamVisualization(Bitmap bitmap) {
        Bitmap oriBitmap = Bitmap.createScaledBitmap(bitmap, this.imgSize[0], this.imgSize[1], false);

        Size cam_size = new Size(this.camSize[0], this.camSize[1]);
        Size img_size = new Size(this.imgSize[0], this.imgSize[1]);

        // Original 이미지
        Mat oriMat = new Mat(img_size, CV_32F);
        Utils.bitmapToMat(oriBitmap, oriMat);

        // CAM 출력 값
        Mat camMat = new Mat(cam_size, CV_32F);
        camMat.put(0, 0, this.cam_outputs);
        Imgproc.resize(camMat, camMat, img_size);

        camMat.convertTo(camMat, CV_8UC1);
        Imgproc.applyColorMap(camMat, camMat, Imgproc.COLORMAP_JET);
        Imgproc.cvtColor(camMat, camMat,  Imgproc.COLOR_BGR2RGBA);

        camMat.convertTo(camMat, CV_32F, 0.35);
        Imgproc.accumulate(oriMat, camMat);

        camMat.convertTo(camMat, CV_8UC1);

        Utils.matToBitmap(camMat, oriBitmap);
        //DiagnosisActivity.imgCAM.setImageBitmap(oriBitmap);
        return oriBitmap;
    }
}
