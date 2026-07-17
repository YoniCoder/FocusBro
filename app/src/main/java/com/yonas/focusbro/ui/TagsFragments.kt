package com.yonas.focusbro.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yonas.focusbro.R
import com.yonas.focusbro.data.AppDatabase
import com.yonas.focusbro.data.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var adapter: TagAdapter
    private var tagList = mutableListOf<Tag>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.tagsRecyclerView)
        addButton = view.findViewById(R.id.addTagButton)

        adapter = TagAdapter(tagList, ::onEditTag, ::onDeleteTag)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadTags()

        addButton.setOnClickListener {
            showAddTagDialog()
        }
    }

    private fun loadTags() {
        CoroutineScope(Dispatchers.IO).launch {
            val tags = AppDatabase.getInstance(requireContext()).tagDao().getAllTags()
            withContext(Dispatchers.Main) {
                tagList.clear()
                tagList.addAll(tags)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddTagDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter tag name"

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Tag")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val existing = AppDatabase.getInstance(requireContext()).tagDao().getTagByName(name)
                        if (existing == null) {
                            AppDatabase.getInstance(requireContext()).tagDao().insertTag(Tag(name = name))
                            withContext(Dispatchers.Main) { loadTags() }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Tag already exists", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun onEditTag(tag: Tag) {
        val input = EditText(requireContext())
        input.setText(tag.name)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Tag")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val existing = AppDatabase.getInstance(requireContext()).tagDao().getTagByName(newName)
                        if (existing == null || existing.id == tag.id) {
                            AppDatabase.getInstance(requireContext()).tagDao().updateTag(tag.copy(name = newName))
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Tag already exists", Toast.LENGTH_SHORT).show()
                            }
                        }
                        withContext(Dispatchers.Main) { loadTags() }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun onDeleteTag(tag: Tag) {
        CoroutineScope(Dispatchers.IO).launch {
            val allTags = AppDatabase.getInstance(requireContext()).tagDao().getAllTags()
            withContext(Dispatchers.Main) {
                if (allTags.size <= 1) {
                    Toast.makeText(requireContext(), "At least one tag is required", Toast.LENGTH_SHORT).show()
                    return@withContext
                }
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Tag")
                    .setMessage("Delete '${tag.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            AppDatabase.getInstance(requireContext()).tagDao().deleteTag(tag)
                            withContext(Dispatchers.Main) { loadTags() }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private class TagAdapter(
        private val tags: List<Tag>,
        private val onEdit: (Tag) -> Unit,
        private val onDelete: (Tag) -> Unit
    ) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag, parent, false)
            return TagViewHolder(view)
        }

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            val tag = tags[position]
            holder.bind(tag)
        }

        override fun getItemCount() = tags.size

        inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameView: TextView = itemView.findViewById(R.id.tagName)
            private val editBtn: Button = itemView.findViewById(R.id.editTagButton)
            private val deleteBtn: Button = itemView.findViewById(R.id.deleteTagButton)

            fun bind(tag: Tag) {
                nameView.text = tag.name
                editBtn.setOnClickListener { onEdit(tag) }
                deleteBtn.setOnClickListener { onDelete(tag) }
            }
        }
    }
}