package xyz.jienan.pushpull;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


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
    private MemoService memoService;
    private RecyclerView recyclerView;
    private BottomSheetBehavior bottomSheetBehavior;
    private FrameLayout bottomLayout;
    private PushPullAdapter mAdapter;
    private FloatingActionButton fabSwipe;
    private TextView tvPush;
    private TextView tvPull;
    private LinearLayout headerBtns;
    private NestedScrollView edtPanel;
    private EditText edtMemo;
    private EditText edtMemo2;
    private TextView tvSwipeHint;
    private FrameLayout foreground;
    private final static int STATE_PUSH  = 1;
    private final static int STATE_PULL  = 2;
    private int state = 0;
    private InputMethodManager imm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = inflater.inflate(R.layout.fragment_pushpull, null, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        bottomLayout = view.findViewById(R.id.bottom_sheet);
        fabSwipe = view.findViewById(R.id.fab_action);
        tvSwipeHint = view.findViewById(R.id.tv_swipe_hint);
        edtMemo2 = view.findViewById(R.id.edt_memo2);
        foreground = view.findViewById(R.id.foreground);
        mDetector = new GestureDetectorCompat(getActivity(), mFabGestureListener);
        setupView();
        setupService();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        ItemTouchHelper.Callback callback = new MemoItemTouchHelperCallback(mAdapter);
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
                return false;
            }
        });
        fabSwipe.setOnClickListener(mClickListener);
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

                if (velocityX > 0) {
                    fabPosition = Math.min(++fabPosition, 1);
                } else if (velocityX < 0) {
                    fabPosition = Math.max(--fabPosition, -1);
                }
                swipeTo(fabPosition);
                int distance = width / 3 * fabPosition;
                ObjectAnimator animation = ObjectAnimator.ofFloat(fabSwipe, "translationX", distance);
                animation.setDuration(500);
                Log.d(TAG, "onFling: " + distance);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
            }
            return true;
        }
    };

    private int fabPosition = 0;

    private void swipeTo(int i) {
        if (i == 0) {
            tvSwipeHint.setVisibility(View.GONE);
            edtMemo2.setVisibility(View.GONE);
            edtMemo2.setText("");
            edtMemo2.clearComposingText();
            final View view = getActivity().getCurrentFocus();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (view != null) {

                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        foreground.setVisibility(View.GONE);
                    }
                }
            }, 500);
            fabAnim(R.drawable.anim_add_to_swipe);
        } else {
            tvSwipeHint.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tvSwipeHint.getLayoutParams();
            if (i == 1) {
                lp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                tvSwipeHint.setText("Create a pull");
                edtMemo2.setHint("Input the memo id");
                edtMemo2.setMaxEms(10);
                edtMemo2.setSingleLine(true);
                fabAnim(R.drawable.anim_swipe_to_add);
            } else if (i == -1) {
                lp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                tvSwipeHint.setText("Create a push");
                edtMemo2.setHint("Input your memo");
                edtMemo2.setSingleLine(false);
                edtMemo2.setMaxEms(Integer.MAX_VALUE);
                fabAnim(R.drawable.anim_swipe_to_add);
            }
            tvSwipeHint.setLayoutParams(lp);
            edtMemo2.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    edtMemo2.requestFocus();
                    imm.showSoftInput(edtMemo2, InputMethodManager.SHOW_IMPLICIT);
                    foreground.setVisibility(View.VISIBLE);
                }
            }, 500);
            ColorDrawable drawable = new ColorDrawable();
            drawable.setColor(0x33000000);
        }
    }
    
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_action:
                    if (fabPosition == -1) {
                        pushMemo();
                    } else if (fabPosition == 1) {
                        pullMemo();
                    }
                    break;
            }

        }
    };

    private void pushMemo() {
        String memoContent = edtMemo2.getText().toString();
        if (TextUtils.isEmpty(memoContent)) {
            Toast.makeText(getActivity(), "Please input some contents", Toast.LENGTH_SHORT).show();
        } else {
            fabSwipe.setClickable(false);
            MemoEntity entity = new MemoEntity();
            entity.setMsg(memoContent);
            entity.setExpiredOn("1day");
            Call<CommonResponse> call = memoService.createMemo(entity);
            call.enqueue(new Callback<CommonResponse>() {

                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                    mAdapter.addMemo(response.body().getMemo());
                    swipeBackFromCheck();
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    fabSwipe.setClickable(true);
                }
            });
        }
    }

    private void pullMemo() {
        String memoId = edtMemo2.getText().toString();
        if (TextUtils.isEmpty(memoId)) {
            Toast.makeText(getActivity(), "Please input correct memo id", Toast.LENGTH_SHORT).show();
        } else {
            fabSwipe.setClickable(false);
            Call<CommonResponse> call = memoService.readMemo(memoId);
            call.enqueue(new Callback<CommonResponse>() {

                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                    if (response.body().getMemo() == null) {
                        Toast.makeText(getActivity(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
                        fabSwipe.setClickable(true);
                    } else {
                        mAdapter.addMemo(response.body().getMemo());
                        swipeBackFromCheck();
                    }

                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    fabSwipe.setClickable(true);
                }
            });
        }
    }

    private void swipeBackFromCheck() {
        fabAnim(R.drawable.anim_add_to_check);
        fabSwipe.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        final View view = getActivity().getCurrentFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    tvSwipeHint.setVisibility(View.GONE);
                    edtMemo2.setVisibility(View.GONE);
                    edtMemo2.setText("");
                    edtMemo2.clearComposingText();
                    foreground.setVisibility(View.GONE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                }
            }
        }, 500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animation = ObjectAnimator.ofFloat(fabSwipe, "translationX", 0);
                animation.setDuration(500);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
                fabAnim(R.drawable.anim_check_to_swipe);
                fabSwipe.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fabSwipe.setClickable(true);
            }
        }, 1000);
    }

    private void fabAnim(int animId) {
        fabSwipe.setImageResource(animId);
        Drawable drawable = fabSwipe.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                        case BottomSheetBehavior.STATE_DRAGGING:
                            edtPanel.setVisibility(View.GONE);
                            tvPull.setVisibility(View.VISIBLE);
                            tvPush.setVisibility(View.VISIBLE);
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:

                            int[] position = new int[2];
                            headerBtns.getLocationOnScreen(position);

                            Log.d(TAG, "onStateChanged: " + position[1]);
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
