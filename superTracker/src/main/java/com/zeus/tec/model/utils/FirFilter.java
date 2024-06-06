package com.zeus.tec.model.utils;

public class FirFilter {

    public static double[] ProcessSample(double[] sample, int dataLen, double fs, double lowFreq, double highFreq, int n)
    {
        double[] result = new double[dataLen];
        for (int i = 0; i < dataLen; i++)
        {
            result[i] = 0;
        }
        double[] Coefficients = FirWin(n, lowFreq / fs, highFreq / fs);
        for (int i = 0; i < (n + 1); i++)
        {
            for (int j = 0; j <= i; j++)
            {
                result[i] += sample[i - j] * Coefficients[j];
            }
        }
        for (int i = n + 1; i < dataLen; i++)
        {
            for (int j = 0; j < (n + 1); j++)
            {
                result[i] += sample[i - j] * Coefficients[j];
            }
        }
        return result;
    }

    public static double[] FirWin(int n, double lowFreq, double highFreq)
    {
        double[] Coefficients = new double[n + 1];
        int m ;
        int mid ;


        if ((n % 2) == 0)//n可以整除2，
        {
            m = n / 2 - 1;//n为偶数
            mid = 1;
        }
        else
        {
            m = n / 2;//n为奇数
            mid = 0;
        }

        double delay = n / 2.0;
        double wc1 = 2.0 * Math.PI * lowFreq;
        double wc2 = 2.0 * Math.PI * highFreq;
        for (int i = 0; i <= m; i++)
        {
            double s = i - delay;
            Coefficients[i] = (Math.sin(wc2 * s) - Math.sin(wc1 * s)) / (Math.PI * s);
            Coefficients[i] = Coefficients[i];
            Coefficients[n - i] = Coefficients[i];
        }
        if (mid == 1)
            Coefficients[n / 2] = (wc2 - wc1) / Math.PI;

        return Coefficients;
    }
}
