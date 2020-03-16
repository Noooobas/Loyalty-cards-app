package com.noooobas.blc;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CardDB.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase mINSTANCE;
    public abstract CardDao cardDao();

    public static AppDatabase getAppDatabase(Context context){
        if (mINSTANCE == null){
            mINSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "card-database")
                    .allowMainThreadQueries().build();
        }
        return mINSTANCE;
    }
    public static void destroyInstance(){
        mINSTANCE = null;

    }
}
