package com.script;

/**
 * Created by     on 2016/8/15.
 */
public class Protocol {
    public static final int VERSION             = 2 ;

    public static final int START_APP_ERR_SPACE     = 1 ;
    public static final int SIZE_OF_TYPE            = 2 ;
    public static final int SIZE_OF_LEN             = 4 ;
    public static final int SIZE_OF_ATTR            = 1 ;

    public static final short TYPE_RESPONSE       = 0X0001 ;
    public static final short TYPE_ADMIN_SHELL    = 0X010D ;
    public static final short TYPE_QUIT           = 0x020c ;
    public static final short TYPE_TEST_FIND     = 0x0002 ;
    public static final short TYPE_REUQEST_LOG   = 0x0003 ;
    public static final short TYPE_THE3RD_HEARTBEAT = 0x0007 ;
    public static final short TYPE_THE3RD_HEARTBEAT_OVER = 0x0008 ;

    public static final short TYPE_FIND_RESULT   = 0x0f01 ;

    public static final byte ATTR_STATE          = 0x06 ;

    public static final byte ATTR_X = 0x07 ;
    public static final byte ATTR_Y = 0x08 ;
    public static final byte ATTR_WIDTH = 0x09 ;
    public static final byte ATTR_HEIGHT = 0x0a ;
    public static final byte ATTR_THRESH = 0x0b ;
    public static final byte ATTR_MAXVAL = 0x0c ;
    public static final byte ATTR_TYPE = 0x0d ;
    public static final byte ATTR_FLAG = 0x0e ;
    public static final byte ATTR_METHOD = 0x0f ;
    public static final byte ATTR_PIC = 0x10 ;
    public static final byte ATTR_TIMESTAMP = 0x11 ;
    public static final byte ATTR_SIM = 0x12 ;
    public static final byte ATTR_SHOWRESULT = 0x13 ;
    public static final byte ATTR_THE3RD_PACK_NAME = 0x4a ;
    public static final byte ATTR_THE3RD_SERV_NAME = 0x4b ;

    public static final byte ATTR_CMD            = 0x2e ;


    public static final byte ATTR_STR            = 0x7f ;



}
