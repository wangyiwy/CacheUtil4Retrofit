package com.wangyi.demo.cache;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created on 2016/11/30.
 *
 * @author WangYi
 */

public interface EnhancedCallback<T> {
    void onResponse(Call<T> call, Response<T> response);

    void onFailure(Call<T> call, Throwable t);

    void onGetCache(T t);
}
