package org.tensorflow.lite.examples.detection;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.env.Utils;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.YoloV4Classifier;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String urlStr;
    String password;
    String urlPst = "http://ec2-13-124-11-51.ap-northeast-2.compute.amazonaws.com:3000/client/checkAuthorization";
    Handler handler = new Handler();
    int flag = 0;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] files = fileList();
        // 데이터 없으면 받아오기 
        if(Arrays.asList(files).indexOf("yolov4-tiny-416.tflite")<0 || Arrays.asList(files).indexOf("label.txt")<0 || Arrays.asList(files).indexOf("coco.txt")<0 || Arrays.asList(files).indexOf("version.txt")<0){
            AlertDialog.Builder oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            String strHtml = "<b><font color='#ff0000'>설치중에 앱을 종료하지 말아주세요.</font>";
            Spanned oHtml;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                oHtml = Html.fromHtml(strHtml);
            }else{
                oHtml = Html.fromHtml(strHtml, Html.FROM_HTML_MODE_LEGACY);
            }
            oDialog.setTitle("파일이 없습니다.").setMessage(oHtml).setPositiveButton("OK",null).setCancelable(false).show();
            FirebaseStorage storage = FirebaseStorage.getInstance("gs://takewalk-9d36a.appspot.com/");
            StorageReference storageReference = storage.getReference("weight/yolov4-tiny-416.tflite");
            StorageReference labelReference = storage.getReference("labels/label.txt");
            StorageReference cocoReference = storage.getReference("labels/coco.txt");
//            StorageReference versionReference = storage.getReference("version.txt");
            final long MB = 40000000;

            storageReference.getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    try {
                        System.out.println("생성");
                        FileOutputStream fos = openFileOutput("yolov4-tiny-416.tflite",MODE_PRIVATE);
                        System.out.println("쓰기 시작");
                        fos.write(bytes);
                        System.out.println("종료");
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    System.out.println("실패");
                }
            });
            labelReference.getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    try {
                        System.out.println("생성");
                        FileOutputStream fos = openFileOutput("label.txt",MODE_PRIVATE);
                        System.out.println("쓰기 시작");
                        fos.write(bytes);
                        System.out.println("종료");
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    System.out.println("실패");
                }
            });
            cocoReference.getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    try {
                        System.out.println("생성");
                        FileOutputStream fos = openFileOutput("coco.txt",MODE_PRIVATE);
                        System.out.println("쓰기 시작");
                        fos.write(bytes);
                        System.out.println("종료");
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    System.out.println("실패");
                }
            });
            try {
                FileOutputStream fos = openFileOutput("version.txt",MODE_PRIVATE);
                String ver = "firstnum";
                fos.write(ver.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            versionReference.getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                @Override
//                public void onSuccess(byte[] bytes) {
//                    // Data for "images/island.jpg" is returns, use this as needed
//                    try {
//                        System.out.println("생성");
//                        FileOutputStream fos = openFileOutput("version.txt",MODE_PRIVATE);
//                        System.out.println("쓰기 시작");
//                        fos.write(bytes);
//                        System.out.println("종료");
//                        fos.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Handle any errors
//                    System.out.println("실패");
//                }
//            });

        }
        else{

        }
        //데이터 다 받아옴
        for(int i=0; i<files.length; i++){
            System.out.println(files[i]);
        }

        try{
            urlStr = "http://ec2-13-124-11-51.ap-northeast-2.compute.amazonaws.com:3000/client/getInfo";
            RequestThread thread = new RequestThread();
            thread.start();
        }catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        cameraButton = findViewById(R.id.cameraButton);
        detectButton = findViewById(R.id.detectButton);
        imageView = findViewById(R.id.imageView);

        cameraButton.setOnClickListener(v -> {
            final int[] nSelectItem = new int[1];
            final CharSequence[] oItems = {"한글 표시", "영어 표시"};
            AlertDialog.Builder oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            oDialog.setTitle("언어를 선택하세요").setItems(oItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println(oItems[which]+"온클릭");
                    if(oItems[which] == "한글 표시"){
                        TF_OD_API_LABELS_FILE = "label.txt";
                }else
                    TF_OD_API_LABELS_FILE = "coco.txt";
                    Intent intent2 = new Intent(MainActivity.this,DetectorActivity.class);
                    startActivity(intent2);
            }
            }).setCancelable(false).show();

        });

        detectButton.setOnClickListener(v -> {
            String pass="";
            showLoginDialog();




        });
        this.sourceBitmap = Utils.getBitmapFromAsset(MainActivity.this, "kite.jpg");

        this.cropBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);

        this.imageView.setImageBitmap(cropBitmap);

    }

    private static final Logger LOGGER = new Logger();

    public static final int TF_OD_API_INPUT_SIZE = 416;

    private static final boolean TF_OD_API_IS_QUANTIZED = false;

    private static final String TF_OD_API_MODEL_FILE = "yolov4-tiny-416.tflite";

    public static String TF_OD_API_LABELS_FILE = "label.txt";

    // Minimum detection confidence to track a detection.
    private static final boolean MAINTAIN_ASPECT = false;
    private Integer sensorOrientation = 90;

    private Classifier detector;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;

    protected int previewWidth = 0;
    protected int previewHeight = 0;

    private Bitmap sourceBitmap;
    private Bitmap cropBitmap;

    private Button cameraButton, detectButton;
    private ImageView imageView;

    private void initBox() {
        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        tracker = new MultiBoxTracker(this);
        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> tracker.draw(canvas));

        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, sensorOrientation);

        try {

            FileInputStream fos = openFileInput(TF_OD_API_LABELS_FILE);
            FileInputStream tf = openFileInput(TF_OD_API_MODEL_FILE);
            detector =
                    YoloV4Classifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            fos,
                            tf,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                canvas.drawRect(location, paint);
//                cropToFrameTransform.mapRect(location);
//
//                result.setLocation(location);
//                mappedRecognitions.add(result);
            }
        }
