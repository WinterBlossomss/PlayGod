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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var db: DataBaseHelper
    private var labelsVisible = false
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

        //Database Methods
        db = DataBaseHelper(this)
        db.createDefaultCats()
        db.addDefaultTags()


        populateSidebar()
        setupWorldUI()


        val btnCreateNotes: FloatingActionButton = findViewById(R.id.btnCreateNote)
        btnCreateNotes.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, NoteCreateFragment())
                .addToBackStack(null) //For fixing overlap
                .commit()
        }
        val btnHelp: ImageButton = findViewById(R.id.btnHelp)
        btnHelp.setOnClickListener {
            labelsVisible = !labelsVisible
            toggleLabels()
        }

        //Fragment Navigation
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, MainFragment())
            .commit()


    }

    //Dynamically creates Buttons
    fun populateSidebar() {
        val dynamicButtonContainer: LinearLayout = findViewById(R.id.dynamicButtonContainer)
        dynamicButtonContainer.removeAllViews()

        val categories = db.getAllCats()

        categories.forEach { category ->
            // Row container
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // Icon button
            val button = ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.sidebar_icon_size),
                    resources.getDimensionPixelSize(R.dimen.sidebar_icon_size)
                )
                setImageResource(db.getCategoryIcon(category.catName))
                val typedValue = TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
                background = ContextCompat.getDrawable(context, typedValue.resourceId)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                contentDescription = category.catName
                setOnClickListener {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFragmentContainer, CategoryFragment.newInstance(category.catName))
                        .commit()
                }
            }

            // Label
            val label = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = 8 }
                text = category.catName
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                textSize = 12f
                visibility = if (labelsVisible) android.view.View.VISIBLE else android.view.View.GONE
            }

            row.addView(button)
            row.addView(label)
            dynamicButtonContainer.addView(row)
        }
    }
    //Helper class for "Help" Button
    private fun toggleLabels() {
        val visibility = if (labelsVisible) android.view.View.VISIBLE else android.view.View.GONE
        val openedPx = resources.getDimensionPixelSize(if (labelsVisible) R.dimen.label_opened else R.dimen.label_closed)

        findViewById<TextView>(R.id.labelHome).visibility = visibility
        findViewById<TextView>(R.id.labelHelp).visibility = visibility
        findViewById<TextView>(R.id.labelCreateNote).visibility = visibility

        val sidebar = findViewById<LinearLayout>(R.id.sidebarContainer)
        sidebar.layoutParams = sidebar.layoutParams.also { it.width = openedPx }

        // Toggle labels in existing rows directly instead of rebuilding
        val dynamicButtonContainer: LinearLayout = findViewById(R.id.dynamicButtonContainer)
        for (i in 0 until dynamicButtonContainer.childCount) {
            val row = dynamicButtonContainer.getChildAt(i)
            if (row is LinearLayout) {
                row.getChildAt(1)?.visibility = visibility
            }
        }
    }
    //enables/disables sidebars if world is selected/not selected
    private fun setSidebarEnabled(enabled: Boolean) {
        val dynamicButtonContainer: LinearLayout = findViewById(R.id.dynamicButtonContainer)
        for (i in 0 until dynamicButtonContainer.childCount) {
            val row = dynamicButtonContainer.getChildAt(i)
            row.isEnabled = enabled
            row.alpha = if (enabled) 1f else 0.5f
            // Also disable the button inside the row
            if (row is LinearLayout) {
                row.getChildAt(0)?.isEnabled = enabled
            }
        }
    }

//    fun openCategoryFragment(category: String) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.mainFragmentContainer, CategoryFragment.newInstance(category))
//            .commit()
//    }

    //sets up world spinner/button
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
            createBtn.visibility = Button.GONE

            // "Select a world..." sits at position 0 and is never a real world - forces you to choose a world to unlock sidebar
            val spinnerItems = mutableListOf("Select a world...")
            spinnerItems.addAll(worlds.map { it.worldName })
            spinnerItems.add("+ Create World")

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Reset
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

                        // Last item is the create button
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

            spinner.setOnLongClickListener {
                val position = spinner.selectedItemPosition
                // Only allow delete on real worlds (not prompt at 0 or "+ Create World")
                if (position > 0 && spinnerItems[position] != "+ Create World") {
                    val world = worlds[position - 1]
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete World")
                        .setMessage("Are you sure you want to delete \"${world.worldName}\"?")
                        .setPositiveButton("Delete") { dialog, _ ->
                            db.deleteWorld(world.worldIDPK)
                            if (currentWorldId == world.worldIDPK) {
                                currentWorldId = null
                            }
                            setupWorldUI()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        .show()
                }
                true
            }

            createBtn.setOnClickListener {
                showCreateWorldDialog()
            }
        }
    }
    //spinner setup
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