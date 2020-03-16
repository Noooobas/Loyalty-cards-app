package com.noooobas.blc;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class CardDB {
    @PrimaryKey
    public int cardNum;

    @ColumnInfo
    public long  purSum;

}
