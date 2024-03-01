package com.tencent.bscp.api;

import com.tencent.bscp.pbfs.App;
import com.tencent.bscp.pojo.AppOption;
import com.tencent.bscp.pojo.Callback;
import com.tencent.bscp.pojo.Release;

import java.util.List;
import java.util.Map;

public interface ClientApi {
    // ListApps list app from remote, only return have perm by token
    List<App> listApp(List<String> match) throws Exception;

    // PullFiles pull files from remote
    Release pullFiles(String app, AppOption... opts) throws Exception;

    // Get KV release from remote
    Release pullKvs(String app, List<String> match, AppOption... opts) throws Exception;

    // Pull Key Value from remote
    String get(String app, String key, AppOption... opts) throws Exception;

    // AddWatcher add a watcher to client
    void addWatcher(Callback callback, String app, AppOption... opts) throws Exception;

    // StartWatch start watch
    void startWatch() throws Exception;

    // StopWatch stop watch
    void stopWatch();

    // ResetLabels reset bscp client labels, if key conflict, app value will overwrite client value
    void resetLabels(Map<String, String> labels);
}
