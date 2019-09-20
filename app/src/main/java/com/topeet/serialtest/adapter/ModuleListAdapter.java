package com.topeet.serialtest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.topeet.serialtest.R;
import com.topeet.serialtest.entity.ModuleBean;

import java.util.List;

/**
 * @author jiazhu
 */
public class ModuleListAdapter extends RecyclerView.Adapter<ModuleListAdapter.ModuleHolder> {
    private List<ModuleBean> mList;
    private Context mContext;
    private MyListener mListener;

    public ModuleListAdapter(List<ModuleBean> list, Context context, MyListener listener) {
        this.mList = list;
        this.mContext = context;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ModuleHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ModuleHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_module_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleHolder moduleHolder, int i) {
        final ModuleBean bean = mList.get(i);
        moduleHolder.ivModule.setImageResource(bean.getIcon());
        moduleHolder.tvModule.setText(bean.getName());

        moduleHolder.clParent.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick(bean);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class ModuleHolder extends RecyclerView.ViewHolder {
        TextView tvModule;
        ImageView ivModule;
        ConstraintLayout clParent;

        ModuleHolder(@NonNull View itemView) {
            super(itemView);
            tvModule = itemView.findViewById(R.id.tv_module);
            ivModule = itemView.findViewById(R.id.iv_module);
            clParent = itemView.findViewById(R.id.cl_parent);
        }
    }


    public interface MyListener {
        /**
         * 点击事件
         *
         * @param bean 模块bean
         */
        void onClick(ModuleBean bean);
    }

}
