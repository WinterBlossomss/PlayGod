package com.example.playgod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: DataBaseHelper

    private var categoryName: String? = null

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryName = arguments?.getString(ARG_CATEGORY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        recyclerView = view.findViewById(R.id.catRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = DataBaseHelper(requireContext())

        loadTags()

        return view
    }

    private fun loadTags() {
        val categories = db.getAllCats()
        val category = categories.find { it.catName == categoryName }

        val tags = if (category != null) {
            db.getTagsByCategory(category.catIDPK)
        } else {
            emptyList()
        }

        recyclerView.adapter = TagAdapter(tags) { selectedTag ->
            openNotesFragment(selectedTag)
        }
    }


    private fun openNotesFragment(tag: Tags) {

        val fragment = NotesFragment.newInstance(tag.tagIDPK)

        parentFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .addToBackStack(null)   // IMPORTANT for back navigation
            .commit()
    }


}
