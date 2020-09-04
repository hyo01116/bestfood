package com.example.bestfood;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.bestfood.lib.BitmapLib;
import com.example.bestfood.lib.FileLib;
import com.example.bestfood.lib.GoLib;
import com.example.bestfood.lib.MyLog;
import com.example.bestfood.lib.MyToast;
import com.example.bestfood.lib.RemoteLib;
import com.example.bestfood.lib.StringLib;
import com.example.bestfood.remote.RemoteService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

public class BestFoodRegisterImageFragment extends Fragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    public static final String INFO_SEQ = "INFO_SEQ";

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    Activity context;
    int infoSeq;

    File imageFile;
    String imageFilename;

    EditText imageMemoEdit;
    ImageView infoImage;

    ImageItem imageItem;

    boolean isSavingImage = false;

    //fooditeminfo를 저장하는 registerimage의 프래그먼트를 불러옴
    public static BestFoodRegisterImageFragment newInstance(int infoSeq){
        Bundle bundle = new Bundle();
        bundle.putInt(INFO_SEQ, infoSeq);

        BestFoodRegisterImageFragment f = new BestFoodRegisterImageFragment();
        f.setArguments(bundle);

        return f;
    }

    //프래그먼트 생성 시 호출, INFOSEQ를 infoseq에 저장
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            infoSeq = getArguments().getInt(INFO_SEQ);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        context = this.getActivity();
        View v = inflater.inflate(R.layout.fragment_bestfood_register_image, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        imageItem = new ImageItem();
        imageItem.infoSeq = infoSeq;

        imageFilename = infoSeq + "_" + String.valueOf(System.currentTimeMillis());
        imageFile = FileLib.getInstance().getImageFile(context, imageFilename);

        infoImage = (ImageView)view.findViewById(R.id.bestfood_image);
        imageMemoEdit = (EditText)view.findViewById(R.id.register_image_memo);

        ImageView imageRegister = (ImageView)view.findViewById(R.id.bestfood_image_register);
        imageRegister.setOnClickListener(this);

        view.findViewById(R.id.prev).setOnClickListener(this);
        view.findViewById(R.id.next).setOnClickListener(this);
    }


    private void getImageFromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        context.startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    private void getImageFromAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        context.startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.bestfood_image_register){
            showImageDialog(context);
        }
        else if(v.getId() == R.id.complete){
            saveImage();
        }
        else if(v.getId() == R.id.prev){
            GoLib.getInstance().goBackFragment(getFragmentManager());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_FROM_CAMERA){
                Picasso.with(context).load(imageFile).into(infoImage);
            }
            else if(requestCode == PICK_FROM_ALBUM && data != null){
                Uri dataUri = data.getData();

                if(dataUri != null){
                    Picasso.with(context).load(dataUri).into(infoImage);

                    Picasso.with(context).load(dataUri).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            BitmapLib.getInstance().saveBitmapToFileThread(imageUploadHandler, imageFile, bitmap);
                            isSavingImage = true;
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
                }
            }
        }
    }
    //사용자가 선택한 이미지와 입력한 메모를 imageitem객체에 저장
    private void setImageItem(){
        String imageMemo = imageMemoEdit.getText().toString();
        if(StringLib.getInstance().isBlank(imageMemo)){
            imageMemo ="";
        }
        imageItem.imageMemo = imageMemo;
        imageItem.filename = imageFilename + ".png";
    }

    //이미지 서버에 업로드
    private void saveImage() {
        if(isSavingImage){
            MyToast.s(context, R.string.no_image_ready);
            return;
        }
        MyLog.d(TAG, "imageFile.length()" + imageFile.length());
        if(imageFile.length() == 0){
            MyToast.s(context, R.string.no_image_selected);
            return;
        }
        setImageItem();

        RemoteLib.getInstance().uploadFoodImage(infoseq, imageItem.imageMemo, imageFile, finishHandler);
        isSavingImage = false;
    }

    public void showImageDialog(Context context){
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_bestfood_image_register)
                .setSingleChoiceItems(R.array.camera_album_category, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if(which == 0){
                            getImageFromCamera();
                        }
                        else {
                            getImageFromAlbum();
                        }
                        dialogInterface.dismiss();
                    }
                }).show();
    }
    Handler imageUploadHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            isSavingImage = false;
            setImageItem();
            Picasso.with(context).invalidate(RemoteService.IMAGE_URL + imageItem.fileName);
        }
    };
    Handler finishHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);

            context.finish();
        }
    }
}

