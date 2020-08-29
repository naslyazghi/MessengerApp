package com.nlmessenger.messenger.model

class UserInfo (val name: String, val profileImage: String){

    // Secondary constructor that calls primary constructor with empty strings
    // In case of calling the constructor with no parameters
    constructor(): this("","")

}
