package com.jashasweejena.camnotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PIX_REQUEST_CODE = 2;
    static final int MAX_SELECTION_COUNT = 5;
    static final int SCAN_REQUEST_CODE = 99;
    Context context = MainActivity.this;
    final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
    final String orderBy = MediaStore.Images.Media._ID;
    ArrayList<String> f = new ArrayList<String>(); // list of file paths
    File[] listOfFiles; //List of files in a path
    String mCurrentPhotoPath;
    GridView imagegrid;
    int count;
    private ImageAdapter imageAdapter;
    private Button imgPickerButton;
    private String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFromSdcard();
        imgPickerButton = findViewById(R.id.imgPickerBtn);
        imagegrid = findViewById(R.id.PhoneImageGrid);
        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "GridView Cleared", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                clearGridView();
            }
        });

        imgPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePicker();
                imageAdapter.notifyDataSetChanged();
                imagegrid.setAdapter(imageAdapter);
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_generatepdf) {
            try {
                makePdf(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void getFromSdcard() {
        File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        f.clear();
        if (file != null && file.isDirectory()) {
            listOfFiles = file.listFiles();


            for (File listOfFile : listOfFiles) {

                f.add(listOfFile.getAbsolutePath());

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //When camera returns photo to calling activity
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == SCAN_REQUEST_CODE) {
                Uri uri = Objects.requireNonNull(data.getExtras()).getParcelable(ScanConstants.SCANNED_RESULT);
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    if (uri != null) {
                        getContentResolver().delete(uri, null, null);
                    }
                    String path = storeImage(bitmap);
                    f.add(path);
                    imageAdapter.notifyDataSetChanged();
                    imagegrid.setAdapter(imageAdapter);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String storeImage(Bitmap image) {
        File pictureFile = null;
        try {
            pictureFile = createImageFile();
            if (pictureFile == null) {
                Log.d(TAG,
                        "Error creating media file, check storage permissions: ");// e.getMessage());
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                image.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pictureFile != null ? pictureFile.getAbsolutePath() : null;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    void imagePicker() {

//        Pix.start(MainActivity.this,                    //Activity or Fragment Instance
//                PIX_REQUEST_CODE,                //Request code for activity results
//                MAX_SELECTION_COUNT);    //Number of images to restict selection count

        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
        startActivityForResult(intent, SCAN_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(MainActivity.this, PIX_REQUEST_CODE, MAX_SELECTION_COUNT);
                } else {
                    Toast.makeText(MainActivity.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void makePdf(ArrayList<String> listOfPaths) {
        try {
            String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "PDF_" + timeStamp + "_";

            String dest = directoryPath + "/" + imageFileName + ".pdf"; //  Change pdf's name.

            Image img = Image.getInstance(f.get(0));
            Document document = new Document(img);
            PdfWriter.getInstance(document, new FileOutputStream(dest));
            document.open();
            for (String image : listOfPaths) {
                img = Image.getInstance(image);
                document.setPageSize(img);
                document.newPage();
                img.setAbsolutePosition(0, 0);
                document.add(img);
            }
            document.close();
        } catch (Exception e) {
            Log.d("devil: ",e.getMessage());
            e.printStackTrace();
        }
    }

    void clearGridView() {

        for (String x : f) {
            Uri uri = Uri.parse(x);
            File file = new File(Objects.requireNonNull(uri.getPath()));
            if (file.exists()) {
                if (file.delete()) {
                    Log.d(TAG, "clearGridView: " + "File deleted");
                }
            }
        }

        f.clear();
        imageAdapter.notifyDataSetChanged();
        imagegrid.setAdapter(imageAdapter);
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public int getCount() {
            return f.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.thumb_item, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                holder.imageview.setLayoutParams(new GridView.LayoutParams(250, 250));
                holder.imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.imageview.setPadding(8, 8, 8, 8);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();

            }
            holder.imageview.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    Log.d(TAG, "onClick imageview: " + "imageview clicked");

                    Uri uri = Uri.parse(f.get(position));
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.d(TAG, "onClick imageview: " + "File deleted");
                        }
                    }

                    imageAdapter.notifyDataSetChanged();
                    imagegrid.setAdapter(imageAdapter);
                }
            });


            Bitmap myBitmap = BitmapFactory.decodeFile(f.get(position));
            holder.imageview.setImageBitmap(myBitmap);
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
        CheckBox checkBox;
        int id;

    }

}