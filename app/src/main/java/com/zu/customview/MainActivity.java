package com.zu.customview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.zu.customview.swiftp.FtpActivity;
import com.zu.customview.view.ZoomActivity;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button gotoSlideButton;
    private Button gotoViewPagerButton;
    private Button gotoTagTestActivity;
    private Button layoutParamsTestButton;
    private Button frameLayoutTestButton;
    private Button zoomLayoutTestButton;
    private Button zoomViewTestButton;
    private Button albumListVIewTestButton;
    private Button listViewTestButton;
    private Button loadImageTestButton;
    private Button scanFileTestButton;
    private Button imageCheckTestButton;
    private Button galleryTestButton;
    private Button ftpTestButton;
    private Button hideHeadTestButton;
    private Button dragToLoadButton;
    private Button drawChartButton;
    private Button waveButton;
    private Button switchButtonActivity;
    private Button switchLayoutActivity;
    private Button tryIncusActivity;
    private Button webViewActivity;

    private Observer observer = new Observer() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {

        }

        @Override
        public void onNext(@NonNull Object o) {
            int id = (Integer)o;
            obtainClick(id);

        }

        @Override
        public void onError(@NonNull Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    };

    private Function<Object, Integer> getViewIdFunc = new Function<Object, Integer>() {
        @Override
        public Integer apply(@NonNull Object o) throws Exception {
            int id = ((View)o).getId();
            return id;
        }
    };

    private ObservableTransformer getViewIdTransformer = new ObservableTransformer() {
        @Override
        public ObservableSource apply(@NonNull Observable upstream) {
            return upstream.map(getViewIdFunc);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gotoSlideButton = (Button)findViewById(R.id.go_to_slide_activity);
        gotoSlideButton.setOnClickListener(this);




        gotoViewPagerButton = (Button)findViewById(R.id.go_to_viewPager_activity);
        gotoViewPagerButton.setOnClickListener(this);
        gotoTagTestActivity = (Button)findViewById(R.id.go_to_tagTest_activity);
        gotoTagTestActivity.setOnClickListener(this);
        layoutParamsTestButton = (Button)findViewById(R.id.layout_params_test);
        layoutParamsTestButton.setOnClickListener(this);
        frameLayoutTestButton = (Button)findViewById(R.id.frame_layout_test);
        frameLayoutTestButton.setOnClickListener(this);
        zoomLayoutTestButton = (Button)findViewById(R.id.zoom_layout_test);
        zoomLayoutTestButton.setOnClickListener(this);

        zoomViewTestButton = (Button)findViewById(R.id.zoom_view_test);
        zoomViewTestButton.setOnClickListener(this);

        albumListVIewTestButton = (Button)findViewById(R.id.albumList_view_test);
        albumListVIewTestButton.setOnClickListener(this);

        listViewTestButton = (Button)findViewById(R.id.list_view_test);
        listViewTestButton.setOnClickListener(this);

        loadImageTestButton = (Button)findViewById(R.id.load_image_test);
        loadImageTestButton.setOnClickListener(this);

        scanFileTestButton = (Button)findViewById(R.id.scan_file_test);
        scanFileTestButton.setOnClickListener(this);

        imageCheckTestButton = (Button)findViewById(R.id.image_check_test);
        imageCheckTestButton.setOnClickListener(this);

        galleryTestButton = (Button)findViewById(R.id.gallery_test);
        galleryTestButton.setOnClickListener(this);

        ftpTestButton = (Button)findViewById(R.id.ftp_test);
        ftpTestButton.setOnClickListener(this);

        hideHeadTestButton = (Button)findViewById(R.id.hide_head);
        hideHeadTestButton.setOnClickListener(this);

        dragToLoadButton = (Button)findViewById(R.id.drag_to_load_activity);
        dragToLoadButton.setOnClickListener(this);

        drawChartButton = (Button)findViewById(R.id.draw_chart_activity);
        drawChartButton.setOnClickListener(this);

        waveButton = (Button)findViewById(R.id.wave_button_activity);
        waveButton.setOnClickListener(this);

        switchButtonActivity = (Button)findViewById(R.id.switch_button_activity);
        switchButtonActivity.setOnClickListener(this);

        switchLayoutActivity = (Button)findViewById(R.id.switch_layout_activity);
        switchLayoutActivity.setOnClickListener(this);

        tryIncusActivity = (Button)findViewById(R.id.try_activity);
        tryIncusActivity.setOnClickListener(this);

        webViewActivity = (Button)findViewById(R.id.webview_activity);
        webViewActivity.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        obtainClick(v.getId());
    }

    public void obtainClick(int id) {
        switch (id)
        {
            case R.id.go_to_slide_activity:
                Intent intent1 = new Intent(this, SlideActivity.class);
                startActivity(intent1);
                break;
            case R.id.go_to_viewPager_activity:
                Intent intent2 = new Intent(this, ViewPagerActivity.class);
                startActivity(intent2);
                break;
            case R.id.go_to_tagTest_activity:
                Intent intent3 = new Intent(this, ViewTagActivity.class);
                startActivity(intent3);
                break;
            case R.id.layout_params_test:
                Intent intent4 = new Intent(this, LayoutParamTest.class);
                startActivity(intent4);
                break;
            case R.id.frame_layout_test:
                Intent intent5 = new Intent(this, FrameLayoutTest.class);
                startActivity(intent5);
                break;
            case R.id.zoom_layout_test:
                Intent intent6 = new Intent(this, ZoomActivity.class);
                startActivity(intent6);
                break;
            case R.id.zoom_view_test:
                Intent intent7 = new Intent(this, ZoomViewActivity.class);
                startActivity(intent7);
                break;
            case R.id.albumList_view_test:
                Intent intent8 = new Intent(this, AlbumListTest.class);
                startActivity(intent8);
                break;
            case R.id.list_view_test:
                Intent intent9 = new Intent(this, ListViewTest.class);
                startActivity(intent9);
                break;
            case R.id.load_image_test:
                Intent intent10 = new Intent(this, ImageLoadActivity.class);
                startActivity(intent10);
                break;
            case R.id.scan_file_test:
                Intent intent11 = new Intent(this, MediaScanActivity.class);
                startActivity(intent11);
                break;
            case R.id.image_check_test:
                Intent intent12 = new Intent(this, ImageCheckActivity.class);
                startActivity(intent12);
                break;
            case R.id.gallery_test:
                Intent intent13 = new Intent(this, ViewImageActivity.class);
                startActivity(intent13);
                break;
            case R.id.ftp_test:
                Intent intent14 = new Intent(this, FtpActivity.class);
                startActivity(intent14);
                break;
            case R.id.hide_head:
                Intent intent15 = new Intent(this, HideHeadTestActivity.class);
                startActivity(intent15);
                break;
            case R.id.drag_to_load_activity:
                Intent intent16 = new Intent(this, DragToLoadActivity.class);
                startActivity(intent16);
                break;
            case R.id.draw_chart_activity:
                Intent intent17 = new Intent(this, ChartActivity.class);
                startActivity(intent17);
                break;
            case R.id.wave_button_activity:
                Intent intent18 = new Intent(this, WaveButtonActivity.class);
                startActivity(intent18);
                break;
            case R.id.switch_button_activity:
                Intent intent19 = new Intent(this, SwitchButtonActivity.class);
                startActivity(intent19);
                break;
            case R.id.switch_layout_activity:
                Intent intent20 = new Intent(this, SwitchLayoutActivity.class);
                startActivity(intent20);
                break;
            case R.id.try_activity:
                Intent intent21 = new Intent(this, TryActivity.class);
                startActivity(intent21);
                break;
            case R.id.webview_activity:
                Intent intent22 = new Intent(this, WebViewActivity.class);
                startActivity(intent22);
                break;
            default:
                break;
        }
    }
}
