package com.proftaak.pts4.json;

import com.proftaak.pts4.database.IDatabaseModel;
import flexjson.transformer.AbstractTransformer;

/**
 * Created by Michon on 13-4-2015.
 */
public class ToPKTransformer extends AbstractTransformer {
    @Override
    public void transform(Object object) {
        if (object != null) {
            if (object instanceof IDatabaseModel) {
                Object pk = ((IDatabaseModel) object).getPK();
                if (pk instanceof String) {
                    getContext().writeQuoted((String) pk);
                } else {
                    getContext().write(pk.toString());
                }
            }
        } else {
            getContext().write(null);
        }
    }
}
