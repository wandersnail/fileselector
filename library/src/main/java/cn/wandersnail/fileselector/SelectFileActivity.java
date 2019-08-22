package cn.wandersnail.fileselector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cn.wandersnail.commons.util.ColorUtils;
import cn.wandersnail.commons.util.FileUtils;
import cn.wandersnail.commons.util.ShellUtils;
import cn.wandersnail.commons.util.SystemUtils;
import cn.wandersnail.commons.util.UiUtils;
import cn.wandersnail.commons.util.entity.Storage;

/**
 * date: 2019/8/8 16:51
 * author: zengfansheng
 */
public class SelectFileActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final int PERMISSON_REQUESTCODE = 10;

    public static final String EXTRA_SELECTION_MODE = "SELECTION_MODE";
    public static final String EXTRA_SELECTOR_HASH = "SELECTOR_HASH";
    public static final String EXTRA_IS_MULTI_SELECT = "IS_MULTI_SELECT";
    public static final String EXTRA_IS_LANDSCAPE = "SCREEN_ORIENTATION";
    public static final String EXTRA_ROOT = "ROOT";
    public static final String EXTRA_SELECTED_FILE_PATH_LIST = "SELECTED_FILE_LIST";
    public static final String EXTRA_TITLE = "TITLE";
    public static final String EXTRA_THEME_COLORS = "THEME_COLORS";
    public static final String EXTRA_LANGUAGE = "LANGUAGE";
    public static final String EXTRA_SHOW_HIDDEN_FILES = "SHOW_HIDDEN_FILES";
    static FilenameFilter filenameFilter;
    
    private int selectionMode = FileSelector.FILES_ONLY;
    private boolean isMultiSelect;
    private List<int[]> posList = new ArrayList<>();
    private List<Item> itemList = new ArrayList<>();
    private List<Item> selectItemList = new ArrayList<>();
    private boolean isSelectedAll;
    private File rootFile;
    private FileListAdapter adapter;
    private SelectedItemDialog selectedItemDialog;
    private String currentPath;//当前路径
    private List<File> rootFiles = new ArrayList<>();
    private String title;//标题
    int[] themeColors;
    private String selectorHash;
    TextHolder textHolder = new TextHolder();
    private boolean showHiddenFiles;//设置是否显示隐藏文件和文件夹
    private View fsStatusBar;
    private RelativeLayout fsLayoutTitle;
    private ImageView fsIvBack;
    private TextView fsTvTitle;
    private ImageView fsIvMore;
    private ImageView fsIvAll;
    private TextView fsTvRoot;
    private HorizontalScrollView fsScrollView;
    private LinearLayout fsDirContainer;
    private ListView fsLv;
    private TextView fsTvCancel;
    private TextView fsTvSelected;
    private TextView fsTvOk;
    private View fsMaskView;

    private void assignViews() {
        fsStatusBar = findViewById(R.id.fsStatusBar);
        fsLayoutTitle = findViewById(R.id.fsLayoutTitle);
        fsIvBack = findViewById(R.id.fsIvBack);
        fsTvTitle = findViewById(R.id.fsTvTitle);
        fsIvMore = findViewById(R.id.fsIvMore);
        fsIvAll = findViewById(R.id.fsIvAll);
        fsTvRoot = findViewById(R.id.fsTvRoot);
        fsScrollView = findViewById(R.id.fsScrollView);
        fsDirContainer = findViewById(R.id.fsDirContainer);
        fsLv = findViewById(R.id.fsLv);
        fsTvCancel = findViewById(R.id.fsTvCancel);
        fsTvSelected = findViewById(R.id.fsTvSelected);
        fsTvOk = findViewById(R.id.fsTvOk);
        fsMaskView = findViewById(R.id.fsMaskView);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        //沉浸状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDataFromIntent();
        setContentView(R.layout.fs_activity_select_file);
        //先检查写权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || 
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSON_REQUESTCODE);
        } else {
            assignViews();
            initViews();
            initEvents();
        }
    }
    
    private void getDataFromIntent() {
        int language = getIntent().getIntExtra(EXTRA_LANGUAGE, -1);
        if (language != -1) {
            Language[] languages = Language.values();
            for (Language lan : languages) {
                if (lan.value == language) {
                    textHolder.language = lan;
                    break;
                }
            }
        }
        selectionMode = getIntent().getIntExtra(EXTRA_SELECTION_MODE, FileSelector.FILES_ONLY);
        isMultiSelect = getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECT, false);
        if (getIntent().getBooleanExtra(EXTRA_IS_LANDSCAPE, false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        rootFile = (File) getIntent().getSerializableExtra(EXTRA_ROOT);
        if (rootFile == null) {
            if (ShellUtils.hasRootPermission()) {
                rootFile = new File("/");
            }
        }
        title = getIntent().getStringExtra(EXTRA_TITLE);
        int[] themeColors = getIntent().getIntArrayExtra(EXTRA_THEME_COLORS);
        if (themeColors == null) {
            this.themeColors = new int[]{Utils.getPrimaryColor(this, ContextCompat.getColor(this, R.color.fsColorPrimary)), Utils.getPrimaryDarkColor(this, ContextCompat.getColor(this, R.color.fsColorPrimaryDark))};
        } else {
            this.themeColors = themeColors;
        }
        showHiddenFiles = getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);
        selectorHash = getIntent().getStringExtra(EXTRA_SELECTOR_HASH);
    }
    
    private Comparator<Item> itemComparator = (o1, o2) -> {
        if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        return o1.file.getName().compareToIgnoreCase(o2.file.getName());
    };

    //检测是否所有的权限都已经授权
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (verifyPermissions(grantResults)) {
                initViews();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        filenameFilter = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!posList.isEmpty()) {
            View child = fsDirContainer.getChildAt(fsDirContainer.getChildCount() - 1);
            TextView tv = child.findViewById(R.id.fsTv);
            DirCell cell = (DirCell) tv.getTag();
            fsDirContainer.removeView(child);
            if (rootFiles.isEmpty()) {
                loadFiles(cell.location.getParentFile());
            } else {
                boolean isSecondLast = false;
                for (File file : rootFiles) {
                    if (cell.location == file) {
                        isSecondLast = true;
                        loadFiles(null);
                        break;
                    }
                }
                if (!isSecondLast) {
                    loadFiles(cell.location.getParentFile());
                }
            }
            int[] ints = posList.remove(posList.size() - 1);
            fsLv.setSelectionFromTop(ints[0], ints[1]);
        } else if (rootFile == null && currentPath != null) {
            loadFiles(null);
        } else {
            super.onBackPressed();
        }
    }
    
    private void addDir(File file) {
        View view = getLayoutInflater().inflate(R.layout.fs_dir_view, null);
        TextView tv = view.findViewById(R.id.fsTv);
        int childCount = fsDirContainer.getChildCount();
        tv.setTag(new DirCell(childCount, file));
        tv.setText(file.getName());
        tv.setOnClickListener(v -> {
            DirCell cell = (DirCell) v.getTag();
            //把当前之后的移除
            int count = fsDirContainer.getChildCount();
            if (cell.index < count - 1) {
                fsDirContainer.removeViews(cell.index + 1, count - cell.index - 1);
            }
            //清除之后的位置记录
            int[] ints = new int[2];
            for (int i = posList.size() -1; i >= 0; i--) {
                if (cell.index < i) {
                    ints = posList.remove(posList.size() -1);
                } else {
                    break;
                }
            }
            //更新列表
            loadFiles(cell.location);
            fsLv.setSelectionFromTop(ints[0], ints[1]);
        });
        fsDirContainer.addView(view);
    }
    
    private void initViews() {
        fsTvRoot.setText(textHolder.getText(TextHolder.ROOT));
        fsTvCancel.setText(textHolder.getText(TextHolder.CANCEL));
        fsTvOk.setText(textHolder.getText(TextHolder.OK));
        ViewGroup.LayoutParams params = fsStatusBar.getLayoutParams();
        params.height = UiUtils.getStatusBarHeight();
        fsStatusBar.setLayoutParams(params);
        if (title == null) {
            fsTvTitle.setText(textHolder.getText(TextHolder.ALL_FILES));
        } else {
            fsTvTitle.setText(title);
        }
        fsTvOk.setTextColor(ColorUtils.createColorStateList(themeColors[0], themeColors[0], ContextCompat.getColor(this, R.color.fsDisable)));
        fsTvOk.setEnabled(false);
        updateSelectedText();
        selectedItemDialog = new SelectedItemDialog(this);
        adapter = new FileListAdapter(this, itemList);
        fsLv.setAdapter(adapter);
        fsLv.setOnItemClickListener(this);
        fsLv.setOnItemLongClickListener(this);
        loadFiles(rootFile);
    }
        
    private void initEvents() {       
        fsIvBack.setOnClickListener(v -> onBackPressed());
        fsIvMore.setOnClickListener(v -> showPopupWindow());
        fsIvAll.setOnClickListener(v -> switchSelectAll(!isSelectedAll));
        fsTvSelected.setOnClickListener(v -> {
            selectedItemDialog.updateList(selectItemList);
            selectedItemDialog.show();
        });
        fsTvRoot.setOnClickListener(v -> {
            if (rootFile != null) {
                if (fsDirContainer.getChildCount() > 0) {
                    loadFiles(rootFile);
                    int[] ints = posList.remove(0);
                    fsLv.setSelectionFromTop(ints[0], ints[1]);
                    posList.clear();
                    fsDirContainer.removeAllViews();
                }
            } else {
                loadFiles(null);
                posList.clear();
                fsDirContainer.removeAllViews();
            }
        });
        fsTvCancel.setOnClickListener(v -> finish());
        fsTvOk.setOnClickListener(v -> {
            Intent intent = new Intent();
            ArrayList<String> pathList = new ArrayList<>();
            if (isMultiSelect) {
                for (Item item : selectItemList) {
                    pathList.add(item.file.getAbsolutePath());
                }
                if (pathList.isEmpty() && currentPath != null) {
                    pathList.add(currentPath);
                }
            } else {
                if (!selectItemList.isEmpty()) {
                    pathList.add(selectItemList.get(0).file.getAbsolutePath());
                } else if (currentPath != null) {
                    pathList.add(currentPath);
                }
            }
            intent.putExtra(EXTRA_SELECTED_FILE_PATH_LIST, pathList);
            intent.putExtra(EXTRA_SELECTOR_HASH, selectorHash);
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
    }
    
    private void loadFiles(File dir) {
        currentPath = dir == null ? null : dir.getAbsolutePath();
        itemList.clear();
        ArrayList<Item> dirList = new ArrayList<>();
        ArrayList<Item> fList = new ArrayList<>();
        if (dir == null) {
            rootFiles.clear();
            List<Storage> list = SystemUtils.getStorages(this);
            if (list != null) {
                for (Storage storage : list) {
                    File file = new File(storage.getPath());
                    rootFiles.add(file);
                    dirList.add(new Item(file, isSelectedItem(file), storage.getDescription()));
                }
            }
        } else {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (showHiddenFiles && file.getName().startsWith(".") && ((file.isDirectory() && selectionMode != FileSelector.FILES_ONLY) ||
                            (file.isFile() && selectionMode != FileSelector.DIRECTORIES_ONLY))) {
                        handleFileList(file, dirList, fList);
                    } else if (!file.getName().startsWith(".") && (filenameFilter == null || filenameFilter.accept(dir, file.getName()))) {
                        handleFileList(file, dirList, fList);
                    }
                }
            }
        }
        Collections.sort(dirList, itemComparator);
        Collections.sort(fList, itemComparator);
        itemList.addAll(dirList);
        itemList.addAll(fList);
        RelativeLayout.LayoutParams ivAllLp = (RelativeLayout.LayoutParams) fsIvAll.getLayoutParams();
        if (dir == null) {
            fsIvMore.setVisibility(View.GONE);
            ivAllLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        } else {
            fsIvMore.setVisibility(View.VISIBLE);
            ivAllLp.removeRule(RelativeLayout.ALIGN_PARENT_END);
        }
        fsIvAll.setLayoutParams(ivAllLp);
        //只有多选，并且当选择文件时，文件列表不为空，当选择文件夹时，文件夹列表不为空
        if (isMultiSelect && (selectionMode == FileSelector.FILES_ONLY && fList.size() > 0 ||
                (selectionMode == FileSelector.DIRECTORIES_ONLY && dirList.size() > 0) ||
                (selectionMode == FileSelector.FILES_AND_DIRECTORIES && (fList.size() > 0 || dirList.size() > 0)))) {
            fsIvAll.setVisibility(View.VISIBLE);
        } else {
            fsIvAll.setVisibility(View.INVISIBLE);
        }
        if (fsIvAll.getVisibility() == View.VISIBLE && selectItemList.containsAll(itemList)) {
            switchSelectAllState(true);
        }
        File file = new File(currentPath == null ? "" : currentPath);
        fsTvOk.setEnabled(!selectItemList.isEmpty() || selectionMode != FileSelector.FILES_ONLY && file.exists());
        adapter.notifyDataSetChanged();
        fsScrollView.post(() -> fsScrollView.fullScroll(ScrollView.FOCUS_RIGHT));
    }
    
    private void handleFileList(File file, List<Item> dirList, List<Item> fList) {
        if (selectionMode == FileSelector.DIRECTORIES_ONLY) {
            if (file.isDirectory()) {
                dirList.add(new Item(file, isSelectedItem(file)));
            }
        } else {
            if (file.isDirectory()) {
                dirList.add(new Item(file, isSelectedItem(file)));
            } else {
                fList.add(new Item(file, isSelectedItem(file)));
            }
        }
    }
    
    private boolean isSelectedItem(File file) {
        for (Item item : selectItemList) {
            if (item.file.equals(file)) {
                return true;
            }
        }
        return false;
    }
    
    private void switchSelectAll(boolean enable) {
        switchSelectAllState(enable);
        for (Item item : itemList) {
            //只全选指定类型
            if (selectionMode == FileSelector.FILES_AND_DIRECTORIES || (selectionMode == FileSelector.DIRECTORIES_ONLY &&
                    item.file.isDirectory()) || (selectionMode == FileSelector.FILES_ONLY && item.file.isFile()) ||
            (currentPath == null && selectionMode != FileSelector.FILES_ONLY)) {
                item.checked = enable;
                updateSelectedFileList(item, false);
            }
        }
        updateViews();
    }
    
    private void switchSelectAllState(boolean selectAll) {
        isSelectedAll = selectAll;
        fsIvAll.setVisibility(isMultiSelect ? View.VISIBLE : View.INVISIBLE);
        if (isSelectedAll) {
            fsIvAll.setColorFilter(themeColors[0]);
        } else {
            fsIvAll.clearColorFilter();
        }
    }
    
    private void updateSelectedText() {
        fsTvSelected.setText(String.format(textHolder.getText(TextHolder.VIEW_SELECTED_PATTERN), selectItemList.size()));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = itemList.get(position);
        if (item.file.isDirectory() || currentPath == null) {
            switchSelectAllState(false);
            loadFiles(item.file);
            //记录点击条目所在文件夹的文件列表滚动到的位置
            posList.add(new int[]{fsLv.getFirstVisiblePosition(), fsLv.getChildAt(0).getTop()});
            //添加导航文件夹
            addDir(item.file);
            //进入的时候重置回到顶端位置
            fsLv.setSelectionFromTop(0, 0);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (currentPath != null) {
            final Item item = itemList.get(position);
            ArrayList<String> menuItems = new ArrayList<>();
            menuItems.add(textHolder.getText(TextHolder.RENAME));
            menuItems.add(textHolder.getText(TextHolder.DELETE));
            new ActionDialog(this, menuItems, position1 -> {
                switch(position1) {
                    case 0:	
                        showInputDialog(textHolder.getText(TextHolder.RENAME), item.file.getName(), 
                                null, text -> handleRenameFile(text, item));
                        break;
                    case 1:	
                        new AlertDialog.Builder(SelectFileActivity.this)
                                .setMessage(textHolder.getText(TextHolder.ENSURE_DELETE_PROMPT))
                                .setNegativeButton(textHolder.getText(TextHolder.CANCEL), null)
                                .setPositiveButton(textHolder.getText(TextHolder.OK), (dialog, which) -> {
                                    if (item.file.isFile()) {
                                        item.file.delete();
                                    } else {
                                        FileUtils.deleteDir(item.file);
                                    }
                                    loadFiles(new File(currentPath));
                                }).show();
                        break;
                }
            }).show();
        }
        return true;
    }

    private void handleRenameFile(String text, Item item) {
        File file = new File(currentPath, text);
        if (!file.exists()) {
            boolean b = item.file.renameTo(file);
            if (b) {
                Toast.makeText(this, textHolder.getText(TextHolder.RENAME_SUCCESS), 
                        Toast.LENGTH_SHORT).show();
                loadFiles(new File(currentPath));
            } else {
                Toast.makeText(this, textHolder.getText(TextHolder.RENAME_FAILED), 
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, textHolder.getText(TextHolder.RENAME_FAILED), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private interface InputCallback {
        void onInput(String text);
    }
        
    private void showInputDialog(String title, String fill, String hint, final InputCallback callback) {
        FrameLayout layout = new FrameLayout(this);
        final EditText et = new EditText(this);
        if (!TextUtils.isEmpty(fill)) {
            et.setText(fill);
            et.setSelection(fill.length());
        }
        if (!TextUtils.isEmpty(hint)) {
            et.setHint(hint);
        }
        int padding = UiUtils.dp2px(8f);
        layout.setPadding(padding, 0, padding, 0);
        layout.addView(et);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setNegativeButton(textHolder.getText(TextHolder.CANCEL), null)
                .setPositiveButton(textHolder.getText(TextHolder.OK), (dialog, which) -> {
                    if (et.getText() != null && !et.getText().toString().trim().isEmpty()) {
                        callback.onInput(et.getText().toString().trim());
                    }
                }).show();
    }

    private class FileListAdapter extends BaseListAdapter<Item> {
        FileListAdapter(Context context, List<Item> data) {
            super(context, data);
        }

        @Override
        BaseHolder<Item> getHolder(int position) {
            return new ViewHolder() {
                @Override
                void setData(Item data, int position) {
                    chkView.setTag(position);
                    if (currentPath == null) {
                        tvName.setText(data.desc);
                    } else {
                        tvName.setText(data.file.getName());
                    }
                    String path = data.file.getAbsolutePath();
                    if (data.file.isDirectory() || currentPath == null) {
                        //选择文件时，不可点击，不显示选框
                        chkView.setClickable(selectionMode != FileSelector.FILES_ONLY);
                        ivSelect.setVisibility(selectionMode == FileSelector.FILES_ONLY ? View.INVISIBLE : View.VISIBLE);
                        int num = 0;
                        //如果是选择文件夹，文件不计数    
                        File[] files = data.file.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (showHiddenFiles && file.getName().startsWith(".") && ((file.isDirectory() && selectionMode != FileSelector.FILES_ONLY) ||
                                        (file.isFile() && selectionMode != FileSelector.DIRECTORIES_ONLY))) {
                                    num++;
                                } else if (!file.getName().startsWith(".") && (filenameFilter == null || filenameFilter.accept(data.file, file.getName()))) {
                                    num++;
                                }
                            }
                        }
                        tvDesc.setText(String.format(textHolder.getText(num > 1 ? TextHolder.MULTI_ITEM_PATTERN : TextHolder.SINGLE_ITEM_PATTERN), num));
                        Glide.with(context).load(R.drawable.fs_ic_folder).into(iv);
                    } else {
                        chkView.setClickable(selectionMode != FileSelector.DIRECTORIES_ONLY);
                        ivSelect.setVisibility(selectionMode != FileSelector.DIRECTORIES_ONLY ? View.VISIBLE : View.INVISIBLE);
                        tvDesc.setText(FileUtils.formatFileSize(data.file.length()));
                        if (Utils.isApk(path)) {
                            Glide.with(context).load(Utils.getApkThumbnail(context, path)).into(iv);
                        } else if (Utils.isImage(path) || Utils.isVideo(path)) {
                            Glide.with(context).setDefaultRequestOptions(new RequestOptions()
                                    .error(R.drawable.fs_file)).load(path).into(iv);                            
                        } else if (Utils.isAudio(path)) {
                            Glide.with(context).load(R.drawable.fs_audio).into(iv);
                        } else if (Utils.isText(path)) {
                            Glide.with(context).load(R.drawable.fs_text).into(iv);
                        } else if (Utils.isPdf(path)) {
                            Glide.with(context).load(R.drawable.fs_pdf).into(iv);
                        } else if (Utils.isExcel(path)) {
                            Glide.with(context).load(R.drawable.fs_excel).into(iv);
                        } else if (Utils.isWord(path)) {
                            Glide.with(context).load(R.drawable.fs_word).into(iv);
                        } else if (Utils.isPPT(path)) {
                            Glide.with(context).load(R.drawable.fs_ppt).into(iv);
                        } else if (Utils.isZip(path)) {
                            Glide.with(context).load(R.drawable.fs_zip).into(iv);
                        } else if (Utils.isFlash(path)) {
                            Glide.with(context).load(R.drawable.fs_flash).into(iv);
                        } else if (Utils.isPs(path)) {
                            Glide.with(context).load(R.drawable.fs_ps).into(iv);
                        } else if (Utils.isHtml(path)) {
                            Glide.with(context).load(R.drawable.fs_html).into(iv);
                        } else if (Utils.isDeveloper(path)) {
                            Glide.with(context).load(R.drawable.fs_developer).into(iv);
                        } else {
                            Glide.with(context).load(R.drawable.fs_file).into(iv);
                        }
                    }
                    ivSelect.setSelected(data.checked);
                    if (data.checked) {
                        ivSelect.setColorFilter(themeColors[0]);
                    } else {
                        ivSelect.setColorFilter(Color.LTGRAY);
                    }
                }

                @Override
                View createConvertView() {
                    View view = View.inflate(context, R.layout.fs_file_item_view, null);
                    tvName = view.findViewById(R.id.fsTvName);
                    tvDesc = view.findViewById(R.id.fsTvDesc);
                    iv = view.findViewById(R.id.iv);
                    ivSelect = view.findViewById(R.id.fsIvSelect);
                    view.findViewById(R.id.fsivDel).setVisibility(View.INVISIBLE);
                    chkView = view.findViewById(R.id.fsChkView);
                    chkView.setOnClickListener(v -> {
                        int position1 = (int) chkView.getTag();
                        Item item = getItem(position1);
                        item.checked = !item.checked;
                        updateSelectedFileList(item, true);
                    });
                    return view;
                }
            };
        }
    }
    
    void clearSelectedFileList() {
        for (Item item : selectItemList) {
            item.checked = false;
        }
        selectItemList.clear();
        switchSelectAllState(false);
        updateViews();
    }
    
    private void updateViews() {
        File file = new File(currentPath == null ? "" : currentPath);
        fsTvOk.setEnabled(!selectItemList.isEmpty() || (selectionMode != FileSelector.FILES_ONLY && file.exists()));
        updateSelectedText();
        adapter.notifyDataSetChanged();
        selectedItemDialog.updateList(selectItemList);
    }
    
    void updateSelectedFileList(Item item, boolean needNotify) {
        if (item.checked) {
            if (!selectItemList.contains(item)) {
                //如果是单选，把已选的删除
                if (!isMultiSelect && !selectItemList.isEmpty()) {
                    Item removeItem = selectItemList.remove(0);
                    for (Item i : itemList) {
                        if (i == removeItem) {
                            i.checked = false;
                            break;
                        }
                    }
                }
                selectItemList.add(item);
            }
        } else {
            selectItemList.remove(item);
        }
        if (needNotify) {
            updateViews();
            boolean b = true;
            for (Item it : itemList) {
                if ((selectionMode == FileSelector.FILES_AND_DIRECTORIES || (selectionMode == FileSelector.FILES_ONLY && it.file.isFile()) ||
                (selectionMode == FileSelector.DIRECTORIES_ONLY && it.file.isDirectory())) && !selectItemList.contains(it)) {
                    b = false;
                }
            }
            switchSelectAllState(b);
        }
    }
    
    private void showPopupWindow() {
        ListView lv = (ListView) View.inflate(this, R.layout.fs_listview, null);
        ArrayList<String> items = new ArrayList<>();
        items.add(textHolder.getText(TextHolder.NEW_FOLDER));
        items.add(textHolder.getText(showHiddenFiles ? TextHolder.DONOT_SHOW_HIDDEN_FILES : TextHolder.SHOW_HIDDEN_FILES));
        lv.setAdapter(new PopupMenuAdapter(this, items));
        int height = items.size() * UiUtils.dp2px(50f) + items.size();
        final PopupWindow popupWindow = new PopupWindow(lv, UiUtils.getDisplayScreenWidth(), height);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.fs_popun_menu_bg));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);    
        popupWindow.setOnDismissListener(() -> {
            //pop消失，去掉蒙层
            fsMaskView.clearAnimation();
            fsMaskView.setVisibility(View.GONE);
        });
        lv.setOnItemClickListener((parent, view, position, id) -> {
            popupWindow.dismiss();
            switch(position) {
                case 0:	
                    showInputDialog(textHolder.getText(TextHolder.NEW_FOLDER), null, null, text -> {
                        File file = new File(currentPath, text);
                        //不存在才新建
                        if (!file.exists()) {
                            if (file.mkdir()) {
                                String msg = textHolder.getText(TextHolder.FOLDER_CREATE_SUCCESS);
                                Toast.makeText(view.getContext(), msg, Toast.LENGTH_SHORT).show();
                                loadFiles(new File(currentPath));
                            } else {
                                String msg = textHolder.getText(TextHolder.FOLDER_CREATE_FAILED);
                                Toast.makeText(view.getContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    break;
                case 1:
                    showHiddenFiles = !showHiddenFiles;
                    loadFiles(currentPath == null ? null : new File(currentPath));
                    break;
            }
        });
        //显示蒙层
        fsMaskView.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
        fsMaskView.startAnimation(alphaAnimation);
        popupWindow.showAsDropDown(fsLayoutTitle, 0, 1);
    }
    
    private class PopupMenuAdapter extends BaseListAdapter<String> {
        PopupMenuAdapter(Context context, List<String> data) {
            super(context, data);
        }

        @Override
        BaseHolder<String> getHolder(int position) {
            return new BaseHolder<String>() {
                TextView tv;
                
                @Override
                void setData(String data, int position) {
                    if (position == 1) {
                        tv.setText(textHolder.getText(showHiddenFiles ? TextHolder.DONOT_SHOW_HIDDEN_FILES : TextHolder.SHOW_HIDDEN_FILES));
                    } else {
                        tv.setText(data);
                    }
                }

                @Override
                View createConvertView() {
                    View view = View.inflate(context, R.layout.fs_item_popup_menu, null);
                    tv = view.findViewById(R.id.fsTv);
                    return view;
                }
            };
        }
    }
}
