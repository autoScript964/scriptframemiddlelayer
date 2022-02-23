package com.script.opencvapi;

/**
 * Created by     on 2018/2/24.
 */
public class OpencvProtocol {
    public static final int STATE_OK = 1;
    public static final int STATE_FAIL = 0 ;
    public static final short TYPE_STATE = 0x0001 ;
    public static final short TYPE_PIC = 0X0002 ;
    public static final short TYPE_TEMPLATE_LIST = 0X0003 ;
    public static final short TYPE_TEMPLATE = 0X0004 ;
    public static final short TYPE_RESULT = 0X0101 ;
    public static final short TYPE_UPDATELIST = 0X0102 ;
    public static final short TYPE_LOG = 0x0102 ;
    public static final short TYPE_UPDATE_TEMPLATE = 0X0103 ;


    public static final byte ATTR_STR = 0X7f ;
    public static final byte ATTR_STATE = 0X02 ;
    public static final byte ATTR_X = 0X0a ;
    public static final byte ATTR_Y = 0X0b ;
    public static final byte ATTR_FLAG = 0X0c ;
    public static final byte ATTR_PIC = 0X0d ;
    public static final byte ATTR_NAME = 0X0e ;
    public static final byte ATTR_SIM = 0X0f ;
    public static final byte ATTR_MD5 = 0X10;
    public static final byte ATTR_PACKAGE = 0X11 ;
    public static final byte ATTR_VERSION = 0X12 ;
    public static final byte ATTR_LIST = 0X13 ;

}
