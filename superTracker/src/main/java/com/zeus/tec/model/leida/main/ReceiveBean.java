package com.zeus.tec.model.leida.main;

import com.hoho.android.usbserial.util.MonotonicClock;
import com.zeus.tec.ui.tracker.util.TimeUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReceiveBean {
    public volatile Boolean Status;
    public byte Sign;
    public String FileName;
    public int TotalLength;
    public int CurrentLength;
    public long StartTime;
    public int OutTime;
    public byte[] Data;
   public ReentrantLock lock  = new ReentrantLock();
    Condition condition;

    private Object LockObj = new Object();

    public ReceiveBean(byte Sign, int OutTime, String FileName)
    {
        this.Sign = Sign;
        this.OutTime = OutTime;
        Status = false;
        TotalLength = 0;
        CurrentLength = 0;
        this.FileName = FileName;
        Data = null;
        StartTime = System.currentTimeMillis();
    }

    public Boolean Get()
    {
         condition = lock.newCondition();
         lock.lock();
        if (!Status)
        {
            try
            {
                if(!Status)
                {
                    condition.await(1000,TimeUnit.MILLISECONDS);
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                Status = false;
            }
            finally
            {
                lock.unlock();
            }
        }
        return Status;
    }

    public void Set(byte[] data)
    {
        lock.lock();
        try
        {
            this.Data = data;
            this.Status = true;
            condition.signalAll();
        }
        catch (Exception ex)
        {
        }
        finally
        {
            lock.unlock();
        }
    }

}
