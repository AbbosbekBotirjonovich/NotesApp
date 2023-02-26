package abbosbek.mobiler.qaydlar.fragments

import abbosbek.mobiler.qaydlar.MainActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import abbosbek.mobiler.qaydlar.R
import abbosbek.mobiler.qaydlar.databinding.BottomSheetLayoutBinding
import abbosbek.mobiler.qaydlar.databinding.FragmentSaveOrDeleteBinding
import abbosbek.mobiler.qaydlar.models.Note
import abbosbek.mobiler.qaydlar.utils.hideKeyboard
import abbosbek.mobiler.qaydlar.viewModel.NoteViewModel
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class SaveOrDeleteFragment : Fragment() {

    private var _binding : FragmentSaveOrDeleteBinding?= null
    val binding get() = _binding!!

    private lateinit var navController: NavController
    private var note : Note ?= null
    private var color = -1
    private lateinit var result : String
    private val viewModel : NoteViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main)
    private val args : SaveOrDeleteFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L
        }

        sharedElementEnterTransition =animation
        sharedElementReturnTransition = animation

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSaveOrDeleteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity

        ViewCompat.setTransitionName(
            binding.noteContentFragmentParent,
            "recyclerView_${args.note?.id}"
        )

        binding.backBtn.setOnClickListener {
            requireActivity().hideKeyboard()
            navController.popBackStack()
        }


        binding.saveNote.setOnClickListener {
            saveNote()
        }
        try {
            binding.etNoteContent.setOnFocusChangeListener{_,hasFocus->
                if (hasFocus){
                    binding.bottomBar.visibility = View.VISIBLE
                    binding.etNoteContent.setStylesBar(binding.styleBar)
                }else{
                    binding.bottomBar.visibility = View.GONE
                }
            }
        }
        catch (e : Throwable){
            Log.d("TAG", "onViewCreated: ${e.stackTraceToString()}")
        }
        binding.fabColorPick.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )
            val bottomSheetView : View = layoutInflater.inflate(
                R.layout.bottom_sheet_layout,
                null
            )
            with(bottomSheetDialog){
                setContentView(bottomSheetView)
                show()
            }
            val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)
            bottomSheetBinding.apply {
                colorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener {
                        value->
                        color = value
                        binding.apply {
                            noteContentFragmentParent.setBackgroundColor(color)
                            toolbarFragmentNoteContent.setBackgroundColor(color)
                            bottomBar.setBackgroundColor(color)
                            activity.window.statusBarColor = color
                        }
                        bottomSheetBinding.bottomSheetParent.setBackgroundColor(color)
                    }
                }
                bottomSheetParent.setCardBackgroundColor(color)
            }
            bottomSheetView.post{
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        setUpNotes()
    }

    private fun setUpNotes() {
        val note = args.note
        val title = binding.etTitle
        val content = binding.etNoteContent
        val lastEdited = binding.lastEdited

        if (note==null){
            lastEdited.text = getString(R.string.edited_on,SimpleDateFormat.getDateInstance().format(Date()))
        }
        if (note != null){
            title.setText(note.title)
            content.renderMD(note.content)
            lastEdited.text = getString(R.string.edited_on,note.date)
            color = note.color
            binding.apply {
                job.launch {
                    delay(10)
                    noteContentFragmentParent.setBackgroundColor(color)

                }
                toolbarFragmentNoteContent.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)
            }
            activity?.window?.statusBarColor = note.color
        }

    }

    private fun saveNote() {
        if (binding.etNoteContent.text.toString().isEmpty() ||
                binding.etTitle.text.toString().isEmpty()){

            Toast.makeText(activity,"Something is Empty",Toast.LENGTH_SHORT).show()
        }else
        {
            note = args.note
            when(note){
                null-> {
                    viewModel.saveNote(
                        Note(
                            0,
                            binding.etTitle.text.toString(),
                            binding.etNoteContent.getMD(),
                            currentDate
                        )
                    )
                    result = "Not Saved"
                    setFragmentResult(
                        "key",
                        bundleOf("bundleKey" to result)
                    )
                    navController.navigate(SaveOrDeleteFragmentDirections.actionSaveOrDeleteFragmentToNoteFragment())
                }
                else ->{
                    //update note
                    updateNote()
                    navController.popBackStack()
                }
            }
        }


    }

    private fun updateNote() {

        if (note!=null){
            viewModel.updateNote(
                Note(
                    note!!.id,
                    binding.etTitle.text.toString(),
                    binding.etNoteContent.getMD(),
                    currentDate,
                    color
                )
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding == null
    }
}