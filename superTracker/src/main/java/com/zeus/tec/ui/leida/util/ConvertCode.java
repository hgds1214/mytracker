package com.zeus.tec.ui.leida.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public  class ConvertCode {

    public static int getint16(byte byte1,byte byte2)
    {
        byte[] byteArray = { byte1, byte2 }; // 两字节的字节数组，byte1 为高字节，byte2 为低字节

        int result = ((byteArray[0] & 0xFF) << 8) | (byteArray[1] & 0xFF);

        return result;
    }
    public static int getint16 (byte [] buffer,ByteOrder order){
        return ByteBuffer.wrap(buffer).order(order).getShort();
    }

    public  static  int getushort (byte [] buffer,ByteOrder order){
       int tmp =   ((buffer[0]&0xff))*256+(buffer[1]&0xff);
       return tmp;
    }

    public static  float getFloat (byte [] buffer , ByteOrder order){
        return ByteBuffer.wrap(buffer).order(order).getFloat();
    }

    public static int getint (byte [] buffer,int index){

        byte [] tmp = new byte[4];
        for (int j = 0;j<4;j++){
            tmp [j] = buffer [j+index];
        }

      return  ByteBuffer.wrap(tmp).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static int getushort (byte [] buffers,int index){
        byte [] tmp = new byte[2];
        for (int j = 0;j<2;j++){
            tmp [j] = buffers [j+index];
        }
        short temp =  ByteBuffer.wrap(tmp).order(ByteOrder.LITTLE_ENDIAN).getShort();
       return getUnsignedShort(temp);
    }
    public static int getUnsignedShort(short data){ //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
        return data&0x0FFFF ;
    }
   // public static

    public static byte [] getBytetoCSharp (int tmp){
      byte [] result =  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(tmp).array();
      return result;
    }

    public static byte [] getBytetoCSharp (short tmp){
        byte [] result =  ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(tmp).array();
        return result;
    }

    public static byte [] getBytetoCSharp (float tmp){
        byte [] result =  ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(tmp).array();
        return result;
    }

    public static byte [] getBytetoCSharp (double tmp){
        byte [] result =  ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).putDouble(tmp).array();
        return result;
    }




}