//        tracker.trackResults(mappedRecognitions, new Random().nextInt());
//        trackingOverlay.postInvalidate();
        imageView.setImageBitmap(bitmap);
    }
    private class RequestThread extends Thread{
        public void run() {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn != null){
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
//                    conn.setDoOutput(true);
                    int resCode = conn.getResponseCode();
                    if(resCode == HttpURLConnection.HTTP_OK){
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String data = null;
                        while(true){
                            data = reader.readLine();
                            if (data!=null && data.contains("modelverinfo")){
                                try {

                                    System.out.print(data+"\n" + "운동");
                                    System.out.println("들어오긴함?");
                                    JSONObject jsonObject = new JSONObject(data);
                                    String version_num = jsonObject.getString("modelverinfo");
                                    FileInputStream ver = openFileInput("version.txt");
                                    BufferedReader br = new BufferedReader(new InputStreamReader(ver));
                                    String line;
                                    line = br.readLine();
                                    System.out.println(line+ "라 버" +  version_num);
                                    if (line.compareTo(version_num) != 0){
                                        Handler mHandler = new Handler(Looper.getMainLooper());

                                        mHandler.postDelayed(new Runnable() {


                                            @Override

                                            public void run() {
                                                AlertDialog.Builder oDialog = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                                                String strHtml = "<b><font color='#ff0000'>설치중에 앱을 종료하지 말아주세요.</font>";
                                                Spanned oHtml;
                                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                                                    oHtml = Html.fromHtml(strHtml);
                                                }else{
                                                    oHtml = Html.fromHtml(strHtml, Html.FROM_HTML_MODE_LEGACY);
                                                }
                                                oDialog.setTitle("새로운 버전이 있습니다.").setMessage(oHtml).setPositiveButton("OK",null).setCancelable(false).show();


                                            }

                                        }, 0);


                                        FirebaseStorage storage = FirebaseStorage.getInstance("gs://takewalk-9d36a.appspot.com/");
                                        StorageReference storageReference = storage.getReference("weight/yolov4-tiny-416.tflite");
                                        StorageReference labelReference = storage.getReference("labels/label.txt");
                                        StorageReference cocoReference = storage.getReference("labels/coco.txt");
//                                        StorageReference versionReference = storage.getReference("version.txt");
                                        final long MB = 40000000;

                                        storageReference.getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                // Data for "images/island.jpg" is returns, use this as needed
                                                try {
                                                    System.out.println("생성_업뎃");
                                                    FileOutputStream fos = openFileOutput("yolov4-tiny-416.tflite",MODE_PRIVATE);
                                                    System.out.println("쓰기_업뎃");
                                                    fos.write(bytes);
                                                    System.out.println("종료");
                                                    fos.close();
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Handle any errors
                                                System.out.println("실패");
                                            }
                                        });
                                        labelReference.getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                // Data for "images/island.jpg" is returns, use this as needed
                                                try {
                                                    System.out.println("생성_업뎃");
                                                    FileOutputStream fos = openFileOutput("label.txt",MODE_PRIVATE);
                                                    System.out.println("쓰기 업뎃");
                                                    fos.write(bytes);
                                                    System.out.println("종료");
                                                    fos.close();
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Handle any errors
                                                System.out.println("실패");
                                            }
                                        });
                                        cocoReference.getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                // Data for "images/island.jpg" is returns, use this as needed
                                                try {
                                                    System.out.println("생성_업뎃");
                                                    FileOutputStream fos = openFileOutput("coco.txt",MODE_PRIVATE);
                                                    System.out.println("쓰기 시작");
                                                    fos.write(bytes);
                                                    System.out.println("종료");
                                                    fos.close();
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Handle any errors
                                                System.out.println("실패");
                                            }
                                        });
