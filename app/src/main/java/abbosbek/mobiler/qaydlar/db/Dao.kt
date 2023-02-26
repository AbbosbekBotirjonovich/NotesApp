package abbosbek.mobiler.qaydlar.db

import abbosbek.mobiler.qaydlar.models.Note
import androidx.lifecycle.LiveData
import androidx.room.*


@androidx.room.Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(note : Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("select * from note order by id desc")
    fun getAllNote() : LiveData<List<Note>>

    @Query("select * from note where title like :query or content like :query or date like :query order by id desc")
    fun searchNote(query: String) : LiveData<List<Note>>

    @Delete
    suspend fun deleteNote(note: Note)

}