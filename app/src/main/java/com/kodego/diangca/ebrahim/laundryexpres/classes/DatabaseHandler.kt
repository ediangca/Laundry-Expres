package com.kodego.diangca.ebrahim.laundryexpres.classes

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASENAME, null, DATABASEVERSION) {


    companion object {
        private val DATABASEVERSION = 1
        private val DATABASENAME = "laundry_express"

        val tableBorrower = "borrower"
        val borrowerId = "id"
        val borrowerFirstName = "firstname"
        val borrowerLastName = "lastname"
        val borrowerContactNo = "contactNo"

        //Student Table
        val tableStudents = "student_table"
        val studentId = "id"
        val studentFirstName = "firstname"
        val studentlastName = "lastname"
        var yearstarted = "year_started"
        var course = "course"

        val tableContacts = "student_contacts"
        val contactID = "id"
        val studentcontactID = "student_id"
        val contactType = "contact_type"
        val contactDetails = "contact_details"


    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATESTUDENTTABLE = "CREATE TABLE $tableBorrower " +
                "($borrowerId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$borrowerFirstName TEXT, " +
                "$borrowerLastName TEXT , " +
                "$borrowerContactNo INTEGER)"
        db?.execSQL(CREATESTUDENTTABLE)

        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'EBRAHIM', 'DIANGCA', 09123456789)"
        )
        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'ROSE MARIE', 'DIANGCA', 09123456789 )"
        )
        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'MOHAMMAD RAFI', 'DIANGCA', 09123456789 )"
        )
        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'FARHANA', 'DIANGCA', 09123456789 )"
        )
        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'SACAR', 'DIANGCA', 09123456789 )"
        )
        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'DIANGCA', 'NAIMA', 09123456789 )"
        )
        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'DIANGCA', 'ISMAEL', 09123456789)"
        )
        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'DIANGCA', 'JAHARAH', 09123456789 )"
        )
        db?.execSQL(
            "Insert into $tableBorrower ($borrowerFirstName, $borrowerLastName, $borrowerContactNo)values(" +
                    "'DIANGCA', 'ASLIAH', 09123456789 )"
        )

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $tableBorrower")
        onCreate(db)
    }


}
