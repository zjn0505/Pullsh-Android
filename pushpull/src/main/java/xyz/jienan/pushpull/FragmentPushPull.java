package xyz.jienan.pushpull;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.jienan.pushpull.network.CommonResponse;
import xyz.jienan.pushpull.network.MemoEntity;
import xyz.jienan.pushpull.network.MemoService;

/**
 * Created by Jienan on 2017/10/30.
 */

public class FragmentPushPull extends Fragment {

    private final static String TAG = FragmentPushPull.class.getSimpleName();
    private final static String BASE_URL = "https://api.jienan.xyz/";
    private PushPullFabListener mListener;
    private MemoService memoService;
    private RecyclerView recyclerView;
    private BottomSheetBehavior bottomSheetBehavior;
    private FrameLayout bottomLayout;
    private PushPullAdapter mAdapter;
    private FloatingActionButton fab;
    private FloatingActionButton fabSwipe;
    private TextView tvPush;
    private TextView tvPull;
    private LinearLayout headerBtns;
    private NestedScrollView edtPanel;
    private EditText edtMemo;
    private final static int STATE_PUSH  = 1;
    private final static int STATE_PULL  = 2;
    private int state = 0;

    public class PushPullFabListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String memo = edtMemo.getText().toString();
            if (state == STATE_PUSH && !TextUtils.isEmpty(memo)) {
                MemoEntity entity = new MemoEntity();
                entity.setMsg(memo);
                entity.setExpiredOn("1day");
                Call<CommonResponse> call = memoService.createMemo(entity);
                call.enqueue(new Callback<CommonResponse>() {

                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                        mAdapter.addMemo(response.body().getMemo());
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {

                    }
                });
            } else if (state == STATE_PULL && !TextUtils.isEmpty(memo)) {
                Call<CommonResponse> call = memoService.readMemo(memo);
                call.enqueue(new Callback<CommonResponse>() {

                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                        mAdapter.addMemo(response.body().getMemo());
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {

                    }
                });
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pushpull, null, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        bottomLayout = view.findViewById(R.id.bottom_sheet);
        fabSwipe = view.findViewById(R.id.fab_action);
        mDetector = new GestureDetectorCompat(getActivity(), mFabGestureListener);
        setupView();
        setupService();
        return view;
    }

    public PushPullFabListener getListener() {
        if (mListener == null) {
            mListener = new PushPullFabListener();
        }
        return mListener;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fab = getActivity().findViewById(R.id.fab);
    }

    private void setupService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        memoService = retrofit.create(MemoService.class);
    }

    private void setupView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mAdapter = new PushPullAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemTouchHelper.Callback callback = new TaskItemTouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomLayout);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
        tvPush = bottomLayout.findViewById(R.id.btn_bottom_left);
        tvPull = bottomLayout.findViewById(R.id.btn_bottom_right);
        tvPush.setOnClickListener(mClickListener);
        tvPull.setOnClickListener(mClickListener);
        headerBtns = bottomLayout.findViewById(R.id.header_btns);
        edtPanel = bottomLayout.findViewById(R.id.edt_panel);
        edtMemo = bottomLayout.findViewById(R.id.edt_memo);

        fabSwipe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                if (originX == 0) {
                    originX = v.getX();
                }
                return true;
            }
        });
    }

    private float originX = 0;

    private GestureDetectorCompat mDetector;
    private GestureDetector.OnGestureListener mFabGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: " + e1.toString() + e2.toString());
            Log.d(TAG, "onFling: " + velocityX +" "+ velocityY);
            if (Math.abs(velocityX) > 500) {

                int width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
                int distance = velocityX > 0 ? width / 3 : -(width/3);
                ObjectAnimator animation = ObjectAnimator.ofFloat(fabSwipe, "translationX", distance);
                animation.setDuration(500);
                animation.start();
            }
            return true;
        }
    };
    
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_bottom_left:
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        ViewGroup.LayoutParams lp = bottomLayout.getLayoutParams();
                        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        bottomLayout.setLayoutParams(lp);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        bottomLayout.requestLayout();
                        fab.setVisibility(View.VISIBLE);
                        state = STATE_PUSH;
                    }
                    break;
                case R.id.btn_bottom_right:
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        ViewGroup.LayoutParams lp = bottomLayout.getLayoutParams();
                        lp.height = 600;
                        bottomLayout.setLayoutParams(lp);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        fab.setVisibility(View.VISIBLE);
                        state = STATE_PULL;
                    }
                    break;

            }

        }
    };

    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                        case BottomSheetBehavior.STATE_DRAGGING:
                            fab.setVisibility(View.GONE);
                            edtPanel.setVisibility(View.GONE);
                            tvPull.setVisibility(View.VISIBLE);
                            tvPush.setVisibility(View.VISIBLE);
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:

                            int[] position = new int[2];
                            headerBtns.getLocationOnScreen(position);
                            fab.setY(position[1]);

                            Log.d(TAG, "onStateChanged: " + position[1]);
                            fab.setVisibility(View.VISIBLE);
                            edtPanel.setVisibility(View.VISIBLE);
                            tvPull.setVisibility(View.INVISIBLE);
                            tvPush.setVisibility(View.INVISIBLE);
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    headerBtns.setAlpha(1-slideOffset);
                }
            };


}
