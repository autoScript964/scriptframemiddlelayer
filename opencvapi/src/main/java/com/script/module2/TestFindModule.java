package com.script.module2;

import com.script.Protocol;
import com.script.AtModule2;

import java.nio.ByteBuffer;

/**
 * Created by     on 2018/3/29.
 */
public class TestFindModule extends AtModule2 {

    /**
     * 查找左上角X坐标
     */
    public int x ;
    /**
     * 查找左上角Y坐标
     */
    public int y ;
    /**
     * 查找范围宽
     */
    public int width ;
    /**
     * 查找范围高
     */
    public int height ;
    /**
     * 图片颜色类型 IMREAD_GRAYSCALE=0 IMREAD_COLOR=1 ，其它二值化
     */
    public int flag ;
    /**
     * 图片查找方法
     */
    public int method ;
    /**
     * 二值化阈值
     */
    public int thresh ;
    /**
     * 二值化最大值
     */
    public int maxval ;
    /**
     * 图片二值化类型
     */
    public int type ;
    /**
     * 要查找的图片
     */
    public boolean showResult ;
    public ByteBuffer pic ;
    public TestFindModule(byte[] data, int offset, int len) throws YpModuleException {
        super(data, offset, len);
    }

    public TestFindModule(){
        super(Protocol.TYPE_TEST_FIND);
    }
    @Override
    public void initField() {
        try {
            fields.put(Protocol.ATTR_X, getClass().getField("x"));
            fields.put(Protocol.ATTR_Y, getClass().getField("y"));
            fields.put(Protocol.ATTR_WIDTH, getClass().getField("width"));
            fields.put(Protocol.ATTR_HEIGHT, getClass().getField("height"));
            fields.put(Protocol.ATTR_FLAG, getClass().getField("flag"));
            fields.put(Protocol.ATTR_METHOD, getClass().getField("method"));
            fields.put(Protocol.ATTR_THRESH, getClass().getField("thresh"));
            fields.put(Protocol.ATTR_MAXVAL, getClass().getField("maxval"));
            fields.put(Protocol.ATTR_TYPE, getClass().getField("type"));
            fields.put(Protocol.ATTR_SHOWRESULT, getClass().getField("showResult")) ;
            fields.put(Protocol.ATTR_PIC, getClass().getField("pic"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
