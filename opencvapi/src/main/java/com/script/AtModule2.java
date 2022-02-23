package com.script;


import com.script.opencvapi.LtLog;
import com.script.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by     on 2016/8/15.
 */
public abstract class AtModule2 {

    protected Map<Byte,Field> fields  = new HashMap<>();
    protected short type ;
    protected static final int FIELD_TYPE_UNKNOWN       = 0 ;
    protected static final int FIELD_TYPE_STRING        = 1 ;
    protected static final int FIELD_TYPE_INT           = 2 ;
    protected static final int FIELD_TYPE_SHORT         = 3 ;
    protected static final int FIELD_TYPE_BYTEBUFFER    = 4 ;
    protected static final int FIELD_TYPE_BOOLEAN       = 5 ;
    protected static final int FIELD_TYPE_ARRAY         = 6 ;
    public static final int SIZE_OF_ATTR                = 1 ;
    public static final int SIZE_OF_LEN                 = 4 ;
    public static final int SIZE_OF_COUNT               = 4 ;
    public static final int SIZE_OF_INT                 = 4 ;
    public static final int SIZE_OF_TYPE                = 2 ;
    public static final int SIZE_OF_SHORT               = 2 ;
    public static final int SIZE_OF_BOOLEAN             = 1 ;

    public abstract void initField() ;
    public AtModule2(byte[] data, int offset, int len) throws YpModuleException {
        initField();
        parse(data, offset, len);
    }

