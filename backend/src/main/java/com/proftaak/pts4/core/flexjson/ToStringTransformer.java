package com.proftaak.pts4.core.flexjson;

import flexjson.transformer.AbstractTransformer;

/**
 * @author Michon
 */
public class ToStringTransformer extends AbstractTransformer {
    @Override
    public void transform(Object o) {
        if (o != null) {
            getContext().writeQuoted(o.toString());
        } else {
            getContext().write(null);
        }
    }
}
