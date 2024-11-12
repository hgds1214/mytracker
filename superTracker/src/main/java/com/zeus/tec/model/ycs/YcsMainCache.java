package com.zeus.tec.model.ycs;

import android.content.Context;
import android.graphics.Paint;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.model.leida.main.DeviceInfoBean;
import com.zeus.tec.model.leida.main.FileBean;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.model.leida.main.PointParamter;
import com.zeus.tec.model.leida.main.ReceiveBean;
import com.zeus.tec.model.leida.main.mainReceiveThread;
import com.zeus.tec.model.leida.main.sendMethod;
import com.zeus.tec.ui.leida.util.INIutil;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.TryParse;

import java.io.File;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class YcsMainCache {

    private static YcsMainCache _instance = new YcsMainCache();
    public ReceiveBean DeviceInfo = null;
    public ReceiveBean DeviceOper = null;
    public YcsDeviceStatus DeviceStatus = null;
    public YcsSendMethod SendMethod1 = null;
    YcsReceiveThread receive = null;
    Thread ReceiveThread;
    private int datasize = 8124;
    private int outtime = 3200;
    public int[] SampleLength = {512, 1024, 2048};
    public int[] Frenquency = {1000, 2000, 4000};

    public String creatTime;



    public int DelayNumber;
    public int amp;
    public int frenquency;
    public float timeSpace;

    public String selectFileName;
    public int newProject = 0;

    ////

    public String projectName;
    public float pointDistance;
    public float reciveAreaX = 0;
    public float reciveAreaY = 0;
    public float reciveAreaZ = 0;
    public float sendArea = 0;
    public float markSpace = 0;
    public int overLayNumber;
    public float sendEnergy ;
    public float sampleTime ;
    public int sendEnergyIndex ;
    public int sampleTimeIndex ;
    public float gyro;
    public int sampleCount;
    public float sampleIntervel;
    public float sendFrenquency;

    ///

    public Properties properties = new Properties();
    public ArrayList<YcsPoint> pointList = new ArrayList<>();

    DatagramSocket sendSocket = null;

    public String rootFilePath = PathUtils.getExternalAppFilesPath()+File.separator+"YcsData";
    public String sysFilePath =  PathUtils.getExternalAppFilesPath()+File.separator+"YcsData"+ File.separator + "sys.properties";
    public String trdFilePath = "" ;
    public String FileSavePath = "";

    public byte code = 0x00;
    public String wifi = "";
    public String password = "";
    public int local_port = 2222;
    public int server_port = 1234;
    public String server_ip = "192.168.43.30";

    public int totalPoint ;
    public int currentPoint;

    // 获取当前App的私有存储路径
    public boolean RefreshInitFile() {
        boolean ok = false;
        IOtool.creatFile(PathUtils.getExternalAppFilesPath()+ File.separator +"YcsData");
        String filePath = rootFilePath + File.separator + "sys.properties";
        if (!FileUtils.isFileExists(filePath)){
            Properties tmpProperties = new Properties();
            tmpProperties.setProperty("server_ip", "192.168.43.30");
            tmpProperties.setProperty("port", "1234");
            tmpProperties.setProperty("Local_port", "2222");
            tmpProperties.setProperty("OutTime", "1000");
            INIutil.writeproperties(tmpProperties,filePath);
        }
        if (IOtool.isFileExists(filePath)) {
            server_ip = INIutil.readINI(filePath, "server_ip", "192.168.43.30");
            String port = INIutil.readINI(filePath, "port", "1234");
            String time = INIutil.readINI(filePath, "OutTime", "1000");

            if (-1 != TryParse.tryparse(port, server_port)) {
                server_port = TryParse.tryparse(port, server_port);
            } else {
                server_port = 1234;
            }

            if (-1 != TryParse.tryparse(time, outtime)) {
                outtime = TryParse.tryparse(time, outtime);
            } else {
                outtime = 3000;
            }

            port = INIutil.readINI(filePath, "Local_port", "2222");

            if (-1 != TryParse.tryparse(port, local_port)) {
                local_port = TryParse.tryparse(port, local_port);
            } else {
                local_port = 2222;
            }
            projectName = INIutil.readINI(filePath, "lastProject", "");
            ok = true;
        } else {
            ok = false;
        }
        return ok;
    }

    public static YcsMainCache GetInstance() {
        if (_instance == null) {
            _instance = new YcsMainCache();

        }
//        if (_instance.pointList.isEmpty()) {
//            _instance.pointList.add(new PointParamter("序号", "时间", "距离"));
//        }
        return _instance;
    }

    public void CreatSendSocket() {
        try {
            if (sendSocket != null) {

            } else {
                sendSocket = new DatagramSocket(local_port);
                SendMethod1 = new YcsSendMethod(outtime, datasize, (byte) 0x00);
            }
        } catch (Exception ex) {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }
    }

    public void closeSendSocket() {
        if (sendSocket != null) {
            try {
                sendSocket.close();
                sendSocket = null;
            } catch (Exception ex) {
                ToastUtils.showLong(ex.getLocalizedMessage());
            }
        }
    }

    public void CreatReceiveThread(Context context) {
        if (ReceiveThread != null) {
        } else {
            receive = new YcsReceiveThread(datasize, outtime, local_port + 1, context);
            ReceiveThread = new Thread(() -> {
                receive.Run();
            });
            //ReceiveThread.setDaemon(true);
            ReceiveThread.start();
        }

    }

    public void CloseReceiveThread() throws InterruptedException {
        if (ReceiveThread != null) {
            if (receive != null) {
                receive.Stop();
                Thread.sleep(100);
                if(  receive.client!=null){
                    receive.client.close();
                }
                ReceiveThread.interrupt();
            } else if (ReceiveThread.isAlive()) {
                ReceiveThread.interrupt();
            }
        }

        receive = null;
        ReceiveThread = null;
    }

    public void GetDeviceStatus() {
        DeviceStatus = SendMethod1.GetdeviceStatus(sendSocket, server_ip, local_port, server_port);
    }

    public int GetStartWork() {
        int result = SendMethod1.GetStartWork(sendSocket, server_ip, local_port, server_port);
        return result;
    }

    public int GetStopWork() {
        int result = SendMethod1.GetStopWork(sendSocket, server_ip, local_port, server_port);
        return result;
    }

    public int DownLoadFile(String fileName, int CurrentLength, String tmpPath) {
        return SendMethod1.DownLoadFile(sendSocket, server_ip, local_port, server_port, fileName, CurrentLength, tmpPath);
    }

    public int GetDeleteFile(String fileName) {
        return SendMethod1.GetDeleteFile(sendSocket, server_ip, local_port, server_port, fileName);
    }

//    public int GetSetting(String FileName, int SampleCount, int StackCount, int NbOfSampleDelayPoint, int AmplifyValue, int SampleFrequency, float TimeInterval, float GYROThreshold) {
//        return SendMethod1.GetSetting(sendSocket, server_ip, local_port, server_port, FileName, SampleCount, StackCount, NbOfSampleDelayPoint, AmplifyValue, SampleFrequency, TimeInterval, GYROThreshold);
//    }

    public int GetSetting(String FileName, float RAreaX, float RAreaY, float RAreaZ, float TArea, float MSpacing, int SampleCount,float SampleIntervel, float TFreq, int StackCount, int PlusPower, int SampleTimes,float GYOThreshold)
    {
        return SendMethod1.GetSetting(sendSocket, server_ip, local_port, FileName, RAreaX, RAreaY, RAreaZ, TArea, MSpacing, SampleCount, SampleIntervel, TFreq, StackCount, PlusPower, SampleTimes, GYOThreshold);
    }

    public List<FileBean> GetFilesName() {
        return SendMethod1.GetCatalogue(sendSocket, server_ip, local_port, server_port);
    }
}
