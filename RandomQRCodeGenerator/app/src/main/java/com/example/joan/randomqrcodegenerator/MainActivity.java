package com.example.joan.randomqrcodegenerator;

import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    Button mButton;
    final int QR_CODE_WIDTH = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mButton = (Button) findViewById(R.id.button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int n = random.nextInt();
                BitMatrix bitMatrix = generateMatrix(n + "");
                Bitmap bitmap = convertBitmap(bitMatrix);
                mImageView.setImageBitmap(bitmap);
            }
        });
    }

    public BitMatrix generateMatrix(String randomNumber) {
        try {
            return new MultiFormatWriter().encode(randomNumber, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_WIDTH, null);
        } catch (WriterException e) {
            return null;
        }
    }

    public Bitmap convertBitmap(BitMatrix bitMatrix) {
        int matrixWidth = bitMatrix.getWidth();
        int matrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[matrixHeight * matrixWidth];
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                pixels[(y * matrixWidth) + x] = ContextCompat.getColor(this, bitMatrix.get(x, y) ? R.color.black : R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 500, 0, 0, matrixWidth, matrixHeight);
        return bitmap;
    }

}
