package abbosbek.mobiler.qaydlar.fragments

import abbosbek.mobiler.qaydlar.MainActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import abbosbek.mobiler.qaydlar.R
import abbosbek.mobiler.qaydlar.adapter.RvNotesAdapter
import abbosbek.mobiler.qaydlar.databinding.FragmentNoteBinding
import abbosbek.mobiler.qaydlar.utils.SwipeToDelete
import abbosbek.mobiler.qaydlar.utils.hideKeyboard
import abbosbek.mobiler.qaydlar.viewModel.NoteViewModel
import android.content.res.Configuration
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NoteFragment : Fragment() {

    private var _binding : FragmentNoteBinding ?= null
    val binding get() = _binding!!

    private val viewModel : NoteViewModel by activityViewModels()

    private lateinit var rvAdapter : RvNotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNoteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)
        requireActivity().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            activity.window.statusBarColor = Color.WHITE
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")
        }

        binding.addNoteFab.setOnClickListener {
            binding.appBarLayout.visibility = View.VISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment())
        }
        binding.innerFab.setOnClickListener {
            binding.appBarLayout.visibility = View.VISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment())
        }

        recyclerViewDisplay()
        swiToDelete(binding.rvNote)
        binding.search.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.noData.isVisible = false
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (s.toString().isNotEmpty()){
                    val text = s.toString()
                    val query ="%$text%"
                    if (query.isNotEmpty()){
                        viewModel.searchNote(query).observe(viewLifecycleOwner){
                            rvAdapter.submitList(it)
                        }
                    }else{
                        observerDataChanges()
                    }
                }else{
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.search.setOnEditorActionListener{v,actionId,_->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                v.clearFocus()
                requireActivity().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }

        binding.rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->

            when{
                scrollY>oldScrollY -> {
                    binding.chatFbText.isVisible = false
                }
                scrollX==scrollY ->{
                    binding.chatFbText.isVisible = true
                }
                else ->{
                    binding.chatFbText.isVisible = true
                }
            }

        }

    }

    private fun swiToDelete(rvNote: RecyclerView) {

        val swipeToDeleteCallback = object : SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition
                val note = rvAdapter.currentList[position]

                var actionBtnTapped = false

                viewModel.deleteNote(note)
                binding.search.apply {
                    requireActivity().hideKeyboard()
                    clearFocus()
                }

                if (binding.search.text.toString().isEmpty()){
                    observerDataChanges()
                }

                val snackbar = Snackbar
                    .make(requireView(),"Note Delete",Snackbar.LENGTH_SHORT)
                    .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                        }

                        override fun onShown(transientBottomBar: Snackbar?) {
                            transientBottomBar?.setAction("Undo"){
                                viewModel.saveNote(note)
                                actionBtnTapped = true
                                binding.noData.isVisible = false
                            }
                            super.onShown(transientBottomBar)
                        }
                    }).apply {
                        animationMode = Snackbar.ANIMATION_MODE_FADE
                        setAnchorView(R.id.add_note_fab)
                    }
                snackbar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellowOrange
                    )
                )
                snackbar.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvNote)
    }

    private fun observerDataChanges() {
        viewModel.getAllNotes().observe(viewLifecycleOwner){list->
            binding.noData.isVisible = list.isEmpty()
            rvAdapter.submitList(list)
        }

    }

    private fun recyclerViewDisplay() {

        when(resources.configuration.orientation){
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerView(2)
            Configuration.ORIENTATION_LANDSCAPE ->setUpRecyclerView(3)
        }

    }

    private fun setUpRecyclerView(spanCount: Int) {

        binding.rvNote.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            rvAdapter = RvNotesAdapter()
            adapter = rvAdapter
            postponeEnterTransition(300L,TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        observerDataChanges()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding == null
    }

}