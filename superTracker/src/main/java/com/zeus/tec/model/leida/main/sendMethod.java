package com.zeus.tec.model.leida.main;

import android.os.Environment;
import android.widget.Toast;

import com.zeus.tec.model.leida.leida_info;

import com.zeus.tec.ui.leida.LeidaDataCollectActivity;
import com.zeus.tec.ui.leida.util.ConvertCode;
import com.zeus.tec.ui.leida.util.IOtool;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;

public class sendMethod {

    public int OutTime = 0;
    public int DataSize = 0;
    public byte code = 0x00;
    MainCache cache = MainCache.GetInstance();
    leida_info leidaInfo = leida_info.GetInstance();


    public sendMethod(int OutTime, int DataSize, byte code) {
        this.OutTime = OutTime;
        this.DataSize = DataSize;
        this.code = code;
    }

    public DeviceInfoBean GetdeviceStatus(DatagramSocket sendSocket, String address, int Local_port, int server_prot) {
        DeviceInfoBean info = null;
        try {
            sendbean order = new sendbean(DataSize, OutTime);
            if (code == 0x00) {
                code = 0x01;
            } else {
                code = 0x00;
            }
            ReceiveBean Status_order = order.DeveiceStatusOrder(code);
            Boolean success = order.SendOrder(Status_order, sendSocket, address, Local_port, server_prot);
            if (success && cache.DeviceInfo.Data != null && cache.DeviceInfo.Data.length > 0) {
                byte[] tmp1 = new byte[2];
                byte[] tmp2 = new byte[4];
                byte[] tmp3 = new byte[4];

                tmp2[0] = cache.DeviceInfo.Data[0];
                tmp2[1] = cache.DeviceInfo.Data[1];
                tmp2[2] = cache.DeviceInfo.Data[2];
                tmp2[3] = cache.DeviceInfo.Data[3];

                tmp3[0] = cache.DeviceInfo.Data[4];
                tmp3[1] = cache.DeviceInfo.Data[5];
                tmp3[2] = cache.DeviceInfo.Data[6];
                tmp3[3] = cache.DeviceInfo.Data[7];

                tmp1[1] = cache.DeviceInfo.Data[8];
                tmp1[0] = cache.DeviceInfo.Data[9];

                info = new DeviceInfoBean();
                info.status = getint16(tmp1[0], tmp1[1]);
                info.quantity = ByteBuffer.wrap(tmp2).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                info.gyro = ByteBuffer.wrap(tmp3).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            }
            if (success) {
                int a = 0;
            }
        } catch (Exception exception) {
            info = null;
        }
        return info;
    }

    public int GetStartWork(DatagramSocket sendSocket, String address, int Local_port, int server_prot) {
        int result = -1;
        try {
            sendbean order = new sendbean(DataSize, OutTime);
            if (code == 0x00) {
                code = 0x01;
            } else {
                code = 0x00;
            }
            ReceiveBean Setting_order = order.StartWorkOrder(code);
            Boolean success = order.SendOrder(Setting_order, sendSocket, address, Local_port, server_prot);
            if (success && cache.DeviceOper.Data != null && cache.DeviceOper.Data.length > 0) {
                byte[] tmp = new byte[2];
                tmp[1] = cache.DeviceOper.Data[0];
                tmp[0] = cache.DeviceOper.Data[1];
                result = getint16(tmp[0], tmp[1]);
            }
        } catch (Exception ex) {
            result = -1;
        }
        return result;
    }

    public int GetStopWork(DatagramSocket sendSocket, String address, int Local_port, int server_prot) {
        int result = -1;
        try {
            sendbean order = new sendbean(DataSize, OutTime);
            if (code == 0x00) {
                code = 0x01;
            } else {
                code = 0x00;
            }
            ReceiveBean Setting_order = order.StopWorkOrder(code);
            Boolean success = order.SendOrder(Setting_order, sendSocket, address, Local_port, server_prot);
            if (success && cache.DeviceOper.Data != null && cache.DeviceOper.Data.length > 0) {
                byte[] tmp = new byte[2];
                tmp[1] = cache.DeviceOper.Data[0];
                tmp[0] = cache.DeviceOper.Data[1];
                result = getint16(tmp[0], tmp[1]);
            }
        } catch (Exception ex) {
            result = -1;
        }
        return result;
    }

