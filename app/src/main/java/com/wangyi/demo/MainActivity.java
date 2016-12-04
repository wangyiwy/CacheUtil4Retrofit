package com.wangyi.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wangyi.demo.cache.EnhancedCacheInterceptor;
import com.wangyi.demo.cache.EnhancedCall;
import com.wangyi.demo.cache.EnhancedCallback;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created on 2016/12/4.
 *
 * @author WangYi
 */

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    public static final String BASE_URL = "https://api.github.com/";

    @BindView(R.id.tvResponse)
    TextView tvResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public void getRequest(View view) {
        ApiService service = getApiService();
        Call<UserList> call = service.getUserList();
        //使用我们自己的EnhancedCall 替换Retrofit的Call
        EnhancedCall<UserList> enhancedCall = new EnhancedCall<>(call);
        enhancedCall.useCache(true)/*默认支持缓存 可不设置*/
                .dataClassName(UserList.class)
                .enqueue(new EnhancedCallback<UserList>() {
                    @Override
                    public void onResponse(Call<UserList> call, Response<UserList> response) {
                        UserList userlist = response.body();
                        if (userlist != null) {
                            Log.d(TAG, "onResponse->" + userlist.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserList> call, Throwable t) {
                        Log.d(TAG, "onFailure->" + t.getMessage());
                    }

                    @Override
                    public void onGetCache(UserList userlist) {
                        Log.d(TAG, "onGetCache" + userlist.toString());
                    }
                });
    }

    public void postRequest(View view) {
        ApiService service = getApiService();
        HashMap<String, String> map = new HashMap<>();
        //todo 添加请求body
        Call<User> call = service.createRepo(map);

        EnhancedCall<User> enhancedCall = new EnhancedCall<>(call);
        enhancedCall.dataClassName(User.class).enqueue(new EnhancedCallback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                if (user != null) {
                    Log.d(TAG, "onResponse->" + user.toString());
                    tvResponse.setText(user.toString());
                }
                tvResponse.setText("onResponse:" + response.message() + " code:" + response.code());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvResponse.setText("onFailure");
            }

            @Override
            public void onGetCache(User user) {
                Log.d(TAG, "onGetCache" + user.toString());
                tvResponse.setText(user.toString());
            }
        });
    }

    public interface ApiService {
        @GET("users/list")
        Call<UserList> getUserList();

        @POST("users/new")
        Call<User> createRepo(@Body Map map);
    }

    public ApiService getApiService() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new EnhancedCacheInterceptor());

//        File cacheDir = new File(getCacheDir(), "response");
//        //缓存的最大尺寸10m
//        Cache cache = new Cache(cacheDir, 1024 * 1024 * 10);
//        builder.cache(cache);
//        builder.addInterceptor(new CacheInterceptor());

        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ApiService.class);
    }

    public void clear(View view) {
        tvResponse.setText("");
    }
}
