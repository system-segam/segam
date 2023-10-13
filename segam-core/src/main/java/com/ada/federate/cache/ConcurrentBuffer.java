package com.ada.federate.cache;

import com.ada.federate.secure.SecureSum;
import com.ada.federate.secure.SecureUnion;

public class ConcurrentBuffer {
    // save local query result
    public ResultKVSet localResultTable;

    public ResultKVsSet localResultKeyValuesTable;

    // cache for secure union
    public SecureUnion.SecureUnionCache unionCache;
    // cache for secure summation
    public SecureSum.SecretSharingCache sharingCache;

    public void clean(Long uuid) {
        if (localResultTable != null)
            localResultTable.clean();
        if (sharingCache != null)
            sharingCache.clean();
        if (unionCache != null)
            unionCache.clean();
    }
}
