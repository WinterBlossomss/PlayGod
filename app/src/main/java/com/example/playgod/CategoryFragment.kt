package com.example.playgod

import android.os.Bundle
import android.util.Log
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
        private const val TAG = "CategoryFragment"

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
        Log.d(TAG, "onCreate: categoryName=$categoryName")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //gets all the views
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        recyclerView = view.findViewById(R.id.catRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = DataBaseHelper(requireContext())

        //loads all tags in database
        loadTags()

        return view
    }
    //Loads all the tags of the category
    private fun loadTags() {
        val categories = db.getAllCats()
        val category = categories.find { it.catName == categoryName }

//        Log.d(TAG, "Looking for category '$categoryName'")
//        Log.d(TAG, "All categories: ${categories.map { "${it.catIDPK}:${it.catName}" }}")
//        Log.d(TAG, "Matched category: id=${category?.catIDPK}, name=${category?.catName}")

        val tags = if (category != null) {
            db.getTagsByCategory(category.catIDPK)
        } else {
            emptyList()
        }

        //Log.d(TAG, "Tags found (${tags.size}): ${tags.map { "${it.tagIDPK}:${it.tagName}" }}")

        recyclerView.adapter = TagAdapter(tags) { selectedTag ->
            //Log.d(TAG, "Tag clicked: id=${selectedTag.tagIDPK}, name=${selectedTag.tagName}")
            openNotesFragment(selectedTag)
        }
    }


    //Navigate to NotesFragment (list of all the notes of the tag)
    private fun openNotesFragment(tag: Tags) {
        val fragment = NotesFragment.newInstance(tag.tagIDPK)

        parentFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}