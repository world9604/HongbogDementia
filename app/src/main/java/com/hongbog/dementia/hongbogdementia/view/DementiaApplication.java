package com.hongbog.dementia.hongbogdementia.view;

import android.app.Application;
import android.util.Log;

import com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class DementiaApplication extends Application {
  private Retrofit retrofit;
  private TensorFlowClassifier mTensorFlowClassifier;

  public DementiaApplication() {
    this.mTensorFlowClassifier = TensorFlowClassifier.getInstance();
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  public TensorFlowClassifier getTensorFlowClassifier() {
    return mTensorFlowClassifier;
  }
}
