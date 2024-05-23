package com.zeus.tec.model.leida.sampleTest;

import com.bumptech.glide.load.model.ModelLoader;
import com.zeus.tec.ui.leida.util.ConvertCode;
import com.zeus.tec.ui.tracker.util.TimeUtil;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

   public class Message1 {

        byte[] buffer = new byte[1024];
        int index = 0;

        public byte[] SettingOrder(byte code, int SampleCount, int StackCount, int NbOfSampleDelayPoint, int AmplifyValue, int SampleFrequency, float TimeInterval,float GYROThreshold)
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

            index = 0;
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;
            short order_length = (short)(1 + 1 + 6 + 32 + 5 * 2 + 4 + 4);

            byte[] length= ConvertCode.getBytetoCSharp(order_length);

           // byte[] length = BitConverter.GetBytes(order_length);
            EncodeInput(length[1]);
            EncodeInput(length[0]);

            byte sign = 0x60;
            sum += EncodeInput(sign);

            sum += EncodeInput(code);

            for (int i = 0; i < 6; i++)
            {
                sum += EncodeInput(time[i]);
            }

            byte[] Name = "test".getBytes();
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

            short_byte =ConvertCode.getBytetoCSharp(NbOfSampleDelayPoint);
            for (int i = 0; i < 2; i++)
            {
                sum += EncodeInput(short_byte[i]);
            }

            short_byte =ConvertCode.getBytetoCSharp((short)AmplifyValue);
            for (int i = 0; i < 2; i++)
            {
                sum += EncodeInput(short_byte[i]);
            }

            short_byte = ConvertCode.getBytetoCSharp((short)SampleFrequency);
            for (int i = 0; i < 2; i++)
            {
                sum += EncodeInput(short_byte[i]);
            }



          //  ByteBuffer.allocate(Float.BYTES).putFloat(TimeInterval).array();
            byte[] FloatBuffer =  ConvertCode.getBytetoCSharp(TimeInterval);
            for (int j = 0; j < 4; j++)
            {
                sum += EncodeInput(FloatBuffer[j]);
            }

           // FloatBuffer = ByteBuffer.allocate(Float.BYTES).putFloat(GYROThreshold).array();
            FloatBuffer =  ConvertCode.getBytetoCSharp(GYROThreshold);
            for (int j = 0; j < 4; j++)
            {
                sum += EncodeInput(FloatBuffer[j]);
            }


            byte check = (byte)(sum);
            EncodeInput(check);

            byte[] tmp = new byte[index];
            System.arraycopy(buffer, 0, tmp, 0, index);
            return tmp;
        }



        public byte[] StartWorkOrder(byte code)
        {
            index = 0;
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;
            short order_length = (short)(1 + 1 + 1);
            byte [] length = ConvertCode.getBytetoCSharp(order_length);
            EncodeInput(length[1]);
            EncodeInput(length[0]);

            byte sign = 0x61;
            sum += EncodeInput(sign);

            sum += EncodeInput(code);

            byte oper = 0x01;
            sum += EncodeInput(oper);

            byte check = (byte)(sum);
            EncodeInput(check);

            byte[] tmp = new byte[index];
            System.arraycopy(buffer, 0, tmp, 0, index);
            return tmp;
        }


        public byte[] StopWorkOrder(byte code)
        {
            index = 0;
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;
            short order_length = (short)(1 + 1 + 1);
            ByteBuffer buffer1 = ByteBuffer.allocate(Short.BYTES);
            byte[] length= buffer1.putShort(order_length).array();
            EncodeInput(length[0]);
            EncodeInput(length[1]);

            byte sign = 0x61;
            sum += EncodeInput(sign);

            sum += EncodeInput(code);

            byte oper = 0x00;
            sum += EncodeInput(oper);

            byte check = (byte)(sum);
            EncodeInput(check);

            byte[] tmp = new byte[index];
            try {
                System.arraycopy(buffer, 0, tmp, 0, index);
            }catch (Exception exception)
            {
                exception.printStackTrace();
            }

            return tmp;

        }

        public byte[] GetDeviceStatus(byte code)
        {
            index = 0;
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;
            short order_length = (short)(1 + 1);
            byte[] length=  ConvertCode.getBytetoCSharp(order_length);
            EncodeInput(length[1]);
            EncodeInput(length[0]);
            byte sign = 0x67;
            sum += EncodeInput(sign);
            sum += EncodeInput(code);
            byte check = (byte)(sum);
            EncodeInput(check);
            byte[] tmp = new byte[index];
            System.arraycopy(buffer, 0, tmp, 0, index);
            return tmp;
        }

        public byte[] SampleReceiveOrder(byte code, short status, int No)
        {
            index = 0;
            long sum = 0;
            byte head = 0x7E;
            buffer[index] = head;
            index++;
            short order_length = (short)(1 + 1 + 4 + 2);
            byte[] length= ConvertCode.getBytetoCSharp(order_length);
            EncodeInput(length[1]);
            EncodeInput(length[0]);
            byte sign = 0x68;
            sum += EncodeInput(sign);
            sum += EncodeInput(code);
            byte[] InttBuffer =ConvertCode.getBytetoCSharp(No);
            for (int j = 0; j < 4; j++)
            {
                sum += EncodeInput(InttBuffer[j]);
            }
            byte[] ShortBuffer = ConvertCode.getBytetoCSharp(status);
            for (int j = 0; j < 2; j++)
            {
                sum += EncodeInput(ShortBuffer[j]);
            }
            byte check = (byte)(sum);
            EncodeInput(check);

            byte[] tmp = new byte[index];
            System.arraycopy(buffer, 0, tmp, 0, index);
            return tmp;

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

