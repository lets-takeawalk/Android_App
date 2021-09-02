package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.exit;

public class ImageChoiceActivity extends AppCompatActivity {
    ArrayList array = new ArrayList();
    BitmapFactory.Options options = new BitmapFactory.Options();
    ImageView imageView2;
    int touchCnt = 0;
    int arrayCnt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_choice);

//        Uri uriFromImageFile = Uri.fromFile((File) imgList.get(0));
//        intent2.setDataAndType(uriFromImageFile, "image/*");
        Intent intent = getIntent();
        imageView2 = findViewById(R.id.imageView2);
        array = intent.getExtras().getParcelableArrayList("imglist");
        Touchimg();
    }

    private void Touchimg() {
        if (array.size() == arrayCnt){
            System.exit(0);
        }
        // array 가져오기
        File img = (File) array.get(arrayCnt);
        //imageview에 저장하는과정..
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
        Bitmap resize = Bitmap.createScaledBitmap(myBitmap,416,416,true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(resize,0,0,resize.getWidth(),resize.getHeight(),matrix,true); // 416,416 and rotate90
//        System.out.println(resize.getWidth()+" " +resize.getHeight());
//        saveBitmapToJpg(resize,"imghyn");
        imageView2.setImageBitmap(rotatedBitmap);
        imageView2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        System.out.println(event.getX() +" "+ event.getY());
                        touchCnt ++;
                        if(touchCnt==2){
                            arrayCnt++;
                            touchCnt = 0;
                            Touchimg();
                        }
                }

                return true;
            }
        });
        //저장끝
        //바깥으로 내보내기 이미지
//        StorageReference storageRef = s
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
//        byte[] data = baos.toByteArray();
//
//        UploadTask uploadTask = mountainsRef.putBytes(data);

    }


}