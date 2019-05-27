package org.helpapaw.helpapaw.authentication

interface PrivacyPolicyConfirmationContract {

    interface Obtain {
        fun onPrivacyPolicyObtained(privacyPolicy: String)
    }

    interface UserResponse {
        fun onUserAcceptedPrivacyPolicy()
        fun onUserDeclinedPrivacyPolicy()
    }
}
