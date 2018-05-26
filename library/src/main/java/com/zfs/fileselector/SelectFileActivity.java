package com.zfs.fileselector;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zeng on 2017/3/1.
 */

public class SelectFileActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String EXTRA_IS_SELECT_FILE = "IS_SELECT_FILE";
    private static final String EXTRA_IS_MULTI_SELECT = "IS_MULTI_SELECT";
    private static final String EXTRA_SCREEN_ORIENTATION = "SCREEN_ORIENTATION";
    private static final String EXTRA_ROOT_DIR = "ROOT_DIR";
    public static final String EXTRA_SELECTED_FILE_LIST = "SELECTED_FILE_LIST";
    public static final String EXTRA_SELECTED_FILE = "SELECTED_FILE";

    private ListView lv;
    private TextView tvSelected;
    private LinearLayout dirContainer;
    private HorizontalScrollView scrollView;

    private boolean isSlectFile;
    private boolean isMultiSelect;
    private static FilenameFilter filenameFilter;
    private List<int[]> posList = new ArrayList<>();
    private List<Item> itemList = new ArrayList<>();
    private List<Item> selectItemList = new ArrayList<>();
    private boolean isSelectedAll;
    private File rootFile;
    private FileListAdapter adapter;
    private SelectedItemDialog selectedItemDialog;
    private TextView tvOk;

    /**
     * 启动Activity
     *
     * @param activity          用于启动Activity的上下文
     * @param screenOrientation 屏幕方向
     * @param requestCode       请求码
	 * @param rootDir          根目录                   
     * @param isSelectFile      是否选择文件
     * @param isMultiSelect     是否多选。只支持文件选择
     * @param filenameFilter    文件过滤器
     */
    public static void startForResult(Activity activity, int screenOrientation, int requestCode, File rootDir, boolean isSelectFile,
                                      boolean isMultiSelect, FilenameFilter filenameFilter) {
        SelectFileActivity.filenameFilter = filenameFilter;
        Intent intent = new Intent(activity, SelectFileActivity.class);
        intent.putExtra(EXTRA_SCREEN_ORIENTATION, screenOrientation);
        intent.putExtra(EXTRA_IS_SELECT_FILE, isSelectFile);
        intent.putExtra(EXTRA_IS_MULTI_SELECT, isMultiSelect);
        if (rootDir != null) {
			intent.putExtra(EXTRA_ROOT_DIR, rootDir);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动Activity
     *
     * @param fragment          用于启动Activity的上下文
     * @param screenOrientation 屏幕方向
     * @param requestCode       请求码
	 * @param rootDir          根目录   
     * @param isSelectFile      是否选择文件
     * @param isMultiSelect     是否多选。只支持文件选择
     * @param filenameFilter    文件过滤器
     */
    public static void startForResult(Fragment fragment, int screenOrientation, int requestCode, File rootDir, boolean isSelectFile,
                                      boolean isMultiSelect, FilenameFilter filenameFilter) {
        SelectFileActivity.filenameFilter = filenameFilter;
        Intent intent = new Intent(fragment.getActivity(), SelectFileActivity.class);
        intent.putExtra(EXTRA_SCREEN_ORIENTATION, screenOrientation);
        intent.putExtra(EXTRA_IS_SELECT_FILE, isSelectFile);
        intent.putExtra(EXTRA_IS_MULTI_SELECT, isMultiSelect);
		if (rootDir != null) {
			intent.putExtra(EXTRA_ROOT_DIR, rootDir);
		}
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataFromIntent();
        setContentView(R.layout.activity_select_file);        
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        filenameFilter = null;
    }

    @Override
    public void onBackPressed() {
        if (!posList.isEmpty()) {
            View child = dirContainer.getChildAt(dirContainer.getChildCount() - 1);
            TextView tv = child.findViewById(R.id.tv);
            DirCell cell = (DirCell) tv.getTag();
            dirContainer.removeView(child);
            loadFiles(cell.location.getParentFile());
            int[] ints = posList.remove(posList.size() - 1);
            lv.setSelectionFromTop(ints[0], ints[1]);
        } else {
            super.onBackPressed();
        }
    }

    private void addDir(File file) {
        View view = getLayoutInflater().inflate(R.layout.dir_view, null);        
        TextView tv = view.findViewById(R.id.tv);
        int childCount = dirContainer.getChildCount();
        tv.setTag(new DirCell(childCount, file));
        tv.setText(file.getName());
        tv.setOnClickListener(this);
        dirContainer.addView(view);
    }    
    
    private void initViews() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.all_files);
        dirContainer = findViewById(R.id.dirContainer);
        scrollView = findViewById(R.id.scrollView);
        TextView tvRoot = findViewById(R.id.tvRoot);
        lv = findViewById(R.id.lv);
        tvSelected = findViewById(R.id.tvSelected);
        tvSelected.setOnClickListener(this);
        tvOk = findViewById(R.id.tvOk);
        tvOk.setOnClickListener(this);
        tvOk.setEnabled(false);
        updateSelectedText();
        selectedItemDialog = new SelectedItemDialog(this);
        adapter = new FileListAdapter();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);     
        tvRoot.setOnClickListener(this);
        loadFiles(rootFile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.menuSelecteAll);
        item.setVisible(isMultiSelect);
        item.setTitle(isSelectedAll ? R.string.all_not_select : R.string.select_all);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSelecteAll) {
            switchSelectAll(!isSelectedAll);
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private void getDataFromIntent() {
        isSlectFile = getIntent().getBooleanExtra(EXTRA_IS_SELECT_FILE, false);
        isMultiSelect = getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECT, false);
        int orientation = getIntent().getIntExtra(EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        rootFile = (File) getIntent().getSerializableExtra(EXTRA_ROOT_DIR);
		if (rootFile == null) {
			if (ShellUtils.hasRootPermission()) {
				rootFile = new File("/");
			} else {
				rootFile = Environment.getExternalStorageDirectory();
			}
		}
    }

    private void loadFiles(File dir) {
        itemList.clear();
        List<File> dirList = new ArrayList<>();
        List<File> fList = new ArrayList<>();
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
                        dirList.add(file);
                    } else {
                        fList.add(file);
                    }
                } else {
                    if (file.isDirectory()) {
                        dirList.add(file);
                    }
                }
            }
			Collections.sort(dirList, comparator);
			Collections.sort(fList, comparator);
            addFileList(dirList);
            addFileList(fList);
        }
        adapter.notifyDataSetChanged();
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_RIGHT);
            }
        });
    }

	private Comparator<File> comparator = new Comparator<File>() {
		@Override
		public int compare(File o1, File o2) {
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	};
    
    //生成Item添加到列表
    private void addFileList(List<File> files) {
        for (File file : files) {
            itemList.add(new Item(file, isSelectedItem(file.getAbsolutePath())));
        }
    }

    //是否已被选
    private boolean isSelectedItem(String path) {
        for (Item item : selectItemList) {
            if (item.file.getAbsolutePath().equals(path)) {
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
            if ((item.file.isDirectory() && !isSlectFile) || (item.file.isFile() && isSlectFile)) {
                item.checked = enable;
                updateSelectedFileList(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    //清除全选状态，更新标题栏按钮文本
    private void switchSelectAllState(boolean selectAll) {
        isSelectedAll = selectAll;
        invalidateOptionsMenu();
    }

    private void updateSelectedText() {
        tvSelected.setText(String.format(getString(R.string.selected_pattern), selectItemList.size()));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvSelected) {
            selectedItemDialog.updateList(selectItemList);
            selectedItemDialog.show();
        } else if (v.getId() == R.id.tvOk) {            
            Intent intent = new Intent();
            if (isMultiSelect) {
                ArrayList<File> fileList = new ArrayList<>();
                for (Item item : selectItemList) {
                    fileList.add(item.file);
                }
                intent.putExtra(EXTRA_SELECTED_FILE_LIST, fileList);
            } else {
                intent.putExtra(EXTRA_SELECTED_FILE, selectItemList.get(0).file);
            }
            setResult(RESULT_OK, intent);
            finish();
        } else if (v.getId() == R.id.tv) {
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
        } else if (v.getId() == R.id.tvRoot) {
            if (dirContainer.getChildCount() > 0) {
                loadFiles(rootFile);
                int[] ints = posList.remove(0);
                lv.setSelectionFromTop(ints[0], ints[1]);
                posList.clear();
                dirContainer.removeAllViews();
            }
        }
    }    
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = itemList.get(position);
        if (item.file.isDirectory()) {
            switchSelectAllState(false);
            //记录点击条目所在文件夹的文件列表滚动到的位置
            posList.add(new int[]{lv.getFirstVisiblePosition(), lv.getChildAt(0).getTop()});
            //添加导航文件夹
            addDir(item.file);
            loadFiles(item.file);
            //进入的时候重置回到顶端位置
            lv.setSelectionFromTop(0, 0);
        } else if (isSlectFile) {
            item.checked = !item.checked;
            ((CheckBox) view.findViewById(R.id.chkBox)).setChecked(item.checked);
            updateSelectedFileList(item);
        }
    }

    private class FileListAdapter extends BaseAdapter {

        FileListAdapter() {
            ImageLoader.getInstance().setDefautImageResoure(R.drawable.file);
            ImageLoader.getInstance().setLoadErrorImageResoure(R.drawable.file);
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
                convertView = View.inflate(SelectFileActivity.this, R.layout.item_view, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.tvName = convertView.findViewById(R.id.tvName);
                holder.tvDesc = convertView.findViewById(R.id.tvDesc);
                holder.iv = convertView.findViewById(R.id.iv);
                holder.chkBox = convertView.findViewById(R.id.chkBox);
                convertView.findViewById(R.id.ivDel).setVisibility(View.INVISIBLE);
                holder.chkView = convertView.findViewById(R.id.chkView);
                holder.chkView.setTag(holder);
                holder.chkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewHolder h = (ViewHolder) v.getTag();
                        Item item = getItem(h.position);
                        item.checked = !h.chkBox.isChecked();
                        updateSelectedFileList(item);
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Item item = getItem(position);
            holder.position = position;
            holder.tvName.setText(item.file.getName());
            String path = item.file.getAbsolutePath();
            if (item.file.isDirectory()) {
                //选择文件时，不可点击，不显示选框
                holder.chkView.setClickable(!isSlectFile);
                holder.chkBox.setVisibility(isSlectFile ? View.INVISIBLE : View.VISIBLE);
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
                holder.tvDesc.setText(String.format(getString(num > 1 ? R.string.multi_item_pattern : R.string.single_item_pattern), num));
                holder.iv.setImageResource(R.drawable.folder);
            } else {
                holder.chkView.setClickable(false);
                holder.chkBox.setVisibility(isSlectFile ? View.VISIBLE : View.INVISIBLE);
                holder.tvDesc.setText(Utils.formatFileSize(item.file.length()));
                if (Utils.isApk(path) || Utils.isImage(path) || Utils.isVideo(path)) {
                    ImageLoader.getInstance().loadImage(path, holder.iv);
                } else if (Utils.isAudio(path)) {
                    holder.iv.setImageResource(R.drawable.audio);
                } else if (Utils.isText(path)) {
                    holder.iv.setImageResource(R.drawable.text);
                } else if (Utils.isPdf(path)) {
                    holder.iv.setImageResource(R.drawable.pdf);
                } else if (Utils.isExcel(path)) {
                    holder.iv.setImageResource(R.drawable.excel);
                } else if (Utils.isWord(path)) {
                    holder.iv.setImageResource(R.drawable.word);
                } else if (Utils.isPPT(path)) {
                    holder.iv.setImageResource(R.drawable.ppt);
                } else if (Utils.isZip(path)) {
                    holder.iv.setImageResource(R.drawable.zip);
                } else if (Utils.isFlash(path)) {
                    holder.iv.setImageResource(R.drawable.flash);
                } else if (Utils.isPs(path)) {
                    holder.iv.setImageResource(R.drawable.ps);
                } else if (Utils.isHtml(path)) {
                    holder.iv.setImageResource(R.drawable.html);
                } else if (Utils.isDeveloper(path)) {
                    holder.iv.setImageResource(R.drawable.developer);
                } else {
                    holder.iv.setImageResource(R.drawable.file);
                }
            }
            holder.chkBox.setChecked(item.checked);
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
        tvOk.setEnabled(!selectItemList.isEmpty());
        adapter.notifyDataSetChanged();
        updateSelectedText();
        selectedItemDialog.updateList(selectItemList);
    }

    public void updateSelectedFileList(Item item) {
        if (item.checked) {
            if (!selectItemList.contains(item)) {
                //如果是单选，把已选的删除
                if (!isMultiSelect && !selectItemList.isEmpty()) {
                    selectItemList.remove(0).checked = false;                    
                }
                selectItemList.add(item);
            }
        } else {
            selectItemList.remove(item);
        }
        updateViews();
        int count = 0;
        for (Item i : itemList) {
            if (i.file.isFile()) {
                count++;
            }
        }
        if (count > 0 && selectItemList.size() == count) {
            switchSelectAllState(true);
        } else {
            switchSelectAllState(false);
        }
    }
}
