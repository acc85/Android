package org.helpapaw.helpapaw.data.models.backendless

import com.backendless.BackendlessUser

data class FINComment(var objectId: String = "", var signalID: String, var text: String, var created: String, var type: String, var author: BackendlessUser?){

    constructor() : this("","","","","",BackendlessUser())

    constructor(signalID: String, text: String, created: String, type: String, author: BackendlessUser?):
            this("",signalID, text, created, type, author)
}