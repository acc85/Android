package org.helpapaw.helpapaw.models.backendless

import com.backendless.BackendlessUser

/**
 * Created by iliyan on 8/4/16
 */
data class FINComment(

    var objectId: String? = null,
    var signalID: String? = null,
    var text: String? = null,
    var created: String? = null,
    val type: String? = null,
    var author: BackendlessUser? = null
)
