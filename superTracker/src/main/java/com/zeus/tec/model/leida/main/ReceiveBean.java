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
      //  TimeSpan Currenttime = DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, 0);
       // StartTime = Convert.ToInt64(Currenttime.TotalMilliseconds);
    }

    public Boolean Get()
    {
         condition = lock.newCondition();
         lock.lock();
        if (!Status)
        {
          //  Monitor.Enter(LockObj);
            try
            {
                if(!Status)
                {
                    condition.await(6000,TimeUnit.MILLISECONDS);
                    //lock.tryLock(3000,TimeUnit.MILLISECONDS);
                   // lock.wait(3000);
                //    Monitor.Wait(LockObj, OutTime);
                   // Thread.sleep(3000);
                }
                    /*
                    while (!Status)
                    {
                        Monitor.Wait(LockObj, OutTime);
                        TimeSpan Currenttime = DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, 0);
                        long EndTime = Convert.ToInt64(Currenttime.TotalMilliseconds);
                        if (Status || (EndTime - StartTime) >= OutTime)
                        {
                            break;
                        }
                    }
                    */
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                Status = false;
            }
            finally
            {
                //Monitor.Exit(LockObj);
               // int a =10;
                lock.unlock();
            }
        }
        return Status;
    }

    public void Set(byte[] data)
    {
        //Monitor.Enter(LockObj);
        lock.lock();
        try
        {
            this.Data = data;
            this.Status = true;
           //Thread.currentThread().start();
            condition.signalAll();
           // Monitor.Pulse(LockObj);
        }
        catch (Exception ex)
        {
        }
        finally
        {
          //  Monitor.Exit(LockObj);
            lock.unlock();
        }

    }

}
