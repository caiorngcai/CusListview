package com.cairongcai.cuslistview.view;


import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cairongcai.cuslistview.R;

/**
 * Created by HY-IT on 2017/6/5.
 */

public class RefreshListview extends ListView implements OnScrollListener {

    public static final int PULL_TO_REFRESH=1;
    public static final int RELEASE_REFRESH=2;
    public static final int REFRESHING=3;
    private int currentstate=PULL_TO_REFRESH;
    private View mViewHeader;
    private View mArrowView;
    private ProgressBar pb;
    private TextView tv_title;
    private TextView mLastRefreshTime;
    private int mHeaderViewHeight;
    private View mViewFoot;
    private int mFootViewHeight;
    private RotateAnimation rotateUpAnim;
    private RotateAnimation rotateDownAnim;
    private float downY;
    private float moveY;
    private onRefreshListener mListener;
    private boolean isLoadingMore;
    public RefreshListview(Context context) {
        super(context);
        init();

    }

    public RefreshListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public RefreshListview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

    }
    private void init() {
        initHeaderView();
        initFootView();
        initAnimation();
        setOnScrollListener(this);//注册屏幕滚动监听事件
    }

    /**
     * 初始化动画的方法--向上反转，向下反转
     */
    private void initAnimation() {
        rotateUpAnim = new RotateAnimation(0f, -180f
                , Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateUpAnim.setDuration(300);
        rotateUpAnim.setFillAfter(true);
        rotateDownAnim = new RotateAnimation(-180f, -360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateDownAnim.setDuration(300);
        rotateDownAnim.setFillAfter(true);
    }

    /**
     * 初始化脚布局的方法，在加载listview之前要先加载
     */
    private void initFootView() {
        mViewFoot = View.inflate(getContext(), R.layout.layout_footer_list, null);
       mViewFoot.measure(0,0);
        mFootViewHeight = mViewFoot.getMeasuredHeight();
        mViewFoot.setPadding(0,-mFootViewHeight,0,0);
        addFooterView(mViewFoot);
    }

    /**
     * 初始化头布局的方法
     */
    private void initHeaderView() {
        mViewHeader = View.inflate(getContext(), R.layout.layout_list_header, null);
        mArrowView = mViewHeader.findViewById(R.id.iv_arrow);
        pb = (ProgressBar) mViewHeader.findViewById(R.id.pb);
        tv_title = (TextView) mViewHeader.findViewById(R.id.tv_title);
        mLastRefreshTime = (TextView) mViewHeader.findViewById(R.id.tv_desc_last_refresh);
        mViewHeader.measure(0,0);
        mHeaderViewHeight = mViewHeader.getMeasuredHeight();
        mViewHeader.setPadding(0,-mHeaderViewHeight,0,0);
        addHeaderView(mViewHeader);//在设置适配器之前就要执行加载头布局方法
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
       switch (ev.getAction())
       {
           case MotionEvent.ACTION_DOWN:
               downY = ev.getY();
               break;
           case MotionEvent.ACTION_MOVE:
               moveY = ev.getY();
               if(currentstate==REFRESHING)
               {
                   return super.onTouchEvent(ev);
               }
               float offset=moveY-downY;
               if(offset>0&&getFirstVisiblePosition()==0)
               {
                   int paddingTop= (int) (offset-mHeaderViewHeight);
                   mViewHeader.setPadding(0,paddingTop,0,0);
                   if(paddingTop>=0&&currentstate!=RELEASE_REFRESH)
                   {
                       currentstate=RELEASE_REFRESH;
                       updateHeader();
                   }else if(offset<0&&currentstate!=PULL_TO_REFRESH)
                   {
                       currentstate=PULL_TO_REFRESH;
                       updateHeader();
                   }
                   return true;
               }
               break;
           case MotionEvent.ACTION_UP:
               if(currentstate==PULL_TO_REFRESH)
               {
                   mViewHeader.setPadding(0,-mHeaderViewHeight,0,0);
               }else if(currentstate==RELEASE_REFRESH)
               {
                   mViewHeader.setPadding(0,0,0,0);
                   currentstate=REFRESHING;
                   updateHeader();
               }

               break;
           default:
               break;

       }
        return super.onTouchEvent(ev);
    }

    private void updateHeader() {
        switch (currentstate)
        {
            case PULL_TO_REFRESH:
                mArrowView.setAnimation(rotateDownAnim);
                tv_title.setText("下拉刷新");
                break;
            case REFRESHING:
                mArrowView.clearAnimation();
                mArrowView.setVisibility(INVISIBLE);
                pb.setVisibility(VISIBLE);
                tv_title.setText("正在刷新中");
                //TODO 这里要调用监听器刷新网络数据,接口
                if(mListener!=null)
                {
                    mListener.onRefresh();
                }

                break;
            default:
                break;
        }
    }
    public void onRefreshComplete()
    {
        if(isLoadingMore)
        {
            mViewFoot.setPadding(0,-mFootViewHeight,0,0);
            isLoadingMore=false;
        }else {
            currentstate=PULL_TO_REFRESH;
            tv_title.setText("下拉刷新");
            mViewHeader.setPadding(0,-mHeaderViewHeight,0,0);
            pb.setVisibility(INVISIBLE);
            mArrowView.setVisibility(VISIBLE);
            String time=getTime();
            mLastRefreshTime.setText(time);
        }

    }

    public String getTime() {
        long currentTimeMillis=System.currentTimeMillis();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(currentTimeMillis);
    }

    public interface onRefreshListener
    {
        void onRefresh();
        void onLoadmore();
    }
    public void setRefreshListener(onRefreshListener mListener)
    {
        this.mListener=mListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(isLoadingMore)
        {
            return;
        }else if(scrollState==SCROLL_STATE_IDLE&&getLastVisiblePosition()>=(getCount()-1))
        {
            isLoadingMore=true;
            mViewFoot.setPadding(0,0,0,0);
            setSelection(getCount());
            if(mListener!=null)
            {
                mListener.onLoadmore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}



