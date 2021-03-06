package es.source.code.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.source.code.model.User;
import es.source.code.service.ServerObserverService;

/**
 * Created by xinhe on 2016/6/18.
 */
public class FoodView extends Activity{
    private ViewPager viewPager;//页卡内容
    private ImageView imageView;// 动画图片
    private TextView textView1,textView2,textView3,textView4;
    private List<View> views;// Tab页面列表
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private View view1,view2,view3,view4;//各个页卡
    private ListView listView1,listView2,listView3,listView4;//各个页卡中的ListView
    private Context context;
    private User user;
    private MenuItem item;
    /**
     * 食物列表
     */
    private List<FoodsOnService>  mFoodItems = new ArrayList<>();


    /**
     * 初始化食物列表
     */
    public void initFood(){
//        /**
//         * 测试数据，自定义一些食物。
//         */
//        String foodNames[] = {"苹果","香蕉","葡萄","猕猴桃","荔枝"};
//        String foodPrices[] = {"12","13","14","15","16"};
//        for(int i = 0; i < foodNames.length;i++){
//            FoodItem item = new FoodItem();
//            item.foodName = foodNames[i];
//            item.foodPrice = foodPrices[i];
//            mFoodItems.add(item);
//        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_view);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("currentuser");
        initFood();
        InitImageView();
        InitTextView();
        InitViewPager();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(FoodView.this,FoodOrderView.class);
        Bundle bundle = new Bundle();
        switch (item.getItemId()){
            case R.id.ordered://已点菜品

                bundle.putSerializable("currentuser",user);
                intent.putExtras(bundle);
                FoodView.this.startActivity(intent);
                Toast.makeText(FoodView.this,"已点菜品",Toast.LENGTH_SHORT).show();
                break;
            case R.id.check_order://查看订单
                bundle.putSerializable("currentuser",user);
                intent.putExtras(bundle);
                FoodView.this.startActivity(intent);
                Toast.makeText(FoodView.this,"查看订单",Toast.LENGTH_SHORT).show();
                break;
            case R.id.call_help:
                break;
            case R.id.refresh://启动实时更新
                        if(item.getTitle().equals("启动实时更新")){
                            //启动ServerObserverService服务
                            Intent serviceIntent = new Intent(FoodView.this, ServerObserverService.class);
                            bindService(serviceIntent,serviceConnection,Context.BIND_AUTO_CREATE);
                            /**
                             * 绑定Service服务后不可以直接进行传递Message。因为severMsger为空。
                             */
                            item.setTitle("停止实时更新");
//                            Message msg = new Message();
//                            Bundle data = new Bundle();
//                            data.putString("send","from client");
//                            msg.setData(data);
//                            msg.what = 1;
//                            msg.replyTo = clientMsger;
//                            try{
//                                serverMsger.send(msg);
//                            }catch (RemoteException e){
//                                e.printStackTrace();
//                            }
                        }else
                        {
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putString("send","from client");
                            msg.setData(data);
                            msg.what = 1;
                            msg.replyTo = clientMsger;
                            try{
                                serverMsger.send(msg);
                            }catch (RemoteException e){
                                e.printStackTrace();
                            }

//                            //// TODO: 2016/6/27 功能应该是向service发送Message信息
//                            Message msg = Message.obtain();
//                            msg.what = 0;
//                            sMessageHandler.sendMessage(msg);
                            item.setTitle("启动实时更新");
                        }
                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isServiceBound = false;
    private Messenger serverMsger;
    //创建Service连接
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.w("component name:", componentName.getClassName());
            serverMsger = new Messenger(iBinder);
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serverMsger = null;
            isServiceBound = false;
        }
    };
    List<FoodsOnService> mFoodOnService;


