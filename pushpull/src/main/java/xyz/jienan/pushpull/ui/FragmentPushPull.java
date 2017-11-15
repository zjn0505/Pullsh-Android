package xyz.jienan.pushpull.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.github.jorgecastilloprz.FABProgressCircle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.jienan.pushpull.BuildConfig;
import xyz.jienan.pushpull.DateUtils;
import xyz.jienan.pushpull.R;
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
    private CoordinatorLayout coordiLayout;
    private RecyclerView recyclerView;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout bottomLayout;
    private PushPullAdapter mAdapter;
    private FloatingActionButton fabSwipe;
    private FABProgressCircle fabWrapper;
    private RelativeLayout bottomHeader;
    private RelativeLayout bottomWrapper;
    private EditText edtMemo;
    private TextView tvSwipeHint;
    private FrameLayout foreground;
    private InputMethodManager imm;
    private SharedPreferences sharedPreferences;
    private ClipboardManager clipboard;

    // START views in bottom sheet
    private View bsbShadow;
    private TextView tvBsbMemoId;
    private ImageView ivBsbIdCopy;
    private TextView tvBsbMemoCreate;
    private TextView tvBsbMemoExpire;
    private TextView tvBsbMemoAccess;
    private TextView tvBsbMemoContent;
    private TextView tvBsbIsAuthor;
    // END views in bottom sheet

    private int swipeTransDistanceX = 0;
    private float originX = 0;
    private GestureDetectorCompat mDetector;
    private int fabPosition = 0;

    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            bsbShadow.setVisibility(View.GONE);
                            foreground.setVisibility(View.GONE);
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            foreground.setVisibility(View.VISIBLE);
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            foreground.setVisibility(View.VISIBLE);
                            bsbShadow.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    foreground.setAlpha(slideOffset);
                    foreground.setVisibility(View.VISIBLE);
                }
            };
    private ItemInteractionCallback itemInteractionCallback = new ItemInteractionCallback() {
        @Override
        public void onClick(final MemoEntity entity) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvBsbMemoId.setText(entity.getId());
            ivBsbIdCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipData clip = ClipData.newPlainText("id", entity.getId());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), "id copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

            tvBsbMemoCreate.setText(String.format(getString(R.string.memo_create), DateUtils.parseMongoUTC(entity.getCreatedDate(), sdf)));
            if (TextUtils.isEmpty(entity.getExpiredOn())) {
                tvBsbMemoExpire.setVisibility(View.GONE);
            } else {
                tvBsbMemoExpire.setVisibility(View.VISIBLE);
                tvBsbMemoExpire.setText(String.format(getString(R.string.memo_expire), DateUtils.parseMongoUTC(entity.getExpiredOn(), sdf)));
            }
            if (entity.getMaxAccessCount() == 0) {
                tvBsbMemoAccess.setVisibility(View.GONE);
            } else {
                tvBsbMemoAccess.setVisibility(View.VISIBLE);
                int resId = entity.createdFromPush ? R.string.memo_allowance_author : R.string.memo_allowance_user;
                tvBsbMemoAccess.setText(String.format(getString(resId), (entity.getMaxAccessCount() - entity.getAccessCount())));
            }
            if (!entity.createdFromPush) {
                tvBsbIsAuthor.setVisibility(View.GONE);
            } else {
                tvBsbIsAuthor.setVisibility(View.VISIBLE);
            }
            final String msg = entity.getMsg();
            tvBsbMemoContent.setText(msg);
            if (msg.length() < 10) {
                tvBsbMemoContent.setTextSize(30);
            } else if (msg.length() < 20) {
                tvBsbMemoContent.setTextSize(25);
            } else {
                tvBsbMemoContent.setTextSize(20);
            }
            tvBsbMemoContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipData clip = ClipData.newPlainText("msg", msg);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), "Memo content copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onDismiss(final int position) {
            Snackbar snackbar = Snackbar.make(coordiLayout,
                    R.string.snackbar_delete, Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.snackbar_undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.undoItemDismiss(position);
                }
            });
            snackbar.show();
        }
    };

    private MainActivity.OnBackPressedListener mBackPressListener = new MainActivity.OnBackPressedListener() {
        @Override
        public boolean onBackPressed() {
            if (fabPosition != 0) {
                fabPosition = 0;
                swipeTo(0);
                return true;
            } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return true;
            } else {
                return false;
            }
        }
    };

    private GestureDetector.OnGestureListener mFabGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > 500) {
                if (velocityX > 0) {
                    fabPosition = Math.min(++fabPosition, 1);
                } else if (velocityX < 0) {
                    fabPosition = Math.max(--fabPosition, -1);
                }
                swipeTo(fabPosition);
            }
            return true;
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_action:
                    if (fabWrapper.getTranslationX() == swipeTransDistanceX) {
                        if (fabPosition == -1) {
                            pushMemo();
                        } else if (fabPosition == 1) {
                            pullMemo();
                        }
                    }
                    break;
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        this.setHasOptionsMenu(true);
        sharedPreferences = getActivity().getSharedPreferences("MEMO_CONFIG", Context.MODE_PRIVATE);
        View view = inflater.inflate(R.layout.fragment_pushpull, null, false);
        coordiLayout = view.findViewById(R.id.coordi_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        bottomLayout = view.findViewById(R.id.bottom_sheet);
        fabSwipe = view.findViewById(R.id.fab_action);
        fabWrapper = view.findViewById(R.id.fab_wrapper);
        tvSwipeHint = view.findViewById(R.id.tv_swipe_hint);
        edtMemo = view.findViewById(R.id.edt_memo2);
        foreground = view.findViewById(R.id.foreground);
        bottomWrapper = view.findViewById(R.id.bottom_wrapper);
        bottomHeader = view.findViewById(R.id.bottom_header);
        bsbShadow = view.findViewById(R.id.bottom_sheet_shadow);
        mDetector = new GestureDetectorCompat(getActivity(), mFabGestureListener);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        setupView();
        setupService();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null)
            mAdapter.expireItems();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setBackPressListener(mBackPressListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_memo, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_push_config) {
            PushConfigDialog dialog = new PushConfigDialog();
            dialog.setContext(getActivity());
            dialog.show(getFragmentManager(), null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupService() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());
        if (BuildConfig.DEBUG) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor()).build();
            builder.client(client);
        }

        Retrofit retrofit = builder.build();
        memoService = retrofit.create(MemoService.class);
    }

    private void setupView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mAdapter = new PushPullAdapter(getActivity(), itemInteractionCallback);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemTouchHelper.Callback callback = new MemoItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        foreground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getAlpha() == 0) {
                    return false;
                }
                return true;
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(bottomLayout);
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
        tvBsbMemoId = bottomLayout.findViewById(R.id.bsb_memo_id);
        ivBsbIdCopy = bottomLayout.findViewById(R.id.bsb_memo_id_copy);
        tvBsbMemoCreate = bottomLayout.findViewById(R.id.bsb_memo_create);
        tvBsbMemoExpire = bottomLayout.findViewById(R.id.bsb_memo_expire);
        tvBsbMemoAccess = bottomLayout.findViewById(R.id.bsb_memo_allowance);
        tvBsbMemoContent = bottomLayout.findViewById(R.id.tv_memo);
        tvBsbIsAuthor = bottomLayout.findViewById(R.id.bsb_memo_create_by_push);
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

    private void swipeTo(int i) {
        if (i == 0) {
            edtMemo.setVisibility(View.GONE);
            edtMemo.setText("");
            edtMemo.clearComposingText();
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
            bottomHeader.setVisibility(View.VISIBLE);
            if (i == 1) {
                tvSwipeHint.setText("Create a pull");
                edtMemo.setHint("Input the memo id");
                edtMemo.setMaxEms(10);
                edtMemo.setSingleLine(true);
                fabAnim(R.drawable.anim_swipe_to_add);
            } else if (i == -1) {
                tvSwipeHint.setText("Create a push");
                edtMemo.setHint("Input your memo");
                edtMemo.setSingleLine(false);
                edtMemo.setMaxEms(Integer.MAX_VALUE);
                fabAnim(R.drawable.anim_swipe_to_add);
            }
            edtMemo.setVisibility(View.VISIBLE);
            foreground.setVisibility(View.VISIBLE);
            foreground.setAlpha(0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    edtMemo.requestFocus();
                    imm.showSoftInput(edtMemo, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 500);
            ColorDrawable drawable = new ColorDrawable();
            drawable.setColor(0x33000000);
        }
        int width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        swipeTransDistanceX = width / 3 * fabPosition;

        ArrayList<ObjectAnimator> objectAnimatorsArray = new ArrayList<ObjectAnimator>();
        ObjectAnimator animatorHintTransX = ObjectAnimator.ofFloat(tvSwipeHint, "translationX", -swipeTransDistanceX/1.5f);
        if (fabPosition == 0) {
            animatorHintTransX.setInterpolator(new AccelerateInterpolator());
        } else {
            animatorHintTransX.setInterpolator(new AccelerateInterpolator());
        }
        objectAnimatorsArray.add(animatorHintTransX);
        ObjectAnimator animatorFabTransX = ObjectAnimator.ofFloat(fabWrapper, "translationX", swipeTransDistanceX);
        animatorFabTransX.setInterpolator(new DecelerateInterpolator());
        objectAnimatorsArray.add(animatorFabTransX);
        int from = fabPosition == 0 ? 1 : 0;
        int to = fabPosition == 0 ? 0 : 1;
        ObjectAnimator animatorBottom = ObjectAnimator.ofFloat(bottomWrapper, "alpha", from, to);
        animatorBottom.setInterpolator(new AnticipateInterpolator());
        objectAnimatorsArray.add(animatorBottom);
        ObjectAnimator animatorHint = ObjectAnimator.ofFloat(tvSwipeHint, "alpha", from, to);
        if (fabPosition == 0) {
            animatorHint.setInterpolator(new DecelerateInterpolator());
        }
        objectAnimatorsArray.add(animatorHint);
        ObjectAnimator animatorCover = ObjectAnimator.ofFloat(foreground, "alpha", from, to);
        animatorCover.setInterpolator(new AccelerateInterpolator());
        objectAnimatorsArray.add(animatorCover);
        ObjectAnimator[] objectAnimators = objectAnimatorsArray.toArray(new ObjectAnimator[objectAnimatorsArray.size()]);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(objectAnimators);
        animSet.setDuration(500);
        animSet.start();
    }

    private void pushMemo() {
        String memoContent = edtMemo.getText().toString();
        if (TextUtils.isEmpty(memoContent)) {
            Toast.makeText(getActivity(), "Please input some contents", Toast.LENGTH_SHORT).show();
        } else {
            fabSwipe.setClickable(false);
            int time = sharedPreferences.getInt("EXPIRED_TIME", 1);
            int type = sharedPreferences.getInt("EXPIRED_TYPE", 3);
            int count = sharedPreferences.getInt("ACCESS_COUNT", 0);
            String expiredArg = "";
            if (time > 0)
                switch (type) {
                    case 0:
                        expiredArg = time + "min";
                        break;
                    case 1:
                        expiredArg = time + "hr";
                        break;
                    case 2:
                        expiredArg = time + "day";
                        break;
                }
            if (type == 3) {
                expiredArg = "";
            }
            fabWrapper.show();
            MemoEntity entity = new MemoEntity();
            entity.setMsg(memoContent);
            entity.setMaxAccessCount(count);
            entity.setExpiredOn(expiredArg);
            Call<CommonResponse> call = memoService.createMemo(entity);
            call.enqueue(new Callback<CommonResponse>() {

                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                    MemoEntity entity = response.body().getMemo();
                    entity.createdFromPush = true;
                    mAdapter.addMemo(entity);
                    swipeBackFromCheck();
                    fabWrapper.hide();
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    fabSwipe.setClickable(true);
                    fabWrapper.hide();
                    Log.e("Request", t.getLocalizedMessage());
                }
            });
        }
    }

    private void pullMemo() {
        String memoId = edtMemo.getText().toString();
        if (TextUtils.isEmpty(memoId)) {
            Toast.makeText(getActivity(), "Please input correct memo id", Toast.LENGTH_SHORT).show();
        } else {
            if (mAdapter.checkExistMeme(memoId)) {
                swipeBackFromCheck();
                return;
            }
            fabSwipe.setClickable(false);
            fabWrapper.show();
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
                    fabWrapper.hide();
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    fabSwipe.setClickable(true);
                    fabWrapper.hide();
                    Log.e("Request", t.getLocalizedMessage());
                }
            });
        }
    }

    private void swipeBackFromCheck() {
        fabPosition = 0;
        fabAnim(R.drawable.anim_add_to_check);
        fabSwipe.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        final View view = getActivity().getCurrentFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    ArrayList<ObjectAnimator> objectAnimatorsArray = new ArrayList<ObjectAnimator>();
                    ObjectAnimator animatorCover = ObjectAnimator.ofFloat(foreground, "alpha", 1, 0);
                    animatorCover.setInterpolator(new AccelerateInterpolator());
                    objectAnimatorsArray.add(animatorCover);
                    ObjectAnimator animatorBottom = ObjectAnimator.ofFloat(bottomWrapper, "alpha", 1, 0);
                    animatorBottom.setInterpolator(new AccelerateInterpolator());
                    objectAnimatorsArray.add(animatorBottom);
                    ObjectAnimator[] objectAnimators = objectAnimatorsArray.toArray(new ObjectAnimator[objectAnimatorsArray.size()]);
                    AnimatorSet animSet = new AnimatorSet();
                    animSet.playTogether(objectAnimators);
                    animSet.setDuration(500);
                    animSet.start();
                    edtMemo.setVisibility(View.GONE);
                    edtMemo.setText("");
                    edtMemo.clearComposingText();
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                }
            }
        }, 500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<ObjectAnimator> objectAnimatorsArray = new ArrayList<ObjectAnimator>();
                ObjectAnimator animatorHintTransX = ObjectAnimator.ofFloat(tvSwipeHint, "translationX", 0);
                animatorHintTransX.setInterpolator(new AccelerateInterpolator());
                objectAnimatorsArray.add(animatorHintTransX);
                ObjectAnimator animatorFab = ObjectAnimator.ofFloat(fabWrapper, "translationX", 0);
                animatorFab.setInterpolator(new DecelerateInterpolator());
                objectAnimatorsArray.add(animatorFab);
                ObjectAnimator[] objectAnimators = objectAnimatorsArray.toArray(new ObjectAnimator[objectAnimatorsArray.size()]);
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(objectAnimators);
                animSet.setDuration(500);
                animSet.start();
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
}
