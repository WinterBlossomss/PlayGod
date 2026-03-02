package com.example.playgod

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var db: DataBaseHelper

    companion object {
        private const val ARG_TAG_ID = "tag_id"

        fun newInstance(tagId: Int): NotesFragment {
            val fragment = NotesFragment()
            val args = Bundle()
            args.putInt(ARG_TAG_ID, tagId)
            fragment.arguments = args
            return fragment
        }
    }

    private var tagId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tagId = arguments?.getInt(ARG_TAG_ID) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        recyclerView = view.findViewById(R.id.noteRecycleView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = DataBaseHelper(requireContext())

        loadNotes()

        return view
    }

    private fun loadNotes() {
        val notes = db.getNotesByTag(tagId)

        if (notes.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            recyclerView.adapter = MyNoteAdapter(notes) { selectedNote ->
                openNoteDetailFragment(selectedNote)
            }
        }
    }

    private fun openNoteDetailFragment(note: Notes) {
        val fragment = NoteDetailFragment.newInstance(note.noteIDPK)

        parentFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}