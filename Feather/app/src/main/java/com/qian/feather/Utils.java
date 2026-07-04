package com.qian.feather;

import android.app.*;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.*;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.qian.feather.item.Msg;
import com.qian.feather.item.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    //public static int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    //public static int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static final int cpuCores = Runtime.getRuntime().availableProcessors();
    public static int screenWidth = 1080;
    public static int screenHeight = 2400;
    public static final String baseStoragePath = File.separator+"storage"+File.separator+"emulated"+File.separator+"0";
    public static final String baseDCIMPath = baseStoragePath+File.separator+"DCIM";
    public static final String basePicturesPath = baseStoragePath+File.separator+"Pictures";
    public static final String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String XiaomiGalleryOwner = basePicturesPath + File.separator+"Gallery"+File.separator+"owner";
    public static final List<String> invalidInputList = new ArrayList<>();
    public static final Map<String,String> folderTranslationMap = new HashMap<>();
    public static final List<User> defaultUserList = new ArrayList<>();
    static {
        User.registerUser("13933867100","JiangHuChuanKeNan","江户川柯南");
        User.registerUser("17274629442","MaoLiLan","毛利兰");
        User.registerUser("15213801777","MaoLiXiaoWuLang","毛利小五郎");
        User.registerUser("13226226928","GongYeZhiBao","宫野志保");
        User.registerUser("13564039590","AnShiTou","安室透");
        User.registerUser("19588164635","ZhuDi","朱蒂老师");
        User.registerUser("13545739234","ChiJinXiuYi","赤井秀一");
        User.registerUser("18262476857","LingMuYuanZi","铃木园子");

        invalidInputList.add("SELECT");
        invalidInputList.add("INSERT");
        invalidInputList.add("UPDATE");
        invalidInputList.add("DELETE");
        invalidInputList.add("CREATE");
        invalidInputList.add("DROP");
        invalidInputList.add("*");
        invalidInputList.add("System.exit(0)");
        invalidInputList.add("return");

        folderTranslationMap.put(baseStoragePath,"所有照片");
        folderTranslationMap.put("Camera","相机");
        folderTranslationMap.put("Screenshots","截屏");
        folderTranslationMap.put("ScreenRecorder","录屏");
        folderTranslationMap.put("Creative","创作");
        folderTranslationMap.put("VideoEditor","视频编辑");
        folderTranslationMap.put("WeChat","微信");
        folderTranslationMap.put("WeiXin","微信");
        folderTranslationMap.put("bili","Bilibili");
        folderTranslationMap.put("Browser","花瓣浏览器");
    }
    public static final String phoneBrandOrManufacturer = Build.BRAND + Build.MANUFACTURER;
    // 方法1：非递归
    public static List<String> listFiles(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.toAbsolutePath().toString())
                    .collect(Collectors.toList());
        }
    }
    // 方法2：递归 + 并行（最快）
    public static List<String> listAllFiles(String dir) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir))) {
            return stream
                    .parallel()
                    .filter(Files::isRegularFile)
                    .map(p -> p.toAbsolutePath().toString())
                    .collect(Collectors.toList());
        }
    }
    public static List<String> getImagePathInFolder(String folderPath) {
        List<String> list = null;
        try {
            if(getFileCountInFolder(folderPath) > 100) {
                list = listAllFiles(folderPath);
            } else {
                list = listFiles(folderPath);
            }
        } catch (IOException e) {
            list = new ArrayList<>();
        }
        return list;
    }
    public static Map<String,String> getSpinnerMap() {
        final int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;  // 通常CPU核心数的2倍
        final int maxPoolSize = corePoolSize * 2;
        ExecutorService executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        if(phoneBrandOrManufacturer.contains("google")) {
            return getAllSpinnerMapsForGoolgle();
        } else if(phoneBrandOrManufacturer.contains("HONOR")) {
            return getAllSpinnerMapsForHonor();
        } else if(phoneBrandOrManufacturer.contains("Xiaomi")) {
            return getAllSpinnerMapsForXiaomi(executor);
        }
        return new HashMap<>();
    }
    private static Map<String, String> getAllSpinnerMapsForGoolgle() {
        CompletableFuture<Map<String, String>> picturesMap = CompletableFuture.supplyAsync(() ->
                getSpinnerMapOfFolder(basePicturesPath)
        );
        return picturesMap.join();
    }
    private static Map<String, String> getAllSpinnerMapsForHonor() {
        CompletableFuture<Map<String, String>> picturesMap = CompletableFuture.supplyAsync(() ->
                getSpinnerMapOfFolder(basePicturesPath)
        );

        CompletableFuture<Map<String, String>> DCIMMap = CompletableFuture.supplyAsync(() ->
                getSpinnerMapOfFolder(baseDCIMPath)
        );

        CompletableFuture<Map<String, String>> combinedFuture = picturesMap.thenCombine(DCIMMap, (resultA,resultB) -> {
            resultB.putAll(resultA);
            return resultB;
        });
        return combinedFuture.join();
    }
    private static Map<String, String> getAllSpinnerMapsForXiaomi(ExecutorService executor) {
        CompletableFuture<Map<String, String>> picturesMap = CompletableFuture.supplyAsync(() ->
            getSpinnerMapOfFolder(basePicturesPath),executor
        );

        CompletableFuture<Map<String, String>> DCIMMap = CompletableFuture.supplyAsync(() ->
            getSpinnerMapOfFolder(baseDCIMPath),executor
        );

        CompletableFuture<Map<String,String>> galleryOwnerMap = CompletableFuture.supplyAsync(() ->
            getSpinnerMapOfFolder(XiaomiGalleryOwner),executor
        );

        CompletableFuture<Map<String, String>> combinedFuture = picturesMap.thenCombineAsync(DCIMMap, (resultA,resultB) -> {
            resultB.putAll(resultA);
            return resultB;
        },executor).thenCombineAsync(galleryOwnerMap,(resultC,resultD)-> {
            resultC.putAll(resultD);
            return resultC;
        },executor);
        return combinedFuture.join();
    }

    //The folder "DCIM" and "Pictures" is usually seen in every Android Phone
    private static Map<String,String> getSpinnerMapOfFolder(String folderPath) {
        Map<String,String> map = new HashMap<>();
        Path folder = Paths.get(folderPath);
        try {
            map = Files.list(folder).parallel().filter(Files::isDirectory).filter(s -> !s.startsWith(".")).
                    collect(Collectors.toMap(p -> {
                                try {
                                    int count = (int)Files.list(p).filter(Files::isRegularFile).filter(s -> !s.startsWith(".")).count();
                                    StringBuilder builder = new StringBuilder(p.toFile().getName());
                                    builder.append("(");
                                    builder.append(count);
                                    builder.append(")");
                                    return builder.toString();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            p -> p.toFile().getAbsolutePath()));
        } catch (IOException e) {}
        /*
        File Folder = new File(folderPath);
        if(Folder.exists() && Folder.isDirectory()) {
            File[] files = Folder.listFiles();
            for(File file : files) {
                String subFolderName = file.getName();
                String absolutePath = file.getAbsolutePath();
                //DCIM or Pictures or Pictures/Gallery/owner目录下的所有子目录
                if(file.isDirectory() && !subFolderName.startsWith(".")) {
                    int fileCountInSubFolder = getFileCountInFolder(absolutePath);
                    if(fileCountInSubFolder > 0) {
                        if(folderTranslationMap.containsKey(subFolderName)) {
                            map.put(folderTranslationMap.get(subFolderName)+String.format("(%d)",fileCountInSubFolder),absolutePath);
                        } else {
                            map.put(file.getName()+String.format("(%d)",fileCountInSubFolder),absolutePath);
                        }
                    }
                }
            }
        }
         */
        return map;
    }
    private static int getFileCountInFolder(String folderPath) {
        int count = 0;
        try {
            Path directory = Paths.get(folderPath);
            count = (int) Files.list(directory).filter(Files::isRegularFile).filter(s -> !s.startsWith(".")).count();
        } catch (IOException e) {}
        return count;
    }
    public static String getFileType(String fileExt) {
        if(fileExt == "jpg" || fileExt == "png" || fileExt == "gif" || fileExt == "bmp" || fileExt == "jpeg") {
            return "IMAGE";
        } else if(fileExt == "mp3" || fileExt == "aac" || fileExt == "wav") {
            return "AUDIO";
        } else if(fileExt == "mp4" || fileExt == "avi" || fileExt == "wav" || fileExt == "ts") {
            return "VIDEO";
        }
        return "UNKNOWN";
    }
    public static void showAlertDialog(Context context, String title,String msg) {
        AlertDialog.Builder alert_dialog= new AlertDialog.Builder(context);
        alert_dialog.setTitle(title);
        alert_dialog.setMessage(msg);
        alert_dialog.setCancelable(true);
        alert_dialog.show();
    }
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }
    public static void sendMessageToHandler(Handler handler, String obj) {
        Message message = new Message();
        message.obj = obj;
        handler.sendMessage(message);
    }
    public static void sendMessageToHandler(Handler handler, String obj, int what) {
        Message message = new Message();
        message.obj = obj;
        message.what = what;
        handler.sendMessage(message);
    }
    public static NotificationChannel createNotificationChannel(String channelId,String channelName) {
        return new NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_DEFAULT);
    }
    public static Notification createNotificationBuilder(Context context, String channelId, Msg msg, PendingIntent pendingIntent) {
        Notification builder = new NotificationCompat.Builder(context,channelId)
                .setContentTitle(msg.getFrom().trim())
                .setContentText(msg.getContent())
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.feather)
                .build();
        return builder;
    }
    public static int getCurrentVisibleFragment(List<Fragment> fragmentList) {
        for(Fragment fragment : fragmentList) {
            if(fragment.isVisible()) {
                return fragment.getId();
            }
        }
        return 0;
    }
    public static int getPhotoCount(Context context) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media._ID };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }
    public static void toUnicodeString() {
        String chineseText = "你好";
        for (int i = 0; i < chineseText.length(); i++) {
            int codePoint = chineseText.codePointAt(i);
            System.out.println("Unicode of " + chineseText.charAt(i) + " is: U+" + Integer.toHexString(codePoint).toUpperCase());
        }
    }
