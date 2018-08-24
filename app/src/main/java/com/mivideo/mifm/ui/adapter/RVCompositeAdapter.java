package com.mivideo.mifm.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RVCompositeAdapter extends RecyclerView.Adapter {

    private static final int INVALID_COUNT = -1;

    private Context mContext;
    private ArrayList<AdapterData> mAdapterList = new ArrayList<AdapterData>();
    // private int mViewTypeIndex = 0;
    private HashMap<RecyclerView.Adapter, ForwardingDataSetObserver> mObservers
            = new HashMap<RecyclerView.Adapter, ForwardingDataSetObserver>();
    private int mCachedItemCount = INVALID_COUNT;

    public RVCompositeAdapter() {
    }

    public RVCompositeAdapter(Context context) {
        this.mContext = context;
    }

    public class AdapterData {
        public final RecyclerView.Adapter mAdapter;
        public int mLocalPosition = 0;
        public Set<Integer> mViewTypesSet = new HashSet<Integer>();

        public AdapterData(RecyclerView.Adapter adapter) {
            mAdapter = adapter;
        }
    }

    List<AdapterData> getAdapterList() {
        return mAdapterList;
    }

    /**
     * 添加 Adapter
     */
    public <T extends RecyclerView.Adapter> void addAdapter(T adapter) {
        addAdapter(mAdapterList.size(), adapter);
    }

    /**
     * Add the given adapter to the list of merged adapters at the given index.
     */
    public <T extends RecyclerView.Adapter> void addAdapter(int index, T adapter) {
        mAdapterList.add(index, new AdapterData(adapter));

        ForwardingDataSetObserver observer = new ForwardingDataSetObserver(adapter);
        mObservers.put(adapter, observer);

        adapter.registerAdapterDataObserver(observer);

        if (adapter.getItemCount() > 0) {
            notifyDataSetChanged();
        }
    }

    /**
     * Remove the given adapter from the list of merged adapters.
     */
    public <T extends RecyclerView.Adapter> void removeAdapter(T adapter) {
        if (!mAdapterList.contains(adapter)) return;
        removeAdapter(mAdapterList.indexOf(adapter));
    }

    /**
     * Remove the adapter at the given index from the list of merged adapters.
     */
    public void removeAdapter(int index) {
        if (index < 0 || index >= mAdapterList.size()) return;
        AdapterData adapter = mAdapterList.remove(index);

        ForwardingDataSetObserver observer = mObservers.get(adapter.mAdapter);
        adapter.mAdapter.unregisterAdapterDataObserver(observer);
        mObservers.remove(adapter.mAdapter);

        notifyDataSetChanged();
    }

    public int getSubAdapterCount() {
        return mAdapterList.size();
    }

    @SuppressWarnings("unchecked")
    public <T extends RecyclerView.Adapter> T getSubAdapter(int index) {
        return (T) mAdapterList.get(index).mAdapter;
    }

    /**
     * Adds a new View to the roster of things to appear in
     * the aggregate list.
     *
     * @param view Single view to add
     */
    public void addView(View view) {
        ArrayList<View> list = new ArrayList<View>(1);
        list.add(view);
        addViews(list);
    }

    /**
     * Adds a list of views to the roster of things to appear
     * in the aggregate list.
     *
     * @param views List of views to add
     */
    public void addViews(List<View> views) {
        addAdapter(new ViewsAdapter(mContext, views));
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (AdapterData adapter : mAdapterList) {
            count += adapter.mAdapter.getItemCount();
        }
        return count;
    }

    /**
     * For a given merged position, find the corresponding Adapter and local position within that Adapter by iterating through Adapters and
     * summing their counts until the merged position is found.
     *
     * @param position a merged (global) position
     * @return the matching Adapter and local position, or null if not found
     */
    public AdapterData getAdapterOffsetForItem(final int position) {
        final int adapterCount = mAdapterList.size();
        int i = 0;
        int count = 0;

        while (i < adapterCount) {
            AdapterData a = mAdapterList.get(i);
            int newCount = count + a.mAdapter.getItemCount();

            if (position < newCount) {
                a.mLocalPosition = Math.max(position - count, 0);
                return a;
            }

            count = newCount;
            i++;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        AdapterData result = getAdapterOffsetForItem(position);
        int viewType = result.mAdapter.getItemViewType(result.mLocalPosition);
        if (!result.mViewTypesSet.contains(viewType)) {
            result.mViewTypesSet.add(viewType);
        }
        return viewType;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        for (AdapterData adapter : mAdapterList) {
            if (adapter.mViewTypesSet.contains(viewType)) {
                return adapter.mAdapter.onCreateViewHolder(viewGroup, viewType);
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        AdapterData result = getAdapterOffsetForItem(position);
        result.mAdapter.onBindViewHolder(viewHolder, result.mLocalPosition);
    }

    /**
     * forwarding data set observer
     */
    private int getMergePositionForLocalPosition(RecyclerView.Adapter adapter, int position) {
        final int adapterPosition = findPositionForAdapter(adapter);
        int index = 0;
        for (AdapterData localAdapter : mAdapterList) {
            if (index < adapterPosition) {
                position += localAdapter.mAdapter.getItemCount();
            } else {
                break;
            }
            index++;
        }
        return position;
    }

    private int findPositionForAdapter(RecyclerView.Adapter adapter) {
        for (int index = 0; index < mAdapterList.size(); index++) {
            if (mAdapterList.get(index).mAdapter == adapter) {
                return index;
            }
        }

        return -1;
    }

    private class ForwardingDataSetObserver extends RecyclerView.AdapterDataObserver {
        private RecyclerView.Adapter mAdapter;

        public ForwardingDataSetObserver(RecyclerView.Adapter adapter) {
            this.mAdapter = adapter;
        }

        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            int mergePositionStart = getMergePositionForLocalPosition(mAdapter, positionStart);
            super.onItemRangeChanged(mergePositionStart, itemCount);
            notifyItemRangeChanged(mergePositionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            int mergePositionStart = getMergePositionForLocalPosition(mAdapter, positionStart);
            super.onItemRangeInserted(mergePositionStart, itemCount);
            notifyItemRangeInserted(mergePositionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            int mergePositionStart = getMergePositionForLocalPosition(mAdapter, positionStart);
            super.onItemRangeRemoved(mergePositionStart, itemCount);
            notifyItemRangeRemoved(mergePositionStart, itemCount);
        }
    }


    /**
     * ViewsAdapter, ported from CommonsWare SackOfViews adapter.
     */
    public static class ViewsAdapter extends RecyclerView.Adapter {
        private List<View> views = null;
        private Context context;

        /**
         * Constructor creating an empty list of views, but with
         * a specified count. Subclasses must override newView().
         */
        public ViewsAdapter(Context context, int count) {
            super();
            this.context = context;

            views = new ArrayList<View>(count);

            for (int i = 0; i < count; i++) {
                views.add(null);
            }
        }

        /**
         * Constructor wrapping a supplied list of views.
         * Subclasses must override newView() if any of the elements
         * in the list are null.
         */
        public ViewsAdapter(Context context, List<View> views) {
            super();
            this.context = context;

            this.views = views;
        }

        /**
         * How many items are in the data set represented by this
         * Adapter.
         */
        @Override
        public int getItemCount() {
            return (views.size());
        }

        /**
         * Get the type of View that will be created by getView()
         * for the specified item.
         *
         * @param position Position of the item whose data we want
         */
        @Override
        public int getItemViewType(int position) {
            return (position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            //view type is equal to the position in this adapter.
            ViewsViewHolder holder = new ViewsViewHolder(views.get(viewType));
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
            }
        }

        /**
         * Get the row id associated with the specified position
         * in the list.
         *
         * @param position Position of the item whose data we want
         */
        @Override
        public long getItemId(int position) {
            return (position);
        }

        public boolean hasView(View v) {
            return (views.contains(v));
        }

        /**
         * Create a new View to go into the list at the specified
         * position.
         *
         * @param position Position of the item whose data we want
         * @param parent   ViewGroup containing the returned View
         */
        protected View newView(int position, ViewGroup parent) {
            throw new RuntimeException("You must override newView()!");
        }
    }

    public static class ViewsViewHolder extends RecyclerView.ViewHolder {
        public ViewsViewHolder(View itemView) {
            super(itemView);
        }
    }
}