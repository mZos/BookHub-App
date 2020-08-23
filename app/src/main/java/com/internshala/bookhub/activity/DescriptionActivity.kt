package com.internshala.bookhub.activity

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.bookhub.R
import com.internshala.bookhub.database.BookDatabase
import com.internshala.bookhub.database.BookEntity
import com.internshala.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_description.*
import org.json.JSONObject
import java.lang.Exception

class DescriptionActivity : AppCompatActivity() {

    lateinit var toolBar: Toolbar
    lateinit var imgBookImage: ImageView
    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var txtBookDescription: TextView
    lateinit var progressBarLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    var bookId: String? = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        toolBar = findViewById(R.id.toolbar)
        imgBookImage = findViewById(R.id.imgBookImage)
        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        txtBookDescription = findViewById(R.id.txtBookDescription)
        progressBarLayout = findViewById(R.id.progressBarLayout)
        progressBar = findViewById(R.id.progressBar)

        progressBarLayout.visibility = View.VISIBLE

        setSupportActionBar(toolBar)
        supportActionBar?.title = "Book Detail"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some unexpected error occurred",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {

            val url = " http://13.235.250.119/v1/book/get_book/"
            val queue = Volley.newRequestQueue(this@DescriptionActivity)

            val jsonParams = JSONObject()
            jsonParams.put("book_id", bookId)

            val jsonObjectRequest =
                object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                    try {

                        val success = it.getBoolean("success")
                        if (success) {
                            val getJsonObject = it.getJSONObject("book_data")
                            progressBarLayout.visibility = View.GONE

                            val bookImageUrl = getJsonObject.getString("image")

                            Picasso.get().load(getJsonObject.getString("image"))
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text = getJsonObject.getString("name")
                            txtBookAuthor.text = getJsonObject.getString("author")
                            txtBookPrice.text = getJsonObject.getString("price")
                            txtBookRating.text = getJsonObject.getString("rating")
                            txtBookDescription.text = getJsonObject.getString("description")

                            val bookEntity = BookEntity(
                                bookId?.toInt() as Int,
                                txtBookName.text.toString(),
                                txtBookAuthor.text.toString(),
                                txtBookPrice.text.toString(),
                                txtBookRating.text.toString(),
                                txtBookDescription.text.toString(),
                                bookImageUrl
                            )

                            val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                            val isFav = checkFav.get()

                            if (isFav) {
                                btnAddToFav.text = "Remove from Favourites"
                                val favColor =
                                    ContextCompat.getColor(applicationContext, R.color.colorAccent)
                                btnAddToFav.setBackgroundColor(favColor)
                            } else {

                                btnAddToFav.text = "Add to Favourites"
                                val noFavColor = ContextCompat.getColor(
                                    applicationContext,
                                    R.color.colorPrimaryDark
                                )
                                btnAddToFav.setBackgroundColor(noFavColor)
                            }

                            btnAddToFav.setOnClickListener {
                                if (!DBAsyncTask(applicationContext, bookEntity, 1).execute()
                                        .get()
                                ) {
                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                    val result = async.get()
                                    if (result) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Book added to favorites",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        btnAddToFav.text = "Remove from Favourites"
                                        val favColor = ContextCompat.getColor(
                                            applicationContext,
                                            R.color.colorAccent
                                        )
                                        btnAddToFav.setBackgroundColor(favColor)
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "some error occurred",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                    val result = async.get()

                                    if (result) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Book removed from favourites",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        btnAddToFav.text = "Add to Favourites"
                                        val nofavColor = ContextCompat.getColor(
                                            applicationContext,
                                            R.color.colorPrimaryDark
                                        )
                                        btnAddToFav.setBackgroundColor(nofavColor)
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "some error occurred",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                        } else {
                            Toast.makeText(
                                this@DescriptionActivity,
                                "Some error occurred please restart the app",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@DescriptionActivity,
                            "Some unexpected error has occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {

                    Toast.makeText(
                        this@DescriptionActivity,
                        "Volley error occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-Type"] = "application/json"
                        headers["token"] = "bf84d4e4d4ff6f"
                        return headers

                    }
                }

            queue.add(jsonObjectRequest)

        } else {
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("error")
            dialog.setMessage("No internet connection found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }

    }


    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        private val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        /*
        Mode 1 -> Check DB if the book is favourite or not
        Mode 2 -> Add the book into the DB as favourites
        Mode 3 -> Remove the book from favourite

        * */

        override fun doInBackground(vararg p0: Void?): Boolean {

            when (mode) {
                1 -> {
                    // Check DB if the book is favourite or not
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }

                2 -> {
                    // Save the book into DB as favourite
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }

                3 -> {
                    // Remove the book from favourites
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }
            return false
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

}