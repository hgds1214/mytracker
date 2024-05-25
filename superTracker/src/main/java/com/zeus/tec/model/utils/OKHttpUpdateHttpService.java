package com.zeus.tec.model.utils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.xuexiang.xupdate._XUpdate;
import com.xuexiang.xupdate.proxy.IUpdateHttpService;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpUpdateHttpService implements IUpdateHttpService {

    private boolean mIsPostJson;

    public OKHttpUpdateHttpService() {
        this(false);
    }

    public OKHttpUpdateHttpService(boolean isPostJson) {
        mIsPostJson = isPostJson;
    }

    @Override
    public void asyncGet(@NonNull String url, @NonNull Map<String, Object> params, @NonNull Callback callBack) {
        OkHttpUtils.get()
                .url(url)
                .params(transform(params))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callBack.onError(e);
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        String [] versionList = response.split("\n");
                        String [] newVersion = versionList[versionList.length-1].split(",");

                        if (hasNewVersion(newVersion))
                        {
                            callBack.onSuccess(versionList[versionList.length-1]);
                        }
                        _XUpdate.setCheckUrlStatus(url,false);
                    }
                });
    }

    public boolean hasNewVersion (String [] lastVersion){
        boolean result =false ;
        int lastCode=1;
        try{
             lastCode = Integer.parseInt(lastVersion[1]);

        }catch (Exception e){
            e.printStackTrace();
            return false ;
        }
        if (versionInfo.versionCode<lastCode)
        {
            return true ;
        }
        else if (versionInfo.versionCode==lastCode){
            ToastUtils.showLong("已经是最新版本！");
            return false;
        }
        else {
            ToastUtils.showLong("该软件可能存在错误，请及时联系生产厂家！");
            return false;
        }
    }



    @Override
    public void asyncPost(@NonNull String url, @NonNull Map<String, Object> params, @NonNull Callback callBack) {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        String content = "";
        Response response = null;
        try {
            response = call.execute();
            content = Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(content);
    }

    private Map<String, String> transform(Map<String, Object> params) {
        Map<String, String> map = new TreeMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toString());
        }
        return map;
    }


    @Override
    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull DownloadCallback callback) {
        OkHttpUtils.get()
                .url(url)
                .tag(url)
                .build()
                .execute(new FileCallBack(path, fileName) {
                    @Override
                    public void inProgress(float progress, long total, int id) {
                        callback.onProgress(progress, total);
                    }
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onError(e);
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        callback.onStart();
                    }
                });
    }

    @Override
    public void cancelDownload(@NonNull String url) {
        OkHttpUtils.getInstance().cancelTag(url);

    }
}
