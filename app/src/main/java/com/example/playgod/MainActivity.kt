package com.example.playgod

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.TypedValue
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var db: DataBaseHelper

    var currentWorldId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnHome: ImageButton = findViewById(R.id.btnHome)
        btnHome.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, MainFragment())
                .commit()
        }

        db = DataBaseHelper(this)
        db.createDefaultCats()
        db.addDefaultTags()
        populateSidebar()

        setupWorldUI()

        val btnCreateNotes: FloatingActionButton = findViewById(R.id.btnCreateNote)
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

        val categories = db.getAllCats()

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
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFragmentContainer, CategoryFragment.newInstance(category.catName))
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

//    fun openCategoryFragment(category: String) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.mainFragmentContainer, CategoryFragment.newInstance(category))
//            .commit()
//    }

    private fun setupWorldUI() {
        val worlds = db.getAllWorlds()

        val worldContainer: LinearLayout = findViewById(R.id.worldContainer)
        val spinner: Spinner = findViewById(R.id.spinnerWorldSelector)
        val createBtn: Button = findViewById(R.id.btnCreateWorld)
        val createOnlyBtn: Button = findViewById(R.id.btnCreateWorldOnly)

        if (worlds.isEmpty()) {
            createOnlyBtn.visibility = Button.VISIBLE
            worldContainer.visibility = LinearLayout.GONE

            createOnlyBtn.setOnClickListener {
                showCreateWorldDialog()
            }

            setSidebarEnabled(false)
            currentWorldId = null

        } else {
            createOnlyBtn.visibility = Button.GONE
            worldContainer.visibility = LinearLayout.VISIBLE

            // "Select a world..." sits at position 0 and is never a real world.
            // This means onItemSelected firing at position 0 on startup is harmless —
            // no world gets set and the sidebar stays locked.
            val spinnerItems = mutableListOf("Select a world...")
            spinnerItems.addAll(worlds.map { it.worldName })
            spinnerItems.add("+ Create World")

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Always reset to the prompt on setup so nothing is silently active
            currentWorldId = null
            setSidebarEnabled(false)

            spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: android.view.View,
                    position: Int,
                    id: Long
                ) {
                    when {
                        // Position 0 is the "Select a world..." prompt — do nothing
                        position == 0 -> {
                            currentWorldId = null
                            setSidebarEnabled(false)
                        }

                        // Last item is the create sentinel
                        spinnerItems[position] == "+ Create World" -> {
                            showCreateWorldDialog()
                        }

                        // Positions 1..n-1 map to worlds[position - 1] because of the prompt at 0
                        else -> {
                            currentWorldId = worlds[position - 1].worldIDPK
                            setSidebarEnabled(true)
                            Toast.makeText(
                                this@MainActivity,
                                "Selected world: ${worlds[position - 1].worldName}",
                                Toast.LENGTH_SHORT
                            ).show()

                            supportFragmentManager.beginTransaction()
                                .replace(R.id.mainFragmentContainer, MainFragment())
                                .commit()
                        }
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }

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
                    setupWorldUI()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }
}