package com.smelet01.joannamap;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
@Dao
public interface PhotoInfoDao {
    @Query("SELECT * FROM photoinfodb")
    List<PhotoInfoDB> getAll();

    @Query("SELECT * FROM photoinfodb WHERE uri IN (:photoUri)")
    List<PhotoInfoDB> loadById(String photoUri);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPhotoInfo(PhotoInfoDB photoInfo);

    @Delete
    void delete(PhotoInfoDB photoInfoDB);

    @Query("SELECT * FROM photoinfodb WHERE orientation = :orientation")
    List<PhotoInfoDB> loadByOrientation(String orientation);

}
