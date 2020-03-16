package com.noooobas.blc;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

@Dao
public interface CardDao {
    @Query("SELECT * FROM cardDB WHERE cardNum = :cardNum")
    CardDB getById(int cardNum);

    @Query("INSERT INTO CardDB (cardNum, purSum) VALUES (:cardNum,'0')")
    void insert_new(int cardNum);

    @Update
    void update(CardDB cardDB);

    @Query("DELETE FROM cardDB")
    public void nukeTable();

    @Query("SELECT * FROM cardDB WHERE cardNum = :cardNum")
    boolean checkExist (int cardNum);

    @Delete
    void delete(CardDB cardDB);

    @RawQuery
    boolean insertDataRawFormat(SupportSQLiteQuery query);
}
