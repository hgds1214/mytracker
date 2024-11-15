# 武汉长盛多模块APP开发

## 2023-12-08

1.home界面添加开孔定向仪

2.directionfindermain界面

3.directionfinderDataview界面

## 2023-12-09

1.ble蓝牙模块学习

2.objectBox数据学习

## 2023-12-12

#### 1.项目信息用单例模式projectInfoManager获取

```java
  private DrillHoleInfo info = ProjectInfoManager.getInstance().getOrNewDrillHoleInfo(true);
```

getOrNewDrillHoleInfo获取一个新的钻孔项目信息，如有最后一次的钻孔信息则使用最后的钻孔信息赋值给drillHoleInfo，否则新建drillHoleInfo。（参数—needReset:是否需要重置，重置id,collectionDateTime,isMerged等，具体代码如下）：

```java
 public DrillHoleInfo getOrNewDrillHoleInfo(boolean needReset) {
        if (drillHoleInfo == null) {
            drillHoleInfo = TrackerDBManager.getLastDrillHoleInfo();
        }
        if (drillHoleInfo == null) {
            drillHoleInfo = new DrillHoleInfo();
        }
        if (needReset) {
            drillHoleInfo.id = 0;
            drillHoleInfo.collectionDateTime = 0;
            drillHoleInfo.isMerged = false;
            drillHoleInfo.livePhotos = "";
            drillHoleInfo.livePhotosMd5 = "";
            drillHoleInfo.zipPath = "";
            drillHoleInfo.projectRoot = "";
            drillHoleInfo.dataPath = "";
            drillHoleInfo.collectCount = 0;
            drillHoleInfo.countTimeTotal = 0;
        }
        return drillHoleInfo;
    }
```

#### 2.新建实体类（model） dirctionfinderDrillHoleInfo 同tracker的  drillHoleInfo

#### 3.新建directionProjectInfoActivity和xml界面

#### 4.新建directionDrillInfoActivity和xml界面

#### 5.新建directionfinderDataViewActivity和xml界面

------

由directionfindermain种选中开始采集→directionProjectInfoActivity，设置完相应参数，保存到对应实体（dirctionfinderDrillHoleInfo ），点击下一步→directionDrillInfoActivity：设置相应参数，点击拍照获取现场照片。参数保存到对应实体（如上），图片保存到对应路径。然后将实体类保存到数据库实现持久化:`info.id = TrackerDBManager.saveOrUpdate(info);`

数据和图片存储方法如下：

