package xyz.jienan.pushpull.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.jienan.pushpull.BuildConfig;
import xyz.jienan.pushpull.DateUtils;
import xyz.jienan.pushpull.R;
import xyz.jienan.pushpull.ToastUtils;
import xyz.jienan.pushpull.network.CommonResponse;
import xyz.jienan.pushpull.network.MemoEntity;
import xyz.jienan.pushpull.network.MemoService;
import xyz.jienan.pushpull.ui.settings.SettingsActivity;

import static xyz.jienan.pushpull.base.Const.PREF_KEY_ALIGN;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_CLICK;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_COPY;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_FRE;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PULLSH_HOST;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_ACCESS_COUNT;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_EXPIRED_TIME;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_EXPIRED_TYPE;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_REVERSE;

/**
 * Created by Jienan on 2017/10/30.
 */

public class FragmentPushPull extends Fragment implements IPullshAction{

    private final static String TAG = FragmentPushPull.class.getSimpleName();
    private static final int REQUEST_SETTINGS = 100;

    private MemoService.MemoAPI memoAPI;
    private CoordinatorLayout coordiLayout;
    private RecyclerView recyclerView;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout bottomLayout;
    private PushPullAdapter mAdapter;
    private FloatingActionButton fabSwipe;
    private FABProgressCircle fabWrapper;
    private RelativeLayout bottomHeader;
    private RelativeLayout bottomInputWrapper;
    private RelativeLayout bottomWrapper;
    private ImageView ivSwipeLeft;
    private ImageView ivSwipeRight;
    private EditText edtMemo;
    private TextView tvSwipeHint;
    private FrameLayout foreground;
    private InputMethodManager imm;
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
    private boolean fabPressTriggered = false;
    private float oldX, oldY, posX, posY;

    private SharedPreferences sharedPref;
    private Typeface fontMonaco;
    private boolean reversed;
    private int oldNightMode;
    private Realm realm;

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
            inflateBsb(entity);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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

