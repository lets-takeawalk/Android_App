package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ImageChoiceActivity extends AppCompatActivity {
    ArrayList array = new ArrayList();
    BitmapFactory.Options options = new BitmapFactory.Options();
    ImageView imageView2;
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
        File img = (File) array.get(0);

        Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
        Bitmap resize = Bitmap.createScaledBitmap(myBitmap,416,416,true);
        System.out.println(resize.getWidth()+" " +resize.getHeight());
        saveBitmapToJpg(resize,"imghyn");
        imageView2.setImageBitmap(resize);
    }
    public String saveBitmapToJpg(Bitmap bitmap , String name) {
        /**
         * 캐시 디렉토리에 비트맵을 이미지파일로 저장하는 코드입니다.
         *
         * @version target API 28 ★ API29이상은 테스트 하지않았습니다.★
         * @param Bitmap bitmap - 저장하고자 하는 이미지의 비트맵
         * @param String fileName - 저장하고자 하는 이미지의 비트맵
         *
         * File storage = 저장이 될 저장소 위치
         *
         * return = 저장된 이미지의 경로
         *
         * 비트맵에 사용될 스토리지와 이름을 지정하고 이미지파일을 생성합니다.
         * FileOutputStream으로 이미지파일에 비트맵을 추가해줍니다.
         */

        File storage = getCacheDir(); //  path = /data/user/0/YOUR_PACKAGE_NAME/cache
        String fileName = name + ".jpg";
        File imgFile = new File(storage, fileName);
        try {
            imgFile.createNewFile();
            FileOutputStream out = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out); //썸네일로 사용하므로 퀄리티를 낮게설정
            out.close();
        } catch (FileNotFoundException e) {
            Log.e("saveBitmapToJpg","FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("saveBitmapToJpg","IOException : " + e.getMessage());
        }
        Log.d("imgPath" , getCacheDir() + "/" +fileName);
        return getCacheDir() + "/" +fileName;
    }

}