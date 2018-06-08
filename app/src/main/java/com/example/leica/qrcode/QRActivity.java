package com.example.leica.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

// http://dev.classmethod.jp/smartphone/android/android-tips-42-zxing-lib/
// http://sakura-bird1.hatenablog.com/entry/20130930/1380550999

public class QRActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        findViewById(R.id.qr_genBtn).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // EditText から文字を取得
            EditText editText = (EditText) findViewById(R.id.qr_edit);
            String contents = editText.getText().toString();
            // 非同期でエンコードする
            Bundle bundle = new Bundle();
            bundle.putString("contents", contents);
            getSupportLoaderManager().initLoader(0, bundle, callbacks);
        }
    };

    private LoaderManager.LoaderCallbacks<Bitmap> callbacks = new LoaderManager.LoaderCallbacks<Bitmap>() {
        @Override
        public Loader<Bitmap> onCreateLoader(int id, Bundle bundle) {
            EncodeTaskLoader loader = new EncodeTaskLoader(
                    getApplicationContext(), bundle.getString("contents"));
            loader.forceLoad();
            return loader;
        }
        @Override
        public void onLoaderReset(Loader<Bitmap> loader) {
        }
        @Override
        public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
            getSupportLoaderManager().destroyLoader(0);

            if (bitmap == null) {                // エンコード失敗
                Toast.makeText(getApplicationContext(), "Error.", Toast.LENGTH_SHORT).show();

            } else {                // エンコード成功
                ImageView imageView = (ImageView) findViewById(R.id.qr_result);
                imageView.setImageBitmap(bitmap);
            }
        }
    };

    public static class EncodeTaskLoader extends AsyncTaskLoader<Bitmap> {
        private String mContents;

        public EncodeTaskLoader(Context context, String contents) {
            super(context);
            mContents = contents;
        }

        @Override
        public Bitmap loadInBackground() {
            try {                // エンコード結果を返す
                return encode(mContents);

            } catch (Exception e) {                // 何らかのエラーが発生したとき
                return null;
            }
        }

        private Bitmap encode(String contents) throws Exception {
            QRCodeWriter writer = new QRCodeWriter();

            // エンコード
            BitMatrix bm = null;
            bm = writer.encode(mContents, BarcodeFormat.QR_CODE, 100, 100);

            // ピクセルを作る
            int width = bm.getWidth();
            int height = bm.getHeight();
            int[] pixels = new int[width * height];

            // データがあるところだけ黒にする
            for (int y = 0; y < height; y++) {
                int offset = y * width;

                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bm.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        }
    }


}
