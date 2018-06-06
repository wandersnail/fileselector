package cn.zfs.fileselector;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zeng on 2017/3/1.
 */

public class SelectFileActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    static final String EXTRA_IS_SELECT_FILE = "IS_SELECT_FILE";
    static final String EXTRA_IS_MULTI_SELECT = "IS_MULTI_SELECT";
    static final String EXTRA_IS_LANDSCAPE = "SCREEN_ORIENTATION";
    static final String EXTRA_ROOT = "ROOT";
    static final String EXTRA_SELECTED_FILE_PATH_LIST = "SELECTED_FILE_LIST";
    static final String EXTRA_FILENAME_FILTER = "FILENAME_FILTER";

    private ListView lv;
    private TextView tvSelected;
    private LinearLayout dirContainer;
    private HorizontalScrollView scrollView;
    private TextView tvAll;

    private boolean isSlectFile;
    private boolean isMultiSelect;
    private MyFilter filenameFilter;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getDataFromIntent();
        setContentView(R.layout.activity_select_file);        
        initViews();
    }

    @Override
    public void onBackPressed() {
        if (!posList.isEmpty()) {
            View child = dirContainer.getChildAt(dirContainer.getChildCount() - 1);
            TextView tv = child.findViewById(R.id.tv);
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
            actionBar.hide();
        }
        View statusBar = findViewById(R.id.statusBar);
        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
        params.height = Utils.getStatusBarHeight(this);
        statusBar.setLayoutParams(params);
        dirContainer = findViewById(R.id.dirContainer);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvAll = findViewById(R.id.tvAll);
        findViewById(R.id.ivBack).setOnClickListener(this);
        tvAll.setOnClickListener(this);
        scrollView = findViewById(R.id.scrollView);
        TextView tvRoot = findViewById(R.id.tvRoot);
        lv = findViewById(R.id.lv);
        tvTitle.setText(R.string.all_files);
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

    private void getDataFromIntent() {
        isSlectFile = getIntent().getBooleanExtra(EXTRA_IS_SELECT_FILE, false);
        isMultiSelect = getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECT, false);
        FilenameFilter filter = getIntent().getParcelableExtra(EXTRA_FILENAME_FILTER);
        if (filter != null) {
            filenameFilter = new MyFilter(filter);
        }
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
    }

    private static class MyFilter implements java.io.FilenameFilter {
        private FilenameFilter filter;

        MyFilter(FilenameFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean accept(File dir, String name) {
            return filter.accept(dir, name);
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
        
        //只有多选，并且当选择文件时，文件列表不为空，当选择文件夹时，文件夹列表不为空
        tvAll.setVisibility(isMultiSelect && ((isSlectFile && !fList.isEmpty()) || (!isSlectFile && !dirList.isEmpty())) ? View.VISIBLE : View.INVISIBLE);
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
                updateSelectedFileList(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    //清除全选状态，更新标题栏按钮文本
    private void switchSelectAllState(boolean selectAll) {
        isSelectedAll = selectAll;
        tvAll.setVisibility(isMultiSelect ? View.VISIBLE : View.INVISIBLE);
        tvAll.setText(isSelectedAll ? R.string.all_not_select : R.string.select_all);
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
            ArrayList<String> pathList = new ArrayList<>();
            if (isMultiSelect) {
                for (Item item : selectItemList) {
                    pathList.add(item.file.getAbsolutePath());
                }
                intent.putExtra(EXTRA_SELECTED_FILE_PATH_LIST, pathList);
            } else {
                pathList.add(selectItemList.get(0).file.getAbsolutePath());
                intent.putExtra(EXTRA_SELECTED_FILE_PATH_LIST, pathList);
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
        } else if (v.getId() == R.id.tvAll) {
            switchSelectAll(!isSelectedAll);
        } else if (v.getId() == R.id.ivBack) {
            onBackPressed();
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
            ((CheckBox) view.findViewById(R.id.chkBox)).setChecked(item.checked);
            updateSelectedFileList(item);
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
                convertView = View.inflate(SelectFileActivity.this, R.layout.file_item_view, null);
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
            if (currentPath == null) {
                holder.tvName.setText(item.desc);
            } else {
                holder.tvName.setText(item.file.getName());
            }
            String path = item.file.getAbsolutePath();
            if (item.file.isDirectory() || currentPath == null) {
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
                holder.iv.setImageResource(R.drawable.fs_folder);
            } else {
                holder.chkView.setClickable(false);
                holder.chkBox.setVisibility(isSlectFile ? View.VISIBLE : View.INVISIBLE);
                holder.tvDesc.setText(Utils.formatFileSize(item.file.length()));
                if (Utils.isApk(path) || Utils.isImage(path) || Utils.isVideo(path)) {
                    ImageLoader.getInstance().loadImage(path, holder.iv);
                } else if (Utils.isAudio(path)) {
                    holder.iv.setImageResource(R.drawable.fs_audio);
                } else if (Utils.isText(path)) {
                    holder.iv.setImageResource(R.drawable.fs_text);
                } else if (Utils.isPdf(path)) {
                    holder.iv.setImageResource(R.drawable.fs_pdf);
                } else if (Utils.isExcel(path)) {
                    holder.iv.setImageResource(R.drawable.fs_excel);
                } else if (Utils.isWord(path)) {
                    holder.iv.setImageResource(R.drawable.fs_word);
                } else if (Utils.isPPT(path)) {
                    holder.iv.setImageResource(R.drawable.fs_ppt);
                } else if (Utils.isZip(path)) {
                    holder.iv.setImageResource(R.drawable.fs_zip);
                } else if (Utils.isFlash(path)) {
                    holder.iv.setImageResource(R.drawable.fs_flash);
                } else if (Utils.isPs(path)) {
                    holder.iv.setImageResource(R.drawable.fs_ps);
                } else if (Utils.isHtml(path)) {
                    holder.iv.setImageResource(R.drawable.fs_html);
                } else if (Utils.isDeveloper(path)) {
                    holder.iv.setImageResource(R.drawable.fs_developer);
                } else {
                    holder.iv.setImageResource(R.drawable.fs_file);
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
