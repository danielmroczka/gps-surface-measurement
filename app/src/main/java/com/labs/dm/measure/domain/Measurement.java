package com.labs.dm.measure.domain;

import java.util.Date;

/**
 * Created by daniel on 2015-08-05.
 */
public class Measurement {

    private Date created;

    public Measurement(Date created) {
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }
}
