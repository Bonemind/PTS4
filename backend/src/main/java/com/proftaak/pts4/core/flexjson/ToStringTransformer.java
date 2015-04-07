package com.proftaak.pts4.core.flexjson;

import flexjson.transformer.AbstractTransformer;

import java.time.LocalDate;
import java.util.Date;

/**
 * @author Michon
 */
public class ToStringTransformer extends AbstractTransformer {
    @Override
    public void transform(Object o) {
        getContext().writeQuoted(o.toString());
    }
}
