package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;

// This exit is called from the Enter key on the payment screen.
// It calculates the change that will be owed to the customer
// based on the payment qty entered.

public class ReturnVerify
    extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Get Screen Fields we will need and check that they exist.
    VtiUserExitScreenField totalOwingField = getScreenField("TOTAL_OWING");
    VtiUserExitScreenField totalOwingCstField = getScreenField("TOTAL_OWING_CST");
    VtiUserExitScreenField payAmountField = getScreenField("PAY_AMOUNT");
    VtiUserExitScreenField totalChangeField = getScreenField("TOTAL_CHANGE");
    VtiUserExitScreenField docTypeField = getScreenField("DOC_TYPE");
    VtiUserExitScreenField orderTotalField = getScreenField("ORDER_TOTAL");
    VtiUserExitScreenField totalPaidField = getScreenField("TOTAL_PAID");
    VtiUserExitScreenField rbCashField = getScreenField("R_CASH");
    VtiUserExitScreenField rbBankChField = getScreenField("R_BANK_CHEQUE");
    VtiUserExitScreenField rbGiftVouchField = getScreenField("R_GIFT_VOUCH");
    VtiUserExitScreenField refOrderField = getScreenField("REF_ORDER");
    VtiUserExitScreenField bezeiField = getScreenField("BEZEI");

    if (totalOwingField == null)
        return new VtiUserExitResult(999, "Field TOTAL_OWING not found");
    if (payAmountField == null)
        return new VtiUserExitResult(999, "Field PAY_AMOUNT not found");
    if (totalChangeField == null)
        return new VtiUserExitResult(999, "Field TOTAL_CHANGE not found");
    if (totalOwingCstField == null)
        return new VtiUserExitResult(999, "Field TOTAL_OWING_CST not found");
    if (docTypeField == null)
        return new VtiUserExitResult(999, "Field DOC_TYPE not found");
    if (orderTotalField == null)
        return new VtiUserExitResult(999, "Field ORDER_TOTAL not found");
    if (totalPaidField == null)
        return new VtiUserExitResult(999, "Field TOTAL_PAID not found");
    if (rbCashField == null)
        return new VtiUserExitResult(999, "Field R_CASH not found");
    if (rbBankChField == null)
        return new VtiUserExitResult(999, "Field R_BANK_CHEQUE not found");
    if (rbGiftVouchField == null)
        return new VtiUserExitResult(999, "Field R_GIFT_VOUCH not found");

    //double totalOwing = totalOwingCstField.getDoubleFieldValue();
    double totalOwing = totalOwingField.getDoubleFieldValue();
    double payAmount = payAmountField.getDoubleFieldValue();
    double totalChange = 0;
    double orderTotal = orderTotalField.getDoubleFieldValue();
    double totalPaid = totalPaidField.getDoubleFieldValue();
    String rbCash = rbCashField.getFieldValue();
    String rbBankCh = rbBankChField.getFieldValue();
    String rbGiftVouch = rbGiftVouchField.getFieldValue();
    String docType = docTypeField.getFieldValue();

    // Check if an invoice number has been filled in and a return reason
	String strRefOrder = refOrderField.getFieldValue();
	String strBezei = bezeiField.getFieldValue();
	
		if(strRefOrder.equals(""))
		{
			setCursorPosition(refOrderField);
			return new VtiUserExitResult(999,"Please enter the Original Invoice Number");
		}
		if(strBezei.equals(""))
		{
			setCursorPosition(bezeiField);
			return new VtiUserExitResult(999,"Please Select a Return Reason");
		}
		
    // If the payment type is cash, we will need to perform some rounding to the order
    // total.

    if ( (rbCash.equals("X")))
    {
      //If a cash payment, check the "pay amount" is valid.
      if (rbCash.equals("X"))
      {
        int intPayAmount = (int) Math.round( (payAmount * 100)) % 5;
        if (! (intPayAmount == 0))
        {
          setCursorPosition(payAmountField);
          return new VtiUserExitResult(999, "Invalid Pay Amount for a cash payment");
        }
      }

      int intTotalOwing = (int) Math.round( (totalOwing * 100)) % 5;

      switch (intTotalOwing)
      {
        case 0:
        {
          break;
        }
        case 1:
        {
          totalOwing -= 0.01;
        }
        break;

        case 2:
        {
          totalOwing -= 0.02;
        }
        break;

        case 3:
        {
          totalOwing += 0.02;
        }
        break;

        case 4:
        {
          totalOwing += 0.01;
        }
        break;
      }
      // Calculate the total amount of chnge
      totalChange = payAmount - totalOwing;
    }
    else
    {
      //Check that the pay amount will not exceed the total outstanding.  Only for payment
      //types other than cash and voucher.
      totalChange = payAmount - totalOwing;
      if (totalChange > 0)
      {
        return new VtiUserExitResult(999, "Pay Amount exceeds outstanding amount");
      }
    }

    int intTotalChange = (int) Math.round( (totalChange * 100)) % 5;

    switch (intTotalChange)
    {
      case 0:
      {
        break;
      }
      case 1:
      {
        totalChange -= 0.01;
      }
      break;

      case 2:
      {
        totalChange -= 0.02;
      }
      break;

      case 3:
      {
        totalChange += 0.02;
      }
      break;

      case 4:
      {
        totalChange += 0.01;
      }
      break;
    }

    // If the change amount is less than zero (i.e. only a part payment has been made)
    // set the value to 0.
    if (totalChange < 0)
    {
      totalChange = 0.0;

      // Set the change and total owing amounts back to the screen.
    }
    totalChangeField.setDoubleFieldValue(totalChange);
    totalOwingField.setDoubleFieldValue(totalOwing);

    return new VtiUserExitResult();
  }
}