    public AtModule2(short type){
        initField();
        this.type = type ;
    }
    public int caleSize(){
        int totalSize = SIZE_OF_TYPE ;
        Iterator<Map.Entry<Byte,Field> > it = fields.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Byte,Field> entry = it.next();
            Field f  = entry.getValue();
            try {
                Object o = f.get(this) ;
                if( o != null){
                   totalSize += caleObjectSize(o) ;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return totalSize ;
    }
    private int caleObjectSize(Object o){
        int size = 0 ;
        int type = clsType(o.getClass()) ;
        switch (type){
            case FIELD_TYPE_INT :
                size = caleIntSize() ;
                break ;
            case FIELD_TYPE_SHORT :
                size = caleShortSize() ;
                break ;
            case FIELD_TYPE_STRING :
                size = caleStringSize((String) o) ;
                break ;
            case FIELD_TYPE_BYTEBUFFER :
                size = caleByteBufferSize((ByteBuffer) o) ;
                break ;
            case FIELD_TYPE_BOOLEAN :
                size = caleBooleanSize() ;
                break ;
            case FIELD_TYPE_ARRAY :
                size = caleArraySize((List) o);
                break;
        }
        return size ;
    }
    private int caleStringSize(String val){
        return  SIZE_OF_ATTR + SIZE_OF_LEN + val.getBytes().length ;
    }
    private int caleIntSize(){
        return SIZE_OF_ATTR + SIZE_OF_LEN + SIZE_OF_INT ;
    }
    private int caleShortSize(){
        return SIZE_OF_ATTR + SIZE_OF_LEN + SIZE_OF_SHORT ;
    }
    private int caleBooleanSize(){
        return SIZE_OF_ATTR + SIZE_OF_LEN + SIZE_OF_BOOLEAN ;
    }
    private int caleByteBufferSize(ByteBuffer byteBuffer){
        LtLog.i("byteBuffer limit:"+byteBuffer.limit()+" position:"+byteBuffer.position()) ;
        return  SIZE_OF_ATTR + SIZE_OF_LEN + byteBuffer.limit() ;
    }
    private int caleArraySize(List array){
        int size = 0 ;
        size += SIZE_OF_ATTR ;
        size += SIZE_OF_COUNT ;
        for(int i = 0 ;i < array.size() ; ++i){
            Object o = array.get(i) ;
            if(o != null && o instanceof AtModule2) {
                size += SIZE_OF_INT;
                size += ((AtModule2) o).caleSize() - SIZE_OF_TYPE ;//数组不包含类型长度
            }else{
                LtLog.w("cale array size error array member must be instanceof YpModule") ;
            }
        }
        LtLog.i("cale array size:"+size) ;
        return  size ;
    }

    //打包所有属性到bytebuffer
    protected ByteBuffer packageData(ByteBuffer byteBuffer){
        Iterator<Map.Entry<Byte, Field> > iter = fields.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Byte,Field> entry = iter.next();
            putField(entry.getValue(), entry.getKey(), byteBuffer);
        }
        return byteBuffer ;
    }

    private ByteBuffer toData(ByteBuffer byteBuffer){
        byteBuffer.putShort(type) ;
        packageData(byteBuffer) ;
        return byteBuffer ;
    }
    public ByteBuffer toData(){
       ByteBuffer byteBuffer = ByteBuffer.allocate(caleSize()) ;
       return  toData(byteBuffer) ;
    }
    public ByteBuffer toDataWithLen(){

        int size = caleSize() ;
        ByteBuffer byteBuffer = ByteBuffer.allocate(size + SIZE_OF_LEN) ;
        byteBuffer.putInt(0) ; //占位
        toData(byteBuffer) ;
        byteBuffer.putInt(0, size) ;

        return byteBuffer ;
    }
//    //打包所有属性到bytebuffer
//    public ByteBuffer packageData(ByteBuffer byteBuffer){
//        Iterator<Map.Entry<Byte, Field> > iter = fields.entrySet().iterator();
//        while (iter.hasNext()) {
//            Map.Entry<Byte,Field> entry = iter.next();
//            putField(entry.getValue(), entry.getKey(), byteBuffer);
//        }
//        return byteBuffer ;
//    }
//    public byte[] toData(){
//        ByteBuffer byteBuffer = ByteBuffer.allocate(caleSize()) ;
//        byteBuffer.putShort(type) ;
//        Iterator<Map.Entry<Byte, Field> > iter = fields.entrySet().iterator();
//        while (iter.hasNext()) {
//            Map.Entry<Byte,Field> entry = iter.next();
//            putField(entry.getValue(), entry.getKey(), byteBuffer);
//        }
//
//        byte b[] = new byte[byteBuffer.position()] ;
//        byteBuffer.flip() ;
//        byteBuffer.get(b);
//        return b ;
//    }
    public static int clsType(Class cls){
        if(cls.equals(String.class)) {
            return FIELD_TYPE_STRING ;
        }
        if(cls.equals(Integer.class) || cls.equals(int.class)) {
            return FIELD_TYPE_INT ;
        }
        if(cls.equals(Short.class) || cls.equals(short.class)) {
            return FIELD_TYPE_SHORT ;
        }
        if(ByteBuffer.class.isAssignableFrom(cls)){
            return FIELD_TYPE_BYTEBUFFER ;
        }
        if(cls.equals(Boolean.class) || cls.equals(boolean.class)){
            return FIELD_TYPE_BOOLEAN ;
        }
        if(List.class.isAssignableFrom(cls)){
            return FIELD_TYPE_ARRAY ;
        }
        return FIELD_TYPE_UNKNOWN ;
    }
    private int fieldType(Field field){
        return clsType(field.getType());
    }
    private void putField(Field field, int attr, ByteBuffer byteBuffer){
        switch (fieldType(field)){
            case FIELD_TYPE_INT :
                putInt(field, (byte) attr,byteBuffer) ;
                break ;
            case FIELD_TYPE_SHORT :
                putShort(field, (byte) attr,byteBuffer) ;
                break ;
            case FIELD_TYPE_STRING :
                putString(field, (byte) attr,byteBuffer) ;
                break ;
            case FIELD_TYPE_BOOLEAN :
                putBoolean(field, (byte) attr, byteBuffer) ;
                break;
            case FIELD_TYPE_BYTEBUFFER :
                putByteBuffer(field, (byte) attr, byteBuffer) ;
                break ;
            case FIELD_TYPE_ARRAY :
                putArray(field, (byte) attr, byteBuffer) ;
                break;
            default:
                break ;
        }
    }

    public int parse(byte[] data, int offset, int len) throws YpModuleException {
        int cursor = offset ;
        while( cursor < (len + offset) ){
            byte attr = data[cursor] ;
            cursor += SIZE_OF_ATTR ;
            Field field =  fields.get(attr) ;
            if(field == null){
                LtLog.w("unknown attr:"+attr) ;
                break ;
            }
            cursor = setFieldValue(field, data, cursor) ;
            if(cursor <=0){
                LtLog.w("error in parse moduel.........") ;
                break ;
            }
        }
        return cursor ;
    }

    private int setStringValue(Field field, byte[]data, int offset){
        int valLen = Utils.bytesToInt(data, offset) ;
        offset += SIZE_OF_LEN ;
        try {
            field.set(this, Utils.bytesToString(data, offset, valLen));
            offset += valLen ;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return offset ;
    }
    private int setBooleanValue(Field field, byte[] data, int offset){
        int len = Utils.bytesToInt(data, offset) ;
        offset += SIZE_OF_LEN ;
        if(len != SIZE_OF_BOOLEAN){
            System.out.println("set Boolean Value error size not "+SIZE_OF_BOOLEAN) ;
        }else {
            try {
                field.set(this, (data[offset] != 0));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        offset += SIZE_OF_BOOLEAN ;
        return  offset ;
    }
    private boolean putBoolean(Field field, byte attr, ByteBuffer byteBuffer){
        boolean ret = false ;
        try {
            boolean val = (boolean) field.get(this);
            if(attr != 0){
                byteBuffer.put(attr) ;
                byteBuffer.putInt(SIZE_OF_BOOLEAN) ;
                byteBuffer.put((byte) (val?1:0)) ;
                ret = true ;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ret ;
    }
    private int setByteBufferValue(Field field, byte[] data, int offset) throws YpModuleException {
        int valLen = Utils.bytesToInt(data, offset) ;
        offset += SIZE_OF_LEN ;
        if(valLen > data.length + offset){
            LtLog.i("YpModule setByteBuffer err: valLen:"+valLen+" data len:"+data.length+" offset:"+offset) ;
            throw new YpModuleException();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(valLen) ;
        byteBuffer.put(data, offset, valLen) ;
        offset += valLen ;
        try {
            field.set(this, byteBuffer);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return offset ;
    }
    public int setArrayValue(Field field, byte[] data, int offset) throws YpModuleException {
        int arraySize = Utils.bytesToInt(data, offset) ;
        offset += SIZE_OF_LEN ;
        List<AtModule2> list = new ArrayList<>() ;
        try {
            field.set(this, list);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class itemClass = (Class) type.getActualTypeArguments()[0];
        int cursor = offset ;
        while(cursor < offset + arraySize)
            try {
                AtModule2 item = (AtModule2) itemClass.newInstance();
                int itemSize = Utils.bytesToInt(data, cursor) ;
                cursor += SIZE_OF_LEN ;
                cursor = item.parse(data, cursor, itemSize) ;
                list.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        return  cursor ;
    }
    public boolean putArray(Field field, byte attr, ByteBuffer byteBuffer){
        boolean ret = false ;
        try {
            List<AtModule2> list = (List<AtModule2>) field.get(this);
            if(attr != 0){
                byteBuffer.put(attr) ;
                int mark = byteBuffer.position() ;
                byteBuffer.putInt(0) ;//占位
                Iterator<AtModule2> iter = list.iterator() ;
                while(iter.hasNext()){
                    AtModule2 ypModule = iter.next() ;
                    int itemMark = byteBuffer.position() ;
                    byteBuffer.putInt(0) ;//占位
                    ypModule.packageData(byteBuffer) ;
                    int itemSize = byteBuffer.position() - itemMark - SIZE_OF_LEN;
                    byteBuffer.putInt(itemMark, itemSize) ;
                }
                int size = byteBuffer.position() - mark - SIZE_OF_LEN ;
                byteBuffer.putInt(mark, size) ;
                ret = true ;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return  ret ;
    }
    private boolean putByteBuffer(Field field, byte attr, ByteBuffer byteBuffer){
        boolean ret = false ;
        try {
            ByteBuffer val = (ByteBuffer) field.get(this);
            if (attr != 0){
                byteBuffer.put(attr) ;
                byteBuffer.putInt(val.limit()) ;
                byteBuffer.put(val) ;
                ret = true ;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ret ;
    }
    private boolean putString(Field field, byte attr, ByteBuffer byteBuffer){
        boolean ret = false ;
        try {
                String val = (String) field.get(this);
                if(val != null && attr != 0) {
                    byteBuffer.put(attr);
                    byteBuffer.putInt(val.getBytes().length) ;
                    byteBuffer.put(val.getBytes()) ;
                    ret = true ;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        return ret ;
    }

    private int setIntValue(Field field, byte[] data, int offset) throws YpModuleException {
        int len = Utils.bytesToInt(data, offset) ;
        offset += SIZE_OF_LEN;
        if(len != SIZE_OF_INT){
            System.out.println("errnor set Int Value size not "+ SIZE_OF_LEN) ;
            throw new YpModuleException() ;
        }else {
            try {
                field.set(this, Utils.bytesToInt(data, offset));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        offset += SIZE_OF_INT ;
        return offset ;
    }
    private boolean putInt(Field field, byte attr, ByteBuffer byteBuffer){
        boolean ret = false ;
        try {
            int val = field.getInt(this) ;
            if(attr != 0){
                byteBuffer.put(attr) ;
                byteBuffer.putInt(SIZE_OF_INT) ;
                byteBuffer.putInt(val);
                ret = true ;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ret ;
    }

    private int setShortValue(Field field, byte[] data, int offset) throws YpModuleException {
        int len = Utils.bytesToInt(data, offset) ;
        offset += SIZE_OF_LEN  ;
        if(len != SIZE_OF_SHORT){
            System.out.println("errnor set Short Value size not "+ SIZE_OF_SHORT);
            throw new YpModuleException() ;
        }else {
            try {
                field.setShort(this, Utils.bytesToShort(data, offset));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        offset += SIZE_OF_SHORT;
        return offset ;
    }
    private boolean putShort(Field field, byte attr, ByteBuffer byteBuffer){
        boolean ret = false ;
        try {
            short val = field.getShort(this) ;
            if(attr != 0){
                byteBuffer.put(attr) ;
                byteBuffer.putInt(SIZE_OF_SHORT) ;
                byteBuffer.putShort(val) ;
                ret = true ;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return ret ;
    }
    private int setFieldValue(Field field, byte[]data, int offset) throws YpModuleException {
        switch (fieldType(field)){
            case FIELD_TYPE_INT :
                return setIntValue(field, data, offset) ;
            case FIELD_TYPE_SHORT :
                return setShortValue(field, data, offset) ;
            case FIELD_TYPE_STRING :
                return setStringValue(field, data, offset) ;
            case FIELD_TYPE_BOOLEAN :
                return setBooleanValue(field, data, offset) ;
            case FIELD_TYPE_BYTEBUFFER :
                return setByteBufferValue(field, data, offset) ;
            case FIELD_TYPE_ARRAY :
                return setArrayValue(field, data, offset) ;
            default:
                break ;
        }
        return -1 ;
    }
    public class YpModuleException extends Exception{

    }


}