package abbosbek.mobiler.qaydlar.viewModel

import abbosbek.mobiler.qaydlar.models.Note
import abbosbek.mobiler.qaydlar.repository.NoteRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    fun saveNote(newNote : Note) = viewModelScope.launch(Dispatchers.IO) {

        repository.addNote(newNote)

    }

    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch (Dispatchers.IO) {
        repository.deleteNote(note)
    }

    fun searchNote(query : String) : LiveData<List<Note>>{
        return repository.searchNote(query)
    }

    fun getAllNotes() : LiveData<List<Note>> = repository.getNote()

}