    private void inflateBsb(final MemoEntity entity) {
        String align = sharedPref.getString(PREF_KEY_ALIGN, "align_center");
        if ("align_center".equals(align)) {
            tvBsbMemoContent.setGravity(Gravity.CENTER);
        } else if ("align_left".equals(align)) {
            tvBsbMemoContent.setGravity(Gravity.START);
        }
        tvBsbMemoId.setTypeface(fontMonaco,Typeface.BOLD);
        tvBsbMemoId.setText(entity.getId());
        ivBsbIdCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip;
                if (sharedPref.getBoolean(PREF_KEY_COPY, true)) {
                    String host = sharedPref.getString(PREF_KEY_PULLSH_HOST, "https://pullsh.me/");
                    clip = ClipData.newPlainText("url", host + entity.getId());
                    ToastUtils.showToast(getActivity(), getString(R.string.toast_memo_link_copied));
                } else {
                    clip = ClipData.newPlainText("id", entity.getId());
                    ToastUtils.showToast(getActivity(), getString(R.string.toast_memo_id_copied));
                }
                clipboard.setPrimaryClip(clip);
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
        tvBsbMemoContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("msg", msg);
                clipboard.setPrimaryClip(clip);
                ToastUtils.showToast(getActivity(), getString(R.string.toast_memo_content_copied));
            }
        });
        tvBsbMemoContent.setTag(entity);
    }


    private MainActivity.OnBackPressedListener mBackPressListener = new MainActivity.OnBackPressedListener() {
        @Override
        public boolean onBackPressed() {
            if (fabPosition != 0) {
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
                    if (fabPosition < 1) {
                        swipeTo(++fabPosition);
                    }
                } else if (velocityX < 0) {
                    if (fabPosition > -1) {
                        swipeTo(--fabPosition);
                    }
                }
            }
            return true;
        }


        @Override
        public void onShowPress(MotionEvent e) {
            if (fabPosition == 0)
                fabPressTriggered = true;
            super.onShowPress(e);
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_action:
                    if (fabWrapper.getTranslationX() == swipeTransDistanceX) {

                        reversed = sharedPref.getBoolean("pref_reverse", false);
                        if (fabPosition == -1) {
                            if (!reversed)
                                pushMemo();
                            else
                                pullMemo();
                        } else if (fabPosition == 1) {
                            if (!reversed)
                                pullMemo();
                            else
                                pushMemo();
                        }
                    }
                    if (fabPosition == 0 && fabWrapper.getTranslationX() == 0) {
                        String action = sharedPref.getString(PREF_KEY_CLICK, "click_push");
                        if (action.equals("click_push")) {
                            swipeTo(--fabPosition);
                        } else if (action.equals("click_pull")) {
                            swipeTo(++fabPosition);
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
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (fontMonaco == null) {
            fontMonaco = Typeface.createFromAsset(getContext().getAssets(), "Monaco.ttf");
        }
        this.setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_pushpull, container, false);
        coordiLayout = view.findViewById(R.id.coordi_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        bottomLayout = view.findViewById(R.id.bottom_sheet);
        fabSwipe = view.findViewById(R.id.fab_action);
        fabWrapper = view.findViewById(R.id.fab_wrapper);
        ivSwipeLeft = view.findViewById(R.id.iv_swipe_left);
        ivSwipeRight = view.findViewById(R.id.iv_swipe_right);
        tvSwipeHint = view.findViewById(R.id.tv_swipe_hint);
        edtMemo = view.findViewById(R.id.edt_memo2);
        foreground = view.findViewById(R.id.foreground);
        bottomWrapper = view.findViewById(R.id.bottom_wrapper);
        bottomInputWrapper = view.findViewById(R.id.rl_bottom_input_wrapper);
        bottomHeader = view.findViewById(R.id.bottom_header);
        bsbShadow = view.findViewById(R.id.bottom_sheet_shadow);
        mDetector = new GestureDetectorCompat(getActivity(), mFabGestureListener);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        setupView();
        if (memoAPI == null) {
            memoAPI = MemoService.getMemoAPI();
        }
        try {
            checkFRE();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null)
            mAdapter.expireItems();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            MemoEntity entity = (MemoEntity)tvBsbMemoContent.getTag();
            if (entity != null) {
                outState.putString("memo_id", entity.getId());
                tvBsbMemoContent.setTag(null);
            }
        }
        if (fabPosition != 0) {
            outState.putInt("fab_position", fabPosition);
            outState.putString("edit_content", edtMemo.getText().toString() + "");
            outState.putInt("edit_content_selection_start", edtMemo.getSelectionStart());
            outState.putInt("edit_content_selection_end", edtMemo.getSelectionEnd());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String memoId = savedInstanceState.getString("memo_id");
            if (!TextUtils.isEmpty(memoId)) {
                if (realm == null) {
                    realm = Realm.getDefaultInstance();
                }
                MemoEntity entity = realm.where(MemoEntity.class).equalTo("_id", memoId).findFirst();
                inflateBsb(entity);
            }
            int fabRestoredPosition = savedInstanceState.getInt("fab_position", 0);
            if (fabRestoredPosition != 0) {
                swipeTo(fabRestoredPosition);
                edtMemo.setText(savedInstanceState.getString("edit_content", ""));
                int startSelection = savedInstanceState.getInt("edit_content_selection_start", 0);
                int endSelection = savedInstanceState.getInt("edit_content_selection_end", 0);
                edtMemo.setSelection(startSelection, endSelection);
            }
        }
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
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (realm == null) {
                    realm = Realm.getDefaultInstance();
                }
                RealmResults<MemoEntity> results = realm.where(MemoEntity.class)
                        .contains("msg", newText, Case.INSENSITIVE)
                        .or().contains("_id", newText, Case.INSENSITIVE).findAll()
                        .sort("index");
                mAdapter.showQueryResult(results);
                Log.d(TAG, "search " + newText + " with result length " + results.size());
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mAdapter.leaveQueryMode();
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.enterQueryMode();
            }
        });
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            int foregroundVisibility;
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mAdapter.enterQueryMode();
                bottomInputWrapper.setVisibility(View.GONE);
                foregroundVisibility = foreground.getVisibility();
                foreground.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.e(TAG, "onMenuItemActionCollapse: ");
                mAdapter.leaveQueryMode();
                bottomInputWrapper.setVisibility(View.VISIBLE);
                foreground.setVisibility(foregroundVisibility);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_push_config) {
            PushConfigDialog dialog = new PushConfigDialog();
            dialog.show(getFragmentManager(), null);
            return true;
        } else if (id == R.id.action_settings) {
            oldNightMode = AppCompatDelegate.getDefaultNightMode();
            startActivityForResult(new Intent(getActivity(), SettingsActivity.class), REQUEST_SETTINGS);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTINGS) {
            if (sharedPref.getBoolean(PREF_KEY_REVERSE, false) != reversed) {
                swipeTo(0);
            }
            String align = sharedPref.getString(PREF_KEY_ALIGN, "align_center");

            if ("align_center".equals(align)) {
                if (fabPosition == -1)
                    edtMemo.setGravity(Gravity.CENTER);
                tvBsbMemoContent.setGravity(Gravity.CENTER);
            } else if ("align_left".equals(align)) {
                if (fabPosition == -1)
                    edtMemo.setGravity(Gravity.START);
                tvBsbMemoContent.setGravity(Gravity.START);

            }
            if (oldNightMode != AppCompatDelegate.getDefaultNightMode()) {
                oldNightMode = AppCompatDelegate.getDefaultNightMode();
                getActivity().recreate();
            }
        }
    }

    private void checkFRE() throws IOException {
        boolean isFRE = sharedPref.getBoolean(PREF_KEY_FRE, true);
        if (isFRE) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREF_KEY_FRE, false);
            editor.apply();
            InputStream inputStream = getActivity().getResources().openRawResource(R.raw.help);

            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }

            String jsonString = writer.toString();
            MemoEntity helpEntity = new Gson().fromJson(jsonString, MemoEntity.class);
            helpEntity.index.set(0);
            helpEntity.setCreatedDate(DateUtils.convertToMongoUTC(new Date()));
            if (mAdapter != null) {
                mAdapter.addMemo(helpEntity);
            }
        }
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
                return !(v.getAlpha() == 0);
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

                swipeDirectShown(event.getAction() == MotionEvent.ACTION_MOVE && fabPosition == 0);
                mDetector.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        oldX = event.getX();
                        oldY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        posX = event.getX();
                        posY = event.getY();
                        if (Math.abs(posX - oldX) > Math.abs(posY - oldY)) {
                            if (Math.abs(posX - oldX) > 90) {
                                if (fabPressTriggered) {
                                    if (posX > oldX) {
                                        if (fabPosition < 1) {
                                            swipeTo(++fabPosition);
                                        }
                                    } else if (posX < oldX) {
                                        if (fabPosition > -1) {
                                            swipeTo(--fabPosition);
                                        }
                                    }
                                    fabPressTriggered = false;
                                }
                            }
                        }
                }


                if (originX == 0) {
                    originX = v.getX();
                }
                return false;
            }
        });
        swipeDirectShown(true);
        fabSwipe.setOnClickListener(mClickListener);
    }

    private void swipeTo(int i) {
        fabPosition = i;
        if (i == 0) {
            edtMemo.setVisibility(View.GONE);
            edtMemo.setText("");
            edtMemo.clearComposingText();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null || !isAdded())
                        return;
                    foreground.setVisibility(View.GONE);
                    View view = getActivity().getCurrentFocus();
                    if (view == null && getView() != null) {
                        view = getView().getRootView();
                    }
                    if (view != null)
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }, 500);
            fabAnim(R.drawable.anim_add_to_swipe, R.drawable.ic_swipe);
        } else {
            swipeDirectShown(false);
            bottomHeader.setVisibility(View.VISIBLE);
            reversed = sharedPref.getBoolean(PREF_KEY_REVERSE, false);
            if ((i == 1) != reversed) {
                tvSwipeHint.setText(getString(R.string.input_area_create_a_pull));
                edtMemo.setHint(getString(R.string.input_area_hint_pull));
                edtMemo.setMaxEms(10);
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(10);
                edtMemo.setFilters(filters);
                edtMemo.setSingleLine(true);
                edtMemo.setGravity(Gravity.CENTER);
            } else {
                tvSwipeHint.setText(getString(R.string.input_area_create_a_push));
                edtMemo.setHint(getString(R.string.input_area_hint_push));
                edtMemo.setSingleLine(false);
                edtMemo.setMaxEms(Integer.MAX_VALUE);
                String align = sharedPref.getString(PREF_KEY_ALIGN, "align_center");
                edtMemo.setFilters(new InputFilter[0]);
                if ("align_center".equals(align)) {
                    edtMemo.setGravity(Gravity.CENTER);
                } else if ("align_left".equals(align)) {
                    edtMemo.setGravity(Gravity.START);
                }
            }
            fabAnim(R.drawable.anim_swipe_to_add, R.drawable.ic_add);
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
            ToastUtils.showToast(getActivity(), getString(R.string.toast_input_correct_memo));
        } else {
            fabSwipe.setClickable(false);
            int time = sharedPref.getInt(PREF_KEY_PUSH_EXPIRED_TIME, 1);
            int type = sharedPref.getInt(PREF_KEY_PUSH_EXPIRED_TYPE, 3);
            int count = sharedPref.getInt(PREF_KEY_PUSH_ACCESS_COUNT, 0);
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
            Call<CommonResponse> call = memoAPI.createMemo(entity);
            call.enqueue(new Callback<CommonResponse>() {

                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                    MemoEntity entity = response.body().getMemo();
                    entity.createdFromPush = true;
                    mAdapter.addMemo(entity);
                    if (getActivity() != null && isAdded()) {
                        swipeBackFromCheck();
                        fabWrapper.hide();
                    }
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    if (getActivity() != null && isAdded()) {
                        fabSwipe.setClickable(true);
                        fabWrapper.hide();
                        Log.e("Request", t.getLocalizedMessage());
                        ToastUtils.showToast(getActivity(), getString(R.string.toast_create_push_failed));
                    }
                }
            });
        }
    }

    private void pullMemo() {
        String memoId = edtMemo.getText().toString().trim();
        if (TextUtils.isEmpty(memoId)) {
            ToastUtils.showToast(getActivity(), getString(R.string.toast_input_correct_id));
        } else {
            if (mAdapter.checkExistMeme(memoId)) {
                swipeBackFromCheck();
                return;
            }
            fabSwipe.setClickable(false);
            fabWrapper.show();
            Call<CommonResponse> call = memoAPI.readMemo(memoId.trim());
            call.enqueue(new Callback<CommonResponse>() {

                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                    if (response.body().getMemo() == null) {
                        if (getActivity() != null && isAdded()) {
                            ToastUtils.showToast(getActivity(), response.body().getMsg());
                        fabSwipe.setClickable(true);
                        }
                    } else {
                        mAdapter.addMemo(response.body().getMemo());
                        if (getActivity() != null && isAdded())
                            swipeBackFromCheck();
                    }
                    if (getActivity() != null && isAdded())
                        fabWrapper.hide();
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    if (getActivity() != null && isAdded()) {
                        fabSwipe.setClickable(true);
                        fabWrapper.hide();
                        Log.e("Request", t.getLocalizedMessage());
                        ToastUtils.showToast(getActivity(), getString(R.string.toast_create_pull_failed));
                    }
                }
            });
        }
    }

    private void swipeBackFromCheck() {
        fabPosition = 0;
        fabAnim(R.drawable.anim_add_to_check, R.drawable.ic_check);
        fabSwipe.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null || !isAdded()) {
                    return;
                }
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
                View view = getActivity().getCurrentFocus();
                if (view == null && getView() != null) {
                    view = getView().getRootView();
                }
                if (view != null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        }, 500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null || !isAdded()) {
                    return;
                }
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
                fabAnim(R.drawable.anim_check_to_swipe, R.drawable.ic_swipe);
                fabSwipe.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fabSwipe.setClickable(true);
            }
        }, 1000);
    }

    private void fabAnim(int animId, int fallbackResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fabSwipe.setImageResource(animId);
            Drawable drawable = fabSwipe.getDrawable();
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        } else {
            fabSwipe.setImageResource(fallbackResId);
        }

    }

    private AnimatorSet animatorSet;

    private void swipeDirectShown(boolean isShown) {
        if (isShown) {
            if (animatorSet!= null && animatorSet.isStarted()) {
                return;
            }
            ivSwipeLeft.setVisibility(View.VISIBLE);
            ivSwipeRight.setVisibility(View.VISIBLE);

            if (animatorSet == null) {
                ObjectAnimator animatorLeftX = ObjectAnimator.ofFloat(ivSwipeLeft, "translationX", -80);
                ObjectAnimator animatorRightX = ObjectAnimator.ofFloat(ivSwipeRight, "translationX", 80);
                ObjectAnimator animatorLeftA = ObjectAnimator.ofFloat(ivSwipeLeft, "alpha", 0, 1, 0);
                ObjectAnimator animatorRightA = ObjectAnimator.ofFloat(ivSwipeRight, "alpha", 0, 1, 0);

                animatorLeftX.setRepeatCount(ValueAnimator.INFINITE);
                animatorRightX.setRepeatCount(ValueAnimator.INFINITE);
                animatorLeftA.setRepeatCount(ValueAnimator.INFINITE);
                animatorRightA.setRepeatCount(ValueAnimator.INFINITE);

                animatorLeftX.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorRightX.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorLeftA.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorRightA.setInterpolator(new AccelerateDecelerateInterpolator());

                ObjectAnimator[] objectAnimators = new ObjectAnimator[4];
                objectAnimators[0] = animatorLeftX;
                objectAnimators[1] = animatorRightX;
                objectAnimators[2] = animatorLeftA;
                objectAnimators[3] = animatorRightA;
                animatorSet = new AnimatorSet();
                animatorSet.playTogether(objectAnimators);
                animatorSet.setDuration(2000);
            }
            animatorSet.start();
        } else {
            ivSwipeLeft.setVisibility(View.GONE);
            ivSwipeRight.setVisibility(View.GONE);
            animatorSet.cancel();
        }
    }

    @Override
    public void goPushState() {
        swipeTo(-1);
    }

    @Override
    public void goPushState(String s) {
        Log.d(TAG, "goPushState: " + s);
        swipeTo(-1);
        if (edtMemo != null) {
            edtMemo.setText(s);
        }
    }

    @Override
    public void goPullState() {
        swipeTo(1);
    }
}