    public int DownLoadFile(DatagramSocket sendSocket, String address, int Loacl_port, int Server_prot, String FileName, int CurrentLength,String tmpPath) {
        int TotalLength = 0;
        try {
            if (code == 0x00) {
                code = 0x01;
            } else {
                code = 0x00;
            }
            sendbean order = new sendbean(DataSize, OutTime);
            ReceiveBean Download_order = order.DownloadFileOrder(code, FileName, CurrentLength);
            Boolean success = order.SendOrder(Download_order, sendSocket, address, Loacl_port, Server_prot);
            if (success) {
                if (cache.DeviceOper.TotalLength == 0) {
                    CurrentLength = 0;
                } else if (cache.DeviceOper.Data != null && cache.DeviceOper.Data.length > 0) {
                    CurrentLength = (int) cache.DeviceOper.CurrentLength;
                    TotalLength = (int) cache.DeviceOper.TotalLength;
                    int length = cache.DeviceOper.Data.length;
                    String Datapath =tmpPath;
                    IOtool.saveText(Datapath, cache.DeviceOper.Data, true);
                    CurrentLength = CurrentLength + length;
                    if (CurrentLength >= TotalLength) {
                        CurrentLength = TotalLength;
                    }
                } else {
                    CurrentLength = -1;
                }
            }
        } catch (Exception ex) {
            CurrentLength = -1;
        }
        return CurrentLength;
    }

    /*    删除指定文件 返回 1：删除成功 0 删除失败 */
    public int GetDeleteFile(DatagramSocket socket, String address, int local_port, int server_prot, String FileName) {
        int result = -1;
        try {
            sendbean order = new sendbean(DataSize, OutTime);
            if (code == 0x00) {
                code = 0x01;
            } else {
                code = 0x00;
            }
            ReceiveBean delete_order = order.DeleteFileOrder(code, FileName);
            Boolean success = order.SendOrder(delete_order, socket, address, local_port, server_prot);
            if (success && cache.DeviceOper.Data != null && cache.DeviceOper.Data.length > 0) {
                byte[] tmp = new byte[2];
                byte[] tmp1 = new byte[32];
                for (int i = 0; i < 32; i++) {
                    tmp1[i] = cache.DeviceOper.Data[i];
                }
                tmp[0] = cache.DeviceOper.Data[32];
                tmp[1] = cache.DeviceOper.Data[33];
                result = ConvertCode.getushort(tmp, 0);
                // String DeleteName = System.Text.Encoding.Default.GetString(tmp1);
                String DeleteName = new String(tmp1);
                String Name = "";
                char[] Chartmp = DeleteName.toCharArray();
                for (int j = 0; j < DeleteName.length(); j++) {
                    char c = Chartmp[j];
                    if (c == '\0') {
                        break;
                    } else {
                        Name += c;
                    }
                }
                if (!FileName.equals(Name))
                    result = 0;
            }
        } catch (Exception ex) {
            result = -1;
        }

        return result;
    }

    public int GetSetting(DatagramSocket socket, String address, int local_port, int server_port, String FileName, int SampleCount, int StackCount, int NbOfSampleDelayPoint, int AmplifyValue, int SampleFrequency, float TimeInterval, float GYROThreshold) {
        int result = -1;
        try {
            sendbean order = new sendbean(DataSize, OutTime);

            if (code == 0x00) {
                code = 0x01;
            } else {
                code = 0x00;
            }
            ReceiveBean Setting_order = order.SettingOrder(code, FileName, SampleCount, StackCount, NbOfSampleDelayPoint, AmplifyValue, SampleFrequency, TimeInterval, GYROThreshold);
            Boolean success = order.SendOrder(Setting_order, socket, address, local_port, server_port);
            if (success && cache.DeviceOper.Data != null && cache.DeviceOper.Data.length > 0) {
                byte[] tmp = new byte[2];
                tmp[0] = cache.DeviceOper.Data[0];
                tmp[1] = cache.DeviceOper.Data[1];
                result = ConvertCode.getint16(tmp, ByteOrder.LITTLE_ENDIAN);
            }
        } catch (Exception ex) {
            result = -1;
        }
        return result;
    }

