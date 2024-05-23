# ble低功耗蓝牙开发

## 2023-12-13

#### 1.AndroidManifest.xml中添加蓝牙权限和模糊地址权限

```xml
<!-- 应用使用蓝牙的权限 -->
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <!--启动设备发现或操作蓝牙设置的权限-->
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>

    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- true 表示手机必须支持BLE，否则无法安装！
	这里设为false, 运行后在Activity中检查-->
    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="false" />
```

#### 2.检查是否支持BLE蓝牙，如果支持打开蓝牙

```java
//检查是否支持蓝牙
if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Util.toast(this, "本机不支持低功耗蓝牙！");
            Toast.makeText(this, "本机不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);//询问打开蓝牙
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)//检查蓝牙是否已经打开
            {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                Toast.makeText(this, "请打开蓝牙连接", Toast.LENGTH_LONG).show();
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
           //
        }
```

#### 3.从Android6.0开始需要动态申请位置权限，所以有了如下一段代码：

```java
private void requestPermission() {
        //动态申请是否有必要看sdk版本哈
        if (Build.VERSION.SDK_INT < 23){return;}
        //判断是否有权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
 
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

```

#### 4.先获取BluetoothAdapter对象，然后调用BluetoothAdapter::startLeScan(LeScanCallback callback)发起设备扫描。

##### 扫码到的设备都会在onLeScan()中拿到。

```java
 //蓝牙编程千古不变：第一行代码
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "mBluetoothAdapter = " + mBluetoothAdapter);
        //设备回调，扫描到的设备都在onLeScan()中拿到
        private static LeScanCallback mLeScanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if(device == null || device.getName() == null || device.getAddress() == null)             {
                    return;
                }
                Log.d(TAG, "onLeScan(), device = " + device.getName() +  "mac = " + device.getAddress() + "rssi = " + rssi);
                for (int i = 0; i < scanRecord.length; i++) {
                    Log.d(TAG, "onLeScan(), device = " + device.getName() + "content = " + scanRecord[i]);
                }
            }
        };
        //调用BluetoothAdapter.startLeScan(LeScanCallback callback)发起设备扫描
        private void startBleScan1() {
            boolean ret = mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.d(TAG, "startBtScan(),ret = " + ret);
        }

```

## 2024-03-22

动态获取uuid码

[android5.0 BLE 蓝牙4.0+浅析demo搜索（一） - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/23341414)
