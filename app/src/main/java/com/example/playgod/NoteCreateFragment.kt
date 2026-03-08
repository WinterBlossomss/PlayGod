package com.example.playgod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class NoteCreateFragment : Fragment() {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var editBrief: EditText
    private lateinit var autoCompleteTag: AutoCompleteTextView
    private lateinit var buttonSave: MaterialButton
    private lateinit var buttonBack: ImageButton

    private lateinit var db: DataBaseHelper
    private lateinit var allTags: List<Tags>

    private var selectedTagId: Int? = null
    private var selectedWorldId: Int? = null
    private var noteId: Int = 0  // 0 = create mode, >0 = edit mode

    companion object {
        private const val ARG_WORLD_ID = "world_id"
        private const val ARG_NOTE_ID = "note_id"

        // Create mode
        fun newInstance(worldId: Int): NoteCreateFragment {
            val fragment = NoteCreateFragment()
            val args = Bundle()
            args.putInt(ARG_WORLD_ID, worldId)
            args.putInt(ARG_NOTE_ID, 0)
            fragment.arguments = args
            return fragment
        }

        // Edit mode
        fun newInstance(noteId: Int, worldId: Int): NoteCreateFragment {
            val fragment = NoteCreateFragment()
            val args = Bundle()
            args.putInt(ARG_NOTE_ID, noteId)
            args.putInt(ARG_WORLD_ID, worldId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = arguments?.getInt(ARG_NOTE_ID, 0) ?: 0
        val argWorldId = arguments?.getInt(ARG_WORLD_ID, -1) ?: -1
        selectedWorldId = if (argWorldId != -1) argWorldId
        else (activity as? MainActivity)?.currentWorldId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_note_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DataBaseHelper(requireContext())

        initViews(view)
        setupTagDropdown()

        if (noteId > 0) {
            prefillFields()
        }

        setupButtons()
    }

    private fun initViews(view: View) {
        editTitle = view.findViewById(R.id.editTitle)
        editContent = view.findViewById(R.id.editContent)
        editBrief = view.findViewById(R.id.editBriefDescription)
        autoCompleteTag = view.findViewById(R.id.autoCompleteTag)
        buttonSave = view.findViewById(R.id.buttonSave)
        buttonBack = view.findViewById(R.id.buttonBack)
    }

    private fun setupTagDropdown() {
        allTags = db.getAllTags()
        val tagNames = allTags.map { it.tagName }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            tagNames
        )

        autoCompleteTag.setAdapter(adapter)

        autoCompleteTag.setOnItemClickListener { _, _, position, _ ->
            val selectedName = adapter.getItem(position)
            selectedTagId = allTags.firstOrNull { it.tagName == selectedName }?.tagIDPK
        }
    }

    private fun prefillFields() {
        val note = db.getNoteById(noteId) ?: return

        editTitle.setText(note.noteName)
        editContent.setText(note.noteDescr)
        editBrief.setText(note.noteBrfDescr)

        note.noteTagFK?.let { tagFk ->
            val tag = allTags.firstOrNull { it.tagIDPK == tagFk }
            if (tag != null) {
                autoCompleteTag.setText(tag.tagName, false)
                selectedTagId = tag.tagIDPK
            }
        }

        selectedWorldId = note.noteWorldFK ?: selectedWorldId
        buttonSave.text = "Update Note"
    }

    private fun resolveTagId(): Int? {
        if (selectedTagId != null) return selectedTagId
        val typed = autoCompleteTag.text.toString().trim()
        return allTags.firstOrNull { it.tagName.equals(typed, ignoreCase = true) }?.tagIDPK
    }

    private fun setupButtons() {

        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        buttonSave.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val content = editContent.text.toString().trim()
            val brief = editBrief.text.toString().trim()

            if (title.isEmpty()) {
                editTitle.error = "Title required"
                return@setOnClickListener
            }

            if (selectedWorldId == null) {
                Toast.makeText(requireContext(), "Select a world first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tagId = resolveTagId()
            if (tagId == null) {
                Toast.makeText(requireContext(), "Please select a valid tag", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (noteId > 0) {
                db.updateNote(
                    noteId = noteId,
                    title = title,
                    brfdescr = brief,
                    content = content,
                    tagId = tagId,
                    worldId = selectedWorldId!!
                )
                Toast.makeText(requireContext(), "Note updated", Toast.LENGTH_SHORT).show()
            } else {
                db.addNote(
                    title = title,
                    content = content,
                    brfdescr = brief,
                    tagId = tagId,
                    worldId = selectedWorldId!!
                )
                Toast.makeText(requireContext(), "Note saved", Toast.LENGTH_SHORT).show()
            }

            parentFragmentManager.popBackStack()
        }
    }
}