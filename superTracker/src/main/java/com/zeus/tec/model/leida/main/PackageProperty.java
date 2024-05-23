package com.zeus.tec.model.leida.main;

public class PackageProperty {
    public byte[] msb; //长度数据
    public byte[] data; //帧数据
    public byte check;  //累计校验数据
    public int msb_length; //获取的长度数据长度
    public int check_length; //获取计校验数据长度
    public int data_length; //获取获取帧数据长度
    public int data_total_length; //帧数据总长度
    public byte last_byte;  //上一个获取数据字节
    public Boolean is_head;

    public PackageProperty()
    {
        is_head = false;
        msb = new byte[2];
        data = null;
        check = 0x00;
        last_byte = 0x7E;
        msb_length = 0;
        check_length = 0;
        data_length = 0;
        data_total_length = 0;
    }

    public void Newly()
    {
        is_head = true;
        data = null;
        check = 0x00;
        last_byte = 0x7E;
        msb_length = 0;
        check_length = 0;
        data_length = 0;
        data_total_length = 0;
    }

    public void Clear()
    {
        is_head = false;
        data = null;
        check = 0x00;
        last_byte = 0x00;
        msb_length = 0;
        check_length = 0;
        data_length = 0;
        data_total_length = 0;
    }
}
