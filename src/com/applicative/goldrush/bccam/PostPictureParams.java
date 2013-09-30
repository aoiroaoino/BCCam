package com.applicative.goldrush.bccam;

import java.io.InputStream;

public class PostPictureParams {
    InputStream input;
    String url;
    String userName;

    public PostPictureParams(InputStream input, String url, String userName) {
        this.input = input;
	    this.url = url;
	    this.userName = userName;
	}
}
