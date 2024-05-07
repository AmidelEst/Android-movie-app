package com.example.movieapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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
// image viewer
class ImageAdapter(private val images: List<Int>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount() = images.size
}

// Interface to handle date selection events
interface DateSelectedListener {
    fun onDateSelected(date: String)
}

// Main activity class that handles UI and user interactions
class MainActivity : AppCompatActivity(), DateSelectedListener {
    // global vars

    private var ticketPurchase: TicketPurchase? = null  // Global variable to store the ticket purchase


    private lateinit var dateBtn: Button
    private lateinit var btnGetTickets: Button

    private var adultSelected = false
    private var childSelected = false
    private var branchSelected = false
    private var dateSelected = false
    private  var childAmount: Int  = 0
    private  var adultAmount: Int  = 0
    private  var totalCost: Int  = 0
    private  lateinit var branchName: String

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
            // Current date and time instance
            val c = Calendar.getInstance()
            // Create date picker dialog
            val datePickerDialog =DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // Calendar instance for the selected date
                val selectedCalendar  = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                // Formatting the date as a string
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val selectedDate = dateFormat.format(selectedCalendar .time)
                // Call function to handle the selected date
                onDateSelected(selectedDate)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            // Set the minimum date to today to prevent past dates selection
            datePickerDialog.datePicker.minDate = c.timeInMillis
            // Show the dialog
            datePickerDialog.show()
        }
        //-----------------------------------------//
        // Movie description dialog
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
        //-----------------------------------------//
        // movie poster animations for the
        val images = listOf(R.drawable.through_the_never, R.drawable.hangingposter_680)
        val adapter = ImageAdapter(images)
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerImages)
        viewPager.adapter = adapter
        val fadeIn = ObjectAnimator.ofFloat(viewPager, "alpha", 0f, 1f).setDuration(3000)
        val scaleUpX = ObjectAnimator.ofFloat(viewPager, "scaleX", 0f, 1.0f).setDuration(3000)
        val scaleUpY = ObjectAnimator.ofFloat(viewPager, "scaleY", 0f, 1.0f).setDuration(3000)
        AnimatorSet().apply {
            playTogether(fadeIn, scaleUpX, scaleUpY)
            start()
        }
        //-----------------------------------------//
        val spinnerAdult = findViewById<Spinner>(R.id.spinnerAdultAmount)
        val spinnerChild = findViewById<Spinner>(R.id.spinnerChildAmount)
        val spinnerBranch =findViewById<Spinner>(R.id.spinnerBranchLocation)
        // Branch Spinner
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
                    branchSelected = false
                    updateGetTicketsButtonState()
                    //showErrorMessage(getString(R.string.please_select_a_valid_branch))
                } else {
                    // A valid branch has been selected
                    branchName = parent?.getItemAtPosition(position).toString()
                    branchSelected = true
                    updateGetTicketsButtonState()
                    // Do something with the selected branch
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing or show an error message
            }
        }
        //-----------------------------------------//
        // Adapter for ticket
        val ticketAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.ticket_numbers,  // ticket spinner data array
            R.layout.spinner_item     // use your custom layout here
        )
        ticketAdapter.setDropDownViewResource(R.layout.spinner_item)
        //-----------------------------------------//
        // Adult amount spinner
        spinnerAdult .adapter = ticketAdapter
        spinnerAdult .onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // Check if a valid item has been selected
                if (position == 0 ) {
                    adultSelected = false
                    updateGetTicketsButtonState()
                } else {
                    // A valid branch has been selected
                    adultAmount = parent.getItemAtPosition(position).toString().toInt()
                    adultSelected = true
                    updateGetTicketsButtonState()
                    Toast.makeText(this@MainActivity,
                        getString(R.string.adults_selected, adultAmount), Toast.LENGTH_SHORT)
                        .show()
                    // Do something with the selected branch
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        //-----------------------------------------//
        // Child amount spinner
        spinnerChild.adapter = ticketAdapter
        spinnerChild.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // Check if a valid item has been selected
                if (position == 0 ) {
                    childSelected = false
                    updateGetTicketsButtonState()
                } else {
                    // A valid branch has been selected
                    childAmount = parent.getItemAtPosition(position).toString().toInt()
                    childSelected = true
                    updateGetTicketsButtonState()
                    Toast.makeText(this@MainActivity,
                        getString(R.string.child_selected, childAmount), Toast.LENGTH_SHORT)
                        .show()
                    // Do something with the selected branch
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        //---------------------------------
        // Confirm purchase button

        btnGetTickets = findViewById<Button>(R.id.btnGetTickets)
        btnGetTickets.isEnabled=true
        btnGetTickets.setOnClickListener {
            if(adultAmount ==0 && childAmount ==0){
                showErrorMessage("At least 1 ticket per purchase")
                btnGetTickets.isEnabled = false
                updateGetTicketsButtonState()

            }else {
                btnGetTickets.isEnabled = true
                confirmPurchaseDialog()
            }
        }
        val historyButton: Button = findViewById(R.id.history)
        historyButton.setOnClickListener {
            showHistoryDialog(ticketPurchase)
        }
        //------------------------------

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

        totalCost = (adultAmount.toInt() * 10) +(childAmount.toInt() * 5 )
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
    }// end of onCreate()

    //on selected dates
    override fun onDateSelected(date: String) {
        dateBtn.text = date
        dateSelected = true
        updateGetTicketsButtonState()

    }
    private fun updateGetTicketsButtonState() {
        btnGetTickets.isEnabled = adultSelected && dateSelected && childSelected && branchSelected && (adultAmount > 0 || childAmount > 0)
    }
    // error
    private fun showErrorMessage(message: String) {
        // Show the error message using a Toast or any other method you prefer
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
