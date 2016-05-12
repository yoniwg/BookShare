package com.hgyw.bookshare.entities;

import com.hgyw.bookshare.entities.Entity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by haim7 on 12/05/2016.
 */
public class ImageEntity extends Entity {
    private byte[] bytes = new byte[0];

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

}
