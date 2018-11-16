package org.helpapaw.helpapaw.data.models.backendless

import com.backendless.BackendlessUser

data class FINComment(var objectId: String? = null, var signalID: String, var comment: String, var created: String, var type: String, var author: BackendlessUser?){

    constructor() : this("","","","","",BackendlessUser())

    constructor(signalID: String, comment: String, created: String, type: String, author: BackendlessUser?):
            this(null,signalID, comment, created, type, author)
}