    private Handler sMessageHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            Toast.makeText(getApplicationContext(),msg.getData().getString("reply"),Toast.LENGTH_LONG).show();
            if(msg.what == 10){
                //解析Message携带的菜品信息,更新菜项信息
                mFoodOnService = (List<FoodsOnService>) msg.getData().getSerializable("foods");
                ListViewAdapter listViewAdapter = new ListViewAdapter(getBaseContext(),mFoodOnService,onClickListener);
                listView1.setAdapter(listViewAdapter);
                listView1.setOnItemClickListener(onItemClickListener);
                Toast.makeText(getApplicationContext(),"从服务器更新菜品成功",Toast.LENGTH_LONG).show();
            }

        }
    };
    private Messenger clientMsger = new Messenger(sMessageHandler);

    /**
     * ActionBar 中的菜单
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        item = menu.findItem(R.id.refresh);

        return super.onCreateOptionsMenu(menu);
    }

    class FoodItem{
        String foodName;
        String foodPrice;
    }
    private void InitViewPager() {
        viewPager=(ViewPager) findViewById(R.id.vPager);
        views=new ArrayList<View>();
        LayoutInflater inflater=getLayoutInflater();
        view1=inflater.inflate(R.layout.lay1, null);
        view2=inflater.inflate(R.layout.lay2, null);
        view3=inflater.inflate(R.layout.lay3, null);
        view4=inflater.inflate(R.layout.lay4, null);
        listView1 = (ListView)view1.findViewById(R.id.list_view1);
        listView2 = (ListView)view2.findViewById(R.id.list_view2);
        listView3 = (ListView)view3.findViewById(R.id.list_view3);
        listView4 = (ListView)view4.findViewById(R.id.list_view4);
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);

//        /**
//         * 为ListView添加适配器
//         */
//        ListViewAdapter listViewAdapter = new ListViewAdapter(getBaseContext(),mFoodItems,onClickListener);
//        listView1.setAdapter(listViewAdapter);
//        listView1.setOnItemClickListener(onItemClickListener);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button button = (Button) view;
            String text = ((Button) view).getText().toString();
            if("点菜".equals(text)){
                button.setText("退点");
                Toast.makeText(FoodView.this,"点菜成功",Toast.LENGTH_SHORT).show();
                // TODO: 2016/6/23  点菜列表中添加已点菜品

            }else if("退点".equals(text)){
                button.setText("点菜");
                Toast.makeText(FoodView.this,"退菜成功",Toast.LENGTH_SHORT).show();
                // TODO: 2016/6/23  点菜列表中移除已点菜品
            }

        }
    };
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Toast.makeText(FoodView.this,"点击item",Toast.LENGTH_LONG).show();
            // TODO: 2016/6/23 进入 菜品详情页面
            Intent intent = new Intent(FoodView.this,FoodDetailed.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("foodsitem",mFoodOnService.get(i).foodName);
            intent.putExtras(bundle);
            FoodView.this.startActivity(intent);

        }
    };
    /**
     * ListView的适配器
     */
    class ListViewAdapter extends BaseAdapter{

        private  Context context;
        /**
         * 食物列表
         */
        private List<FoodsOnService>  mFoodItems = new ArrayList<>();

        private View.OnClickListener onClickListener;

        private LayoutInflater mInflater;

        public ListViewAdapter(Context con, List<FoodsOnService> mFoodItems, View.OnClickListener onClickListener){
            this.context = con;
            this.mFoodItems = mFoodItems;
            this.onClickListener = onClickListener;
            this.mInflater = LayoutInflater.from(context);

        }
        @Override
        public int getCount() {
            return mFoodItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mFoodItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view == null){
                view = mInflater.inflate(R.layout.food_item,null);
                viewHolder = new ViewHolder();
                viewHolder.nameTv = (TextView)view.findViewById(R.id.food_name);
                viewHolder.priceTv = (TextView) view.findViewById(R.id.food_price);
                viewHolder.orderBtn = (Button)view.findViewById(R.id.order_btn);
                viewHolder.foodNumber = (TextView)view.findViewById(R.id.food_num);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)view.getTag();
            }

            FoodsOnService foodItem = mFoodItems.get(i);
            if(foodItem != null){
                viewHolder.nameTv.setText(foodItem.getFoodName());
                viewHolder.priceTv.setText("价格："+foodItem.getFoodPrice());
                viewHolder.foodNumber.setText("库存："+foodItem.getFoodNum());
                viewHolder.orderBtn.setTag(i);
                viewHolder.orderBtn.setOnClickListener(this.onClickListener);
            }

            return view;
        }
         class ViewHolder{
            TextView nameTv;
            TextView priceTv;
            Button orderBtn;
             TextView foodNumber;//库存
        }

    }


    /**
     *  初始化导航栏标题
     */

    private void InitTextView() {
        textView1 = (TextView) findViewById(R.id.text1);
        textView2 = (TextView) findViewById(R.id.text2);
        textView3 = (TextView) findViewById(R.id.text3);
        textView4 = (TextView) findViewById(R.id.text4);

        textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));
        textView3.setOnClickListener(new MyOnClickListener(2));
        textView4.setOnClickListener(new MyOnClickListener(3));
    }

    /**
     2      * 初始化动画，这个就是页卡滑动时，下面的横线也滑动的效果，在这里需要计算一些数据
     3 */

    private void InitImageView() {
        imageView= (ImageView) findViewById(R.id.cursor);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 4 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// 设置动画初始位置
    }
    /**
     *
     * 头标点击监听 3 */
    private class MyOnClickListener implements View.OnClickListener {
        private int index=0;
        public MyOnClickListener(int i){
            index=i;

        }
        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }

    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) 	{
            container.removeView(mListViews.get(position));
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return  mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==arg1;
        }

    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量
        public void onPageScrollStateChanged(int arg0) {


        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {


        }

        public void onPageSelected(int arg0) {
			//两种方法，这个是一种，下面还有一种，显然这个比较麻烦
//			Animation animation = null;
//			switch (arg0) {
//			case 0:
//				if (currIndex == 1) {
//					animation = new TranslateAnimation(one, 0, 0, 0);
//				} else if (currIndex == 2) {
//					animation = new TranslateAnimation(two, 0, 0, 0);
//				}
//				break;
//			case 1:
//				if (currIndex == 0) {
//					animation = new TranslateAnimation(offset, one, 0, 0);
//				} else if (currIndex == 2) {
//					animation = new TranslateAnimation(two, one, 0, 0);
//				}
//				break;
//			case 2:
//				if (currIndex == 0) {
//					animation = new TranslateAnimation(offset, two, 0, 0);
//				} else if (currIndex == 1) {
//					animation = new TranslateAnimation(one, two, 0, 0);
//				}
//				break;
//
//			}
            Animation animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);//显然这个比较简洁，只有一行代码。
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            imageView.startAnimation(animation);
            Toast.makeText(FoodView.this, "您选择了"+ viewPager.getCurrentItem()+"页卡", Toast.LENGTH_SHORT).show();
        }

    }
}


