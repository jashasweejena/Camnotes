package com.jashasweejena.camnotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.asksira.bsimagepicker.BSImagePicker;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener, BSImagePicker.OnMultiImageSelectedListener {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PIX_REQUEST_CODE = 2;
    static final int MAX_SELECTION_COUNT = 5;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFromSdcard();
        imgPickerButton = findViewById(R.id.imgPickerBtn);
        imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void getFromSdcard() {
        File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        f.clear();
        if (file.isDirectory()) {
            listOfFiles = file.listFiles();


            for (int i = 0; i < listOfFiles.length; i++) {

                f.add(listOfFiles[i].getAbsolutePath());

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //When camera returns photo to calling activity
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            getFromSdcard();
            imagegrid.setAdapter(imageAdapter);
        }

        if (resultCode == Activity.RESULT_OK && requestCode == PIX_REQUEST_CODE) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

            for (String x : returnValue) {
                f.add(x);
            }

            imageAdapter.notifyDataSetChanged();
            imagegrid.setAdapter(imageAdapter);


        }
    }

    @Override
    public void onSingleImageSelected(Uri uri, String tag) {
        Log.d(TAG, "onSingleImageSelected: " + uri);
    }

    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {
        Log.d(TAG, "onMultiImageSelected: " + uriList.get(uriList.size() - 1));
    }

    void imagePicker() {

        Pix.start(MainActivity.this,                    //Activity or Fragment Instance
                PIX_REQUEST_CODE,                //Request code for activity results
                MAX_SELECTION_COUNT);    //Number of images to restict selection count


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
                return;
            }
        }
    }

    void makePdf(ArrayList<String> listOfPaths) {
        try {
            String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "PDF_" + timeStamp + "_";

            String dest = directoryPath + "/CamNotes/" + imageFileName + ".pdf"; //  Change pdf's name.

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
            e.printStackTrace();
        }
    }

    void clearGridView() {

        for (String x : f) {
            Uri uri = Uri.parse(x);
            File file = new File(uri.getPath());
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

        public ImageAdapter() {
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

    public static void saveListToSharedPref(Context context, ArrayList<String> parentList) {
        SharedPreferences mPrefs = context.getSharedPreferences("f", context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(parentList);
        prefsEditor.putString("myJson", json);
        prefsEditor.commit();
    }

    public static ArrayList<String> getListFromSharedPref(Context context) {
        ArrayList<String> savedCollage = new ArrayList<String>();
        SharedPreferences mPrefs = context.getSharedPreferences("f", context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("myJson", "");
        if (json.isEmpty()) {
            savedCollage = new ArrayList<String>();
        } else {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            savedCollage = gson.fromJson(json, type);
        }

        return savedCollage;
    }

}