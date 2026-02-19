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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.coroutines.MainScope


class MainActivity : AppCompatActivity() {

    private lateinit var db : DataBaseHelper



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



    fun openCategoryFragment(category: String) {

        val fragment = CategoryFragment.newInstance(category)

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()
    }

}

