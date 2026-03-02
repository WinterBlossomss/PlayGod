package com.example.playgod

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class NoteDetailFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var db: DataBaseHelper

    companion object {
        private const val ARG_NOTE_ID = "note_id"

        fun newInstance(noteId: Int): NoteDetailFragment {
            val fragment = NoteDetailFragment()
            val args = Bundle()
            args.putInt(ARG_NOTE_ID, noteId)
            fragment.arguments = args
            return fragment
        }
    }

    private var noteId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = arguments?.getInt(ARG_NOTE_ID) ?: 0
        db = DataBaseHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_note_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.textTitle)
        tvContent = view.findViewById(R.id.textContent)

        loadNote()
    }

    private fun loadNote() {
        val note = db.getNoteById(noteId)

        if (note != null) {
            tvTitle.text = note.noteName
            tvContent.text = note.noteDescr
        }
    }
}
