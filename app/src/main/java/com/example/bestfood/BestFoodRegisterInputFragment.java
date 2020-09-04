package com.example.bestfood;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.bestfood.lib.EtcLib;
import com.example.bestfood.lib.GeoLib;
import com.example.bestfood.lib.GoLib;
import com.example.bestfood.lib.MyLog;
import com.example.bestfood.lib.MyToast;
import com.example.bestfood.lib.StringLib;
import com.example.bestfood.remote.RemoteService;
import com.example.bestfood.remote.ServiceGenerator;
import com.google.android.gms.maps.model.LatLng;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//맛집정보 입력 액티비티
public class BestFoodRegisterInputFragment extends Fragment implements View.OnClickListener{
    public static final String INFO_ITEM = "INFO_ITEM";
    private final String TAG = this.getClass().getSimpleName();

    Context context;
    FoodInfoItem infoItem;
    Address address;

    EditText nameEdit;
    EditText telEdit;
    EditText descriptionEdit;
    TextView currentLength;

    //fooditeminfo를 객체로 저장 registerinputfragment 인스턴스 생성해서 반환
    public static BestFoodRegisterInputFragment newInstance(FoodInfoItem infoItem){
        //newinstance : location프래그먼트에서 다음버튼 클릭시 input fragment생성을 위한 메소드
        //location프래그먼트에서 위치를 저장하고 있는 foodinfoitem을 넘겨받음
        Bundle bundle = new Bundle();
        bundle.putParcelable(INFO_ITEM, Parcels.wrap(infoItem));

        BestFoodRegisterInputFragment fragment = new BestFoodRegisterInputFragment();
        fragment.setArguments(bundle);     //다른 액티비티나 프래그먼트로 전달가능
        return fragment;
    }

    //프래그먼트 생성시 호출, foodinfoitem -> bestfoodregisteractivity에 저장
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            infoItem = Parcels.unwrap(getArguments().getParcelable(INFO_ITEM));
            if(infoItem.seq != 0){
                BestFoodRegisterActivity.currentItem = infoItem;
            }
            MyLog.d(TAG, "infoitem "+infoItem);
        }
    }

    //뷰 생성 layoutinflater 객체 생성
    @Override
    public View onCrateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        context = this.getActivity();
        address = GeoLib.getInstance().getAddressString(context, new LatLng(infoItem.latitude, infoItem.longitude));
        MyLog.d(TAG, "address" + address);

        return inflater.inflate(R.layout.fragment_bestfood_register_input, container, false);
    }

    //맛집 정보 입력할 뷰 생성
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        currentLength = (TextView)view.findViewById(R.id.current_length);
        nameEdit = (EditText)view.findViewById(R.id.bestfood_name);
        telEdit = (EditText)view.findViewById(R.id.bestfood_tel);
        descriptionEdit = (EditText)view.findViewById(R.id.bestfood_description);
        descriptionEdit.addTextChangedListener(new TextWatcher() {           //문자열 변경 감지해서 글자수 나타냄
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //길이가 변화되면
                currentLength.setText(String.valueOf(s.length()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        EditText addressEdit = (EditText)view.findViewById(R.id.bestfood_address);
        infoItem.address = GeoLib.getInstance().getAddressString(address);
        if (!StringLib.getInstance().isBlank(infoItem.address)) {
            addressEdit.setText(infoItem.address);
        }

        Button prevButton = (Button) view.findViewById(R.id.prev);
        prevButton.setOnClickListener(this);

        Button nextButton = (Button) view.findViewById(R.id.next);
        nextButton.setOnClickListener(this);
    }

    //클릭 이벤트 처리
    @Override
    public void onClick(View v){
        infoItem.name = nameEdit.getText().toString();
        infoItem.tel = telEdit.getText().toString();
        infoItem.description = descriptionEdit.getText().toString();
        MyLog.d(TAG, "onclick imageItem" + infoItem);

        if(v.getId() == R.id.prev){      //이전 버튼 누르면 content_main에 위치 설정 넣음
            GoLib.getInstance().goFragment(getFragmentManager(), R.id.content_main, BestFoodRegisterLocationFragment.newInstance(infoItem));
        }
        else if(v.getId() == R.id.next){     //다음 버튼 누르면
            save();
        }
    }
    private void save(){
        if(StringLib.getInstance().isBlank(infoItem.name)){
            MyToast.s(context, context.getResources().getString(R.string.input_bestfood_name));
            return;
        }

        if(StringLib.getInstance().isBlank(infoItem.tel) || !EtcLib.getInstance().isValidPhoneNumber(infoItem.tel)){
            MyToast.s(context, context.getResources().getString(R.string.not_valid_tel_number));
            return;
        }

        insertFoodInfo();
    }

    //사용자가 입력한 정보 서버에 저장
    //서버의 응답이 성공적인 경우 서버에서 넘어온 일련번호를 infoitem에 저장
    private void insertFoodInfo(){
        MyLog.d(TAG, infoItem.toString());

        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertFoodInfo(infoItem);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                //bestfood_info 테이블에 데이터 추가하고 일련번호 받음 (seq로 실제 데이터 접근)
                //문자열로 넘어오는 일련번호를 숫자로 변경
                if(response.isSuccessful()) {
                    int seq = 0;
                    String seqString = response.body();

                    try {
                        seq = Integer.parseInt(seqString);
                    } catch (Exception e) {
                        seq = 0;
                    }
                    if(seq == 0){

                    }
                    else{
                        infoItem.seq = seq;
                        goNextPage();
                    }
                }
                else{
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    MyLog.d(TAG, "fail" + statusCode + errorBody.toString());
                }
            }
            @Override
            public void onFailure(Call <String> call, Throwable t) {
                MyLog.d(TAG, "no interner connectivity");
            }
        });
    }

    //맛집 이미지 등록 프래그먼트로 이동
    private void goNextPage(){
        GoLib.getInstance().goFragmentBack(getFragmentManager(), R.id.content_main, BestFoodRegisterImageFragment.newInstance(infoItem.seq));
    }
}

