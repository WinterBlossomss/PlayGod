package com.example.playgod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoNotes: TextView
    private lateinit var db: DataBaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recentNotesRecyclerView)
        tvNoNotes = view.findViewById(R.id.tvNoNotes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = DataBaseHelper(requireContext())
    }

    override fun onResume() {
        super.onResume()
        // Reload every time the fragment becomes visible so that switching
        // worlds in the spinner is immediately reflected in the list.
        loadRecentNotes()
    }

    private fun loadRecentNotes() {
        val worldId = (activity as? MainActivity)?.currentWorldId

        val recentNotes = db.getRecentNotes(limit = 10, worldId = worldId)

        if (recentNotes.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvNoNotes.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvNoNotes.visibility = View.GONE

            recyclerView.adapter = RecentNoteAdapter(recentNotes) { selectedNote ->
                openNoteDetailFragment(selectedNote.noteIDPK)
            }
        }
    }

    private fun openNoteDetailFragment(noteId: Int) {
        val fragment = NoteDetailFragment.newInstance(noteId)

        parentFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}