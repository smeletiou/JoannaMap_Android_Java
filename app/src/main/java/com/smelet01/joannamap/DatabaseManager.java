package com.smelet01.joannamap;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public Future<List<PhotoInfoDB>> getPhotoInfoByUriAsync(final PhotoInfoDao dao, String photoInfoUri) {
        return executorService.submit(() -> dao.loadById(photoInfoUri));
    }

    public void insertPhotoInfoInBackground(final PhotoInfoDao dao, PhotoInfoDB photoInfo) {
        executorService.execute(() -> dao.insertPhotoInfo(photoInfo));
    }

    public void deletePhotoInfoInBackground(final PhotoInfoDao dao, PhotoInfoDB photoInfo) {
        executorService.execute(() -> dao.delete(photoInfo));
    }

    public CompletableFuture<List<PhotoInfoDB>> loadByOrientationAsync(PhotoInfoDao dao, String orientation) {
        return CompletableFuture.supplyAsync(() -> dao.loadByOrientation(orientation));
    }
}
