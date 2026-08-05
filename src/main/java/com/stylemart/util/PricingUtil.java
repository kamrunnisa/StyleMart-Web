package com.stylemart.util;

import com.stylemart.model.Coupon;
import com.stylemart.model.OrderSummary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;

/**
 * One place for the money math so the cart page, the checkout page, and the
 * order-placement servlet can never disagree about what a customer owes.
 * Rates below are simplified for this project -- real GST slabs vary by
 * item category (5% under 1000/piece, 12% above) and delivery pricing
 * would come from a shipping provider, not a flat constant.
 */
public final class PricingUtil {

    public static final BigDecimal GST_RATE = new BigDecimal("0.05");           // flat 5%, simplified
    public static final BigDecimal DELIVERY_CHARGE = new BigDecimal("79.00");
    public static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("999.00");

    private PricingUtil() {}

    /** Builds the full breakdown for a cart subtotal, applying a coupon if one is still valid. */
    public static OrderSummary computeSummary(BigDecimal subtotal, Coupon coupon) {
        OrderSummary summary = new OrderSummary();
        subtotal = nullSafe(subtotal);
        summary.setSubtotal(subtotal);

        BigDecimal discount = BigDecimal.ZERO;
        if (coupon != null) {
            String reason = ineligibilityReason(coupon, subtotal);
            if (reason == null) {
                discount = discountFor(coupon, subtotal);
                summary.setCouponCode(coupon.getCode());
            } else {
                summary.setCouponError(reason);
            }
        }
        summary.setDiscount(discount);

        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        summary.setTaxableAmount(taxable);

        BigDecimal tax = taxable.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        summary.setTax(tax);

        BigDecimal delivery = (subtotal.compareTo(BigDecimal.ZERO) == 0
                || subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0)
                ? BigDecimal.ZERO
                : DELIVERY_CHARGE;
        summary.setDeliveryCharge(delivery);

        BigDecimal total = taxable.add(tax).add(delivery).setScale(2, RoundingMode.HALF_UP);
        summary.setTotal(total);

        return summary;
    }

    /** Null if the coupon is currently usable against this subtotal, otherwise a user-facing reason. */
    public static String ineligibilityReason(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null) return "Coupon not found";
        if (!coupon.isActive()) return "This coupon is no longer active";
        Date today = new Date(System.currentTimeMillis());
        if (coupon.getValidFrom() != null && today.before(coupon.getValidFrom())) {
            return "This coupon isn't active yet";
        }
        if (coupon.getValidUntil() != null && today.after(coupon.getValidUntil())) {
            return "This coupon has expired";
        }
        BigDecimal minOrder = nullSafe(coupon.getMinOrderValue());
        if (subtotal.compareTo(minOrder) < 0) {
            return "Add items worth \u20B9" + minOrder.stripTrailingZeros().toPlainString()
                    + " or more to use this coupon";
        }
        return null;
    }

    private static BigDecimal discountFor(Coupon coupon, BigDecimal subtotal) {
        BigDecimal value = nullSafe(coupon.getDiscountValue());
        BigDecimal discount;
        if ("flat".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = value;
        } else {
            discount = subtotal.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        }
        return discount.min(subtotal).max(BigDecimal.ZERO);
    }

    private static BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
