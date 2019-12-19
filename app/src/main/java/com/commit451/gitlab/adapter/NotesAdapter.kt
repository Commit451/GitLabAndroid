package com.commit451.gitlab.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.NoteViewHolder
import java.util.*

/**
 * Nice notes
 */
class NotesAdapter(private val project: Project) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val TYPE_COMMENT = 2
        private const val TYPE_FOOTER = 3

        private const val FOOTER_COUNT = 1
    }

    private val notes: LinkedList<Note> = LinkedList()
    private var loading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_COMMENT) {
            return NoteViewHolder.inflate(parent)
        } else if (viewType == TYPE_FOOTER) {
            return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalArgumentException("No view type matches")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NoteViewHolder) {
            val note = getNoteAt(position)
            holder.bind(note, project)
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(loading)
        }
    }

    override fun getItemCount(): Int {
        return notes.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == notes.size) {
            TYPE_FOOTER
        } else {
            TYPE_COMMENT
        }
    }

    fun getNoteAt(position: Int): Note {
        return notes[position]
    }

    fun setNotes(notes: List<Note>) {
        this.notes.clear()
        addNotes(notes)
    }

    fun addNotes(notes: List<Note>) {
        if (!notes.isEmpty()) {
            this.notes.addAll(notes)
            notifyItemRangeChanged(0, this.notes.size)
        }
    }

    fun addNote(note: Note) {
        notes.addFirst(note)
        notifyItemInserted(0)
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(notes.size)
    }
}
