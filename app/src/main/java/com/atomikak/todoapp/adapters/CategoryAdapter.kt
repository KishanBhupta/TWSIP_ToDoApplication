package com.atomikak.todoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.atomikak.todoapp.R
import com.atomikak.todoapp.listeners.MyCheckBoxClick

class CategoryAdapter(val context: Context, val categoryList: List<String>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private lateinit var clistener: MyCheckBoxClick
    fun OnClick(myCheckBoxClick: MyCheckBoxClick) {
        clistener = myCheckBoxClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_category_list_item, parent, false)
        return CategoryViewHolder(view, clistener)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.mcl_categoryName.setText(categoryList[position])
    }

    class CategoryViewHolder(itemView: View, clistener: MyCheckBoxClick) :
        RecyclerView.ViewHolder(itemView) {
        val mcl_categoryName: TextView = itemView.findViewById(R.id.mcl_categoryName)
        val mcl_categoryCard: CardView = itemView.findViewById(R.id.mcl_categoryCard)

        init {
            mcl_categoryCard.setOnClickListener {
                clistener.OnCheckCliked(adapterPosition, "Category")
            }
        }
    }
}