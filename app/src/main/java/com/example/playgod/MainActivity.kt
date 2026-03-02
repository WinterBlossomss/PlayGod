package com.example.playgod

import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.util.TypedValue
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.MainScope


class MainActivity : AppCompatActivity() {

    private lateinit var db : DataBaseHelper

    private var selectedWorldId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnhome: ImageButton = findViewById(R.id.btnHome)
        btnhome.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, MainFragment())
                .commit()
        }



        db = DataBaseHelper(this)

        db.createDefaultCats()
        db.addDefaultTags()
        populateSidebar()

        setupWorldUI()

        val btnCreateNotes : FloatingActionButton = findViewById(R.id.btnCreateNote)
        btnCreateNotes.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, NoteCreateFragment())
                .commit()
        }


        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, MainFragment())
            .commit()


    }

    fun populateSidebar() {
        val dynamicButtonContainer: LinearLayout = findViewById(R.id.dynamicButtonContainer)
        dynamicButtonContainer.removeAllViews()

        val categories = db.getAllCats() // fetch Categories objects

        categories.forEach { category ->
            val button = ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.sidebar_icon_size),
                    resources.getDimensionPixelSize(R.dimen.sidebar_icon_size)
                ).apply {
                    bottomMargin = 12
                }

                setImageResource(db.getCategoryIcon(category.catName))

                val typedValue = TypedValue()
                theme.resolveAttribute(
                    android.R.attr.selectableItemBackgroundBorderless,
                    typedValue,
                    true
                )
                background = ContextCompat.getDrawable(context, typedValue.resourceId)

                scaleType = ImageView.ScaleType.CENTER_INSIDE
                contentDescription = category.catName

                setOnClickListener {
                    val catfrag = CategoryFragment.newInstance(category.catName)

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFragmentContainer, catfrag)
                        .commit()

                }
            }

            dynamicButtonContainer.addView(button)
        }
    }

    private fun setSidebarEnabled(enabled: Boolean) {
        val dynamicButtonContainer: LinearLayout = findViewById(R.id.dynamicButtonContainer)
        for (i in 0 until dynamicButtonContainer.childCount) {
            dynamicButtonContainer.getChildAt(i).isEnabled = enabled
            dynamicButtonContainer.getChildAt(i).alpha = if (enabled) 1f else 0.5f
        }
    }

    fun openCategoryFragment(category: String) {

        val fragment = CategoryFragment.newInstance(category)

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()
    }


    private fun setupWorldUI() {
        val worlds = db.getAllWorlds()
        val worldContainer: LinearLayout = findViewById(R.id.worldContainer)
        val spinner: Spinner = findViewById(R.id.spinnerWorldSelector)
        val createBtn: Button = findViewById(R.id.btnCreateWorld)
        val createOnlyBtn: Button = findViewById(R.id.btnCreateWorldOnly)

        if (worlds.isEmpty()) {
            // No worlds, show only create button
            createOnlyBtn.visibility = Button.VISIBLE
            worldContainer.visibility = LinearLayout.GONE

            createOnlyBtn.setOnClickListener {
                showCreateWorldDialog()
            }

            // Disable sidebar until a world is created
            setSidebarEnabled(false)
            selectedWorldId = null

        } else {
            // Worlds exist, show spinner + create button
            createOnlyBtn.visibility = Button.GONE
            worldContainer.visibility = LinearLayout.VISIBLE

            // Prepare spinner data
            val spinnerItems = worlds.map { it.worldName }.toMutableList()
            spinnerItems.add("Create World") // last item is create option

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Initially, no world selected
            selectedWorldId = null
            setSidebarEnabled(false)

            spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: android.view.View,
                    position: Int,
                    id: Long
                ) {
                    val selected = spinnerItems[position]
                    if (selected == "Create World") {
                        showCreateWorldDialog()
                    } else {
                        selectedWorldId = worlds[position].worldIDPK
                        setSidebarEnabled(true)
                        Toast.makeText(this@MainActivity, "Selected world: $selected", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            })

            createBtn.setOnClickListener {
                showCreateWorldDialog()
            }
        }
    }
    private fun showCreateWorldDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val input = android.widget.EditText(this)
        input.hint = "Enter world name"
        builder.setTitle("Create New World")
            .setView(input)
            .setPositiveButton("Create") { dialog, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    db.addWorld(name)
                    setupWorldUI() // refresh UI
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }
}