[Android工具类blankj-CSDN博客](https://blog.csdn.net/Crystal_xing/article/details/82798241)

```java
 
ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<dirctionfinderDrillHoleInfo>() {
            @Override
            public dirctionfinderDrillHoleInfo doInBackground() throws Throwable {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
                    String rootPath = PathUtils.getExternalAppFilesPath()+ File.separator + "privateData";
                    if (!FileUtils.createOrExistsDir(rootPath)) {
                        LogUtils.e("创建文件失败：" + rootPath);
                        ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                        return null;
                    }
                    String projectRoot = rootPath + File.separator + sdf.format(new Date(System.currentTimeMillis()));
                    if (!FileUtils.createOrExistsDir(rootPath)) {
                        LogUtils.e("创建文件失败：" + projectRoot);
                        ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                        return null;
                    }
                    String filePath = projectRoot + File.separator + "scene.png";
                    if (!ImageUtils.save(shotPicture, filePath, Bitmap.CompressFormat.PNG)) {
                        LogUtils.e("保存图片失败：" + filePath);
                        ToastUtils.showLong("保存图片失败，请重试!");
                        return null;
                    }
                    info.holeX = x;
                    info.holeY = y;
                    info.holeZ = z;
                    info.jacketLength = jacketLength;
                    info.designDirection = designDirection;
                    info.designAngle = designAngle;
                    info.adjustMode = adjustMode;
                    info.livePhotos = filePath;
                    info.livePhotosMd5 = ConvertUtils.bytes2HexString(EncryptUtils.encryptMD5File(filePath));
                    info.projectRoot = projectRoot;

                    info.collectionDateTime = time;/*System.currentTimeMillis();*/
                    info.id = TrackerDBManager.saveOrUpdate(info);
                    return info;
                }catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showLong("保存信息失败，请重试!");
                }
                return null;
            }
            @Override
            public void onSuccess(dirctionfinderDrillHoleInfo result) {
                hideLoading();
                if (result != null) {
                    DataCollectActivity.launch(directionDrillInfoActivity.this, result.id);
                    ActivityUtils.finishActivity(directionProjectInfoActivity.class);
                    finish();
                }
            }
        });
```

## 2023-12-15

####    1.object存取及使用，object实体类构建

2. #### ble蓝牙使用，bleManager熟练
3. #### 闪退问题记录：使用null值去赋值，在binding之前使用binding，textview.setText(传的不是string而是int).
4. #### 各个界面开发，蓝牙数据通讯
5. #### intent携带参数到下一个activity :  launch 方法 或者 buildIntent方法。使用的是intent.putExtra（）函数
6. #### ble开发问题记录， 接收数据时， 接收数据函数需要在读写服务开启后.
7. `lvDevicesAdapter.addDevice(bleDevice);`修改函数：如果新增的设备address和列表第一个的address一样则不添加至列表

8. 

## 2023-12-18

添加微信，蓝牙分享模块，collect 模块增加打点记录，增加新数据表，

添加数据浏览模块，可继续采集，删除记录（同时删除本地文件），分享数据，上传模块（暂时不具备功能）

注意事项：delect文件夹时，需要使用FileUtils类去删除指定文件 ；File.delect 删除不了，因该是由于FileUtils占用了资源；

开发注意事项，导入图片时，找到本地资源文件夹，将图片拷贝到相应文件夹即可（如：mipmap底下的mdpi,图片名开头只能字母或者下划线，命名规则和属性一样）

## 2023-12-19

新建接口 ILeidaReciveListener 用于处理收到数据后回调 ，回调方法进行数据处理和波形显示

ReceiveMessage 400行 处  调用回调函数

## 2023-12-21

取消采集指令编写

chart1波形显示（一段时间的波形） ，chart2 波行显示（当前采集波形）注：都有5-4秒延迟

## 2023-12-22->2024-01-02

修改leida_info为对象实体 @Entity

增加leidaPointRecordInfo 对象实体

重写项目信息模块，将项目信息保存通过实体对象 leida_info保存至objectBox数据库

重写数据采集模块，将数据信息通过实体对象leidaPointRecordInfo保存至objectBox数据库

待修复问题：

#### 1.对中断的数据进行续测的时候打点记录重写从1开始计数

（已修复）:每次打点更新leida_info 的totalDis和pointCount ;

#### 2.续测时，进行数据下载会出现数据文件不存在的报错

（已修复）由于leida_info实体被重新赋值导致，数据下载路径为null

新问题：由数据管理模块下载数据时，可能有其他地方的数据，这些数据并没有创建文件夹；数据库也没有这些数据的相关信息。

（已修复）当是当前项目时直接下载，不是当前项目单数据库有记录，查到对应leida_Info,然后保存在相应文件夹。不是当前项目，并且数据库没有记录，将数据保存在tmpData 文件夹。

#### 3.设备电压值显示精度不够，当前点数为0的时候显示有问题

已修复：修改 quantity为float leidacollectAct.438

#### 4.需要完善数据浏览模块

已添加数据浏览模块（12-29）：LeidaDataveiewActivity  、 leidaDataListAdapater 

数据浏览模块增加 数据分享功能（将leidaData文件夹的内工程文件里的文件进行打包拷贝至leidasharedata文件夹。然后将打包好的zip文件进行分享），数据删除功能（将本地文件和数据库数据一起删除）；



#### 5.是否可以从数据浏览界面进行续测。（现阶段不可以）

#### 6.项目文件和数据文件建立时先创建一个文件夹进行保存

已修复：（将文件路径保存在leida_info，创建项目时创建以时间命名的文件夹放入leidaData文件夹，将trd和data文件保存在创建的文件夹）

#### 7.数据文件下载后后缀为.tmp而不是.dat

已修复：在Matask.235 添加

```java
else {
    File file = new File(save_file);
    File filetmp = new File(tmp_file);
    filetmp.renameTo(file);

}
```

# 2024-01-02

1.在数据浏览界面增加数据详情功能，leidadeteilActivity

(浏览对应数据的详细工程信息和对应的打点记录)

2.禁用了屏幕自动旋转（防止UI错乱）

```xml
<activity
    android:name=".ui.leida.ProjectleidainfoActivity"
    android:screenOrientation="portrait"//让屏幕保持纵向 landscape为横向
    android:exported="false" />
```

3.开始采集需要新建项目才可以进行采集

# 2024-01-04

1.雷达软件 的检测模块和测试模块退出时，将接收线程关闭。防止打开测试模块和检测模块不能打开接受socket.因为为同一个端口，会占用。

> *（已修复）（错误的修复）datacache.send的socket用的常量2223，导致的错误。已修改为变量local （2227）*

（已修复）使用2227端口收不到下位机的回复数据。send端口已经固定为2223发送。故尽管datacache.send的socket端口和Maincache.receive的socket端口一样，但不能更改端口来解决占用的问题。只可以在collectactivity和testactivity生命周期destroy时将线程关闭，receive函数关闭，socket.close。

# 2024-01-08

1.leidacollectActivity 的陀螺值修改

# 2024-01-15

1.雷达软件增加使用说明模块 leidahelpAvtivity 

2.添加翻页按钮

3.添加左右滑动界面翻页

```java
 private GestureDetector gestureDetector;
 private MyGestureDetector myGestureDetector;

 myGestureDetector=new MyGestureDetector();
        //实例化GestureDetector并将MyGestureDetector实例传入
 gestureDetector=new GestureDetector(this,myGestureDetector);

 public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

 public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            int a = 0;
            // 在这里处理滑动事件，例如根据滑动方向执行相应的操作
            // 返回值表示事件是否被消耗，如果返回true，表示消耗了事件，不再传递给其他监听器
            if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
                if (currentPage < maxpage) {
                    currentPage++;
                    refreshPage();
                } else {
                    ToastUtils.showLong("这是最后一页");
                }
                // Toast.makeText(MainActivity.this,"左滑",Toast.LENGTH_SHORT).show();
            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE) {
                if (currentPage > minpage) {
                    currentPage--;
                    refreshPage();
                } else {
                    ToastUtils.showLong("这是第一页");
                }
                // Toast.makeText(MainActivity.this,"右滑",Toast.LENGTH_SHORT).show();
            } else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE) {
             //   ToastUtils.showLong("这是最后一页");
                // Toast.makeText(MainActivity.this,"上滑",Toast.LENGTH_SHORT).show();
            } else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE) {
                //ToastUtils.showLong("这是最后一页");
                // Toast.makeText(MainActivity.this,"下滑",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

```

4. 增加说明书四个章节的页面跳转按钮

   # 20240402

   ## project

   修改拍照模块，先保存为原图bmp在读取。直接读取为缩略图。

   ## leida

   1.增加配置ip地址界面,ip地址默认为192.168.43.100

   2.通信方式变成手机开热点雷达探头主动寻找上位机设备（上位机设备热点名称设置为CS_LDK，密码设置为12345678）

   3.修改UI适配安卓平板1280X800dp

   4.修改雷达波形调试UI

   ## 无线编码器

   1.完成无线编码器的通信模块

   2.基础的编码显示，及基本的下位机配置
   
   # 20240403
   
   修改照相模块，保存为png然后缩小16倍后
   
   踩坑记录
   
   1.解决android 7以上时，调用相机保存图片报错
   
   [解决exposed beyond app through ClipData.Item.getUri() 错误-CSDN博客](https://blog.csdn.net/weixin_42105630/article/details/86305354)
   
   2.解决图片过大 用BitmapFactory导入时bitmap 为null 问题
   
   [Android之优雅地加载大图片 - 简书 (jianshu.com)](https://www.jianshu.com/p/0f56f35068e2)

# 武汉长盛多模块APP开发（雷达单模块版本）

## v1.0.1(Radan) 0523

1修复新建项目时，手机与平板的”采样点数“与”采样频率“下拉框位置错位

2修复splash页的logo图标显示过大

3更新说明书

## v2.1.1release(Radan)0524

1修复下载大于21m的数据时进度条归零bug （由于currentLength*100>int的上限导致的赋予进度条的值为null）

2增加更新模块。引入了

```java
implementation 'com.github.xuexiangjys:XUpdate:2.1.4'
implementation 'com.squareup.okio:okio:2.0.0'
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation 'com.zhy:okhttputils:2.6.2'//鸿洋okhttputils
```

### v2.2.1base(Radan)0524

1修复更新提示的安装包大小不一致

2优化更细提示UI

### v2.2.2base(Radan)0525

1修复spalsh界面的版本号显示不正确问题

2将数据管理中数据的大小从字节显示变为分不同大小用B,KB,MB显示

### 0526-28雷达数据合并与数据分析

### 0529bug修复

### 0530无线编码器

增加修改滑轮直径和配置滑轮直径

0601雷达数据合并与数据分析进度

1.无断点数据读取与合并

2.数据波列显示

3.数据FirFilter滤波功能

4.波列填充功能

5.波列图形缩放

（bug :重新进软件不可以点打点）

（bug:轨迹仪与开孔定向仪拍照后无法点击确认导致进不去采集界面）

### v2.3.0base(Radan)0606

1.数据读取与合并（包括右断点数据：断点数据使用前一个钻杆最后一个数据填充）

2.波列显示

3.带通滤波

4.单页波列显示数量选择

5.单页灰度图比例选择

6.图像显示高度选择java

7.波列填充与波形排列疏密选择

怎加两个引用

```java
  implementation 'com.github.chrisbanes:PhotoView:2.3.0' //20240531
  implementation 'com.github.arcadefire:nice-spinner:1.4.4'//20240531
```

修复了各种bug，目前主要功能已经可以使用。

还未实装的模块：

数据整理，灰度图刻度，灰度图与波形图深度刻度，软件增益，

## 分支：scale工具开发

快速绘制刻度轴

### v2.3.1base(Radan)0607

1.波列图与灰度图增加进尺刻度轴和深度刻度轴

2.深度刻度轴透明切浮动在图片上方

3.使用波速与采样频率和采样点数计算出最大深度作为参数带入深度刻度轴

4.深度刻度轴用另外的photoView 显示，两个photoView放入FrameLayout。都使用wrap_content

将FrameLayout放入ScrollView。ScrollView   Fill父容器。

这样深度刻度轴可以随图片放大，可以上下滚动，左右滚动。且解决了phototView没有靠左显示的视图，当生成的bmp宽度ScrollView时

会靠左显示。

5.更改画图时预留的ScaleHeight为动态。根据高度的缩放大小，在图像放大前先将预留的高度缩小指定倍数，放大后刚好为固定刻度高度的高度。图像放大后再将刻度轴绘画至bmp，以保持刻度轴不变。

### v2.3.4relese(Radan)0612

1.增加数据整理功能

2.自动补充为无有效数据的钻杆补充数据

钻杆列表（当有效数据个数为0时字体变为红色）

数据列表（点击可浏览对应波形）

3.增加单个数据波形浏览

4.增加合并成功与合并失败的提示

### v2.3.6relese(Radan)0627

1.修改缺省的陀螺阈值为40

2.修改默认的采样间距为50

3.修改波列显示规则

### v2.3.7(暂未上传)

1.修改开孔定向仪与轨迹仪部分机型图片保存失败bug

2.取消开孔和轨迹测试时必须拍照才可进行下一步的设定

3.修改轨迹和开孔的设计倾角和z坐标可为负数

4.修复雷达显示小于1的数时显示为“.3”的bug

### v2.3.7(暂未上传)

1.增加点击“仪器列表5次显示设备清单界面”

2.修改开孔定向仪通讯协议，新增以下协议。当使打开激光测距时会显示距离。

![image-20240911144525559](C:\Users\tsp\AppData\Roaming\Typora\typora-user-images\image-20240911144525559.png)

![image-20240911144534807](C:\Users\tsp\AppData\Roaming\Typora\typora-user-images\image-20240911144534807.png)

3.增加有无激光两种协议，无激光时距离（4B）没用有

### v2.3.7(未上传)0918

轨迹仪增加磁偏角设置

### v2.3.7（）0919

修复开孔定向仪测试界面返回按键无效bug

（希望将开孔定向寻北报错由Toast改为弹窗并且增加确认按钮）

### v3.0.0 11月12日

##### 主界面模块

1.增加可出厂配置所使用的设备模块

##### 开孔定向仪

1.增加红外测距模式

2.兼容红外测距的通信协议

3.怎加红外测距距离显示

##### 轨迹仪模块

1. 修改外包轨迹仪数据合成算法，当点不够时自动补点
2. 在项目设置页面增加磁偏角设置，TrackerCollectData fromFrame 在解析数据帧时将方位角加上磁偏角
3. 数据浏览模块增加数据删除功能

##### 锚杆数据上传模块（新增）

1.已完成 登录平台。获取返回的token用于上传数据

2.蓝牙搜所和连接功能

3.蓝牙数据传输功能

4.锚杆数据文件浏览

5.锚杆数据上传和删除

##### 钻孔瞬变模块（新增）

1.无线通信模块

2.开始采集，停止采集，设备状态，数据浏览，数据删除，数据下载通信实现

3.项目构建，时间同步，打点记录功能

4.本地数据浏览功能。-数据删除-数据观看-数据合成-分享

5.三个方向的响应电压曲线绘制。
