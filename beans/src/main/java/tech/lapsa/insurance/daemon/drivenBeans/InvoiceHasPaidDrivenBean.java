package tech.lapsa.insurance.daemon.drivenBeans;

import java.time.Instant;
import java.util.Currency;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;

import tech.lapsa.epayment.shared.entity.InvoiceHasPaidJmsEvent;
import tech.lapsa.epayment.shared.jms.EpaymentDestinations;
import tech.lapsa.insurance.facade.InsuranceRequestFacade.InsuranceRequestFacadeRemote;
import tech.lapsa.java.commons.exceptions.IllegalArgument;
import tech.lapsa.java.commons.exceptions.IllegalState;
import tech.lapsa.lapsa.jmsRPC.service.JmsReceiverServiceDrivenBean;

@MessageDriven(mappedName = EpaymentDestinations.INVOICE_HAS_PAID)
public class InvoiceHasPaidDrivenBean extends JmsReceiverServiceDrivenBean<InvoiceHasPaidJmsEvent> {

    public InvoiceHasPaidDrivenBean() {
	super(InvoiceHasPaidJmsEvent.class);
    }

    @Override
    public void receiving(final InvoiceHasPaidJmsEvent entity, final Properties properties)
	    throws IllegalArgumentException, IllegalStateException {
	_receiving(entity, properties);
    }

    // PRIVATE

    @EJB
    private InsuranceRequestFacadeRemote insuranceRequests;

    private void _receiving(final InvoiceHasPaidJmsEvent entity, final Properties properties)
	    throws IllegalArgumentException, IllegalStateException {
	final String paymentMethodName = entity.getMethod();
	final Integer id = Integer.valueOf(entity.getExternalId());
	final Instant paymentInstant = entity.getInstant();
	final Double paymentAmount = entity.getAmount();
	final Currency paymentCurrency = entity.getCurrency();
	final String paymentCard = entity.getPaymentCard();
	final String paymentCardBank = entity.getPaymentCardBank();
	final String paymentReference = entity.getReferenceNumber();
	final String payerName = entity.getPayerName();

	try {
	    insuranceRequests.invoicePaidByTheir(id,
		    paymentMethodName,
		    paymentInstant,
		    paymentAmount,
		    paymentCurrency,
		    paymentCard,
		    paymentCardBank,
		    paymentReference,
		    payerName);
	} catch (final IllegalArgument e) {
	    throw e.getRuntime();
	} catch (IllegalState e) {
	    throw e.getRuntime();
	}
    }
}
