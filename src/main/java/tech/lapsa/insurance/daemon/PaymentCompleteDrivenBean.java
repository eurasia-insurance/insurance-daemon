package tech.lapsa.insurance.daemon;

import static tech.lapsa.java.commons.function.MyExceptions.*;

import java.time.Instant;
import java.util.Currency;
import java.util.Properties;

import javax.ejb.MessageDriven;
import javax.inject.Inject;

import tech.lapsa.epayment.domain.Invoice;
import tech.lapsa.epayment.domain.Payment;
import tech.lapsa.epayment.domain.QazkomPayment;
import tech.lapsa.epayment.facade.beans.EpaymentFacadeBean;
import tech.lapsa.insurance.facade.InsuranceRequestFacade;
import tech.lapsa.java.commons.function.MyExceptions;
import tech.lapsa.java.commons.function.MyObjects;
import tech.lapsa.javax.jms.ObjectConsumerListener;

@MessageDriven(mappedName = PaymentCompleteDrivenBean.JNDI_JMS_DEST)
public class PaymentCompleteDrivenBean extends ObjectConsumerListener<Invoice> {

    public PaymentCompleteDrivenBean() {
	super(Invoice.class);
    }

    public static final String JNDI_JMS_DEST = EpaymentFacadeBean.JNDI_JMS_DEST_PAID_EBILLs;

    @Inject
    private InsuranceRequestFacade insuranceRequests;

    @Override
    public void accept(final Invoice invoice, final Properties properties) {
	final Payment payment = MyObjects.requireNonNull(invoice, "invoice") //
		.optionalPayment() //
		.orElseThrow(MyExceptions.illegalStateSupplierFormat("No payment attached %1$s", invoice));
	final String methodName = payment.getMethod().regular();
	final Integer id = Integer.valueOf(invoice.getExternalId());
	final Instant paid = payment.getCreated();
	final Double amount = payment.getAmount();
	final Currency currency = payment.getCurrency();
	final String ref = MyObjects.isA(payment, QazkomPayment.class) //
		? MyObjects.requireA(payment, QazkomPayment.class).getReference() //
		: null;
	reThrowAsUnchecked(() -> insuranceRequests.markPaymentSuccessful(id, methodName, paid, amount, currency, ref));
    }
}
