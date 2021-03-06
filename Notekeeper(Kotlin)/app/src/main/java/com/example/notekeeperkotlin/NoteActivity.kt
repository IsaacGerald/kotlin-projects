package com.example.notekeeperkotlin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notekeeperkotlin.appwidget.NoteKeeperAppWidget
import com.google.android.material.snackbar.Snackbar
import com.jwhh.notekeeper.CommentRecyclerAdapter
import com.jwhh.notekeeper.PseudoLocationManager
import com.jwhh.notekeeper.ReminderNotification
import kotlinx.android.synthetic.main.content_main.*

class NoteActivity : AppCompatActivity() {

    private val tag = this::class.simpleName

   val noteGetTogetherHelper = NoteGetTogetherHelper(this, lifecycle)

    private var notePosition = POSITION_NOT_SET
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))


        val arrayAdapter = ArrayAdapter<CourseInfo>(
            this,
            android.R.layout.simple_spinner_item, DataManager.courses.values.toList()
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses.adapter = arrayAdapter

        notePosition =
            savedInstanceState?.getInt(NOTE_POSITION, POSITION_NOT_SET) ?: intent.getIntExtra(
                NOTE_POSITION,
                POSITION_NOT_SET
            )

        if (notePosition != POSITION_NOT_SET) {
            displayNote()
        } else {
            createNewNote()
        }

         val commentsAdapter = CommentRecyclerAdapter(this, DataManager.notes[notePosition])
        commentsList.layoutManager = LinearLayoutManager(this)
        commentsList.adapter = commentsAdapter
    }


    private fun createNewNote() {
        DataManager.notes.add(NoteInfo())
        notePosition = DataManager.notes.lastIndex
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.putInt(NOTE_POSITION, notePosition)
    }

    private fun displayNote() {
        val note = DataManager.notes[notePosition]
        textNoteTitle.setText(note.title)
        textNoteText.setText(note.text)

        val coursePosition = DataManager.courses.values.indexOf(note.course)
        spinnerCourses.setSelection(coursePosition)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_next -> {
                if (notePosition < DataManager.notes.lastIndex) {
                    moveNext()
                } else {
                    val message = "No more notes"
                    showMessage(message)
                }

                true
            }
            R.id.action_get_together -> {
                noteGetTogetherHelper.sendMessage(DataManager.notes[notePosition])
                true
            }
            R.id.action_reminder -> {
                ReminderNotification.notify(
                    this,
                DataManager.notes[notePosition],
                    notePosition
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(textNoteTitle, message, Snackbar.LENGTH_LONG).show()
    }

    private fun moveNext() {

        ++notePosition
        displayNote()

        invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        if (notePosition >= DataManager.notes.lastIndex) {
            val menuItem = menu?.findItem(R.id.action_next)
            if (menuItem != null) {
                menuItem.icon = getDrawable(R.drawable.ic_block_white)
                menuItem.isEnabled = false
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }


    override fun onPause() {
        super.onPause()
        saveNote()
    }

    private fun saveNote() {
        val note = DataManager.notes[notePosition]
        note.title = textNoteTitle.text.toString()
        note.text = textNoteText.text.toString()
        note.course = spinnerCourses.selectedItem as CourseInfo

        NoteKeeperAppWidget.sendRefreshBroadCast(this)
    }
}