package com.santarest.sample;

import com.santarest.annotations.Part;
import com.santarest.annotations.Query;
import com.santarest.annotations.Response;
import com.santarest.annotations.RestAction;
import com.santarest.annotations.Status;

@RestAction(value = "/post.php", type = RestAction.Type.MULTIPART)
public class UploadFileAction {

    @Query("dir")
    String name = "testDir";
    @Part("name")
    String part = "sdfsadfdsafasfdasdfdasfasdfasdfasfsdfdsfasd";
    @Response
    String response;
    @Status
    boolean success;

    public UploadFileAction() {
    }

    public String getResponse() {
        return response;
    }
}
