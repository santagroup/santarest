package com.santarest.sample;

import com.santarest.annotations.Part;
import com.santarest.annotations.Query;
import com.santarest.annotations.Response;
import com.santarest.annotations.RestAction;
import com.santarest.annotations.Status;

import java.io.File;

@RestAction(value = "/post.php", type = RestAction.Type.MULTIPART, method = RestAction.Method.POST)
public class UploadFileAction {

    @Query("dir")
    String name = "testDir";
    @Part("name")
    String part = "sdfsadfdsafasfdasdfdasfasdfasdfasfsdfdsfasd";
    @Part(value = "file", encoding = "multipart/form-data")
    File file;
    @Part(value = "file2", encoding = "multipart/form-data")
    File file2;
    @Part(value = "file3", encoding = "multipart/form-data")
    File file3;
    @Response
    String response;
    @Status
    boolean success;

    public UploadFileAction(File file) {
        this.file = file;
        this.file2 = file;
        this.file3 = file;
    }

    public String getResponse() {
        return response;
    }
}
