package abbosbek.mobiler.qaydlar

import abbosbek.mobiler.qaydlar.databinding.ActivityMainBinding
import abbosbek.mobiler.qaydlar.db.NoteDatabase
import abbosbek.mobiler.qaydlar.repository.NoteRepository
import abbosbek.mobiler.qaydlar.viewModel.NoteViewModel
import abbosbek.mobiler.qaydlar.viewModel.NoteViewModelFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: NoteViewModel
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val noteRepository = NoteRepository(NoteDatabase(this))
        val viewModelFactory = NoteViewModelFactory(noteRepository)

        viewModel = ViewModelProvider(this,viewModelFactory)[NoteViewModel::class.java]


    }
}