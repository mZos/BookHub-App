package com.internshala.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.bookhub.R
import com.internshala.bookhub.adapter.DashboardRecyclerAdapter
import com.internshala.bookhub.model.Book
import com.internshala.bookhub.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class DashboardFragment : Fragment() {

    lateinit var recyclerDashboard: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var progressBarLayout: RelativeLayout
    private lateinit var progressbar: ProgressBar
    val bookInfoList = arrayListOf<Book>()
    lateinit var recyclerAdapter: DashboardRecyclerAdapter

    private val ratingComparator = Comparator<Book> { book1, book2 ->
        if (book1.bookRating.compareTo(book2.bookRating, true) == 0) {
            //sort according to name if rating is same
            book1.bookName.compareTo(book2.bookName, true)
        } else {
            book1.bookRating.compareTo(book2.bookRating, true)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        progressBarLayout = view.findViewById(R.id.progressBarLayout)
        progressbar = view.findViewById(R.id.progressBar)
        progressBarLayout.visibility = View.VISIBLE

        setHasOptionsMenu(true)


        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v1/book/fetch_books/"

            val jsonObjectRequest =
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener {
                    //Response Handle Here
                    try {
                        val success = it.getBoolean("success")
                        progressBarLayout.visibility = View.GONE

                        if (success) {
                            val data = it.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val bookJsonObject = data.getJSONObject(i)
                                val bookObject = Book(
                                    bookJsonObject.getString("book_id"),
                                    bookJsonObject.getString("name"),
                                    bookJsonObject.getString("author"),
                                    bookJsonObject.getString("rating"),
                                    bookJsonObject.getString("price"),
                                    bookJsonObject.getString("image")
                                )
                                //adding data into bookArrayList
                                bookInfoList.add(bookObject)
                                //Sending data to adapter
                                layoutManager = LinearLayoutManager(activity!!)
                                if (activity != null) {
                                    recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookInfoList)
                                    recyclerDashboard.adapter = recyclerAdapter
                                    recyclerDashboard.layoutManager = layoutManager
                                }
                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error has occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {
                    //Error Handle Here
                    if (activity != null) {
                        Toast.makeText(
                            activity as Context,
                            "Connection error occurred please restart the app",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "bf84d4e4d4ff6f"
                        return headers
                    }

                }

            queue.add(jsonObjectRequest)

        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")

            dialog.setMessage("No internet connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val openSettingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(openSettingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookInfoList)
        val id = item.itemId
        if (id == R.id.action_sort) {
            Collections.sort(bookInfoList, ratingComparator)
            bookInfoList.reverse()
            recyclerAdapter.notifyDataSetChanged()
        }

        return super.onOptionsItemSelected(item)
    }
}