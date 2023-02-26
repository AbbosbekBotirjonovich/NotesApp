package abbosbek.mobiler.qaydlar.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0,
    val title : String,
    val content : String,
    val date : String,
    val color : Int = -1
) : java.io.Serializable