    public List<FileBean> GetCatalogue(DatagramSocket socket, String address, int local_port, int server_port) {
        Boolean ok = false;
        int CurrentLength = 0;
        int TotalLength = 0;
        byte[] result = null;
        int Size = 0;
        List<FileBean> files = null;
        try {
            sendbean order = new sendbean(DataSize, OutTime);
            Boolean complete = false;
            while (true) {
                if (code == 0x00) {
                    code = 0x01;
                } else {
                    code = 0x00;
                }
                ReceiveBean Catalogue_order = order.CreateCatalogueOrder(code, CurrentLength);
                Boolean success = order.SendOrder(Catalogue_order, socket, address, local_port, server_port);
                if (success && cache.DeviceOper.Data != null && cache.DeviceOper.Data.length > 0) {
                    int Length = cache.DeviceOper.Data.length;
                    int beginIndex = (int) cache.DeviceOper.CurrentLength;
                    if (CurrentLength == 0) {
                        Size = (int) cache.DeviceOper.TotalLength;
                        result = new byte[Size];
                    }
                    if (result != null) {
                        for (int i = 0; i < Length; i++) {
                            result[i + beginIndex] = cache.DeviceOper.Data[i];
                        }

                        CurrentLength = CurrentLength + Length;

                        if (CurrentLength >= Size) {
                            ok = true;
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception ex) {
            ok = false;
        }
        if (ok) {
            files = ExplainCatalogue(result);
        }
        return files;
    }

    public List<FileBean> ExplainCatalogue(byte[] catalogue) {
        List<FileBean> files = new ArrayList<>();
        if (catalogue != null) {
            int length = catalogue.length;
            int size = 32 + 4 + 6;
            int num = length / size;
            for (int i = 0; i < num; i++) {
                FileBean bean = null;
                try {
                    bean = new FileBean();
                    int index = i * size;
                    byte[] tmp = new byte[32];
                    System.arraycopy(catalogue, index, tmp, 0, 32);

                    String ss = new String(tmp);
                    bean.FileName = "";
                    char[] strTmp = ss.toCharArray();
                    for (int j = 0; j < ss.length(); j++) {
                        char c = strTmp[j];
                        if (c == '\0') {
                            break;
                        } else {
                            bean.FileName += c;
                        }
                    }
                    index = index + 32;
                    tmp = new byte[6];
                    System.arraycopy(catalogue, index, tmp, 0, 0);
                    System.arraycopy(catalogue, index, tmp, 0, 6);
                    bean.Time = readDate(tmp);
                    index = index + 6;
                    tmp = new byte[4];

                    System.arraycopy(catalogue, index, tmp, 0, 4);
                    bean.Size = ConvertCode.getint(tmp, 0);
                } catch (Exception ex) {
                    bean = null;
                }
                if (bean != null)
                    files.add(bean);
            }
        }
        return files;
    }

    public static String readDate(byte[] date) {
        int year = date[0] & 0xff;
        year = year + 2000;
        //2000;
        int month = date[1] & 0xff;
        int day = date[2] & 0xff;
        int hour = date[3] & 0xff;
        int minute = date[4] & 0xff;
        int second = date[5] & 0xff;
        String s_year = String.valueOf(year);
        String s_month = String.valueOf(month);
        if (month < 10) {
            s_month = "0" + s_month;
        }
        String s_day = String.valueOf(day);
        if (day < 10) {
            s_day = "0" + s_day;
        }
        String s_hour = String.valueOf(hour);
        if (hour < 10) {
            s_hour = "0" + s_hour;
        }
        String s_minute = String.valueOf(minute);
        if (minute < 10) {
            s_minute = "0" + s_minute;
        }
        String s_second = String.valueOf(second);
        if (second < 10) {
            s_second = "0" + s_second;
        }
        String time = s_year + "-" + s_month + "-" + s_day + " " + s_hour + ":" + s_minute + ":" + s_second;
        return time;
    }

    public int getint16(byte byte1, byte byte2) {
        byte[] byteArray = {byte1, byte2}; // 两字节的字节数组，byte1 为高字节，byte2 为低字节
        int result = ((byteArray[0] & 0xFF) << 8) | (byteArray[1] & 0xFF);
        return result;
    }

}
