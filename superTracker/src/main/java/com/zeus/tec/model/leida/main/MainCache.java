package com.zeus.tec.model.leida.main;

import android.content.Context;
import android.graphics.Region;
import android.os.Environment;

import java.io.File;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.content.Context;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.ui.leida.util.INIutil;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.TryParse;

/*
  雷达设备通讯缓存
 */
public class MainCache {
    private static MainCache _instance = new MainCache();
    public ReceiveBean DeviceInfo = null;
    public ReceiveBean DeviceOper = null;
    public DeviceInfoBean DeviceStatus = null;
    public sendMethod SendMethod1 = null;
    mainReceiveThread receive = null;
    Thread ReceiveThread;
    public byte code = 0x00;
    public String wifi = "";
    public String password = "";
    public int local_port = 2222;
    public int server_port = 1234;
    public String server_ip = "192.168.43.100";
    // public String server_ip = "192.168.0.111";
    private int datasize = 1024;
    private int outtime = 3200;
    public int[] SampleLength = {512, 1024, 2048};
    public int[] Frenquency = {1000, 2000, 4000};
    public String projectName;
    public String creatTime;
    public int pointDistance;
    public int Samplecount;
    public int overLayNumber;
    public int DelayNumber;
    public int amp;
    public int frenquency;
    public float timeSpace;
    public float gyro;
    public String selectFileName;
    public int newProject = 0;

    public Properties properties = new Properties();
    public ArrayList<PointParamter> pointList = new ArrayList<>();


    DatagramSocket sendSocket = null;
    public String FileSavePath = "";
    public Test_setting test_setting = new Test_setting();

    public class Test_setting {
        public float TimeInterval;
        public int SampleCount;
        public int StackCount;
        public int NbOfSampleDelayPoint;
        public float Amplify;
        public int SampleFrequency;
    }

    // String privatePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();

    // 获取当前App的私有存储路径
    public boolean RefreshInitFile() {
        boolean ok = false;
        String filePath = FileSavePath + "/" + "sys.properties";
        String DataFilePath = FileSavePath + "/" + "Data";
        IOtool.creatFile(DataFilePath);
        if (IOtool.isFileExists(filePath)) {
            server_ip = INIutil.readINI(filePath, "server_ip", "192.168.43.100");
            String port = INIutil.readINI(filePath, "port", "1234");
            String time = INIutil.readINI(filePath, "OutTime", "3000");
            // server_ip = InitUtil.Read("Device", "Ip", "127.0.0.1", filePath);
            //  String  port = InitUtil.Read("Device", "Port", "1234", filePath);
            // String time= InitUtil.Read("Device", "OutTime", "3000", filePath);
            server_port = 1234;
            if (-1 != TryParse.tryparse(port, server_port)) {
                server_port = TryParse.tryparse(port, server_port);
            } else {
                server_port = 1234;
            }
            outtime = 3000;
            if (-1 != TryParse.tryparse(time, outtime)) {
                outtime = TryParse.tryparse(time, outtime);
            } else {
                outtime = 3000;
            }
            // port = InitUtil.Read("Local", "Port", "2222", filePath);
            port = INIutil.readINI(filePath, "Local_port", "2222");
            local_port = 2222;
            if (-1 != TryParse.tryparse(port, local_port)) {
                local_port = TryParse.tryparse(port, local_port);
            } else {
                local_port = 2222;
            }
            projectName = INIutil.readINI(filePath, "ProjectName", "");
            // LastProjectName = InitUtil.Read("Local", "ProjectName", "", filePath);
            ok = true;
        } else {
            ok = false;
        }
        return ok;
    }

    public static MainCache GetInstance() {
        if (_instance == null) {
            _instance = new MainCache();
        }
        if (_instance.pointList.isEmpty()) {
            _instance.pointList.add(new PointParamter("序号", "时间", "距离"));
        }
        return _instance;
    }

    public void CreatSendSocket() {
        try {
            if (sendSocket != null) {
            } else {
                sendSocket = new DatagramSocket(local_port);
                SendMethod1 = new sendMethod(outtime, datasize, (byte) 0x00);
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
            // CloseReceiveThread();
        } else {
            receive = new mainReceiveThread(datasize, outtime, local_port + 1, context);
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

    public int GetSetting(String FileName, int SampleCount, int StackCount, int NbOfSampleDelayPoint, int AmplifyValue, int SampleFrequency, float TimeInterval, float GYROThreshold) {
        return SendMethod1.GetSetting(sendSocket, server_ip, local_port, server_port, FileName, SampleCount, StackCount, NbOfSampleDelayPoint, AmplifyValue, SampleFrequency, TimeInterval, GYROThreshold);
    }

    public List<FileBean> GetFilesName() {
        return SendMethod1.GetCatalogue(sendSocket, server_ip, local_port, server_port);
    }

}
