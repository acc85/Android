package org.helpapaw.helpapaw

import com.google.android.gms.common.api.GoogleApiClient
import org.koin.dsl.module
import org.mockito.Mockito

var testModule = module{

    single { Mockito.mock(GoogleApiClient::class.java)}
}