package com.smelet01.joannamap;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PhotoInfoDB.class}, version = 1, exportSchema = false)
public abstract class AppStorage extends RoomDatabase {
    public abstract PhotoInfoDao photoInfoDao();
}
