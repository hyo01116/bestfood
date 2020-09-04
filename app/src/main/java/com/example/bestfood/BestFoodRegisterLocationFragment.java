package com.example.bestfood;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;



import com.example.bestfood.lib.GeoLib;
import com.example.bestfood.lib.GoLib;
import com.example.bestfood.lib.MyLog;
import com.example.bestfood.lib.StringLib;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

public class BestFoodRegisterLocationFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {
    private static final int MAP_ZOOM_LEVEL_DEFAULT = 16;
    private static final int MAP_ZOOM_LEVEL_DETAIL = 18;
    private static final String INFO_ITEM = "INFO ITEM";

    private String TAG = this.getClass().getSimpleName();

    Context context;
    FoodInfoItem infoItem;
    GoogleMap map;

    TextView addressText;

    //foodinfoitem 객체를 인자로 저장 + fragment 인스턴스 생성해서 반환 (infoitem = 맛집정보를 저장하는 객체)
    public static BestFoodRegisterLocationFragment newInstance(FoodInfoItem infoItem){
        Bundle bundle = new Bundle();
        bundle.putParcelable(INFO_ITEM, Parcels.wrap(infoItem));

        BestFoodRegisterLocationFragment bestFoodRegisterLocationFragment = new BestFoodRegisterLocationFragment();
        bestFoodRegisterLocationFragment.setArguments(bundle);

        return bestFoodRegisterLocationFragment;
    }

    //foodinfoitem을 bestfoodregisteractivity에 currentItem으로 저장
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            infoItem = Parcels.unwrap(getArguments().getParcelable(INFO_ITEM));
            if(infoItem.seq != 0){
                BestFoodRegisterActivity.currentItem = infoItem;
            }
            MyLog.d(TAG, "infoItem " +infoItem);
        }
    }
    //fragment bestfood register location 을 기반으로 뷰 생성 (inflater => LayoutInflater)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        context = this.getActivity();
        View layout = inflater.inflate(R.layout.fragment_bestfood_register_location, container, false);

        return layout;
    }

    //oncreateView 다음 호출되며 구글맵을 화면에 보여줌
    @Override
    public void onViewCreate(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        if(fragment != null){
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.content_main, fragment).commit();
        }
        fragment.getMapAsync(this);     //지도 보여줄 준비가되면 onmapready 호출
        addressText = (TextView)view.findViewById(R.id.bestfood_address);

        Button nextButton = (Button) view.findViewById(R.id.next);
        nextButton.setOnClickListener(this);
    }

    //맵에서 마커 클릭시 호출됨
    //marker = 클릭한 마커에 대한 정보를 가진 객체, 마커 이벤트 처리시 return true
    @Override
    public boolean onMarkerClick(Marker marker){
        movePosition(marker.getPosition(), MAP_ZOOM_LEVEL_DETAIL);
        return false;
    }

    //맵이 준비되었을때 호출되며, 구글맵 설정하고 기본마커 추가
    @Override
    public void onMapReady(GoogleMap map){
        this.map = map;

        String fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (ActivityCompat.checkSelfPermission(context, fineLocationPermission) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnMapClickListener(this);

        UiSettings setting = map.getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);

        LatLng firstLatLng = new LatLng(infoItem.latitude, infoItem.longitude);
        if(infoItem.latitude != 0){
            addMarker(firstLatLng, MAP_ZOOM_LEVEL_DEFAULT);
        }
        setAddressText(firstLatLng);
    }

    //구글 맵 초기화, 마커 생성해서 맵에 추가
    private void addMarker(LatLng latLng, float zoomLevel){
        MarkerOptions marker = new MarkerOptions();
        marker.position(latLng);
        marker.title("현재위치");
        marker.draggable(true);

        map.clear();
        map.addMarker(marker);

        movePosition(latLng, zoomLevel);
    }

    //구글맵의 카메라를 위도, 경도, 줌레벨을 기반으로 이동
    private void movePosition(LatLng latLng, float zoomLevel){
        CameraPosition cp = new CameraPosition.Builder().target((latLng)).zoom(zoomLevel).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    @Override
    public void onMarkerDragStart(Marker marker){

    }
    @Override
    public void onMarkerDrag(Marker marker){

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    //사용자가 마커 이동을 끝냈을때 호출, 최종 마커위치 선정
    @Override
    public void onMarkerEnd(Marker marker){
        setCurrentLatLng(marker.getPosition());
        MyLog.d(TAG, "onMarkerDragEnd infoItem" +infoItem);
    }

    //지정된 위도 경도를 infoitem에 저장
    private void setCurrentLatLng(LatLng latLng){
        infoItem.latitude = latLng.latitude;
        infoItem.longitude = latLng.longitude;
        setAddressText(latLng);
    }

    //클릭 이벤트 처리, 맛집 정보 담당하는 프래그먼트 이동
    @Override
    public void onClick(View v){
        GoLib.getInstance().goFragment(getFragmentManager(), R.id.content_main, BestFoodRegisterInputFramgent.newInstance(infoItem));
    }
    //사용자가 맵 클릭시 호출, 현재 위도와 경도 저장 후 마커 추가
    @Override
    public void onMapClick(LatLng latLng){
        MyLog.d(TAG, "onMapClick "+latLng);
        setCurrentLatLng(latLng);

        addMarker(latLng, map.getCameraPosition().zoom);
    }

    //위도와 경도를 기반으로 주소 출력
    private void setAddressText(LatLng latLng) {
        MyLog.d(TAG, "setAddressText" + latLng);
        Address address = GeoLib.getInstance().getAddressString(context, latLng);

        String addressStr = GeoLib.getInstance().getAddressString(address);
        if (!StringLib.getInstance().isBlank(addressStr)) {
            addressText.setText(addressStr);
        }
    }
}









