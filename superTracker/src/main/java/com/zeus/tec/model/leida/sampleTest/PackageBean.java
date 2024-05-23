package com.zeus.tec.model.leida.sampleTest;

public class PackageBean {
    public byte[] msb;
    public byte[] data;
    public byte check;
    public int msb_length;
    public int check_length;
    public int data_length;
    public int data_total_length;
    public byte last_byte;
    public boolean is_head;

    public PackageBean()
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