//                                        versionReference.getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                                            @Override
//                                            public void onSuccess(byte[] bytes) {
//                                                // Data for "images/island.jpg" is returns, use this as needed
//                                                try {
//                                                    System.out.println("생성_업뎃");
//                                                    FileOutputStream fos = openFileOutput("version.txt",MODE_PRIVATE);
//                                                    System.out.println("쓰기 시작");
//                                                    fos.write(bytes);
//                                                    System.out.println("종료");
//                                                    fos.close();
//                                                } catch (FileNotFoundException e) {
//                                                    e.printStackTrace();
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception exception) {
//                                                // Handle any errors
//                                                System.out.println("실패");
//                                            }
//                                        });
                                        try {
                                            FileOutputStream fos = openFileOutput("version.txt",MODE_PRIVATE);
                                            fos.write(version_num.getBytes());
                                            fos.close();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                } catch (JSONException | FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                            if(data == null)
                                break;
                        }
                        reader.close();
                    }else{
                    }
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void println(final String data){
        handler.post(new Runnable() {
            @Override
            public void run() {


            }
        });
    }

    private void showLoginDialog() {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout loginLayout = (LinearLayout) vi.inflate(R.layout.pass, null);
        final EditText pw = (EditText)loginLayout.findViewById(R.id.pw);

        new AlertDialog.Builder(this).setTitle("관리자 권한 실행").setView(loginLayout).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "@nPW : " + pw.getText().toString(), Toast.LENGTH_SHORT).show();
                password = pw.getText().toString();
                RequestPost thread2 = new RequestPost();
                thread2.start();

            }
        }).show();

    }
    private class RequestPost extends Thread{
        public void run() {
            try {
                URL url = new URL(urlPst);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                JSONObject obj = new JSONObject();
                if(conn != null){
                    conn.setRequestProperty("Accept", "application/json");
//                    conn.setRequestProperty("Content-type", "application/json");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//                    conn.setRequestProperty("Accept-Charset","UTF-8");
                    String json;
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("POST");
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    obj.put("key",password);
                    wr.write(obj.toString());  //<--- sending data.
                    System.out.println(obj.toString());

                    wr.flush();

                    //  Here you read any answer from server.
                    BufferedReader serverAnswer = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = serverAnswer.readLine()) != null) {
                        JSONObject jsonObject = new JSONObject(line);
                        String code = jsonObject.getString("code");
                        if (code.equals("200")){
                            Intent intent2 = new Intent(MainActivity.this,DCameraActivity.class);
                            startActivity(intent2);
                        }
                        System.out.println("LINE: " + line); //<--If any response from server
                        //use it as you need, if server send something back you will get it here.
                    }

                    wr.close();
                    serverAnswer.close();


////                    conn.setRequestProperty("content-type","application/x-www-form-urlencoded");
//                    OutputStream os = conn.getOutputStream();
//                    json = obj.toString();
//                    os.write(json.getBytes("UTF-8"));
//                    System.out.println("json"+json);
////                    System.out.println(json);
////                    StringBuffer buffer = new StringBuffer();
////                    buffer.append(json);
//
////                    PrintWriter writer = new PrintWriter(outStream);
////                    writer.write(buffer.toString());
////                    System.out.println(buffer.toString());
////                    System.out.println(buffer.toString());
////                    writer.flush();
//                    os.flush();
//                    System.out.println("flush");
//                    os.close();
//                    System.out.println("close");
////                    conn.setDoOutput(true);
//                    int resCode = conn.getResponseCode();
//                    System.out.println(resCode);
//                    if(resCode == HttpURLConnection.HTTP_OK){
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        String line = null;
//                        println("sd?");
//                        while(true){
//                            line = reader.readLine();
//                            println(line);
//                            if(line == null)
//                                break;
//                        }
//                        reader.close();
//                    }else{
//                        println("외안되?");
//                    }
//                    conn.disconnect();
                    flag = 1;
                }flag = 1;
            } catch (Exception e) {
                flag =1;
                e.printStackTrace();
            }
        }
    }
}
