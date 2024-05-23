package com.zeus.tec.model.leida.sampleTest;

import android.media.Image;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.model.leida.main.MainCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class DataCache {
    public static class SampleBean
    {
        public int SampleLength;
        public float Max;
        public float Min;
        public float[] Sample;
        public float pich;
        public float roll;
        public float dip;
    }
    public static int stackCount  = 32;
    public static int DelayPointNumber = 1;
    public static int Amp =1;
    public static int frequency;
    public static float timeinterval =100;
    public static long dataTime;
    public static int sendcode = 0;
    public MainCache mainCache = MainCache.GetInstance();
    public  ILeidaReciveListener iLeidaReciveListener;


    public static class StatusBean
    {
        public long Time;
        public int WorkStatus;
        public float Electricity;
        public float GYRO;
        public StatusBean()
        {
            Time = 0;
            WorkStatus = -1;//
            Electricity = -1;
            GYRO = -1;
        }
    }

    public class WorkStatusBean
    {
        public int WorkStatus;
    }

    public static class SettingStatusBean
    {
        public int SettingStatus;
    }
    private static DataCache _instance = new DataCache();
    private ReceiveMessage receive = null;
    private Thread ReceiveThread = null;
    public sendMessage1 send = null;
    private int datasize = 2048;
    private int outtime = 5000;
    public int local_port = 2228;
    public int server_port = 1234;
    public String server_ip = "192.168.43.100";
   // public SendMessage send = null;

    //public Image MainImage = null;
   // public Image LastImage = null;
    public int ScreenWidth = 0;
    public int ScreenHeight = 0;
    public int SampleWidth = 0;
    public int SampleHeight = 0;
    public int ShowSampleNum = 26;
    public int LastWidth = 0;
    public int LastHeight = 0;
    public int LastNo = 0;
    public long LastTime = 0;
    //public static Queue<SampleBean> sampleQue = new ArrayBlockingQueue<SampleBean>(5);
   public static List<SampleBean> SampleQue = new ArrayList<>();
    public List<Image> ImageList = new ArrayList<Image>();
    public SampleBean LastSample = null;
    //public Rectangle LastSampleRect = new Rectangle();
    public StatusBean DeviceStatus = null;
    public WorkStatusBean WorkStatus = null;
    public SettingStatusBean SettingStatus = null;
    public float sampleintervel = 0;

    public static DataCache GetInstance()
    {
        if (_instance == null)
        {
            _instance = new DataCache();
        }
        return _instance;
    }

    public  void CreateReceiveThread(ILeidaReciveListener leidaLister)
    {
        if (ReceiveThread != null)
        {
            try {
                if (receive!=null){
                    receive.Stop();
                }
               // ReceiveThread.destroy();
                ReceiveThread.interrupt();
                ReceiveThread =null;
            }
            catch (Exception exception){
                exception.printStackTrace();
            }

        }
        receive = new ReceiveMessage(datasize, outtime, local_port,leidaLister);
        ReceiveThread = new Thread(()->{
            receive.Run();
        });

        ReceiveThread.start();

    }

    public void CloseReceiveThread()
    {
        try{
            if (ReceiveThread != null)
            {

                if (receive != null)
                {
                    if ( receive.datagramSocket!=null){
                        receive.datagramSocket.close();
                    }
                    receive.Stop();

                }
               // ReceiveThread.interrupt();
                if (ReceiveThread.isAlive())
                {
                    ReceiveThread.interrupt();
                }
            }
            receive = null;
            ReceiveThread = null;
        }
        catch (Exception ex)
        {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }

    }

    /*public void InitSamples(int MainWidth, int MainHeight, int LastWidth, int LastHeight)
    {
        LastNo = 0;
        SampleQue.Clear();
        ImageList.Clear();
        ScreenWidth = MainWidth;
        ScreenHeight = MainHeight;
        SampleWidth = ScreenWidth / ShowSampleNum;
        SampleHeight = ScreenHeight;
        this.LastWidth = LastWidth;
        this.LastHeight = LastHeight;

        LastSampleRect = new Rectangle(0, 0, LastWidth, LastHeight);

        if (MainImage != null)
            MainImage.Dispose();
        MainImage = new Bitmap(ScreenWidth, ScreenHeight);
    }*/

  public void CreateSendSocket() throws IOException, InterruptedException {
      mainCache.CloseReceiveThread();

        if (send != null)
        {
            if (send.datagramSocket != null)
            {
                send.datagramSocket.close();
                send.datagramSocket = null;
            }
        }
        send = new sendMessage1(server_ip, server_port, local_port-1);
    }

   public void CloseSendSocket()
    {
        if (send != null)
        {
            if (send.datagramSocket != null)
            {
                try {
                    send.datagramSocket.close();
                    send.datagramSocket = null;
                }
                catch (Exception ex)
                {

                }

            }
        }
    }

}
