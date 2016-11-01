package hn.mycapsuletest;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.session.MediaController;
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
import android.widget.VideoView;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

public class MainActivity extends AppCompatActivity {
    public final int RESULT_LOAD_IMAGE = 10;

    private Button uploadButton;
    private ImageButton imageToLoad;
    private ImageButton facebookUpload;
    private ImageButton twitterUpload;
    private ImageButton instagramUpload;
    private Uri linkToMediaURI;
    private String linkToMediaString;
    private VideoView videoToLoad;
    private android.widget.MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        TwitterAuthConfig authConfig = new TwitterAuthConfig("consumerKey", "consumerSecret");
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        // set up wings
        uploadButton = (Button) findViewById(R.id.upload_button);
        imageToLoad = (ImageButton) findViewById(R.id.image_load);
        facebookUpload = (ImageButton) findViewById(R.id.facebook_upload);
        twitterUpload = (ImageButton) findViewById(R.id.twitter_upload);
        instagramUpload = (ImageButton) findViewById(R.id.instagram_upload);
        videoToLoad = (VideoView) findViewById(R.id.videoView);
        videoToLoad.setVisibility(View.INVISIBLE);

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

        twitterUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkToMediaString == null){
                    Toast.makeText(MainActivity.this, "Please select a photo or a video", Toast.LENGTH_SHORT).show();
                } else {
                    String type = checkType(linkToMediaURI.toString());
                    TweetComposer.Builder builder = new TweetComposer.Builder(MainActivity.this)
                            .text("I hope this is what you're asking for!")
                            .image(linkToMediaURI);
                    builder.show();
                }
            }
        });

        instagramUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkToMediaString == null){
                    Toast.makeText(MainActivity.this, "Please select a photo or a video", Toast.LENGTH_SHORT).show();
                } else {
                    String type = checkType(linkToMediaURI.toString());
                    Intent instaUpload = new Intent("com.instagram.android");
                    instaUpload.setType(type);
                    instaUpload.putExtra(Intent.EXTRA_STREAM, linkToMediaURI);
                    startActivity(instaUpload);
                }
            }
        });

        facebookUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkToMediaString == null){
                    Toast.makeText(MainActivity.this, "Please select a photo or a video", Toast.LENGTH_SHORT).show();
                } else {
                    String type = checkType(linkToMediaURI.toString());
                    ShareDialog shareDialog = null;
                    shareDialog = new ShareDialog(MainActivity.this);

                    switch (type){
                        case "image/*":
                            Bitmap image = BitmapFactory.decodeFile(linkToMediaString);
                            SharePhoto photo = new SharePhoto.Builder()
                                    .setBitmap(image)
                                    .build();
                            SharePhotoContent contentPhoto = new SharePhotoContent.Builder()
                                    .addPhoto(photo)
                                    .build();
                            shareDialog.show(contentPhoto, ShareDialog.Mode.AUTOMATIC);
                            break;
                        case "video/*":
                            Uri videoFileUri = linkToMediaURI;
                            ShareVideo video = new ShareVideo.Builder()
                                    .setLocalUrl(videoFileUri)
                                    .build();
                            ShareVideoContent contentVideo = new ShareVideoContent.Builder()
                                    .setVideo(video)
                                    .build();
                            shareDialog.show(contentVideo, ShareDialog.Mode.AUTOMATIC);
                            break;
                    }


                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkToMediaString == null){
                    Toast.makeText(MainActivity.this, "Please select a photo or a video", Toast.LENGTH_SHORT).show();
                } else {
                    String type = checkType(linkToMediaURI.toString());
                    new SharingController.MediaSharing(MainActivity.this, linkToMediaURI, type);

                    imageToLoad.setImageResource(R.drawable.image_icon);
                }
            }
        });
    }

    private String checkType(String uriString) {
        String type;
        uriString = linkToMediaURI.toString();
        if (uriString.contains("video")) {
            type = "video/*";
        } else {
            type = "image/*";
        }
        return type;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            // clear the existing data
            videoToLoad.setVisibility(View.INVISIBLE);
            linkToMediaURI = null;
            linkToMediaString = null;
            mediaController = null;

            Utils.verifyStoragePermissions(MainActivity.this);
            linkToMediaURI = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(linkToMediaURI, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            linkToMediaString = cursor.getString(columnIndex);
            cursor.close();

            switch (checkType(linkToMediaString)){
                case "image/*":
                    imageToLoad.setImageBitmap(BitmapFactory.decodeFile(linkToMediaString));
                    break;
                case "video/*":
                    prepareVideo(linkToMediaURI);
                    break;
            }
        }
    }

    private void prepareVideo(Uri videoUri){
//        imageToLoad.setVisibility(View.INVISIBLE);
        imageToLoad.setImageResource(R.drawable.image_icon);
        if (mediaController == null) {
            mediaController = new android.widget.MediaController(MainActivity.this);
        }

        videoToLoad.setMediaController(mediaController);
        videoToLoad.setVideoURI(videoUri);
        videoToLoad.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
