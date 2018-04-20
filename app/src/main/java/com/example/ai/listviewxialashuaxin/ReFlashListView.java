package com.example.ai.listviewxialashuaxin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by AI on 2017/11/20.
 */

public class ReFlashListView extends ListView implements AbsListView.OnScrollListener{
    /**
     * 顶部布局文件
     * @param context
     */
    View header;
    /**
     * 顶部布局文件的高度
     */
    int headerHeight;

    boolean isRemark;//标记，当前是在listview最顶端按下的；
    int startY;//按下时的Y值

    int state;//当前的状态
    final int NONE=0;//正常状态
    final int PULL=1;//提示下拉状态
    final int RELESE=2;//提示释放状态
    final int REFLASHING=3;//刷新状态

    int firstVisibleItem;//当前第一个可见的Item的位置

    int scrollState;//listview当前滚动状态

    IReflashListener reflashListener;

    public ReFlashListView(Context context) {
        super(context);
        initView(context);
    }

    public ReFlashListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ReFlashListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }



    /**
     * 初始化界面，添加顶部布局文件到listview
     * @param context
     */

    private void initView(Context context){
        LayoutInflater inflater=LayoutInflater.from(context);
        header=inflater.inflate(R.layout.header_layout,null);
        measureView(header);
        headerHeight=header.getMeasuredHeight();
        topPadding(-headerHeight);
        this.addHeaderView(header);
        this.setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view,int firstVisibleItem,
                         int visibleItem,int totalItemCount){
        this.firstVisibleItem=firstVisibleItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        this.scrollState=scrollState;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(firstVisibleItem==0){
                    isRemark=true;
                    startY=(int)event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                 onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                 if(state==RELESE){
                     state=REFLASHING;
                     //加载最新数据
                     reflashViewByState();
                     reflashListener.onReflash();

                 }else if(state==PULL){
                     state=NONE;
                     isRemark=false;
                     reflashViewByState();
                 }
                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 判断移动过程中操作；
     * @param event
     */
    private void onMove(MotionEvent event){
        if(!isRemark){
            return;
        }

        int tempY=(int)event.getY();
        int space=tempY-startY;
        int topPadding=space-headerHeight;//header是不断显示出来的，不断记录位置
        switch (state){
            case NONE:
                //下拉刷新
                 if(space>0){
                     state=PULL;
                     reflashViewByState();
                 }
                break;
                 //松开可以刷新，大于一定高度显示，高度自定义
            case PULL:
                topPadding(topPadding);
                 if(space>headerHeight+30&&scrollState==SCROLL_STATE_TOUCH_SCROLL){
                     state=RELESE;
                     reflashViewByState();
                 }
                break;
                //显示松开可以刷新
            case RELESE:
                topPadding(topPadding);
                if(space<headerHeight+30){
                    state=PULL;
                    reflashViewByState();
                }else if (space<=0){
                    state=NONE;
                    isRemark=false;
                    reflashViewByState();
                }
                break;
        }
    }

    /**
     * 通知父布局，占用的宽高
     * @param view
     */

    private void measureView(View view){
        ViewGroup.LayoutParams params=view.getLayoutParams();
        if (params==null){
            params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        /**
         * 三个参数的意思，第一个：header的左右边距
         */
        int width=ViewGroup.getChildMeasureSpec(0,0,params.width);
        int height;
        int tempHeight=params.height;
        if (tempHeight>0){
            height=MeasureSpec.makeMeasureSpec(tempHeight,MeasureSpec.EXACTLY);
        }else{
            height=MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        }
        view.measure(width,height);

    }



    /**
     * 设置header布局的上边距
     * @param topPadding
     */
    private void topPadding(int topPadding){
        header.setPadding(header.getPaddingLeft(),
                topPadding,header.getPaddingRight(),
                header.getPaddingBottom());
        /**
         * invalidate()是用来刷新View的，必须是在UI线程中进行工作。
         * 比如在修改某个view的显示时，调用invalidate()才能看到重新绘制的界面。
         * invalidate()的调用是把之前的旧的view从主UI线程队列中pop掉。
         */
        header.invalidate();
    }

    /**
     * 根据当前状态，改变界面显示
     */
    private void reflashViewByState(){
        TextView tip=(TextView)header.findViewById(R.id.tip);
        ImageView arrow=(ImageView)header.findViewById(R.id.pull_to_refresh_arrow);
        ProgressBar progressBar=(ProgressBar)header.findViewById(R.id.progress);

        RotateAnimation animation=new RotateAnimation(0,180,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,
                RotateAnimation.RELATIVE_TO_SELF,0.5f);

        animation.setDuration(500);
        animation.setFillAfter(true);
        RotateAnimation animation1=new RotateAnimation(180,0,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,
                RotateAnimation.RELATIVE_TO_SELF,0.5f);
        animation1.setDuration(500);
        animation1.setFillAfter(true);
        switch (state){
            case NONE:
                arrow.clearAnimation();
                topPadding(-headerHeight);
                break;
            case PULL:
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tip.setText("下拉可以刷新！");
                arrow.clearAnimation();
                arrow.setAnimation(animation1);
                break;
            case RELESE:
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tip.setText("松开可以刷新！");
                arrow.clearAnimation();
                arrow.setAnimation(animation);
                break;
            case REFLASHING:
                topPadding(50);
                arrow.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                tip.setText("正在刷新...");
                arrow.clearAnimation();
                break;

        }
    }

    /**
     * 获取完数据
     */
    public void reflashComplete(){
        state=NONE;
        isRemark=false;
        reflashViewByState();
        TextView lastupdatetime=header.findViewById(R.id.lastUpdate_time);
        SimpleDateFormat format=new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
        Date date =new Date(System.currentTimeMillis());
        String time=format.format(date);
        lastupdatetime.setText(time);
    }

    /**
     * 刷新数据接口
     */
    public interface IReflashListener{
       public void onReflash();
    }

    public void setInterface(IReflashListener reflashListener){
        this.reflashListener=reflashListener;
    }
}
