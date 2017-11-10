package vitdube.com.vidtube;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingjia.zhang on 29/10/17.
 */

public class CustomArrayAdapter extends BaseExpandableListAdapter {

    Context context;
    List<Video> videos;

    public CustomArrayAdapter(Context context, int resource, List<Video> items) {
        this.context = context;
        this.videos = items;
    }

    @Override
    public int getGroupCount() {
        return this.videos.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return this.videos.get(i).getClips().size();
    }

    @Override
    public Object getGroup(int i) {
        return this.videos.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.videos.get(i).getClips().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int position, boolean isExapnded, View view, ViewGroup viewGroup) {
        Video p =  this.videos.get(position);
        View v = view;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            v = vi.inflate(R.layout.video_list_item, null);
        }

        if (p != null) {
            TextView videoNameView = (TextView) v.findViewById(R.id.video_name);
            videoNameView.setText(p.getName());
            Button uploadButton = (Button) v.findViewById(R.id.upload_button);
            uploadButton.setOnClickListener(new Uploader(this.context, p.getName(), p));
            if (p.hasBeenUploaded()) {
                uploadButton.setClickable(false);
                uploadButton.setText("Uploaded");
            }
        }
        return v;
    }

    @Override
    public View getChildView(int parentPosition, int childPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
        VideoClip chunk = this.videos.get(parentPosition).getClips().get(childPosition);
        View v = view;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            v = vi.inflate(R.layout.db_list_item, null);
        }
        TextView chunkNameView = v.findViewById(R.id.chunk_info);
        String uploaded = chunk.getUploaded() == 0 ? "Not uploaded." : "Uploaded";
        chunkNameView.setText(chunk.getChunkId() + ": " + chunk.getFilePath() + " - " + uploaded);

        return v;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    public void indicateFailure(String msg) {
        Toast.makeText(this.context, msg, Toast.LENGTH_SHORT).show();
    }

}