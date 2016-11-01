package hn.mycapsuletest;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {
    public final int RESULT_LOAD_IMAGE = 10;

    private Button uploadButton;
    private ImageView imageToLoad;
    private Uri linkToMediaURI;
    private String linkToMediaString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_main);

        // set up wings
        uploadButton = (Button) findViewById(R.id.upload_button);
        imageToLoad = (ImageButton) findViewById(R.id.image_load);

        imageToLoad.setImageResource(R.drawable.image_icon);

        imageToLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // choose media to upload
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/* video/*");
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkToMediaString == null){
                    Toast.makeText(MainActivity.this, "Please select a photo or a video", Toast.LENGTH_SHORT).show();
                } else {
                    String type;
                    if (linkToMediaURI.toString().contains("video")){
                        type = "video/*";
                    } else {
                        type = "image/*";
                    }
                    Log.d("debug", linkToMediaURI.toString() + ":type:" + type);
                    new SharingController.MediaSharing(MainActivity.this, linkToMediaURI, type);

                    imageToLoad.setImageResource(R.drawable.image_icon);
                    Toast.makeText(MainActivity.this, "Your media is being uploaded!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Utils.verifyStoragePermissions(MainActivity.this);
            linkToMediaURI = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(linkToMediaURI, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            linkToMediaString = cursor.getString(columnIndex);
            cursor.close();

            imageToLoad.setImageBitmap(BitmapFactory.decodeFile(linkToMediaString));
        }
    }
}
