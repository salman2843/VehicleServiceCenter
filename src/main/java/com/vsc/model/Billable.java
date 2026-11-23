package com.vsc.model;

import java.math.BigDecimal;

/**
 * Anything that can appear as a chargeable line on an invoice, such as a
 * service charge or a spare part. Invoice totals are built from these two
 * methods, so the calculation lives on the object instead of in an HTTP
 * handler or a React component.
 */
public interface Billable {

    /** Human readable text shown on the invoice line, e.g. "Oil change". */
    String getDescription();

    /** Price charged for one unit of this item. */
    BigDecimal getAmount();
}