/*
    public static void showMyToast(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);
        TextView text = layout.findViewById(R.id.text);
        text.setText("这是一个非常长的消息，我们可以使用自定义的Toast布局来更好地展示它。");
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
 */
    public static void showMySnackBar(View view) {
        Snackbar.make(view, "这是一个非常长的消息，我们使用Snackbar来展示它。", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public static boolean isInfoValid(String account,String password) {
        for(String e : invalidInputList) {
            if(e.equalsIgnoreCase(account) || e.equalsIgnoreCase(password)) {
                return false;
            }
        }
        return true;
    }
}
/*
        //自己发送
        if(msg.getType1() == Msg.TYPE_SENT) {
            holder.layout_oppo_text.setVisibility(View.GONE);
            holder.layout_oppo_image.setVisibility(View.GONE);
            if(msg.getType2() == Msg.TYPE_TEXT) {
                holder.layout_self_image.setVisibility(View.GONE);
                holder.textview_self.setText(msgList.get(position).getContent());
            } else if(msg.getType2() == Msg.TYPE_IMAGE) {
                holder.layout_self_text.setVisibility(View.GONE);
                holder.imageview_self.setImageBitmap(BitmapFactory.decodeFile(msg.getContent()));
            }
            //对方发送，自己收到
        } else if(msg.getType1() == Msg.TYPE_RECEIVED) {
            holder.layout_self_text.setVisibility(View.GONE);
            holder.layout_self_image.setVisibility(View.GONE);
            if(msg.getType2() == Msg.TYPE_TEXT) {
                holder.layout_oppo_image.setVisibility(View.GONE);
                holder.textview_oppo.setText(msgList.get(position).getContent());
            } else if(msg.getType2() == Msg.TYPE_IMAGE) {
                holder.layout_oppo_text.setVisibility(View.GONE);
                holder.imageview_oppo.setImageBitmap(BitmapFactory.decodeFile(msg.getContent()));
            }
        }
 */
/*
                    AlertDialog.Builder alert_dialog= new AlertDialog.Builder(context);
                    alert_dialog.setMessage("加载中");
                    alert_dialog.setCancelable(true);
                    alert_dialog.show();

 */
/*
        BundleData bundleData = (BundleData) getArguments().getSerializable("imagePathsData");
        imagePathList = bundleData.getList();
 */