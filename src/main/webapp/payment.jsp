<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4" style="max-width: 960px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="fw-bold mb-0">Complete your payment</h4>
            <div class="text-muted small">Order #${order.orderNumber} &middot; Amount due
                &#8377;<fmt:formatNumber value="${order.totalAmount}" maxFractionDigits="2"/></div>
        </div>
        <a href="<%= request.getContextPath() %>/checkout" class="small"><i class="fa-solid fa-arrow-left"></i> Back to checkout</a>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><i class="fa-solid fa-circle-exclamation"></i> ${errorMessage}</div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-7">
            <form action="<%= request.getContextPath() %>/payment/process" method="post" id="paymentForm">
                <input type="hidden" name="orderId" value="${order.id}">
                <input type="hidden" name="method" id="methodField" value="${not empty selectedMethod ? selectedMethod : 'upi'}">

                <div class="border rounded p-3 mb-3">
                    <h6 class="fw-bold mb-3">Choose a payment method</h6>
                    <div class="row g-2">
                        <div class="col-6 col-md-3">
                            <div class="method-choice border rounded p-2 text-center" data-method="upi">
                                <div class="method-icon mx-auto mb-2"><i class="fa-solid fa-mobile-screen"></i></div>
                                <div class="small fw-semibold">UPI</div>
                            </div>
                        </div>
                        <div class="col-6 col-md-3">
                            <div class="method-choice border rounded p-2 text-center" data-method="card">
                                <div class="method-icon mx-auto mb-2"><i class="fa-solid fa-credit-card"></i></div>
                                <div class="small fw-semibold">Card</div>
                            </div>
                        </div>
                        <div class="col-6 col-md-3">
                            <div class="method-choice border rounded p-2 text-center" data-method="netbanking">
                                <div class="method-icon mx-auto mb-2"><i class="fa-solid fa-building-columns"></i></div>
                                <div class="small fw-semibold">Net Banking</div>
                            </div>
                        </div>
                        <div class="col-6 col-md-3">
                            <div class="method-choice border rounded p-2 text-center" data-method="wallet">
                                <div class="method-icon mx-auto mb-2"><i class="fa-solid fa-wallet"></i></div>
                                <div class="small fw-semibold">Wallet</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- UPI panel -->
                <div class="border rounded p-3 mb-3 method-panel" id="panel-upi">
                    <h6 class="fw-bold mb-3">Pay via UPI</h6>
                    <div class="row g-2 mb-3">
                        <div class="col-3">
                            <div class="upi-app-chip" data-app="Google Pay">Google Pay</div>
                        </div>
                        <div class="col-3">
                            <div class="upi-app-chip" data-app="PhonePe">PhonePe</div>
                        </div>
                        <div class="col-3">
                            <div class="upi-app-chip" data-app="Paytm">Paytm</div>
                        </div>
                        <div class="col-3">
                            <div class="upi-app-chip" data-app="BHIM">BHIM</div>
                        </div>
                    </div>
                    <input type="hidden" name="upiApp" id="upiApp" value="Google Pay">
                    <label class="form-label small fw-semibold">UPI ID</label>
                    <input type="text" name="upiId" class="form-control" placeholder="yourname@bank" value="${param.upiId}">
                    <div class="form-text">e.g. rahul.kumar@okhdfcbank</div>
                </div>

                <!-- Card panel -->
                <div class="border rounded p-3 mb-3 method-panel d-none" id="panel-card">
                    <h6 class="fw-bold mb-3">Pay via Card</h6>
                    <div class="mb-2">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="cardType" id="cardDebit" value="debit" checked>
                            <label class="form-check-label small" for="cardDebit">Debit Card</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="cardType" id="cardCredit" value="credit">
                            <label class="form-check-label small" for="cardCredit">Credit Card</label>
                        </div>
                        <span class="text-muted small ms-2">Visa &middot; MasterCard &middot; RuPay</span>
                    </div>
                    <label class="form-label small fw-semibold">Card number</label>
                    <input type="text" name="cardNumber" class="form-control mb-2" maxlength="19" placeholder="1234 5678 9012 3456">
                    <label class="form-label small fw-semibold">Name on card</label>
                    <input type="text" name="cardName" class="form-control mb-2" placeholder="As printed on card" value="${param.cardName}">
                    <div class="row g-2">
                        <div class="col-6">
                            <label class="form-label small fw-semibold">Expiry (MM/YY)</label>
                            <input type="text" name="cardExpiry" class="form-control" maxlength="5" placeholder="MM/YY">
                        </div>
                        <div class="col-6">
                            <label class="form-label small fw-semibold">CVV</label>
                            <input type="password" name="cardCvv" class="form-control" maxlength="4" placeholder="&bull;&bull;&bull;">
                        </div>
                    </div>
                </div>

                <!-- Net banking panel -->
                <div class="border rounded p-3 mb-3 method-panel d-none" id="panel-netbanking">
                    <h6 class="fw-bold mb-3">Net Banking</h6>
                    <label class="form-label small fw-semibold">Select your bank</label>
                    <select name="bank" class="form-select">
                        <option value="">Choose a bank</option>
                        <option>State Bank of India</option>
                        <option>HDFC Bank</option>
                        <option>ICICI Bank</option>
                        <option>Axis Bank</option>
                        <option>Kotak Mahindra Bank</option>
                        <option>Other Bank</option>
                    </select>
                </div>

                <!-- Wallet panel -->
                <div class="border rounded p-3 mb-3 method-panel d-none" id="panel-wallet">
                    <h6 class="fw-bold mb-3">Wallet</h6>
                    <label class="form-label small fw-semibold">Select wallet</label>
                    <select name="walletProvider" class="form-select mb-2">
                        <option value="">Choose a wallet</option>
                        <option>Paytm</option>
                        <option>Amazon Pay</option>
                        <option>Mobikwik</option>
                    </select>
                    <label class="form-label small fw-semibold">Registered mobile number</label>
                    <input type="text" name="walletPhone" class="form-control" maxlength="10" placeholder="10-digit mobile number" value="${param.walletPhone}">
                </div>

                <div class="border rounded p-3 mb-3 bg-light-subtle">
                    <div class="small fw-semibold mb-2"><i class="fa-solid fa-flask"></i> Demo gateway</div>
                    <div class="text-muted small mb-2">No real payment is made. Choose what this attempt should simulate:</div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="simulateOutcome" id="simSuccess" value="success" checked>
                        <label class="form-check-label small" for="simSuccess">Simulate success</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="simulateOutcome" id="simFailure" value="failure">
                        <label class="form-check-label small" for="simFailure">Simulate failure</label>
                    </div>
                </div>

                <button type="submit" class="btn btn-gradient w-100" id="payBtn">
                    Pay &#8377;<fmt:formatNumber value="${order.totalAmount}" maxFractionDigits="2"/>
                </button>
            </form>
        </div>

        <div class="col-lg-5">
            <div class="border rounded p-3">
                <h6 class="fw-bold mb-3">Order summary</h6>
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">Subtotal</span>
                    <span>&#8377;<fmt:formatNumber value="${order.subtotal}" maxFractionDigits="0"/></span>
                </div>
                <c:if test="${order.discountAmount > 0}">
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Discount</span>
                        <span class="text-success">-&#8377;<fmt:formatNumber value="${order.discountAmount}" maxFractionDigits="0"/></span>
                    </div>
                </c:if>
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">Delivery</span>
                    <span>&#8377;<fmt:formatNumber value="${order.deliveryCharge}" maxFractionDigits="0"/></span>
                </div>
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">GST</span>
                    <span>&#8377;<fmt:formatNumber value="${order.taxAmount}" maxFractionDigits="0"/></span>
                </div>
                <hr>
                <div class="d-flex justify-content-between">
                    <span class="fw-bold">Total</span>
                    <span class="fw-bold fs-5">&#8377;<fmt:formatNumber value="${order.totalAmount}" maxFractionDigits="2"/></span>
                </div>
            </div>
            <div class="text-muted small mt-3">
                <i class="fa-solid fa-lock"></i> This is a demo checkout. No card or bank details are transmitted anywhere.
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script>
(function () {
    var methodChoices = document.querySelectorAll('.method-choice');
    var panels = document.querySelectorAll('.method-panel');
    var methodField = document.getElementById('methodField');

    function selectMethod(method) {
        methodChoices.forEach(function (el) {
            el.classList.toggle('active', el.dataset.method === method);
        });
        panels.forEach(function (el) {
            el.classList.toggle('d-none', el.id !== 'panel-' + method);
        });
        methodField.value = method;
    }

    methodChoices.forEach(function (el) {
        el.addEventListener('click', function () { selectMethod(el.dataset.method); });
    });
    selectMethod(methodField.value || 'upi');

    document.querySelectorAll('.upi-app-chip').forEach(function (chip) {
        chip.addEventListener('click', function () {
            document.querySelectorAll('.upi-app-chip').forEach(function (c) { c.classList.remove('selected'); });
            chip.classList.add('selected');
            document.getElementById('upiApp').value = chip.dataset.app;
        });
    });

    // Format card number with spaces as the person types, cosmetic only.
    var cardInput = document.querySelector('input[name="cardNumber"]');
    if (cardInput) {
        cardInput.addEventListener('input', function () {
            var digits = cardInput.value.replace(/\D/g, '').slice(0, 19);
            cardInput.value = digits.replace(/(.{4})/g, '$1 ').trim();
        });
    }
    var expiryInput = document.querySelector('input[name="cardExpiry"]');
    if (expiryInput) {
        expiryInput.addEventListener('input', function () {
            var digits = expiryInput.value.replace(/\D/g, '').slice(0, 4);
            expiryInput.value = digits.length > 2 ? digits.slice(0, 2) + '/' + digits.slice(2) : digits;
        });
    }

    var form = document.getElementById('paymentForm');
    var payBtn = document.getElementById('payBtn');
    form.addEventListener('submit', function () {
        payBtn.disabled = true;
        payBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing payment&hellip;';
    });
})();
</script>
</body>
</html>
