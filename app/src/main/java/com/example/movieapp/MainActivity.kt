package com.example.movieapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

data class TicketPurchase(
    val branchName: String,
    val eventDate: String,
    val adultTicket: Int,
    val childTicket: Int,
    val totalCost: Int
) {
    override fun toString(): String {
        return "Branch Name: $branchName\n" +
                "Event Date: $eventDate\n" +
                "Number of Adult Tickets: $adultTicket\n" +
                "Number of Child Tickets: $childTicket\n" +
                "Total Cost: $totalCost ₪"
    }
}

// Interface to handle date selection events
interface DateSelectedListener {
    fun onDateSelected(date: String)
}

// Main activity class that handles UI and user interactions
class MainActivity : AppCompatActivity(), DateSelectedListener {
    // global vars
    private lateinit var dateBtn: Button
    private lateinit var btnGetTickets: Button
    private var ticketPurchase: TicketPurchase? = null  // Global variable to store the ticket purchase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize the UI components
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //-----------------------------------------//
        // date picker dialog
        dateBtn = findViewById<Button>(R.id.date_dialog_btn)
        dateBtn.setOnClickListener {
            // Display date picker dialog and handle date selection
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val selectedDate = dateFormat.format(calendar.time)
                onDateSelected(selectedDate)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        // description dialog
        findViewById<Button>(R.id.show_description).setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.movie_description, null)
            val customDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            val okButton = dialogView.findViewById<Button>(R.id.okButton)
            okButton.setOnClickListener {
                customDialog.dismiss()
            }
            customDialog.show()
        }

        btnGetTickets = findViewById<Button>(R.id.btnGetTickets)
        btnGetTickets.setOnClickListener {
            confirmPurchaseDialog()
        }
        val cartButton: Button = findViewById(R.id.history)
        cartButton.setOnClickListener {
            showHistoryDialog(ticketPurchase)
        }
        //------------------------------
        // movie poster animations for the
        val ivMoviePoster = findViewById<ImageView>(R.id.ivMoviePoster)
        val fadeIn = ObjectAnimator.ofFloat(ivMoviePoster, "alpha", 0f, 1f).setDuration(3000)
        val scaleUpX = ObjectAnimator.ofFloat(ivMoviePoster, "scaleX", 0f, 1.0f).setDuration(3000)
        val scaleUpY = ObjectAnimator.ofFloat(ivMoviePoster, "scaleY", 0f, 1.0f).setDuration(3000)
        AnimatorSet().apply {
            playTogether(fadeIn, scaleUpX, scaleUpY)
            start()
        }
        //-----------------------------------------//
        // Branch Spinner
        val spinnerBranch = findViewById<Spinner>(R.id.spinnerBranchLocation)
        val branchAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.branches_names,  // branches spinner data array
            R.layout.spinner_item     // custom layout here
        )
        branchAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerBranch.adapter = branchAdapter

        spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Check if a valid item has been selected
                if (position == 0) {
                    // First item (e.g., "Select a branch") is selected, show an error message
                    showErrorMessage(getString(R.string.please_select_a_valid_branch))
                } else {
                    // A valid branch has been selected
                    val selectedBranch = parent?.getItemAtPosition(position).toString()
                    // Do something with the selected branch
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing or show an error message
            }
        }
        //-----------------------------------------//
        // Adapter

        val ticketAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.ticket_numbers,  // ticket spinner data array
            R.layout.spinner_item     // use your custom layout here
        )
        ticketAdapter.setDropDownViewResource(R.layout.spinner_item)
        // Adult amount spinner
        val spinnerAdultAmount = findViewById<Spinner>(R.id.spinnerAdultAmount)
        spinnerAdultAmount.adapter = ticketAdapter
        spinnerAdultAmount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity,
                    getString(R.string.adults_selected, selectedItem), Toast.LENGTH_SHORT)
                    .show()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        // Child amount spinner
        val spinnerChildAmount = findViewById<Spinner>(R.id.spinnerChildAmount)
        spinnerChildAmount.adapter = ticketAdapter
        spinnerChildAmount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity,
                    getString(R.string.child_selected, selectedItem), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        //---------------------------------

    }

    // Display the history of ticket purchases
    private fun showHistoryDialog(ticketPurchase: TicketPurchase?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.purchase_history_dialog, null)
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        val purchaseInfoTextView = dialogView.findViewById<TextView>(R.id.tvPurchaseInfo)
        if (ticketPurchase != null) {
            purchaseInfoTextView.text = ticketPurchase.toString()
        } else {
            purchaseInfoTextView.text = getString(R.string.no_purchase_info_available)
        }
        customDialog.show()
    }

    // preform purchase
    private fun confirmPurchaseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.confirm_purchase_dialog, null)
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val branchName = findViewById<Spinner>(R.id.spinnerBranchLocation).selectedItem.toString()
        val childAmount = findViewById<Spinner>(R.id.spinnerChildAmount).selectedItem.toString().toInt()
        val adultAmount = findViewById<Spinner>(R.id.spinnerAdultAmount).selectedItem.toString().toInt()
        val totalCost = (adultAmount * 10) +(childAmount * 5 )

        // setting info
        dialogView.findViewById<TextView>(R.id.tvNumberOfAdult).text =
            getString(R.string.number_of_adult, adultAmount)
        dialogView.findViewById<TextView>(R.id.tvNumberOfChild).text =
            getString(R.string.number_of_child, childAmount)
        dialogView.findViewById<TextView>(R.id.tvTotalCost).text =
            getString(R.string.total_cost, totalCost)
        dialogView.findViewById<TextView>(R.id.tvDate).text = getString(R.string.date, dateBtn.text)
        dialogView.findViewById<TextView>(R.id.tvBranchName).text =
            getString(R.string.branch_name, branchName)
        ticketPurchase = TicketPurchase(
            branchName,
            dateBtn.text.toString(),
            adultAmount,
            childAmount,
            totalCost
        )

        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            showHistoryDialog(ticketPurchase)
            customDialog.dismiss()
            Toast.makeText(this, getString(R.string.purchase_confirmed), Toast.LENGTH_SHORT).show()
        }
        customDialog.show()
    }

    //on selected dates
    override fun onDateSelected(date: String) { dateBtn.text = date}


    // error
    private fun showErrorMessage(message: String) {
        // Show the error message using a Toast or any other method you prefer
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
