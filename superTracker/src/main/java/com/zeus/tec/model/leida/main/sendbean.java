package com.zeus.tec.model.leida.main;

import android.os.AsyncTask;
import android.widget.Toast;

import com.zeus.tec.ui.leida.util.ConvertCode;
import com.zeus.tec.ui.leida.util.MyApplicationContext;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class sendbean {
    public byte[] buffer = null;
    public int OutTime = 0;
    int index = 0;

    public sendbean(int data_size, int OutTime)
    {
        this.buffer = new byte[data_size];
        this.OutTime = OutTime;
    }
    public ReceiveBean StartWorkOrder(byte code)
    {
        ReceiveBean order = null;
        index = 0;
        try
        {
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;
            short order_length = 1 + 1 + 1;
            byte[] length = ByteBuffer.allocate(2).putShort(order_length).array();
            EncodeInput(length[0]);
            EncodeInput(length[1]);
            byte sign = 0x66;
            sum += EncodeInput(sign);
            sum += EncodeInput(code);
            byte oper = 0x01;
            sum += EncodeInput(oper);
            byte check = (byte)(sum);
            EncodeInput(check);
            String Name = "";
            order = new ReceiveBean(sign, this.OutTime, Name);
            byte[] tmp = new byte[index];
            //Buffer.BlockCopy(buffer, 0, tmp, 0, index);
            System.arraycopy(buffer,0,tmp,0,index);
            order.Data = tmp;
        }
        catch (Exception ex)
        {
            order = null;
        }
        return order;
    }

    public ReceiveBean DeveiceStatusOrder(byte code)
    {
        ReceiveBean order = null;
        index = 0;
        try
        {
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;
            short order_length = 1 + 1;
            byte[] length = ByteBuffer.allocate(2).putShort(order_length).array();
            EncodeInput(length[0]);
            EncodeInput(length[1]);
            byte sign = 0x67;
            sum += EncodeInput(sign);
            sum += EncodeInput(code);
            byte check = (byte)(sum);
            EncodeInput(check);
            String Name = "";
            order = new ReceiveBean(sign, this.OutTime, Name);
            byte[] tmp = new byte[index];
            System.arraycopy(buffer,0,tmp,0,index);
            //Buffer.BlockCopy(buffer, 0, tmp, 0, index);
            order.Data = tmp;
        }
        catch (Exception ex)
        {
            order = null;
        }
        return order;
    }

    public ReceiveBean StopWorkOrder(byte code){
        ReceiveBean order = null;
        index = 0;
        try
        {
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;
            short order_length = 1 + 1 + 1;
            byte[] length = ByteBuffer.allocate(2).putShort(order_length).array();
            EncodeInput(length[0]);
            EncodeInput(length[1]);
            byte sign = 0x66;
            sum += EncodeInput(sign);
            sum += EncodeInput(code);
            byte oper = 0x00;
            sum += EncodeInput(oper);
            byte check = (byte)(sum);
            EncodeInput(check);
            String Name = "";
            order = new ReceiveBean(sign, this.OutTime, Name);
            byte[] tmp = new byte[index];
            System.arraycopy(buffer,0,tmp,0,index);
            order.Data = tmp;
        }
        catch (Exception ex)
        {
            order = null;
        }

        return order;
    }

    public Boolean SendOrder(ReceiveBean order, DatagramSocket socket, String address, int local_port,int server_port)
    {

        Boolean success = false;
        MainCache cache = MainCache.GetInstance();
        if (order != null)
        {
            if (order.Data != null && order.Data.length > 0)
            {

                byte[] Message = new byte[order.Data.length];
                System.arraycopy(order.Data,0,Message,0,order.Data.length);
                //Buffer.BlockCopy(order.Data, 0, Message, 0, order.Data.Length);
                ReceiveBean old_order = null;
                if (order.Sign == (byte)0x67)
                {
                    old_order = cache.DeviceInfo;
                    order.Data = null;
                    cache.DeviceInfo = order;
                    if (address != null)
                    {
                        try
                        {
                            if (socket == null)
                            {

                                socket = new DatagramSocket(local_port);

                            }
                            DatagramSocket finalSocket1 = socket;
                            Thread th = new Thread(()->{
                                DatagramPacket datagramPacket = null;
                                try {

                                    datagramPacket = new DatagramPacket(Message,Message.length, InetAddress.getByName(address),server_port);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    finalSocket1.send(datagramPacket);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            th.start();

                            //int tmp=socket.SendTo(Message, address);
                            success = cache.DeviceInfo.Get();
                        }
                        catch (Exception ex)
                        {

                            success = false;
                            ReceiveBean tmp = cache.DeviceInfo;
                            cache.DeviceInfo = null;
                            //MessageBox.Show(ex.ToString());
                        }
                        finally
                        {
                            //socket.Close();
                            //socket = null;
                        }
                    }

                }
                else
                {
                    old_order = cache.DeviceOper;
                    order.Data = null;
                    cache.DeviceOper = order;
                    if (address != null)
                    {
                        try
                        {
                            if (socket == null)
                            {
                                socket = new DatagramSocket(2222);
                                //socket.Bind(new IPEndPoint(IPAddress.Any, local_port));
                            }
                           // TimeSpan ts = DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, 0);
                            //long time1 = Convert.ToInt64(ts.TotalMilliseconds);
                            long time1 = System.currentTimeMillis();
                           // Debug.WriteLine("发送时间" + time1);
                            DatagramSocket finalSocket = socket;
                            Thread th = new Thread(()->{
                                try {
                                  DatagramPacket  datagramPacket = new DatagramPacket(Message,Message.length, InetAddress.getByName(address),1234);
                                    finalSocket.send(datagramPacket);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //  int tmp=socket.SendTo(Message, address);
                            });
                            th.setDaemon(true);
                            th.start();
                            long time2 = System.currentTimeMillis();
//                            ts = DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, 0);
//                            long time2 = Convert.ToInt64(ts.TotalMilliseconds);
//                            Debug.WriteLine("发送成功时间：" + time2);
                            success = cache.DeviceOper.Get();
                        }
                        catch (Exception ex)
                        {
                           // Debug.WriteLine("error");
                            success = false;
                            ReceiveBean tmp = cache.DeviceOper;
                            cache.DeviceOper = null;
                            //MessageBox.Show(ex.ToString());
                        }
                        finally
                        {
                            // socket.Close();
                            // socket = null;
                        }
                    }
                }




            }
        }

        return success;
    }

    public ReceiveBean DownloadFileOrder(byte code, String FileName, int CurrentLength)
    {
        ReceiveBean order = null;
        index = 0;
        if (FileName.length()> 31)
        {

        }
        else
        {
            try
            {
                long sum = 0;
                byte head = 0x7E;
                buffer[index] = head;
                index++;

                short order_length = 1 + 1 + 32 + 4;
                byte[] length = ByteBuffer.allocate(2).putShort(order_length).array();
                EncodeInput(length[0]);
                EncodeInput(length[1]);

                byte sign = 0x63;
                sum += EncodeInput(sign);

                sum += EncodeInput(code);
               // byte[] Name = Encoding.UTF8.GetBytes(FileName);
                byte [] Name = FileName.getBytes(StandardCharsets.UTF_8);
                for (int i = 0; i < Name.length; i++)
                {
                    sum += EncodeInput(Name[i]);
                }
                for (int i = Name.length; i < 32; i++)
                {
                    sum += EncodeInput((byte)0x00);
                }
                byte[] intBuffer = ConvertCode.getBytetoCSharp(CurrentLength);
                for (int i = 0; i < 4; i++)
                {
                    sum += EncodeInput(intBuffer[i]);
                }
                byte check = (byte)(sum);
                EncodeInput(check);
                order = new ReceiveBean(sign, this.OutTime, FileName);
                byte[] tmp = new byte[index];
                System.arraycopy(buffer,0,tmp,0,index);
                order.Data = tmp;
                order.CurrentLength = CurrentLength;
            }
            catch (Exception ex)
            {
                order = null;
            }
        }
        return order;
    }

    public ReceiveBean DeleteFileOrder(byte code, String FileName)
    {
        ReceiveBean order = null;
        index = 0;
        if (FileName.length() > 31)
        {

        }
        else
        {
            try
            {
                long sum = 0;
                byte head = 0x7E;
                buffer[index] = head;
                index++;
                short order_length = (short)(1 + 1 + 32);
                byte[] length = ConvertCode.getBytetoCSharp(order_length);
                EncodeInput(length[1]);
                EncodeInput(length[0]);
                byte sign = 0x62;
                sum += EncodeInput(sign);
                sum += EncodeInput(code);
                byte[] Name = FileName.getBytes(StandardCharsets.UTF_8);
                for (int i = 0; i < Name.length; i++)
                {
                    sum += EncodeInput(Name[i]);
                }
                for (int i = Name.length; i < 32; i++)
                {
                    sum += EncodeInput((byte)0x00);
                }
                byte check = (byte)(sum);
                EncodeInput(check);
                order = new ReceiveBean(sign, this.OutTime, FileName);
                byte[] tmp = new byte[index];
                System.arraycopy(buffer,0,tmp,0,index);
                order.Data = tmp;

            }
            catch (Exception ex)
            {
                order = null;
            }
        }

        return order;
    }

    public ReceiveBean SettingOrder(byte code, String FileName, int SampleCount, int StackCount, int NbOfSampleDelayPoint, int AmplifyValue, int SampleFrequency, float TimeInterval,float GYROThreshold)
    {
        ReceiveBean order = null;
        index = 0;
        if (FileName.length() > 31)
        {
            Toast.makeText(MyApplicationContext.getInstance().getAppContext(),"文件名不能超过31个字母",Toast.LENGTH_LONG);
          //  MessageBox.Show("文件名不能超过31个字母!");
        }
        else
        {
            try
            {
                LocalDateTime now;
                byte[] time = new byte[6];
                // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    now = LocalDateTime.now();
                    int year = now.getYear();
                    int month = now.getMonthValue();
                    int day = now.getDayOfMonth();
                    int hour = now.getHour();
                    int minute = now.getMinute();
                    int second = now.getSecond();
                    year = year - 2000;

                    time[0] = (byte)year;
                    time[1] = (byte)month;
                    time[2] = (byte)day;
                    time[3] = (byte)hour;
                    time[4] = (byte)minute;
                    time[5] = (byte)second;
                }



                long sum = 0;
                byte head = 0x7E;
                buffer[index] = head;
                index++;

                short order_length = (short)(1 + 1 + 6 + 32 + 5*2+4 +4);
                byte[] length = ConvertCode.getBytetoCSharp(order_length);
                EncodeInput(length[1]);
                EncodeInput(length[0]);


                byte sign = 0x65;
                sum += EncodeInput(sign);


                sum += EncodeInput(code);

                for (int i = 0; i < 6; i++)
                {
                    sum += EncodeInput(time[i]);
                }


                byte[] Name = FileName.getBytes(StandardCharsets.UTF_8);
                for (int i = 0; i < Name.length; i++)
                {
                    sum += EncodeInput(Name[i]);
                }
                for (int i = Name.length; i < 32; i++)
                {
                    sum += EncodeInput((byte)0x00);
                }


                byte[] short_byte = ConvertCode.getBytetoCSharp((short)SampleCount);
                for (int i = 0; i < 2; i++)
                {
                    sum += EncodeInput(short_byte[i]);
                }

                short_byte = ConvertCode.getBytetoCSharp((short)StackCount);
                for (int i = 0; i < 2; i++)
                {
                    sum += EncodeInput(short_byte[i]);
                }

                short_byte = ConvertCode.getBytetoCSharp((short)NbOfSampleDelayPoint);
                for (int i = 0; i < 2; i++)
                {
                    sum += EncodeInput(short_byte[i]);
                }

                short_byte = ConvertCode.getBytetoCSharp((short)AmplifyValue);
                for (int i = 0; i < 2; i++)
                {
                    sum += EncodeInput(short_byte[i]);
                }

                short_byte = ConvertCode.getBytetoCSharp((short)SampleFrequency);
                for (int i = 0; i < 2; i++)
                {
                    sum += EncodeInput(short_byte[i]);
                }



                byte[] FloatBuffer = ConvertCode.getBytetoCSharp(TimeInterval);
                for (int j = 0; j < 4; j++)
                {
                    sum += EncodeInput(FloatBuffer[j]);
                }

                FloatBuffer = ConvertCode.getBytetoCSharp(GYROThreshold);
                for (int j = 0; j < 4; j++)
                {
                    sum += EncodeInput(FloatBuffer[j]);
                }




                byte check = (byte)(sum);
                EncodeInput(check);

                order = new ReceiveBean(sign, this.OutTime, FileName);
                byte[] tmp = new byte[index];
                System.arraycopy(buffer,0,tmp,0,index);

                order.Data = tmp;

            }
            catch (Exception ex)
            {

                order = null;
            }
        }
        return order;
    }

    public ReceiveBean CreateCatalogueOrder(byte code, int CurrentLength)
    {
        ReceiveBean order = null;
        index = 0;
        try
        {
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;

            short order_length = 1 + 1 + 4;
            byte[] length = ConvertCode.getBytetoCSharp(order_length);
            EncodeInput(length[1]);
            EncodeInput(length[0]);

            byte sign = 0x64;
            sum += EncodeInput(sign);
            sum += EncodeInput(code);

            byte[] ParamID = ConvertCode.getBytetoCSharp(CurrentLength);
            for (int i = 0; i < 4; i++)
            {
                sum += EncodeInput(ParamID[i]);
            }
            byte check = (byte)(sum);
            EncodeInput(check);

            String Name = "";
            order = new ReceiveBean(sign, OutTime, Name);

            byte[] tmp = new byte[index];

            System.arraycopy(buffer,0,tmp,0,index);
            order.Data = tmp;
            order.CurrentLength = CurrentLength;
        }
        catch (Exception ex)
        {
            order = null;
        }
        return order;

    }


    public int EncodeInput(byte code)
    {
        int size = (code) & 0xFF;
        if (code == (byte)0x7E)
        {
            code = (byte)0x7D;
            buffer[index] = code;
            index++;
            code = (byte)0x5E;
            buffer[index] = code;
            index++;
        }
        else if (code == (byte)0x7D)
        {
            code = (byte)0x7D;
            buffer[index] = code;
            index++;
            code = (byte)0x5D;
            buffer[index] = code;
            index++;
        }
        else
        {
            buffer[index] = code;
            index++;
        }
        return size;
    }

}
