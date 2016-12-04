package com.wangyi.demo.cache;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.wangyi.demo.AppIml;
import com.wangyi.demo.NetWorkUtil;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created on 2016/11/30.
 *
 * @author WangYi
 */

public class EnhancedCall<T> {
    private Call<T> mCall;
    private Class dataClassName;
    // 是否使用缓存 默认开启
    private boolean mUseCache = true;

    public EnhancedCall(Call<T> call) {
        this.mCall = call;
    }

    /**
     * Gson反序列化缓存时 需要获取到泛型的class类型
     */
    public EnhancedCall<T> dataClassName(Class className) {
        dataClassName = className;
        return this;
    }

    /**
     * 是否使用缓存 默认使用
     */
    public EnhancedCall<T> useCache(boolean useCache) {
        mUseCache = useCache;
        return this;
    }

    public void enqueue(final EnhancedCallback<T> enhancedCallback) {
        mCall.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                enhancedCallback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (!mUseCache || NetWorkUtil.isNetAvailable(AppIml.appContext)) {
                    //不使用缓存 或者网络可用 的情况下直接回调onFailure
                    enhancedCallback.onFailure(call, t);
                    return;
                }

                Request request = call.request();
                String url = request.url().toString();
                RequestBody requestBody = request.body();
                Charset charset = Charset.forName("UTF-8");
                StringBuilder sb = new StringBuilder();
                sb.append(url);
                if (request.method().equals("POST")) {
                    MediaType contentType = requestBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(Charset.forName("UTF-8"));
                    }
                    Buffer buffer = new Buffer();
                    try {
                        requestBody.writeTo(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sb.append(buffer.readString(charset));
                    buffer.close();
                }

                String cache = CacheManager.getInstance().getCache(sb.toString());
                Log.d(CacheManager.TAG, "get cache->" + cache);

                if (!TextUtils.isEmpty(cache) && dataClassName != null) {
                    Object obj = new Gson().fromJson(cache, dataClassName);
                    if (obj != null) {
                        enhancedCallback.onGetCache((T) obj);
                        return;
                    }
                }
                enhancedCallback.onFailure(call, t);
                Log.d(CacheManager.TAG, "onFailure->" + t.getMessage());
            }
        });
    }
}
