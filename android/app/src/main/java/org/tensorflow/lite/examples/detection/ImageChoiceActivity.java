package org.tensorflow.lite.examples.detection;

import androidx.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
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
    ArrayList array = new ArrayList(); // 그 이미지 어레이
    ArrayList xyarray = new ArrayList<String>();
    ArrayList<String> narray = new ArrayList<String>(); // 그 이름 들어간 어레이
    BitmapFactory.Options options = new BitmapFactory.Options();
    ImageView imageView2;
    EditText keditText;
    EditText eeditText;
    int touchCnt = 0;
    int arrayCnt = 0;
    float mx = 0;
    float my = 0;
    float mmx = 0;
    float mmy = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_choice);

//        Uri uriFromImageFile = Uri.fromFile((File) imgList.get(0));
//        intent2.setDataAndType(uriFromImageFile, "image/*");
        Intent intent = getIntent();
        array = intent.getExtras().getParcelableArrayList("imglist");
        narray = intent.getExtras().getStringArrayList("nameList");

//        Touchimg();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Touchimg();
        }
    }

    private void Touchimg() {
        if (array.size() == arrayCnt){
            System.out.println(xyarray.get(0)+ eeditText.getText().toString() + keditText.getText().toString()+xyarray.size());
            System.exit(0);
        }

        imageView2 = findViewById(R.id.imageView2);
        keditText = findViewById(R.id.editText2);
        eeditText = findViewById(R.id.editText);
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

        configImgview(rotatedBitmap);

    }

    private void configImgview(Bitmap rotatedBitmap){
        float mx = imageView2.getX();
        float my = imageView2.getY();
        float mmx = imageView2.getWidth();
        float mmy = imageView2.getHeight();
        System.out.println(mx + " " + my);
        System.out.println(mmx + " " + mmy);
        imageView2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        float X = event.getX() *(416/(mmx-mx));
                        float Y = event.getY() *(416/(mmy-my));
                        String XY = Float.toString(X) + " " + Float.toString(Y);
                        xyarray.add(XY);
                        System.out.println(X +" "+ Y);
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
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://let-s-take-a-walk-76161.appspot.com");

        StorageReference storageRef = storage.getReference();
        StorageReference mountainsRef = storageRef.child(narray.get(arrayCnt));
        StorageReference mountainImagesRef = storageRef.child("images/" + narray.get(arrayCnt));
        System.out.println(narray.get(arrayCnt));
        //기본셋팅

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] data = baos.toByteArray();
//
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
    }


}