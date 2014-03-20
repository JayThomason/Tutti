package com.stanford.tutti;
 
import java.util.HashMap;
import java.util.List;
 
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Context _context;
    
    // These are the "header" strings for the expandable list view;
    // i.e., the top-level categories you click to expand/hide the associated children. 
    private List<String> _listDataHeader; 
    
    // Map from headers to lists of children strings.  
    // Use to retrieve the list of children that appear when clicking on a given header. 
    private HashMap<String, List<String>> _listDataChild;
 
    
    public ExpandableListAdapter(Context context, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }
 
	/*
	 * Return the child UI element at the given group position
	 * (position of the group header in listDataHeader) and 
	 * child position (position of the selected child in
	 * listDataChild.get(headerName) ).  
	 * 
	 * @return Object child
	 */
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }
 
	/*
	 * This method is essentially useless; it just returns the 
	 * second argument it is passed, childPosition. 
	 * However, it needs to be implemented for the 
	 * ExpandableListAdapter interface.  
	 * 
	 * @return long ID
	 */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
	/*
	 * Gets the View associated with the given child element. 
	 * 
	 * @return View view
	 */
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
        final String childText = (String) getChild(groupPosition, childPosition);
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
 
        txtListChild.setText(childText);
        return convertView;
    }
 
	/*
	 * Gets the number of children associated with the 
	 * header at the given position. 
	 * 
	 * @return int childrenCount
	 */
    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }
 
	/*
	 * Gets the header String at the given index. 
	 * 
	 * @return Object string
	 */
    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }
 
	/*
	 * Gets the total number of headers in the list view. 
	 * 
	 * @return int headerCount
	 */
    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }
 
	/*
	 * Useless method that needs to be implemented 
	 * for the ExpandableListView interface. 
	 * 
	 * @return long groupPosition
	 */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
	/*
	 * Gets the View associated with the given header group element. 
	 * 
	 * @return View view
	 */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
 
        return convertView;
    } 
    
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
	/*
	 * Checks whether the child at the given header/child index is selectable
	 * (i.e., whether the associated header is currently expanded, making the child visible). 
	 * 
	 * @return boolean isSelectable
	 */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}