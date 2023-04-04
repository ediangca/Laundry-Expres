package com.kodego.diangca.ebrahim.laundryexpres.classes

class Image {

    private lateinit var mName: String
    private lateinit var mImageUrl: String

    constructor() {

    }

    constructor(mName: String, mImageUrl: String) {
        this.mName = mName
        this.mImageUrl = mImageUrl
        if (mName.isEmpty()) {
            this.mName = "Profile"
        }

    }

    fun getName(): String {
        return mName
    }

    fun setName(mName: String) {
        this.mName = mName
    }

    fun getImageUrl(): String{
        return mImageUrl
    }

    fun setImageUrl(mImageUrl: String){
        this.mImageUrl = mImageUrl
    }
}