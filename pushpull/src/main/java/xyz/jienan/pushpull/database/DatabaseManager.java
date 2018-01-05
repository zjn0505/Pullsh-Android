package xyz.jienan.pushpull.database;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Jienan on 2018/1/5.
 */

public class DatabaseManager {
    private Context context;
    public Realm realm;

    public DatabaseManager(Context context) {
        this.context = context;
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("memo.realm")
                .schemaVersion(0)
                .build();
        realm = Realm.getInstance(config);
    }

    public void close() {
        realm.close();
    }
}
