package abbosbek.mobiler.qaydlar.adapter

import abbosbek.mobiler.qaydlar.databinding.NotesItemLayoutBinding
import abbosbek.mobiler.qaydlar.fragments.NoteFragmentDirections
import abbosbek.mobiler.qaydlar.models.Note
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak

class RvNotesAdapter : ListAdapter<Note,RvNotesAdapter.NotesViewHolder>(DiffUtilCallback()) {

    inner class NotesViewHolder(val binding: NotesItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        val title = binding.noteItemTitle
        val content = binding.noteContentItem
        val date = binding.noteDate
        val parent = binding.noteItemLayoutParent

        val markwon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
            .usePlugin(object : AbstractMarkwonPlugin(){
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ){visitor ,_,->
                        visitor.forceNewLine()
                    }
                }
            }).build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            NotesItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,false
            )
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        getItem(position).let { note->
            holder.apply {
                parent.transitionName = "recyclerView_${note.id}"
                title.text = note.title
                markwon.setMarkdown(content,note.content)
                date.text = note.date
                parent.setCardBackgroundColor(note.color)

                itemView.setOnClickListener {

                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    Navigation.findNavController(it).navigate(action,extras)

                }
                content.setOnClickListener {
                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    Navigation.findNavController(it).navigate(action,extras)

                }
            }
        }
    }
}