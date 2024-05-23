package com.zeus.tec.model.leida.sampleTest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.zeus.tec.model.leida.sampleTest.Message1;
import androidx.annotation.NonNull;

import com.zeus.tec.model.leida.sampleTest.DataCache;
import com.zeus.tec.model.leida.sampleTest.PackageBean;
import com.zeus.tec.ui.leida.util.ConvertCode;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class ReceiveMessage {

    volatile Boolean stop = false;
   // Socket ReceiveSocket = null;
    int TimeOut = 3000;
    int port = 2228;
    int date_size = 1024;
    DatagramSocket datagramSocket = null ;
    PackageBean property = new PackageBean();
    DataCache cache = DataCache.GetInstance();
    public  static  int startconunt = 0;
    Context context;
    public ILeidaReciveListener  onleidaLister ;

    int No = 0;

    public ReceiveMessage(int data_size,int TimeOut,int port,ILeidaReciveListener leidaReciveListener)
    {
        this.onleidaLister = leidaReciveListener;
        this.date_size = data_size;
        this.TimeOut = TimeOut;
        this.port = port;

    }
    public void Stop(){
        stop = true;
    }
    DatagramPacket packet;

    public void Run( )  {

        if (datagramSocket == null)
        {
            try
            {
                datagramSocket = new DatagramSocket(2224);
            }
            catch (Exception ex)
            {
                return;
            }
            finally
            {

            }
        }
        No = 0;
        while (!stop)
        {
          Receive();
        }
    }


    public int getint16(byte byte1,byte byte2)
    {
        byte[] byteArray = { byte1, byte2 }; // 两字节的字节数组，byte1 为高字节，byte2 为低字节
        int result = ((byteArray[0] & 0xFF) << 8) | (byteArray[1] & 0xFF);
        return result;
    }

    public void Receive()
    {
        byte[] buffer = new byte[date_size];
        packet = new DatagramPacket(buffer,buffer.length);
        try
        {
            datagramSocket.receive(packet);
            int size = packet.getLength();
            byte[] c_short = new byte[2];
            int length = 0;
            if (size > 0)
            {
                for (int i = 0; i < size; i++)
                {
                    byte tmp = buffer[i];
                    if (tmp == (byte)0x7E)
                    {
                        property.Newly();
                    }
                    else if (!property.is_head)
                    {
                        continue;
                    }
                    else
                    {
                        if (property.last_byte == (byte)0x7D)
                        {
                            if (tmp == (byte)0x5E)
                            {
                                property.last_byte = tmp;
                                tmp = 0x7E;
                            }
                            else if (tmp == (byte)0x5D)
                            {
                                property.last_byte = tmp;
                                tmp = 0x7D;
                            }
                            else
                            {
                                property.Clear();
                                continue;
                            }
                            if (property.msb_length < 2)
                            {
                                property.msb[property.msb_length] = tmp;
                                property.msb_length++;
                                if (property.msb_length == 2)
                                {
                                    c_short[0] = property.msb[0];
                                    c_short[1] = property.msb[1];
                                    try
                                    {
                                        length = ConvertCode.getint16(c_short,ByteOrder.BIG_ENDIAN);
                                    }
                                    catch (Exception ex)
                                    {
                                        length = 0;
                                    }
                                    if (length <= 0)
                                    {
                                        property.Clear();
                                        continue;
                                    }
                                    property.data = new byte[length];
                                    property.data_total_length = length;
                                    property.msb_length = 2;
                                    property.data_length = 0;
                                    property.check_length = 0;
                                }
                            }
                            else if (property.data_length < property.data_total_length)
                            {
                                property.data[property.data_length] = tmp;
                                property.data_length++;
                                if (property.data_length == property.data_total_length)
                                {
                                    property.check_length = 0;
                                }
                            }
                            else if (property.check_length < 1)
                            {
                                property.check = tmp;
                                property.check_length = 1;
                            }
                        }
                        else
                        {
                            if (tmp == (byte)0x7D)
                            {
                                property.last_byte = tmp;
                            }
                            else
                            {
                                property.last_byte = tmp;
                                //接收到包头后读取两位为消息长度
                                if (property.msb_length < 2)
                                {
                                    property.msb[property.msb_length] = tmp;
                                    property.msb_length++;
                                    if (property.msb_length == 2)
                                    {
                                        //大小端
                                        c_short[1] = property.msb[1];
                                        c_short[0] = property.msb[0];
                                        try
                                        {
                                            length = ConvertCode.getint16(c_short,ByteOrder.BIG_ENDIAN);
                                        }
                                        //异常说明没有信息或者信息错误，重新解包剩下的
                                        catch (Exception ex)
                                        {
                                            length = 0;
                                        }
                                        if (length <= 0)
                                        {
                                            property.Clear();
                                            continue;
                                        }
                                        property.data = new byte[length];
                                        property.data_total_length = length;
                                        property.msb_length = 2;
                                        property.data_length = 0;
                                        property.check_length = 0;
                                    }
                                }
                                else if (property.data_length < property.data_total_length)
                                {
                                    property.data[property.data_length] = tmp;
                                    property.data_length++;
                                    if (property.data_length == property.data_total_length)
                                    {
                                        property.check_length = 0;
                                    }
                                }
                                else if (property.check_length < 1)
                                {
                                    property.check = tmp;
                                    property.check_length = 1;

                                }
                            }
                        }
                        if (property.check_length == 1)
                        {
                            if (property.data_length <= 0 || property.data == null)
                            {
                                property.Clear();
                                continue;
                            }
                            else
                            {
                                ExplainMessage(property);
                            }
                        }

                    }
                }
            }
        }
        catch (Exception ex)
        {
            return;
        }
        finally
        {

        }
    }

    private void ExplainMessage(PackageBean property)
    {
        long sum = 0L;
        byte Sign = (byte)0x00;
        byte BIP = (byte)0x00;
        for (int j = 0; j < property.data_length; j++)
        {
            int t = property.data[j] & 0xFF;
            sum += t;
        }
        try
        {
            int check = property.check & 0xff;
            byte low = (byte)sum;
            int check_ed = low & 0xFF;
            if (check == check_ed)
            {
                int index = 0;
                Sign = property.data[index];
                index++;

                BIP = property.data[index];
                index++;

                if (Sign == (byte)0x67)   //设备工作状态
                {
                    int begin = 2;
                    int length = property.data_length - begin;
                    if (length == (6+4))
                    {
                        byte[] tmp1 = new byte[2];
                        byte[] tmp2 = new byte[4];
                        byte[] tmp3 = new byte[4];
                        tmp2[0] = property.data[begin];
                        tmp2[1] = property.data[begin + 1];
                        tmp2[2] = property.data[begin + 2];
                        tmp2[3] = property.data[begin + 3];

                        tmp3[0] = property.data[begin + 4];
                        tmp3[1] = property.data[begin + 5];
                        tmp3[2] = property.data[begin + 6];
                        tmp3[3] = property.data[begin + 7];

                        tmp1[0] = property.data[begin + 8];
                        tmp1[1] = property.data[begin + 9];

                        if (cache.DeviceStatus == null)
                        {
                            cache.DeviceStatus = new DataCache.StatusBean();
                        }
                        long time = System.currentTimeMillis();
                        cache.LastTime = time;
                        cache.DeviceStatus.Time = time;
                        cache.DeviceStatus.WorkStatus =ConvertCode.getint16(tmp1,ByteOrder.LITTLE_ENDIAN);
                        cache.DeviceStatus.Electricity = ByteBuffer.wrap(tmp2).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        cache.DeviceStatus.GYRO = ByteBuffer.wrap(tmp3).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    }
                }
                else if (Sign == (byte)0x60) //参数配置返回结果
                {
                    int begin = 2;
                    int length = property.data_length - begin;
                    if (length == 2)
                    {
                        byte[] tmp = new byte[2];
                        tmp[0] = property.data[begin];
                        tmp[1] = property.data[begin + 1];
                        int status = ConvertCode.getint16(tmp,ByteOrder.LITTLE_ENDIAN);

                        if (cache.SettingStatus != null)
                        {
                            cache.SettingStatus.SettingStatus = status;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            LocalDateTime dateTime = LocalDateTime.now();
                        }

                        long time = System.currentTimeMillis();
                        cache.LastTime = time;
                    }
                }
                else if (Sign == (byte)0x61) //工作或空闲返回结果
                {
                    int begin = 2;
                    int length = property.data_length - begin;
                    if (length == 2)
                    {
                        byte[] tmp = new byte[2];
                        tmp[0] = property.data[begin];
                        tmp[1] = property.data[begin + 1];
                        int status = ConvertCode.getushort(tmp,0);
                        if (cache.DeviceStatus != null)
                        {
                            cache.DeviceStatus.WorkStatus = status;
                        }
                        if (cache.DeviceStatus != null)
                        {
                            cache.DeviceStatus.WorkStatus = status;
                        }
                        cache.LastTime = System.currentTimeMillis();
                        if (cache.sendcode==2){
                            onleidaLister.ongetStatus();
                        }
                    }
                }
                else if (Sign == (byte)0x68) //采集数据包
                {
                    int begin = 2;
                    float max = 0;
                    float min = 0;

                    byte[] tmp = new byte[4];

                    int length = property.data_length - begin;
                    int SampleLength = length / 4;
                    DataCache.SampleBean bean = new DataCache.SampleBean();
                    bean.Sample = new float[SampleLength];
                    bean.SampleLength = SampleLength;

                    for (int i = 0; i < SampleLength; i++)
                    {
                        int step = i * 4;
                        step = begin + step;
                        tmp[0] = property.data[step];
                        tmp[1] = property.data[step + 1];
                        tmp[2] = property.data[step + 2];
                        tmp[3] = property.data[step + 3];
                        float value  = ByteBuffer.wrap(tmp).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        if (value > max)
                            max = value;
                        if (value < min)
                            min = value;
                        bean.Sample[i] = value;
                    }
                    bean.Max = max;
                    bean.Min = min;
                    if (cache.SampleQue != null)
                        cache.SampleQue.add(bean);
                    //接收数据回调
                    if (startconunt > 3){
                        onleidaLister.onReciveData();
                        int a =1;
                       // Thread.sleep(500);
                    }
                    startconunt++;
                    cache.LastTime = System.currentTimeMillis();
                     Message1 message = new Message1();
                    byte[] data = message.SampleReceiveOrder((byte) 1, (short) 1, No);
                    cache.send.Send(data);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            property.Clear();
        }
    }
}
