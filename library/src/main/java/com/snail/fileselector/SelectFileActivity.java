package com.snail.fileselector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zeng on 2017/3/1.
 */

public class SelectFileActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final int PERMISSON_REQUESTCODE = 10;
    
    static final String EXTRA_IS_SELECT_FILE = "IS_SELECT_FILE";
    static final String EXTRA_IS_MULTI_SELECT = "IS_MULTI_SELECT";
    static final String EXTRA_IS_LANDSCAPE = "SCREEN_ORIENTATION";
    static final String EXTRA_ROOT = "ROOT";
    static final String EXTRA_SELECTED_FILE_PATH_LIST = "SELECTED_FILE_LIST";
    static final String EXTRA_TITLE = "TITLE";
    static final String EXTRA_THEME_COLORS = "THEME_COLORS";

    private ListView lv;
    private TextView tvSelected;
    private LinearLayout dirContainer;
    private HorizontalScrollView scrollView;
    private TextView tvAll;

    private boolean isSlectFile;
    private boolean isMultiSelect;
    static FilenameFilter filenameFilter;
    private List<int[]> posList = new ArrayList<>();
    private List<Item> itemList = new ArrayList<>();
    private List<Item> selectItemList = new ArrayList<>();
    private boolean isSelectedAll;
    private File rootFile;
    private FileListAdapter adapter;
    private SelectedItemDialog selectedItemDialog;
    private TextView tvOk;
    private String currentPath;//当前路径
    private List<File> rootFiles = new ArrayList<>();
    private String title;//标题
    private int[] themeColors;
    private View layoutTitle;
    private PopupWindow popupWindow;
    private View maskView;
    private View ivMore;

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
            initViews();
        }
    }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (verifyPermissions(paramArrayOfInt)) {
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
            View child = dirContainer.getChildAt(dirContainer.getChildCount() - 1);
            TextView tv = child.findViewById(R.id.fstv);
            DirCell cell = (DirCell) tv.getTag();
            dirContainer.removeView(child);
            if (rootFiles.isEmpty()) {
                loadFiles(cell.location.getParentFile());
            } else {
                boolean isSecondLast = false;
                for (File file : rootFiles) {
                    if (cell.location.equals(file)) {
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
            lv.setSelectionFromTop(ints[0], ints[1]);
        } else if (rootFile == null && currentPath != null) {
            loadFiles(null);
        } else {
            super.onBackPressed();
        }
    }

    public int[] getThemeColors() {
        return themeColors;
    }
    
    private void addDir(File file) {
        View view = getLayoutInflater().inflate(R.layout.fs_dir_view, null);
        TextView tv = view.findViewById(R.id.fstv);
        int childCount = dirContainer.getChildCount();
        tv.setTag(new DirCell(childCount, file));
        tv.setText(file.getName());
        tv.setOnClickListener(this);
        dirContainer.addView(view);
    }

    private void initViews() {
        View statusBar = findViewById(R.id.fsstatusBar);
        statusBar.setBackgroundColor(themeColors[0]);
        maskView = findViewById(R.id.fsMaskView);
        layoutTitle = findViewById(R.id.fslayoutTitle);
        layoutTitle.setBackgroundColor(themeColors[0]);
        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
        params.height = Utils.getStatusBarHeight(this);
        statusBar.setLayoutParams(params);
        dirContainer = findViewById(R.id.fsdirContainer);
        TextView tvTitle = findViewById(R.id.fstvTitle);
        tvAll = findViewById(R.id.fstvAll);
        findViewById(R.id.fsivBack).setOnClickListener(this);
        ivMore = findViewById(R.id.fsivMore);
        ivMore.setOnClickListener(this);
        tvAll.setOnClickListener(this);
        scrollView = findViewById(R.id.fsscrollView);
        TextView tvRoot = findViewById(R.id.fstvRoot);
        lv = findViewById(R.id.fslv);
        if (title == null) {
            tvTitle.setText(R.string.fs_all_files);
        } else {
            tvTitle.setText(title);
        }
        tvSelected = findViewById(R.id.fstvSelected);
        tvSelected.setBackground(Utils.getFrameBlueBg(this, themeColors[0]));
        tvSelected.setTextColor(Utils.createColorStateList(themeColors[0], Color.WHITE));
        tvSelected.setOnClickListener(this);
        tvOk = findViewById(R.id.fstvOk);
        TextView tvCancel = findViewById(R.id.fstvCancel);
        tvOk.setBackground(Utils.getFillBlueBg(this, themeColors));
        tvCancel.setBackground(Utils.getFillGrayBg(this));
        tvOk.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvOk.setEnabled(false);
        updateSelectedText();
        selectedItemDialog = new SelectedItemDialog(this);
        adapter = new FileListAdapter();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        tvRoot.setOnClickListener(this);
        loadFiles(rootFile);
    }

    private void getDataFromIntent() {
        isSlectFile = getIntent().getBooleanExtra(EXTRA_IS_SELECT_FILE, false);
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
            this.themeColors = new int[] {Utils.getPrimaryColor(this, ContextCompat.getColor(this, R.color.fsColorPrimary)),
                    Utils.getPrimaryColor(this, ContextCompat.getColor(this, R.color.fsColorPrimaryDark))};
        } else {
            this.themeColors = themeColors;
        }
    }

    private void loadFiles(File dir) {
        currentPath = dir == null ? null : dir.getAbsolutePath();
        itemList.clear();
        List<Item> dirList = new ArrayList<>();
        List<Item> fList = new ArrayList<>();
        if (dir == null) {
            rootFiles.clear();
            ArrayList<Storage> list = Utils.getStorages(this);
            if (list != null) {
                for (Storage storage : list) {
                    File file = new File(storage.path);
                    rootFiles.add(file);
                    dirList.add(new Item(storage.description, file, isSelectedItem(file)));
                }
            }
        } else {
            File[] files;
            if (filenameFilter != null) {
                files = dir.listFiles(filenameFilter);
            } else {
                files = dir.listFiles();
            }
            if (files != null) {
                for (File file : files) {
                    if (isSlectFile) {
                        if (file.isDirectory()) {
                            dirList.add(new Item(file, isSelectedItem(file)));
                        } else {
                            fList.add(new Item(file, isSelectedItem(file)));
                        }
                    } else {
                        if (file.isDirectory()) {
                            dirList.add(new Item(file, isSelectedItem(file)));
                        }
                    }
                }
            }
        }
        Collections.sort(dirList, comparator);
        Collections.sort(fList, comparator);
        itemList.addAll(dirList);
        itemList.addAll(fList);

        RelativeLayout.LayoutParams tvAllLp = (RelativeLayout.LayoutParams) tvAll.getLayoutParams();
        if (dir == null) {
            ivMore.setVisibility(View.GONE);
            tvAllLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        } else {
            ivMore.setVisibility(View.VISIBLE);
            tvAllLp.removeRule(RelativeLayout.ALIGN_PARENT_END);
        }
        tvAll.setLayoutParams(tvAllLp);
        //只有多选，并且当选择文件时，文件列表不为空，当选择文件夹时，文件夹列表不为空
        tvAll.setVisibility(isMultiSelect && ((isSlectFile && !fList.isEmpty()) || (!isSlectFile && !dirList.isEmpty())) ? View.VISIBLE : View.INVISIBLE);
        if (tvAll.getVisibility() == View.VISIBLE && selectItemList.containsAll(itemList)) {
            switchSelectAllState(true);
        }
        File file = new File(currentPath == null ? "" : currentPath);
        tvOk.setEnabled(!selectItemList.isEmpty() || (!isSlectFile && file.exists()));
        
        adapter.notifyDataSetChanged();
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_RIGHT);
            }
        });
    }

    private Comparator<Item> comparator = new Comparator<Item>() {
        @Override
        public int compare(Item o1, Item o2) {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.file.getName().compareToIgnoreCase(o2.file.getName());
        }
    };

    //是否已被选
    private boolean isSelectedItem(File file) {
        for (Item item : selectItemList) {
            if (item.file.equals(file)) {
                return true;
            }
        }
        return false;
    }

    //改变全选或全不选
    private void switchSelectAll(boolean enable) {
        switchSelectAllState(enable);
        for (Item item : itemList) {
            //只全选指定类型
            if ((item.file.isDirectory() && !isSlectFile) || (item.file.isFile() && isSlectFile) || (currentPath == null && !isSlectFile)) {
                item.checked = enable;
                updateSelectedFileList(item, false);
            }
        }
        updateViews();
    }

    //清除全选状态，更新标题栏按钮文本
    private void switchSelectAllState(boolean selectAll) {
        isSelectedAll = selectAll;
        tvAll.setVisibility(isMultiSelect ? View.VISIBLE : View.INVISIBLE);
        tvAll.setText(isSelectedAll ? R.string.fs_all_not_select : R.string.fs_select_all);
    }

    private void updateSelectedText() {
        tvSelected.setText(String.format(getString(R.string.fs_selected_pattern), selectItemList.size()));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fstvSelected) {
            selectedItemDialog.updateList(selectItemList);
            selectedItemDialog.show();
        } else if (v.getId() == R.id.fstvOk) {
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
            setResult(RESULT_OK, intent);
            finish();
        } else if (v.getId() == R.id.fstvCancel) {
            finish();
        } else if (v.getId() == R.id.fstv) {
            DirCell cell = (DirCell) v.getTag();
            //把当前之后的移除
            int childCount = dirContainer.getChildCount();
            if (cell.index < childCount - 1) {
                dirContainer.removeViews(cell.index + 1, childCount - cell.index - 1);
            }
            //清除之后的位置记录
            int[] ints = new int[2];
            for (int i = posList.size() - 1; i >= 0; i--) {
                if (cell.index < i) {
                    ints = posList.remove(i);
                } else {
                    break;
                }
            }
            //更新列表
            loadFiles(cell.location);
            lv.setSelectionFromTop(ints[0], ints[1]);
        } else if (v.getId() == R.id.fstvRoot) {
            if (rootFile != null) {
                if (dirContainer.getChildCount() > 0) {
                    loadFiles(rootFile);
                    int[] ints = posList.remove(0);
                    lv.setSelectionFromTop(ints[0], ints[1]);
                    posList.clear();
                    dirContainer.removeAllViews();
                }
            } else {
                loadFiles(null);
                posList.clear();
                dirContainer.removeAllViews();
            }
        } else if (v.getId() == R.id.fstvAll) {
            switchSelectAll(!isSelectedAll);
        } else if (v.getId() == R.id.fsivBack) {
            onBackPressed();
        } else if (v.getId() == R.id.fsivMore) {
            showPopupWindow();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = itemList.get(position);
        if (item.file.isDirectory() || currentPath == null) {
            switchSelectAllState(false);
            loadFiles(item.file);
            //记录点击条目所在文件夹的文件列表滚动到的位置
            posList.add(new int[]{lv.getFirstVisiblePosition(), lv.getChildAt(0).getTop()});
            //添加导航文件夹
            addDir(item.file);
            //进入的时候重置回到顶端位置
            lv.setSelectionFromTop(0, 0);
        } else if (isSlectFile) {
            item.checked = !item.checked;
            view.findViewById(R.id.fsivSelect).setSelected(item.checked);
            updateSelectedFileList(item, true);
        }
    }

    private class FileListAdapter extends BaseAdapter {

        FileListAdapter() {
            ImageLoader.getInstance().setDefautImageResoure(R.drawable.fs_file);
            ImageLoader.getInstance().setLoadErrorImageResoure(R.drawable.fs_file);
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Item getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(SelectFileActivity.this, R.layout.fs_file_item_view, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.tvName = convertView.findViewById(R.id.fstvName);
                holder.tvDesc = convertView.findViewById(R.id.fstvDesc);
                holder.iv = convertView.findViewById(R.id.iv);
                holder.ivSelect = convertView.findViewById(R.id.fsivSelect);
                convertView.findViewById(R.id.fsivDel).setVisibility(View.INVISIBLE);
                holder.chkView = convertView.findViewById(R.id.fschkView);
                holder.chkView.setTag(holder);
                holder.chkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewHolder h = (ViewHolder) v.getTag();
                        Item item = getItem(h.position);
                        item.checked = !item.checked;
                        updateSelectedFileList(item, true);
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Item item = getItem(position);
            holder.position = position;
            if (currentPath == null) {
                holder.tvName.setText(item.desc);
            } else {
                holder.tvName.setText(item.file.getName());
            }
            String path = item.file.getAbsolutePath();
            if (item.file.isDirectory() || currentPath == null) {
                //选择文件时，不可点击，不显示选框
                holder.chkView.setClickable(!isSlectFile);
                holder.ivSelect.setVisibility(isSlectFile ? View.INVISIBLE : View.VISIBLE);
                File[] files;
                if (filenameFilter != null) {
                    files = item.file.listFiles(filenameFilter);
                } else {
                    files = item.file.listFiles();
                }
                int num = 0;
                //如果是选择文件夹，文件不计数
                if (files != null) {
                    if (isSlectFile) {
                        num = files.length;
                    } else {
                        for (File f : files) {
                            if (f.isDirectory()) {
                                num++;
                            }
                        }
                    }
                }
                holder.tvDesc.setText(String.format(getString(num > 1 ? R.string.fs_multi_item_pattern : R.string.fs_single_item_pattern), num));
                ImageLoader.getInstance().loadImage(R.drawable.fs_folder, holder.iv);
            } else {
                holder.chkView.setClickable(false);
                holder.ivSelect.setVisibility(isSlectFile ? View.VISIBLE : View.INVISIBLE);
                holder.tvDesc.setText(Utils.formatFileSize(item.file.length()));
                if (Utils.isApk(path) || Utils.isImage(path) || Utils.isVideo(path)) {
                    ImageLoader.getInstance().loadImage(path, holder.iv);
                } else if (Utils.isAudio(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_audio, holder.iv);
                } else if (Utils.isText(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_text, holder.iv);
                } else if (Utils.isPdf(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_pdf, holder.iv);
                } else if (Utils.isExcel(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_excel, holder.iv);
                } else if (Utils.isWord(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_word, holder.iv);
                } else if (Utils.isPPT(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_ppt, holder.iv);
                } else if (Utils.isZip(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_zip, holder.iv);
                } else if (Utils.isFlash(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_flash, holder.iv);
                } else if (Utils.isPs(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_ps, holder.iv);
                } else if (Utils.isHtml(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_html, holder.iv);
                } else if (Utils.isDeveloper(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_developer, holder.iv);
                } else {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_file, holder.iv);
                }
            }
            holder.ivSelect.setSelected(item.checked);
            if (item.checked) {
                holder.ivSelect.setColorFilter(themeColors[0]);
            } else {
                holder.ivSelect.setColorFilter(Color.LTGRAY);
            }
            return convertView;
        }
    }

    public void clearSelectedFileList() {
        for (Item item : selectItemList) {
            item.checked = false;
        }
        selectItemList.clear();
        switchSelectAllState(false);
        updateViews();
    }

    private void updateViews() {
        File file = new File(currentPath == null ? "" : currentPath);
        tvOk.setEnabled(!selectItemList.isEmpty() || (!isSlectFile && file.exists()));
        updateSelectedText();
        adapter.notifyDataSetChanged();
        selectedItemDialog.updateList(selectItemList);
    }

    public void updateSelectedFileList(Item item, boolean needNotify) {
        if (item.checked) {
            if (!selectItemList.contains(item)) {
                //如果是单选，把已选的删除
                if (!isMultiSelect && !selectItemList.isEmpty()) {
                    Item removeItem = selectItemList.remove(0);
                    for (Item i : itemList) {
                        if (i.equals(removeItem)) {
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
            for (Item i : itemList) {
                if (((isSlectFile && i.file.isFile()) || (!isSlectFile && i.file.isDirectory())) && !selectItemList.contains(i)) {
                    b = false;
                }
            }
            if (b) {
                switchSelectAllState(true);
            } else {
                switchSelectAllState(false);
            }
        }        
    }
    
    private void showPopupWindow() {
        if (popupWindow == null) {
            ListView lv = (ListView) View.inflate(this, R.layout.fs_listview, null);
            List<String> items = new ArrayList<>();
            items.add(getString(R.string.fs_new_folder));
            lv.setAdapter(new PopupMenuAdapter(this, items));
            popupWindow = new PopupWindow(lv, Utils.getDisplayScreenWidth(this), Utils.dp2px(this, 50));
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.fs_popun_menu_bg));
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    //pop消失，去掉蒙层
                    maskView.setVisibility(View.GONE);
                }
            });
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupWindow.dismiss();
                    //显示设置文件夹名称对话框
                    final FrameLayout layout = new FrameLayout(SelectFileActivity.this);
                    final EditText et = new EditText(SelectFileActivity.this);
                    int padding = Utils.dp2px(SelectFileActivity.this, 8);
                    layout.setPadding(padding, 0, padding, 0);
                    layout.addView(et);
                    new AlertDialog.Builder(SelectFileActivity.this)
                            .setTitle(R.string.fs_new_folder)
                            .setView(layout)
                            .setNegativeButton(R.string.fs_cancel, null)
                            .setPositiveButton(R.string.fs_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (et.getText() != null && !et.getText().toString().trim().isEmpty()) {
                                        String dirName = et.getText().toString().trim();
                                        File file = new File(currentPath, dirName);
                                        //不存在才新建
                                        if (!file.exists()) {
                                            if (file.mkdir()) {
                                                Toast.makeText(SelectFileActivity.this, R.string.fs_folder_create_success, Toast.LENGTH_SHORT).show();
                                                loadFiles(new File(currentPath));
                                            } else {
                                                Toast.makeText(SelectFileActivity.this, R.string.fs_folder_create_fail, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                            }).show();
                }
            });
        }
        //显示蒙层
        maskView.setVisibility(View.VISIBLE);
        popupWindow.showAsDropDown(layoutTitle);
    }

    private class PopupMenuAdapter extends BaseListAdapter<String> {
        PopupMenuAdapter(@NonNull Context context, @NonNull List<String> data) {
            super(context, data);
        }

        @Override
        protected BaseHolder<String> getHolder(int position) {
            return new BaseHolder<String>() {
                private TextView tv;
                
                @Override
                protected void setData(@NonNull String data, int position) {
                    tv.setText(data);
                }

                @Override
                protected View createConvertView() {
                    View view = View.inflate(getContext(), R.layout.fs_item_popup_menu, null);
                    tv = view.findViewById(R.id.fstv);
                    return view;
                }
            };
        }
    }
}
