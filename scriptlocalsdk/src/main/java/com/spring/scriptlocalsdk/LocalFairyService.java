package com.spring.scriptlocalsdk;

import com.script.opencvapi.AtFairy2;
import com.script.opencvapi.AtFairyService;
import com.script.framework.AtFairyImpl;

public class LocalFairyService extends AtFairyService {
    @Override
    protected void onYpFairyCreated(AtFairy2 mFairy) {
        if(mFairy instanceof AtFairyImpl) {
            ScriptSdkProxy.init((AtFairyImpl) mFairy);
        }
    }

}
