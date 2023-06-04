package com.example.mlkitcrown.ml_kit

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mlkitcrown.databinding.ItemAdapterObjectNameBinding
import com.example.mlkitcrown.inflateAdapter

class DetectorObjectAdapter : RecyclerView.Adapter<DetectorObjectAdapter.Holder>() {

    private val _list = mutableListOf<String>()
    private val list = _list.asReversed()

    fun updateData(newObject: String) {
        _list.add(newObject)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(parent.inflateAdapter(ItemAdapterObjectNameBinding::inflate))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount() = _list.size

    class Holder(private val binding: ItemAdapterObjectNameBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(s: String) {
            binding.itemObjectName.text = s
        }
    }
}

