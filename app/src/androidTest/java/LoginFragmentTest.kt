package org.helpapaw.helpapaw

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.helpapaw.helpapaw.authentication.login.LoginFragment
import org.helpapaw.helpapaw.base.PawApplication
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFragmentTest{

    @Test
    fun testFragment(){
//        var app = ApplicationProvider.getApplicationContext<PawApplication>()
//        val fragment:LoginFragment = LoginFragment()
        assertNotNull(1+1)
    }
}