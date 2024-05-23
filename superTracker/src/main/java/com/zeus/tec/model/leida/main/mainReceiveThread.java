package com.zeus.tec.model.leida.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.zeus.tec.ui.leida.util.ConvertCode;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class mainReceiveThread {
    private volatile Boolean stop = false;
    public int date_size = 1024;
    public DatagramSocket client = null;
    public int TimeOut = 3000;
    public int port = 2223;
    int No = 0;
    Context context;
    PackageProperty property = new PackageProperty();

    public mainReceiveThread(int data_size,int TimeOut,int port,Context context){
        this.date_size = data_size;
        this.TimeOut = TimeOut;
        this.port = port;
        this.context = context;
    }
    public void Run(){
        if (client == null)
        {
            try
            {
                client = new DatagramSocket(2223);
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
            Acception();
           // stop =true;
        }
//        if (client != null)
//        {
//            client.close();
//            client = null;
//            Message message = new Message();
//            message.what = 1;
//            message.obj = "传输成功";
//            handler.sendMessage(message);
//        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        public void handleMessage(@NonNull Message msg){
            switch (msg.what){
                case 1:
                    Toast.makeText(context,(String)msg.obj,Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
    public void Stop()
    {
        this.stop = true;
    }

    public void Acception()
    {
        byte[] buffer = new byte[date_size];
        try
        {
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
             client.receive(packet);
           int size =  packet.getLength();
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
                                       // length = ConvertCode.getint16(c_short[0],c_short[1]);
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
                                          //  length = ConvertCode.getint16(c_short[0],c_short[1]);
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
//                                TimeSpan ts = DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, 0);
//                                long time = Convert.ToInt64(ts.TotalMilliseconds);
//                                Debug.WriteLine("接受整包时间：" + time);
                                long time = System.currentTimeMillis();

                                ExplainMessage(property);

//                                ts = DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, 0);
//                                time = Convert.ToInt64(ts.TotalMilliseconds);
//                                Debug.WriteLine("解析包时间：" + time);
                                time = System.currentTimeMillis();

                            }
                        }

                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return;
        }
        finally
        {

        }
    }

    private void ExplainMessage(PackageProperty property)
    {
        MainCache cache = MainCache.GetInstance();
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
            //  int check = property.check & 0xff;
            byte low = (byte)sum;
            int check_ed= low & 0xFF;
            int check = check_ed;
            if (check == check_ed)
            {

                int index = 0;
                Sign = property.data[index];
                index++;

                BIP = property.data[index];
                index++;

                if (Sign == (byte)0x67)   //设备工作状态
                {
                    if(cache.DeviceInfo!=null)
                    {
                        int DataLength = 6+4;
                        int begin = 2;
                        int length = property.data_length - begin;

                        if (length == DataLength)
                        {
                            byte[] data = new byte[DataLength];
                            System.arraycopy(property.data,begin,data,0,DataLength);
                            //  Buffer.BlockCopy(property.data, begin, data, 0, DataLength);
                            cache.DeviceInfo.Set(data);
                        }
                    }
                }
                else
                {
                    if (cache.DeviceOper != null)
                    {
                        if (Sign == cache.DeviceOper.Sign)
                        {
                            if (Sign == (byte)0x63)
                            {
                                byte[] buffer = new byte[32];
                                for (int i = 0; i < 32; i++)
                                {
                                    buffer[i] = property.data[index];
                                    index++;
                                }

                                // string ss = System.Text.Encoding.Default.GetString(buffer);
                                // String ss = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).ge;
                                String ss = new String(buffer);
                                String FileName = "";
                                char [] ssChar =  ss.toCharArray();
                                for (int j = 0; j < ss.length(); j++)
                                {

                                    char c = ssChar[j];
                                    if (c == '\0')
                                    {
                                        break;
                                    }
                                    else
                                    {
                                        FileName += c;
                                    }
                                }

                                if (FileName.equals(cache.DeviceOper.FileName) )
                                {

                                    // int TotalLength = BitConverter.ToInt32(property.data,index);
                                    int TotalLength = ConvertCode.getint(property.data,index);
                                    index += 4;

                                    if (cache.DeviceOper.TotalLength == 0)
                                    {
                                        cache.DeviceOper.TotalLength = TotalLength;
                                    }

                                    if (cache.DeviceOper.TotalLength == TotalLength)
                                    {

                                        int CurrentLength = ConvertCode.getint(property.data,index);
                                        index += 4;

                                        if (cache.DeviceOper.CurrentLength == CurrentLength)
                                        {

                                            int DataLength = 0;
                                            DataLength = ConvertCode.getushort(property.data,index);
                                            index += 2;



                                            int begin = 1 + 1 + 32 + 4 + 4 + 2;
                                            int length = property.data_length - begin;
                                            if (length == DataLength)
                                            {
                                                byte[] data = new byte[DataLength];
                                                System.arraycopy(property.data, begin, data, 0, DataLength);
                                               // Buffer.BlockCopy(property.data, begin, data, 0, DataLength);
                                                cache.DeviceOper.Set(data);
                                            }
                                        }
                                    }
                                }
                            }
                            else if (Sign == (byte)0x64)
                            {

                                int TotalLength = ConvertCode.getint(property.data,index);
                                index += 4;

                                if (cache.DeviceOper.TotalLength == 0)
                                {
                                    cache.DeviceOper.TotalLength = TotalLength;
                                }

                                if (cache.DeviceOper.TotalLength == TotalLength)
                                {

                                    int  CurrentLength = ConvertCode.getint(property.data,index);
                                    index += 4;

                                    if (cache.DeviceOper.CurrentLength == CurrentLength)
                                    {
                                        int DataLength = 0;
                                        DataLength = ConvertCode.getushort(property.data,index);
                                        index += 2;

                                        int begin = 1 + 1 + 4 + 4 + 2;
                                        int length = property.data_length - begin;
                                        if (length == DataLength)
                                        {
                                            byte[] data = new byte[DataLength];
                                            System.arraycopy(property.data, begin, data, 0, DataLength);
                                           // Buffer.BlockCopy(property.data, begin, data, 0, DataLength);
                                            cache.DeviceOper.Set(data);
                                        }
                                    }
                                }
                            }
                            else if (Sign == (byte)0x65)
                            {
                                int DataLength = 2;
                                int begin = 2;
                                int length = property.data_length - begin;

                                if (length == DataLength)
                                {
                                    byte[] data = new byte[DataLength];
                                    System.arraycopy(property.data, begin, data, 0, DataLength);
                                   // Buffer.BlockCopy(property.data, begin, data, 0, DataLength);
                                    cache.DeviceOper.Set(data);
                                }
                            }
                            else if (Sign == (byte)0x66)
                            {
                                int DataLength = 2;
                                int begin = 2;
                                int length = property.data_length - begin;

                                if (length == DataLength)
                                {
                                    byte[] data = new byte[DataLength];
                                    System.arraycopy(property.data, begin, data, 0, DataLength);
                                    //Buffer.BlockCopy(property.data, begin, data, 0, DataLength);
                                    cache.DeviceOper.Set(data);
                                }
                            }

                            else if (Sign == (byte)0x67)
                            {
                                int DataLength = 6+4;
                                int begin = 2;
                                int length = property.data_length - begin;

                                if (length == DataLength)
                                {
                                    byte[] data = new byte[DataLength];
                                    System.arraycopy(property.data, begin, data, 0, DataLength);
                                   // Buffer.BlockCopy(property.data, begin, data, 0, DataLength);
                                    cache.DeviceOper.Set(data);
                                }
                            }
                            else if (Sign == (byte)0x62)
                            {
                                int DataLength = 34;
                                int begin = 2;
                                int length = property.data_length - begin;

                                if (length == DataLength)
                                {
                                    byte[] data = new byte[DataLength];
                                    System.arraycopy(property.data, begin, data, 0, DataLength);
                                   // Buffer.BlockCopy(property.data, begin, data, 0, DataLength);
                                    cache.DeviceOper.Set(data);
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception ex){
              int a =0;
        }

    }

}
